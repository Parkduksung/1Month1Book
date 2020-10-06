package com.rsupport.mobile.agent.modules.push;


import com.rsupport.util.log.RLog;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthStateHC4;
import org.apache.http.auth.Credentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.params.HttpClientParamConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteInfo.LayerType;
import org.apache.http.conn.routing.RouteInfo.TunnelType;
import org.apache.http.entity.BufferedHttpEntityHC4;
import org.apache.http.impl.DefaultConnectionReuseStrategyHC4;
import org.apache.http.impl.auth.BasicSchemeFactoryHC4;
import org.apache.http.impl.auth.DigestSchemeFactoryHC4;
import org.apache.http.impl.auth.HttpAuthenticator;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProviderHC4;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.execchain.TunnelRefusedException;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParamConfig;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContextHC4;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestTargetHostHC4;
import org.apache.http.protocol.RequestUserAgentHC4;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtilsHC4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.net.SocketFactory;

/**
 * ProxyClient can be used to establish a tunnel via an HTTP proxy.
 */
public class ProxyClient {

    private final HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory;
    private final ConnectionConfig connectionConfig;
    private final RequestConfig requestConfig;
    private final HttpProcessor httpProcessor;
    private final HttpRequestExecutor requestExec;
    private final ProxyAuthenticationStrategy proxyAuthStrategy;
    private final HttpAuthenticator authenticator;
    private final AuthStateHC4 proxyAuthState;
    private final Lookup<AuthSchemeProvider> authSchemeProvider;
    private final ConnectionReuseStrategy reuseStrategy;

    private SocketFactory socketFactory;

