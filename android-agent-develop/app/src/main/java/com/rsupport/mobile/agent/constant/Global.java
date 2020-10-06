package com.rsupport.mobile.agent.constant;

import android.content.Context;

import com.rsupport.mobile.agent.api.DefaultWebCryptoFactory;
import com.rsupport.mobile.agent.api.WebConnection;

import com.rsupport.mobile.agent.api.WebStreamFactory;
import com.rsupport.mobile.agent.api.parser.DefaultStreamParserFactory;
import com.rsupport.mobile.agent.service.HxdecThread;

public class Global {

    /**
     * Singleton
     **/
    private static Global m_global = null;

    private Context appContext = null;

    private WebConnection webConnection = null;

    private HxdecThread agentThread = null;

    private static void createInstance() {
        m_global = new Global();
    }

    private Global() {
    }

    public synchronized static Global getInstance() {
        if (m_global == null) {
            createInstance();
        }
        return m_global;
    }

    public synchronized WebConnection getWebConnection() {
        if (webConnection == null) {
            DefaultWebCryptoFactory defaultWebCryptoFactory = new DefaultWebCryptoFactory();
            webConnection = new WebConnection(defaultWebCryptoFactory, new WebStreamFactory(appContext), new DefaultStreamParserFactory());
            webConnection.setContext(appContext);
            webConnection.setNetworkInfo();
        }
        return webConnection;
    }

    public Context getAppContext() {
        return appContext;
    }

    public void setAppContext(Context appContext) {
        this.appContext = appContext;
    }

    public HxdecThread getAgentThread() {
        return agentThread;
    }

    public void setAgentThread(HxdecThread agentThread) {
        this.agentThread = agentThread;
    }


}
