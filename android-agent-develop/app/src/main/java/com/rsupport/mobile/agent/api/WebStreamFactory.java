package com.rsupport.mobile.agent.api;

import android.content.Context;

import com.rsupport.mobile.agent.api.stream.HttpReadStream;
import com.rsupport.mobile.agent.api.stream.MultiPartReadStream;
import com.rsupport.mobile.agent.api.stream.ProxyHttpReadStream;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.koin.java.KoinJavaComponent;

import java.util.HashMap;

import kotlin.Lazy;

public class WebStreamFactory {
    private Context context;
    private String serverURL;
    private HashMap<String, String> parameters;
    private String[] files;
    private boolean isPost;
    private boolean isUseAccessToken = false;
    private boolean isMultiPart = false;

    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);

    public WebStreamFactory(Context context) {
        this.context = context;
    }

    public WebStreamFactory setRequestParams(@NotNull String url, @NotNull HashMap<String, String> parameters, boolean isPost) {
        isMultiPart = false;
        this.serverURL = url;
        this.parameters = parameters;
        this.isPost = isPost;
        return this;
    }

    public WebStreamFactory setUseAccessToken(boolean isUseAccessToken) {
        this.isUseAccessToken = isUseAccessToken;
        return this;
    }

    public WebStreamFactory setMultiPart(@NotNull String url, @NotNull HashMap<String, String> hashMap, @NotNull String[] files) {
        isMultiPart = true;
        this.serverURL = url;
        this.parameters = hashMap;
        this.files = files;
        return this;
    }

    public WebReadStream create() {
        if (isMultiPart) {
            return new MultiPartReadStream(serverURL, parameters, files);
        } else {
            if (configRepositoryLazy.getValue().isProxyUse()) {
                return new ProxyHttpReadStream.Builder()
                        .setServerURL(serverURL)
                        .setPost(isPost)
                        .setParameter(parameters)
                        .setAccessToken(getAccessToken())
                        .setProxyInfo(configRepositoryLazy.getValue().getProxyInfo())
                        .build();
            } else {
                return new HttpReadStream.Builder()
                        .setServerURL(serverURL)
                        .setPost(isPost)
                        .setParameter(parameters)
                        .setAccessToken(getAccessToken())
                        .build();
            }
        }
    }

    @Nullable
    private String getAccessToken() {
        return isUseAccessToken ? AgentBasicInfo.getAccessToken(context) : null;
    }
}