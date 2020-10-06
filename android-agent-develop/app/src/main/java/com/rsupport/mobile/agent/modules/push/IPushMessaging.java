package com.rsupport.mobile.agent.modules.push;

public interface IPushMessaging {
    public static final String ACTION_PUSH_MESSAGING = "com.rsupport.rvagent.action.push.MESSAGING";

    public static final int TYPE_CONNECTED = 100;
    public static final int TYPE_MSG_ARRIVED = 200;
    public static final int TYPE_CONNECTION_LOST = 400;

    public static final int TYPE_DISCONNECTED = 900;
    public static final int TYPE_CONNECT_ERROR = 910;

    public static final int TYPE_WEB_LOGIN_ERROR = 920;
    public static final int TYPE_SELF_DISCONNECT_REMOTE = 930;
    public static final int TYPE_FORCE_UPDATE = 940;

    public static final int TYPE_VERSION_CONNECTED = 101;
    public static final int TYPE_VERSION_MSG_ARRIVED = 201;
    public static final int TYPE_VERSION_CONNECTION_LOST = 401;

    public static final int TYPE_VERSION_DISCONNECTED = 901;
    public static final int TYPE_VERSION_CONNECT_ERROR = 911;

    public static final String EXTRA_KEY_TYPE = "extra_key_type";
    public static final String EXTRA_KEY_VALUE = "extra_key_value";

}
