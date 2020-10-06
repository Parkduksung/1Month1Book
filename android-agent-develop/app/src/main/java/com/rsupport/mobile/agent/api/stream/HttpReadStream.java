package com.rsupport.mobile.agent.api.stream;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rsupport.mobile.agent.api.WebReadStream;
import com.rsupport.mobile.agent.api.net.HttpURLConnectionFactory;
import com.rsupport.rscommon.define.RSErrorCode;
import com.rsupport.rscommon.exception.RSException;
import com.rsupport.util.log.RLog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class HttpReadStream extends BaseHttpReadStream {
    private String url;
    private HashMap<String, String> parameters;
    private boolean isPost;
    private String accessToken;
    private ParameterGenerator parameterGenerator;
    private HttpURLConnectionFactory httpURLConnectionFactory;

    public HttpReadStream(
            @NonNull String url,
            @NonNull HashMap<String, String> parameters,
            boolean isPost,
            @Nullable String accessToken,
            @NonNull ParameterGenerator parameterGenerator,
            @NonNull HttpURLConnectionFactory httpURLConnectionFactory
    ) {
        this.url = url;
        this.parameters = parameters;
        this.isPost = isPost;
        this.accessToken = accessToken;
        this.parameterGenerator = parameterGenerator;
        this.httpURLConnectionFactory = httpURLConnectionFactory;
    }

    @NotNull
    @Override
    protected InputStream getInputStream() throws RSException {
        RLog.v("ServerURL : " + url);
        return getInputStreamByHttpConnection();
    }

    private @NonNull
    InputStream getInputStreamByHttpConnection() throws RSException {
        return isPost ? connectByHttpPost(url, parameters) : connectByHttpGet(url, parameters);
    }

    private @NonNull
    InputStream connectByHttpPost(@NonNull String serverAddr, @NonNull HashMap<String, String> parameters) throws RSException {
        try {
            final String param = parameterGenerator.generate(parameters);
            final HttpURLConnection httpConn = getHttpURLConnection(serverAddr);
            writeParams(httpConn, param);
            checkResponseCode(httpConn);
            return getInputStream(httpConn);
        } catch (MalformedURLException e) {
            throw new RSException(RSErrorCode.Network.MALFORMED_URL);
        } catch (IOException e) {
            throw new RSException(RSErrorCode.Network.IO_ERROR);
        }
    }

    private InputStream connectByHttpGet(@NonNull String serverAddr, @NonNull HashMap<String, String> hashMap) throws RSException {
        try {
            final String param = parameterGenerator.generate(hashMap);
            final HttpURLConnection httpConn = getHttpURLConnection(serverAddr, param);
            checkResponseCode(httpConn);
            return getInputStream(httpConn);
        } catch (MalformedURLException e) {
            throw new RSException(RSErrorCode.Network.MALFORMED_URL);
        } catch (IOException e) {
            throw new RSException(RSErrorCode.Network.IO_ERROR);
        }
    }

    @NotNull
    private HttpURLConnection getHttpURLConnection(@NonNull String serverAddr, String param) throws IOException {
        final HttpURLConnection httpConn = httpURLConnectionFactory.create(serverAddr + "?" + param);
        httpConn.setRequestMethod("GET");
        setHttpProperties(httpConn);
        return httpConn;
    }

    @NotNull
    private HttpURLConnection getHttpURLConnection(@NonNull String serverAddr) throws IOException {
        final HttpURLConnection httpConn = httpURLConnectionFactory.create(serverAddr);
        httpConn.setDoOutput(true);
        httpConn.setRequestMethod("POST");
        setHttpProperties(httpConn);
        return httpConn;
    }

    private void checkResponseCode(HttpURLConnection httpConn) throws IOException, RSException {
        if (httpConn.getResponseCode() == -1) {
            throw new RSException(RSErrorCode.Network.IO_ERROR);
        }
    }

    private InputStream getInputStream(@NonNull HttpURLConnection httpConn) throws IOException {
        InputStream inputStream;
        String s = httpConn.getHeaderField("content-Encoding");
        if (s == null) {
            s = "unEncoding";
        }
        InputStream is = httpConn.getInputStream();
        if (!s.contains("gzip")) {
            inputStream = is;
        } else {
            inputStream = new GZIPInputStream(is);
        }
        return inputStream;
    }


    private void writeParams(HttpURLConnection httpConn, String param) throws IOException {
        try (PrintWriter out = new PrintWriter(httpConn.getOutputStream())) {
            out.print(param);
            out.flush();
        }
    }

    private void setHttpProperties(HttpURLConnection httpConn) {
        httpConn.setUseCaches(false);
        httpConn.setRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

        httpConn.setRequestProperty(
                "Accept",
                "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
        httpConn.setRequestProperty("Accept-Language", "ko");
        httpConn.setRequestProperty("Accept-Encoding", "gzip");
        httpConn.setRequestProperty("Connection", "close");
        httpConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");


        if (!TextUtils.isEmpty(accessToken)) {
            httpConn.setRequestProperty("X-AUTH-TOKEN", accessToken);
        }

        HttpURLConnection.setFollowRedirects(true);
        httpConn.setInstanceFollowRedirects(true);
    }

    public static class Builder {
        private String serverURL = "";
        private HashMap<String, String> parameter = new HashMap<>();
        private boolean isPost;
        private String accessToken;
        private ParameterGenerator parameterGenerator = new ParameterGenerator();
        private HttpURLConnectionFactory httpURLConnectionFactory = new HttpURLConnectionFactory();

        @NonNull
        public WebReadStream build() {
            return new HttpReadStream(serverURL, parameter, isPost, accessToken, parameterGenerator, httpURLConnectionFactory);
        }

        @NonNull
        public Builder setServerURL(@NonNull String serverURL) {
            this.serverURL = serverURL;
            return this;
        }

        @NonNull
        public Builder setParameter(@NonNull HashMap<String, String> parameter) {
            this.parameter = parameter;
            return this;
        }

        @NonNull
        public Builder setPost(boolean post) {
            isPost = post;
            return this;
        }

        @NonNull
        public Builder setAccessToken(@Nullable String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        @NonNull
        public Builder setParameterGenerator(@NonNull ParameterGenerator parameterGenerator) {
            this.parameterGenerator = parameterGenerator;
            return this;
        }

        @NonNull
        public Builder setHttpURLConnectionFactory(@NonNull HttpURLConnectionFactory httpURLConnectionFactory) {
            this.httpURLConnectionFactory = httpURLConnectionFactory;
            return this;
        }
    }
}
