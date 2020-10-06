package com.rsupport.mobile.agent.api.stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rsupport.mobile.agent.api.WebReadStream;
import com.rsupport.mobile.agent.api.net.GetHttpRequestFactory;
import com.rsupport.mobile.agent.api.net.HttpClientFactory;
import com.rsupport.mobile.agent.api.net.HttpRequestFactory;
import com.rsupport.mobile.agent.api.net.PostHttpRequestFactory;
import com.rsupport.mobile.agent.api.net.ProxyHttpClientFactory;
import com.rsupport.mobile.agent.constant.ComConstant;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.repo.config.ProxyInfo;
import com.rsupport.rscommon.define.RSErrorCode;
import com.rsupport.rscommon.exception.RSException;
import com.rsupport.util.log.RLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProxyHttpReadStream extends BaseHttpReadStream {
    private String serverURL;
    private HashMap<String, String> parameters;
    private boolean isPost;
    private String accessToken;
    private ProxyInfo proxyInfo;
    private ParameterGenerator parameterGenerator;
    private HttpClientFactory httpClientFactory;
    private HttpRequestFactory httpRequestFactory;

    public ProxyHttpReadStream(
            @NonNull String serverURL,
            @NonNull HashMap<String, String> parameters,
            @Nullable String accessToken,
            boolean isPost,
            @NonNull ParameterGenerator parameterGenerator,
            @NonNull ProxyInfo proxyInfo,
            @NonNull HttpClientFactory httpClientFactory,
            @NonNull HttpRequestFactory httpRequestFactory) {
        this.serverURL = serverURL;
        this.parameters = parameters;
        this.isPost = isPost;
        this.accessToken = accessToken;
        this.parameterGenerator = parameterGenerator;
        this.proxyInfo = proxyInfo;
        this.httpClientFactory = httpClientFactory;
        this.httpRequestFactory = httpRequestFactory;
    }

    @NotNull
    @Override
    protected InputStream getInputStream() throws RSException {
        RLog.v("ServerURL : " + serverURL);
        if (!isValidProxyInfo()) {
            throw new RSException(ComConstant.NET_ERR_PROXYINFO_NULL);
        }
        return getInputStreamByProxyConnection(serverURL, parameters, isPost);
    }

    @NonNull
    private InputStream getInputStreamByProxyConnection(@NonNull String serverAddr, @NonNull HashMap<String, String> parameters, boolean isPost) throws RSException {
        try {
            String parameterString = parameterGenerator.generate(parameters);
            final HttpClient httpclient = httpClientFactory.create();
            final HttpUriRequest httpUriRequest = createHttpUriRequest(serverAddr, isPost, parameterString);
            final HttpResponse httpResponse = httpclient.execute(httpUriRequest);
            return getBodyOnProxy(httpResponse);
        } catch (IOException e) {
            throw new RSException(RSErrorCode.Network.IO_ERROR);
        }
    }

    @NotNull
    private HttpRequestBase createHttpUriRequest(@NonNull String serverAddr, boolean isPost, String parameterString) throws UnsupportedEncodingException {
        return isPost ? createPostRequestBase(serverAddr, parameterString, accessToken) : createGetRequestBase(serverAddr, parameterString);
    }

    private boolean isValidProxyInfo() {
        return proxyInfo.isValidate();
    }

    @NonNull
    private InputStream getBodyOnProxy(HttpResponse httpResponse) throws RSException {
        try {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new RSException(ComConstant.NET_ERR_PROXY_VERIFY);
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity == null) {
                throw new RSException(ComConstant.NET_ERR_PROXY_VERIFY);
            }
            return httpEntity.getContent();
        } catch (Exception e) {
            RLog.e(e);
            throw new RSException(ComConstant.NET_ERR_PROXY_VERIFY);
        }
    }

    @NotNull
    private HttpRequestBase createGetRequestBase(String url, String param) {
        return httpRequestFactory.create(url + "?" + param);
    }

    @NotNull
    private HttpRequestBase createPostRequestBase(String url, String params, String accessToken) throws UnsupportedEncodingException {
        HttpPost httpPost = (HttpPost) httpRequestFactory.create(url);
        httpPost.addHeader("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
        httpPost.addHeader(
                "Accept",
                "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
        httpPost.addHeader("Accept-Language", "ko");
        httpPost.addHeader("Accept-Encoding", "gzip");
        httpPost.addHeader("Connection", "close");
        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded");

        if (!TextUtils.isEmpty(accessToken)) {
            httpPost.addHeader("X-AUTH-TOKEN", accessToken);
        }

        List<NameValuePair> paramsList = new ArrayList<>();

        String[] ps = params.split("&");
        for (int i = 0; i < ps.length; i++) {
            String[] pss = ps[i].split("=");
            if (pss.length > 1) {
                paramsList.add(new BasicNameValuePair(pss[0], URLDecoder.decode(pss[1], HTTP.UTF_8)));
            }
        }
        if (!paramsList.isEmpty()) {
            httpPost.setEntity(new UrlEncodedFormEntity(paramsList));
        }
        return httpPost;
    }

    public static class Builder {
        private String serverURL = "";
        private HashMap<String, String> parameter = new HashMap<>();
        private boolean isPost;
        private String accessToken;
        private ParameterGenerator parameterGenerator = new ParameterGenerator();
        private HttpClientFactory httpClientFactory;
        private HttpRequestFactory httpRequestFactory;
        private ProxyInfo proxyInfo = new ProxyInfo();

        @NonNull
        public WebReadStream build() {
            if (httpClientFactory == null) {
                httpClientFactory = new ProxyHttpClientFactory(proxyInfo);
            }

            if (httpRequestFactory == null) {
                httpRequestFactory = isPost ? new PostHttpRequestFactory() : new GetHttpRequestFactory();
            }
            return new ProxyHttpReadStream(serverURL, parameter, accessToken, isPost, parameterGenerator, proxyInfo, httpClientFactory, httpRequestFactory);
        }

        @NonNull
        public Builder setProxyInfo(@NonNull ProxyInfo proxyInfo) {
            this.proxyInfo = proxyInfo;
            return this;
        }

        @NonNull
        public Builder setHttpClientFactory(@NonNull HttpClientFactory httpClientFactory) {
            this.httpClientFactory = httpClientFactory;
            return this;
        }

        @NonNull
        public Builder setHttpRequestFactory(@NonNull HttpRequestFactory httpRequestFactory) {
            this.httpRequestFactory = httpRequestFactory;
            return this;
        }

        @NonNull
        public ProxyHttpReadStream.Builder setServerURL(@NonNull String serverURL) {
            this.serverURL = serverURL;
            return this;
        }

        @NonNull
        public ProxyHttpReadStream.Builder setParameter(@NonNull HashMap<String, String> parameter) {
            this.parameter = parameter;
            return this;
        }

        @NonNull
        public ProxyHttpReadStream.Builder setPost(boolean post) {
            isPost = post;
            return this;
        }

        @NonNull
        public ProxyHttpReadStream.Builder setAccessToken(@Nullable String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        @NonNull
        public ProxyHttpReadStream.Builder setParameterGenerator(@NonNull ParameterGenerator parameterGenerator) {
            this.parameterGenerator = parameterGenerator;
            return this;
        }
    }
}
