package com.rsupport.mobile.agent.api.stream;

import androidx.annotation.NonNull;

import com.android.internal.http.multipart.Part;
import com.rsupport.mobile.agent.api.WebReadStream;
import com.rsupport.mobile.agent.constant.ComConstant;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.repo.config.ProxyInfo;
import com.rsupport.rscommon.define.RSErrorCode;
import com.rsupport.rscommon.exception.RSException;
import com.rsupport.util.log.RLog;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicSchemeFactoryHC4;
import org.apache.http.impl.auth.DigestSchemeFactoryHC4;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtilsHC4;
import org.koin.java.KoinJavaComponent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import config.EngineConfigSetting;
import kotlin.Lazy;

public class MultiPartReadStream implements WebReadStream {
    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);

    private String serverAddr;
    private HashMap<String, String> hashMap;
    private String[] files;

    public MultiPartReadStream(String serverAddr, HashMap<String, String> parameters, String[] files) {
        this.serverAddr = serverAddr;
        this.hashMap = parameters;
        this.files = files;
    }

    @NonNull
    @Override
    public InputStream getStream() throws RSException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        URL url = null;
        HttpURLConnection httpConn = null;
        InputStream inputStream = null;
        PrintWriter out = null;
        try {
            RLog.d("------------------------------------ serverAddr : " + serverAddr);

            if (hashMap == null) {
                throw new RSException(RSErrorCode.Network.MISSING_PARAMETER);
            }

            Set<String> keySet = hashMap.keySet();

            Part[] parts = new Part[hashMap.size() + 1];
            int index = 0;

            Iterator<String> iterator = keySet.iterator();

            MultipartEntity multi = new MultipartEntity();

            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = (String) hashMap.get(key);
                //				parts[index++] = new StringPart(URLEncoder.encode(key, "UTF-8"), URLEncoder.encode(value, "UTF-8"));
                multi.addPart(URLEncoder.encode(key, "UTF-8"), new StringBody(URLEncoder.encode(value, "UTF-8")));

            }

            for (String filePath : files) {
                File f = new File(filePath);
                multi.addPart("Image", new FileBody(f, "image/jpg"));
            }


            String param = url_encoding(hashMap);
            String strFullURL = serverAddr;
            if (configRepositoryLazy.getValue().isProxyUse()) {
                if (!isValidProxyInfo()) {
                    throw new RSException(ComConstant.NET_ERR_PROXYINFO_NULL);
                }
                strFullURL = serverAddr + "?" + param;
                byte[] bytes = getBodyOnProxy(strFullURL, param, true);
                if (bytes == null) {
                    throw new RSException(ComConstant.NET_ERR_PROXY_VERIFY);
                }
                inputStream = new ByteArrayInputStream(bytes);
            } else {
                url = new URL(serverAddr);
                if (serverAddr.contains("https")) {
                    HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                    RLog.d("connectByHttpPost : url openConnection : " + serverAddr);
                    https.setHostnameVerifier(DO_NOT_VERIFY);
                    httpConn = https;
                } else {
                    url = new URL(serverAddr);
                    httpConn = (HttpURLConnection) url.openConnection();
                }

                httpConn.setUseCaches(false);
                httpConn.setDoOutput(true);
                httpConn.setRequestMethod("POST");
                httpConn.setRequestMethod("GET");
                httpConn.setRequestProperty("User-Agent",
                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

                httpConn.setRequestProperty(
                        "Accept",
                        "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                httpConn.setRequestProperty("Accept-Language", "ko");
                httpConn.setRequestProperty("Accept-Encoding", "gzip");
                httpConn.setRequestProperty("Connection", "close");
                httpConn.setConnectTimeout(8000);
                httpConn.setRequestProperty("Content-Type", "multipart/form-data");
                //				httpConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                HttpURLConnection.setFollowRedirects(true);
                httpConn.setInstanceFollowRedirects(true);
                httpConn.setRequestProperty(multi.getContentType().getName(), multi.getContentType().getValue());

                RLog.d("connectByHttpPost : url getOutputStream serverAddr : " + serverAddr);
                multi.writeTo(httpConn.getOutputStream());
                out = new PrintWriter(httpConn.getOutputStream());
                RLog.d("connectByHttpPost : url getOutputStream strFullURL : " + strFullURL);
                //				out.print(param);
                out.flush();

                RLog.d("------------------------------------ httpConn.getResponseCode() : " + httpConn.getResponseCode());

                if (httpConn.getResponseCode() == (-1)) {
                    throw new RSException(RSErrorCode.Network.IO_ERROR);
                }

                String s = httpConn.getHeaderField("content-Encoding");

                if (s == null) {
                    s = "unEncoding";
                }
                InputStream is = httpConn.getInputStream();
                if (!s.contains("gzip")) {
                    inputStream = is;
                } else {
                    GZIPInputStream gz = new GZIPInputStream(is);
                    inputStream = gz;
                }
            }
        } catch (MalformedURLException e) {
            RLog.w(e);
            throw new RSException(RSErrorCode.Network.MALFORMED_URL);
        } catch (IOException e) {
            RLog.w(e);
            throw new RSException(RSErrorCode.Network.IO_ERROR);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return inputStream;
    }

    private String url_encoding(HashMap<String, String> hashMap) throws UnsupportedEncodingException {
        if (hashMap == null) {
            throw new IllegalArgumentException("argument is null");
        }
        Set<String> keySet = hashMap.keySet();
        StringBuffer buf = new StringBuffer();
        boolean isFirst = true;

        Iterator<String> iterator = keySet.iterator();

        while (iterator.hasNext()) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append('&');
            }
            String key = (String) iterator.next();
            String value = (String) hashMap.get(key);

            RLog.d("URL Encoding key == " + key);
            RLog.d("URL Encoding value == " + value);

            buf.append(URLEncoder.encode(key, "UTF-8"));
            buf.append('=');
            buf.append(URLEncoder.encode(value, "UTF-8"));
        }
        return buf.toString();
    }

    private byte[] getBodyOnProxy(String url, String param, boolean isPost) throws RSException {
        byte[] bytes = null;
        try {
            ProxyInfo proxyInfo = configRepositoryLazy.getValue().getProxyInfo();

            String proxyAddr = proxyInfo.getAddress();
            int proxyPort = Integer.parseInt(proxyInfo.getPort());
            String proxyUsername = proxyInfo.getId();
            String proxyPasswd = proxyInfo.getPwd();

            Lookup<AuthSchemeProvider> authSchemeRegistryLookup = RegistryBuilder.<AuthSchemeProvider>create()
                    .register(AuthSchemes.NTLM, new NTLMSchemeFactory())
                    .register(AuthSchemes.DIGEST, new DigestSchemeFactoryHC4())
                    .register(AuthSchemes.BASIC, new BasicSchemeFactoryHC4())
                    //                    .register(AuthSchemes.KERBEROS, new BasicSchemeFactoryHC4())
                    .build();

            NTCredentials ntCreds = new NTCredentials(proxyUsername, proxyPasswd, "", "");
            //NTCredentials                             :: NTLM
            //UsernamePasswordCredentials               :: DIGEST
            //BasicUsernamePasswordCredentials          :: BASIC
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(proxyAddr, proxyPort), ntCreds);
            HttpHost proxy = new HttpHost(proxyAddr, proxyPort);

            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .setDefaultAuthSchemeRegistry(authSchemeRegistryLookup)
                    .setProxy(proxy)
                    .build();

            HttpResponse httpResponse;
            HttpRequestBase httpRequestBase = null;
            if (isPost) {
                HttpPost httpPost = new HttpPost(url);

                httpPost.addHeader("User-Agent",
                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
                httpPost.addHeader(
                        "Accept",
                        "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                httpPost.addHeader("Accept-Language", "ko");
                httpPost.addHeader("Accept-Encoding", "gzip");
                httpPost.addHeader("Connection", "close");
                httpPost.addHeader("Content-type", "application/x-www-form-urlencoded");

                List<NameValuePair> params = new ArrayList<>();

                String[] ps = param.split("&");
                for (int i = 0; i < ps.length; i++) {
                    String[] pss = ps[i].split("=");
                    if (pss.length > 1) {
                        RLog.e("pss[0]=" + pss[0] + "  pss[1]=" + pss[1]);
                        params.add(new BasicNameValuePair(pss[0], URLDecoder.decode(pss[1], HTTP.UTF_8)));
                    }
                }
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                httpRequestBase = httpPost;

            } else {
                HttpGet httpGet = new HttpGet(url + "?" + param);

                httpRequestBase = httpGet;
            }

            httpResponse = httpclient.execute(httpRequestBase);

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            RLog.e("getStatusCode : " + statusCode);
            if (statusCode != HttpStatus.SC_OK) {
                throw new RSException(ComConstant.NET_ERR_PROXY_VERIFY);
            }
            bytes = EntityUtilsHC4.toString(httpResponse.getEntity()).getBytes(EngineConfigSetting.UTF_8);

        } catch (IOException e) {
            throw new RSException(ComConstant.NET_ERR_PROXY_VERIFY);
        }

        return bytes;
    }

    private boolean isValidProxyInfo() {
        ProxyInfo proxyInfo = configRepositoryLazy.getValue().getProxyInfo();
        return proxyInfo.isValidate();
    }

    private final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
