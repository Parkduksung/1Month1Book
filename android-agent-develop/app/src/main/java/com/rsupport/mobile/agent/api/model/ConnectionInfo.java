package com.rsupport.mobile.agent.api.model;

import android.content.Context;

import com.rsupport.mobile.agent.constant.AgentBasicInfo;

import org.jetbrains.annotations.NotNull;
import org.koin.java.KoinJavaComponent;

import kotlin.Lazy;

public class ConnectionInfo {

    private static final String HTTP_PORT = "80";
    private static final String HTTPS_PORT = "443";

    private String agentlisturl = "";
    private String agentupdateurl = "";
    private String newnoticeseq = "";
    private String passwordlimitdays = "";

    //0:Desktop, 1:iTouch, 2:WM, 3:android
    public final String ctype = "3";

    private String newVersion = "0";
    private String accountLock = "0";
    private String waitLockTime = "00:00";
    private String loginFailCount = "0";
    private String userKey;

    private Lazy<Context> contextLazy = KoinJavaComponent.inject(Context.class);

    //원격탐색기 서버옵션( 7 : 모든 Agent의 원격탐색기 감추기, 권한보다도 우선한다.)
    private String rvoemtype = "";

    public void clear() {
        setPasswordlimitdays("");

        setAccountLock("0");
        setWaitLockTime("00:00");
        setLoginFailCount("0");
    }

    public String getServerPageURL(String pass) {
        String serverAddr = "";
        String webServerAddr = getWebServer().replace("https://", "").replace("http://", "");

        if (isSSLProtocol()) {
            serverAddr = getWebServerHttpsAddress(webServerAddr);
        } else {
            serverAddr = getWebServerHttpAddress(webServerAddr);
        }
        return serverAddr + pass;
    }

    @NotNull
    private String getWebServerHttpAddress(String webServerAddr) {
        String serverAddr;
        if (isWebServerPortHttp()) {
            serverAddr = "http://" + webServerAddr + ":" + getWebServerPort();
        } else {
            serverAddr = "http://" + webServerAddr;
        }
        return serverAddr;
    }

    @NotNull
    private String getWebServerHttpsAddress(String webServerAddr) {
        String serverAddr;
        if (isWebServerPortHttps()) {
            serverAddr = "https://" + webServerAddr + ":" + getWebServerPort();
        } else {
            serverAddr = "https://" + webServerAddr;
        }
        return serverAddr;
    }

    private boolean isWebServerPortHttp() {
        return getWebServerPort() != null && !getWebServerPort().equals("") && !getWebServerPort().equals(HTTP_PORT);
    }

    private boolean isWebServerPortHttps() {
        return getWebServerPort() != null && !getWebServerPort().equals("") && !getWebServerPort().equals(HTTPS_PORT);
    }

    private boolean isSSLProtocol() {
        return getWebProtocol() != null && getWebProtocol().toLowerCase().equals("https");
    }

    public String getWebProtocol() {
        return AgentBasicInfo.getAgentWebProtocol(contextLazy.getValue());
    }

    public void setWebProtocol(String webProtocol) {
        AgentBasicInfo.setAgentWebProtocol(contextLazy.getValue(), webProtocol);
    }

    public String getWebServer() {
        return AgentBasicInfo.getAgentWebServer(contextLazy.getValue());
    }

    public void setWebServer(String webServer) {
        AgentBasicInfo.setAgentWebServer(contextLazy.getValue(), webServer);
    }

    public String getWebServerPort() {
        return AgentBasicInfo.getAgentWebPort(contextLazy.getValue());
    }

    public void setWebServerPort(String webServerPort) {
        AgentBasicInfo.setAgentWebPort(contextLazy.getValue(), webServerPort);
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getAgentlisturl() {
        return agentlisturl;
    }

    public void setAgentlisturl(String agentlisturl) {
        this.agentlisturl = agentlisturl;
    }

    public String getAgentupdateurl() {
        return agentupdateurl;
    }

    public void setAgentupdateurl(String agentupdateurl) {
        this.agentupdateurl = agentupdateurl;
    }

    public String getNewnoticeseq() {
        return newnoticeseq;
    }

    public void setNewnoticeseq(String newnoticeseq) {
        this.newnoticeseq = newnoticeseq;
    }

    public String getPasswordlimitdays() {
        return passwordlimitdays;
    }

    public void setPasswordlimitdays(String passwordlimitdays) {
        this.passwordlimitdays = passwordlimitdays;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public String getAccountLock() {
        return accountLock;
    }

    public void setAccountLock(String accountLock) {
        this.accountLock = accountLock;
    }

    public String getWaitLockTime() {
        return waitLockTime;
    }

    public void setWaitLockTime(String waitLockTime) {
        this.waitLockTime = waitLockTime;
    }

    public String getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(String loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public String getRvoemtype() {
        return rvoemtype;
    }

    public void setRvoemtype(String rvoemtype) {
        this.rvoemtype = rvoemtype;
    }
}