    /**
     * @since 4.3
     */
    public ProxyClient(
            final HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory,
            final ConnectionConfig connectionConfig,
            final RequestConfig requestConfig) {
        super();
        this.connFactory = connFactory != null ? connFactory : ManagedHttpClientConnectionFactory.INSTANCE;
        this.connectionConfig = connectionConfig != null ? connectionConfig : ConnectionConfig.DEFAULT;
        this.requestConfig = requestConfig != null ? requestConfig : RequestConfig.DEFAULT;
        this.httpProcessor = new ImmutableHttpProcessor(
                new RequestTargetHostHC4(), new RequestClientConnControl(), new RequestUserAgentHC4());
        this.requestExec = new HttpRequestExecutor();
        this.proxyAuthStrategy = new ProxyAuthenticationStrategy();
        this.authenticator = new HttpAuthenticator();
        this.proxyAuthState = new AuthStateHC4();
        this.authSchemeProvider = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactoryHC4())
                .register(AuthSchemes.DIGEST, new DigestSchemeFactoryHC4())
                .register(AuthSchemes.NTLM, new NTLMSchemeFactory()).build();
        this.reuseStrategy = new DefaultConnectionReuseStrategyHC4();
    }

    /**
     * @deprecated (4.3) use {@link ProxyClient#ProxyClient(HttpConnectionFactory, ConnectionConfig, RequestConfig)}
     */
    @Deprecated
    public ProxyClient(final HttpParams params) {
        this(null,
                HttpParamConfig.getConnectionConfig(params),
                HttpClientParamConfig.getRequestConfig(params));
    }

    /**
     * @since 4.3
     */
    public ProxyClient(final RequestConfig requestConfig) {
        this(null, null, requestConfig);
    }

    public ProxyClient() {
        this(null, null, null);
    }

    public ProxyClient(SocketFactory socketFactory) {
        this(null, null, null);
        this.socketFactory = socketFactory;
    }

    /**
     * @deprecated (4.3) do not use.
     */
    @Deprecated
    public HttpParams getParams() {
        return new BasicHttpParams();
    }

    /**
     * @deprecated (4.3) do not use.
     */
    @Deprecated
    public Lookup<AuthSchemeProvider> getAuthSchemeProvider() {
        return this.authSchemeProvider;
    }

    public Socket tunnel(
            final HttpHost proxy,
            final HttpHost target,
            final Credentials credentials) throws IOException, HttpException {
        Args.notNull(proxy, "Proxy host");
        Args.notNull(target, "Target host");
        Args.notNull(credentials, "Credentials");
        HttpHost host = target;
        if (host.getPort() <= 0) {
            host = new HttpHost(host.getHostName(), 80, host.getSchemeName());
        }
        final HttpRoute route = new HttpRoute(
                host,
                this.requestConfig.getLocalAddress(),
                proxy, false, TunnelType.TUNNELLED, LayerType.PLAIN);

        final ManagedHttpClientConnection conn = this.connFactory.create(
                route, this.connectionConfig);
        final HttpContext context = new BasicHttpContextHC4();
        HttpResponse response;

        final HttpRequest connect = new BasicHttpRequest(
                "CONNECT", host.toHostString(), HttpVersion.HTTP_1_1);

        final BasicCredentialsProviderHC4 credsProvider = new BasicCredentialsProviderHC4();
        credsProvider.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()), credentials);

        // Populate the execution context
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, target);
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, conn);
        context.setAttribute(HttpCoreContext.HTTP_REQUEST, connect);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.PROXY_AUTH_STATE, this.proxyAuthState);
        context.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider);
        context.setAttribute(HttpClientContext.AUTHSCHEME_REGISTRY, this.authSchemeProvider);
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, this.requestConfig);

        this.requestExec.preProcess(connect, this.httpProcessor, context);


        for (; ; ) {
            if (!conn.isOpen()) {
//                final Socket tunnel = new Socket(proxy.getHostName(), proxy.getPort());
////                final SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getSocketFactory().createSocket(tunnel, host.getHostName(), host.getPort(), true);
////                sslSocket.startHandshake();
//                conn.bind(tunnel);

                final Socket socket = new Socket(proxy.getHostName(), proxy.getPort());
                conn.bind(socket);
            }

            this.authenticator.generateAuthResponse(connect, this.proxyAuthState, context);

            response = this.requestExec.execute(connect, conn, context);

            final int status = response.getStatusLine().getStatusCode();

            if (status < 200) {
                throw new HttpException("Unexpected response to CONNECT request: " +
                        response.getStatusLine());
            }

            if (this.authenticator.isAuthenticationRequested(proxy, response,
                    this.proxyAuthStrategy, this.proxyAuthState, context)) {
                if (this.authenticator.handleAuthChallenge(proxy, response,
                        this.proxyAuthStrategy, this.proxyAuthState, context)) {
                    // Retry request
                    if (this.reuseStrategy.keepAlive(response, context)) {
                        // Consume response content
                        final HttpEntity entity = response.getEntity();
                        EntityUtilsHC4.consume(entity);
                    } else {
                        conn.close();
                    }
                    // discard previous auth header
                    connect.removeHeaders(AUTH.PROXY_AUTH_RESP);
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        final int status = response.getStatusLine().getStatusCode();

        RLog.d("ProxyClient tunnal status : " + status);

        if (status > 299) {

            // Buffer response content
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                response.setEntity(new BufferedHttpEntityHC4(entity));
            }

            conn.close();
            throw new TunnelRefusedException("CONNECT refused by proxy: " +
                    response.getStatusLine(), response);
        }
        return conn.getSocket();
    }

    private void doTunnelHandshake(Socket tunnel, String host, int port, String username, String password)
            throws IOException {
        OutputStream out = tunnel.getOutputStream();
        String msg = "CONNECT " + host + ":" + port + " HTTP/1.1\n"
                + "User-Agent: "
                + "Proxy-Authorization: NTLM "
                + "\r\n\r\n";
        byte b[];
        try {
            /*
             * We really do want ASCII7 -- the http protocol doesn't change
             * with locale.
             */
            b = msg.getBytes("ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            /*
             * If ASCII7 isn't there, something serious is wrong, but
             * Paranoia Is Good (tm)
             */
            b = msg.getBytes();
        }
        out.write(b);
        out.flush();

        /*
         * We need to store the reply so we can create a detailed
         * error message to the user.
         */
        byte reply[] = new byte[200];
        int replyLen = 0;
        int newlinesSeen = 0;
        boolean headerDone = false;     /* Done on first newline */

        InputStream in = tunnel.getInputStream();
        boolean error = false;

        while (newlinesSeen < 2) {
            int i = in.read();
            if (i < 0) {
                throw new IOException("Unexpected EOF from proxy");
            }
            if (i == '\n') {
                headerDone = true;
                ++newlinesSeen;
            } else if (i != '\r') {
                newlinesSeen = 0;
                if (!headerDone && replyLen < reply.length) {
                    reply[replyLen++] = (byte) i;
                }
            }
        }

        /*
         * Converting the byte array to a string is slightly wasteful
         * in the case where the connection was successful, but it's
         * insignificant compared to the network overhead.
         */
        String replyStr;
        try {
            replyStr = new String(reply, 0, replyLen, "ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            replyStr = new String(reply, 0, replyLen);
        }

        /* We asked for HTTP/1.0, so we should get that back */
        if (!replyStr.startsWith("HTTP/1.1 200")) {
            throw new IOException("Unable to tunnel through "
                    + ".  Proxy returns \"" + replyStr + "\"");
        }

        /* tunneling Handshake was successful! */
    }

}
