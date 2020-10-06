package com.rsupport.mobile.agent.api;

import android.content.Context;
import android.os.Build;

import com.rsupport.knox.KnoxManagerCompat;
import com.rsupport.mobile.agent.api.model.AccountChangeResult;
import com.rsupport.mobile.agent.api.model.AgentDeleteResult;
import com.rsupport.mobile.agent.api.model.AgentInstallResult;
import com.rsupport.mobile.agent.api.model.AgentLoginResult;
import com.rsupport.mobile.agent.api.model.AgentLogoutResult;
import com.rsupport.mobile.agent.api.model.AgentSessionResult;
import com.rsupport.mobile.agent.api.model.ChangeDeviceNameResult;
import com.rsupport.mobile.agent.api.model.CheckAccessIDValidateResult;
import com.rsupport.mobile.agent.api.model.CheckSupportKnoxResult;
import com.rsupport.mobile.agent.api.model.ConnectAgreeResult;
import com.rsupport.mobile.agent.api.model.ConsoleLoginResult;
import com.rsupport.mobile.agent.api.model.FcmRegisterResult;
import com.rsupport.mobile.agent.api.model.GroupInfo;
import com.rsupport.mobile.agent.api.model.KnoxKeyResult;
import com.rsupport.mobile.agent.api.model.NotifyConnectedResult;
import com.rsupport.mobile.agent.api.model.NotifyDisconnectedResult;
import com.rsupport.mobile.agent.api.model.SendLiveViewResult;
import com.rsupport.mobile.agent.api.parser.AccountChangeParser;
import com.rsupport.mobile.agent.api.parser.AgentDeleteParser;
import com.rsupport.mobile.agent.api.parser.AgentGroupListParser;
import com.rsupport.mobile.agent.api.parser.AgentGroupSearchParser;
import com.rsupport.mobile.agent.api.parser.AgentGroupSubListParser;
import com.rsupport.mobile.agent.api.parser.AgentInstallParser;
import com.rsupport.mobile.agent.api.parser.AgentLoginParser;
import com.rsupport.mobile.agent.api.parser.AgentLogoutParser;
import com.rsupport.mobile.agent.api.parser.AgentSessionResultParser;
import com.rsupport.mobile.agent.api.parser.ChangeDeviceNameParser;
import com.rsupport.mobile.agent.api.parser.CheckAccessIDValidateParser;
import com.rsupport.mobile.agent.api.parser.CheckSupportKnoxParser;
import com.rsupport.mobile.agent.api.parser.ConnectAgreeParser;
import com.rsupport.mobile.agent.api.parser.ConsoleLoginParser;
import com.rsupport.mobile.agent.api.parser.FcmRegisterParser;
import com.rsupport.mobile.agent.api.parser.KnoxKeyParser;
import com.rsupport.mobile.agent.api.parser.NotifyConnectedParser;
import com.rsupport.mobile.agent.api.parser.NotifyDisconnectedParser;
import com.rsupport.mobile.agent.api.parser.SendLiveViewParser;
import com.rsupport.mobile.agent.api.parser.StreamParser;
import com.rsupport.mobile.agent.api.parser.StreamParserFactory;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.modules.engine.EngineType;
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.utils.Result;
import com.rsupport.mobile.agent.utils.crypto.WebCrypto;
import com.rsupport.rscommon.define.RSErrorCode;
import com.rsupport.rscommon.define.RSErrorCode.Network;
import com.rsupport.rscommon.exception.RSException;
import com.rsupport.util.log.RLog;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.koin.java.KoinJavaComponent;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import kotlin.Lazy;

public class WebConnection {
    private final String BASE_CONSOLE_URL = "/services/api/agent/console_login";
    private String protocol;
    private String webServerIP;
    private String webServerPort;
    private boolean isSuccess = false;


    //agentList
    private Context context;

    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);
    private Lazy<EngineTypeCheck> engineTypeCheckLazy = KoinJavaComponent.inject(EngineTypeCheck.class);
    private Lazy<KnoxManagerCompat> knoxManagerCompatLazy = KoinJavaComponent.inject(KnoxManagerCompat.class);

    private boolean isAESEnable;
    private WebCryptoFactory webCryptoFactory;
    private WebStreamFactory webStreamFactory;
    private StreamParserFactory streamParserFactory;

    public WebConnection(
            @NotNull WebCryptoFactory webCryptoFactory,
            @NotNull WebStreamFactory webStreamFactory,
            @NotNull StreamParserFactory streamParserFactory
    ) {
        this.webCryptoFactory = webCryptoFactory;
        this.webStreamFactory = webStreamFactory;
        this.streamParserFactory = streamParserFactory;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setNetworkInfo() {
        webServerIP = configRepositoryLazy.getValue().getServerInfo().getUrl();
        setProtocolInfo(webServerIP);
    }

    public void setAESEnable(boolean isAESEnable) {
        this.isAESEnable = isAESEnable;
    }

    public Result<ConsoleLoginResult> consoleLogin(String webId, String webPass, String BizID, String macAddress, String localIP) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            if (isAESEnable) {
                WebCrypto crypto = webCryptoFactory.create();
                RLog.d("crypto : " + crypto);
                hashMap.put("WI", crypto.encrypt(webId));
                hashMap.put("WP", crypto.encrypt(webPass));
                hashMap.put("MAddr", crypto.encrypt(macAddress));
                hashMap.put("CIP", crypto.encrypt(localIP));
            } else {
                hashMap.put("WebID", webId);
                hashMap.put("WebPass", webPass);
                hashMap.put("MACADDR", macAddress);
                hashMap.put("CMDIP", localIP);
            }
            hashMap.put("CTYPE", GlobalStatic.connectionInfo.ctype);
            hashMap.put("BizID", BizID);
            hashMap.put("MModel", Build.MODEL);
            hashMap.put("MosVer", Build.VERSION.RELEASE);
            hashMap.put("MappVer", GlobalStatic.APPVERSION_NAME);

            // 기업용이 아니면 remove
            if (configRepositoryLazy.getValue().getProductType() == GlobalStatic.PRODUCT_PERSONAL) {
                hashMap.remove("MACADDR");
                hashMap.remove("MAddr");
            }

            InputStream is = getConnectStream(getFullUrl(getConsoleURL()), hashMap, true);
            if (is == null) {
                throw new IOException("stream is null");
            }
            StreamParser<ConsoleLoginResult> streamParser = streamParserFactory.create(ConsoleLoginParser.class);
            return streamParser.parse(is);
        } catch (IOException e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(Network.IO_ERROR));
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }


    public Result<List<GroupInfo>> getAgentGroupList(String lType, String BizID) {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        hashMap.put("LTYPE", lType);
        hashMap.put("CTYPE", "3"); //androidAgent...!
        hashMap.put("IDTYPE", GlobalStatic.connectionInfo.ctype);
        hashMap.put("BizID", BizID);
        hashMap.put("XML", "1");
        hashMap.put("USERKEY", GlobalStatic.connectionInfo.getUserKey());//prevent hacking

        isSuccess = false;
        InputStream is = null;
        try {
            is = getConnectStream(getServerPageUrl(GlobalStatic.connectionInfo.getAgentlisturl()), hashMap, true, true);
            if (is == null) {
                throw new IOException("stream is null");
            }
            ;
            final StreamParser<List<GroupInfo>> streamParser = streamParserFactory.create(AgentGroupListParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (IOException e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(Network.IO_ERROR));
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<List<GroupInfo>> getAgentGroupSubList(String lType, String groupId) {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        hashMap.put("LTYPE", lType);
        hashMap.put("CTYPE", "3"); //androidAgent...!
        hashMap.put("XML", "1");
        hashMap.put("USERKEY", GlobalStatic.connectionInfo.getUserKey());//prevent hacking
        hashMap.put("agentGroupId", groupId);

        try {
            InputStream is = getConnectStream(getServerPageUrl(GlobalStatic.connectionInfo.getAgentlisturl()), hashMap, true, true);
            if (is == null) {
                throw new IOException("stream is null");
            }
            ;
            final StreamParser<List<GroupInfo>> streamParser = streamParserFactory.create(AgentGroupSubListParser.class);
            return streamParser.parse(is);
        } catch (IOException e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(Network.IO_ERROR));
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    /**
     * 해당 device 가 knox 를 지원하는 단말인지 확인한다.
     *
     * @return knox 를 지원하면 true 그렇지 않으면 false.
     * @throws RSException
     */
    public Result<CheckSupportKnoxResult> reqeustCheckSupportKnox() {
        RLog.d("reqeustDeviceBlackList start " + Build.MODEL + "/" + Build.VERSION.RELEASE);
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();

            hashMap.put("modelName", Build.MODEL);
            hashMap.put("osVersion", Build.VERSION.RELEASE); //androidAgent...!
            if (engineTypeCheckLazy.getValue().getEngineType() == EngineType.ENGINE_TYPE_KNOX) {
                hashMap.put("knoxSdkVersion", knoxManagerCompatLazy.getValue().getKnoxSdkVersion(Global.getInstance().getAppContext()));
            } else {
                hashMap.put("knoxSdkVersion", "");
            }

            InputStream is = getConnectStream(getFullUrl("/services/api/agent/app_version_check"), hashMap, false);
            RLog.d("reqeustDeviceBlackList parsing begin");
            if (is == null) {
                throw new RSException(Network.IO_ERROR);
            }
            final StreamParser<CheckSupportKnoxResult> streamParser = streamParserFactory.create(CheckSupportKnoxParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<List<GroupInfo>> getGroupSearch(String lType, String cType, String searchText, String parentGroupId) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("USERKEY", GlobalStatic.connectionInfo.getUserKey());//prevent hacking
            hashMap.put("LTYPE", lType);
            hashMap.put("XML", "1");
            hashMap.put("CTYPE", cType);
            hashMap.put("searchText", searchText);
            hashMap.put("agentGroupId", parentGroupId);

            InputStream is = getConnectStream(getServerPageUrl(GlobalStatic.connectionInfo.getAgentlisturl()), hashMap, true, true);
            if (is == null) {
                return Result.Companion.failure(new RSException(Network.IO_ERROR));
            }
            final StreamParser<List<GroupInfo>> streamParser = streamParserFactory.create(AgentGroupSearchParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<CheckAccessIDValidateResult> checkAccessIDValidate(String accessId, String accessPass, String bizId) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            WebCrypto crypto = webCryptoFactory.create();
            hashMap.put("AI", crypto.encrypt(accessId));
            hashMap.put("AP", crypto.encrypt(accessPass));
            if (!bizId.equals("")) {
                hashMap.put("BizID", bizId);
            }
            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.RV_AGENT_URL_VALIDATE), hashMap);
            if (is == null) {
                return Result.Companion.failure(new RSException(Network.IO_ERROR));
            }
            StreamParser<CheckAccessIDValidateResult> streamParser = streamParserFactory.create(CheckAccessIDValidateParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.w(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.w(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<AgentInstallResult> agentInstall(String guid, String accessID, String accessPass, String agentName, String BizId) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            WebCrypto crypto = webCryptoFactory.create();

            hashMap.put("GUID", guid);
            hashMap.put("WI", crypto.encrypt(GlobalStatic.g_loginInfo.userid));
            hashMap.put("WP", crypto.encrypt(GlobalStatic.g_loginInfo.userpasswd));
            hashMap.put("BizID", BizId);
            hashMap.put("SSL", "1");
            hashMap.put("AIP", crypto.encrypt(GlobalStatic.getLocalIP()));
            hashMap.put("MAddr", crypto.encrypt(GlobalStatic.getMacAddress(context).replace(":", "")));
            hashMap.put("OSName", "Android" + android.os.Build.VERSION.RELEASE);
            hashMap.put("GroupID", GlobalStatic.g_agentInstallGroupID);
            hashMap.put("RSVFlag", "80");
            hashMap.put("model", android.os.Build.MODEL);

            hashMap.put("AI", crypto.encrypt(accessID));
            hashMap.put("AP", crypto.encrypt(accessPass));
            if (!GlobalStatic.connectionInfo.getRvoemtype().equals("15")) {
                hashMap.put("PCName", android.os.Build.MODEL);
                hashMap.put("DISPLAYNAME", agentName);
            } else {
                RLog.d("skcustom 15");
            }

            isSuccess = false;
            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.RV_AGENT_URL_INSTALL), hashMap);
            if (is == null) {
                throw new RSException(Network.IO_ERROR);
            }
            final StreamParser<AgentInstallResult> streamParser = streamParserFactory.create(AgentInstallParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.w(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.w(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<AccountChangeResult> agentAccountChange(String guid, String oldId, String oldPasswd, String newId, String newPasswd) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            WebCrypto crypto = webCryptoFactory.create();

            hashMap.put("GUID", guid);
            hashMap.put("OAI", crypto.encrypt(oldId));
            hashMap.put("OAP", crypto.encrypt(oldPasswd));
            hashMap.put("NAI", crypto.encrypt(newId));
            hashMap.put("NAP", crypto.encrypt(newPasswd));

            isSuccess = false;
            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.RV_AGENT_ACCOUNTCHANGE_PAGE), hashMap);
            if (is == null) {
                return Result.Companion.failure(new RSException(Network.IO_ERROR));
            }
            final StreamParser<AccountChangeResult> streamParser = streamParserFactory.create(AccountChangeParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.w(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.w(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public synchronized Result<AgentLoginResult> agentLogin(String guid) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("GUID", guid);
            hashMap.put("RSVFlag", "0");
            hashMap.put("MappVer", GlobalStatic.APPVERSION_NAME);
            hashMap.put("CTYPE", GlobalStatic.connectionInfo.ctype);
            AgentBasicInfo.RV_AGENT_NEWVERSION = "0";
            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.getLoginURL(context)), hashMap);
            if (is == null) {
                throw new RSException(Network.IO_ERROR);
            }
            final StreamParser<AgentLoginResult> streamParser = streamParserFactory.create(AgentLoginParser.class);
            return streamParser.parse(is);
        } catch (RSException rs) {
            RLog.e(rs);
            return Result.Companion.failure(rs);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<AgentLogoutResult> agentLogout(String guid) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("GUID", guid);

        try {
            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.getLogoutURL(context)), hashMap);
            if (is == null) {
                throw new RSException(Network.IO_ERROR);
            }
            final StreamParser<AgentLogoutResult> streamParser = streamParserFactory.create(AgentLogoutParser.class);
            return streamParser.parse(is);
        } catch (RSException rs) {
            RLog.e(rs);
            return Result.Companion.failure(rs);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<ChangeDeviceNameResult> deviceNameChange(String guid, String newDeviceName, String ssl, String localIp, String macAddress) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            WebCrypto crypto = webCryptoFactory.create();
            hashMap.put("GUID", guid);
            hashMap.put("DisplayName", newDeviceName);
            hashMap.put("AgentIP", localIp);
            hashMap.put("SSL", ssl);
            hashMap.put("MacAddr", macAddress);
            hashMap.put("AIP", crypto.encrypt(localIp));
            hashMap.put("MAddr", crypto.encrypt(macAddress));
            hashMap.put("OSName", "Android" + android.os.Build.VERSION.RELEASE);
            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.RV_AGENT_CONFIGMODIFY_PAGE), hashMap);
            if (is == null) {
                return Result.Companion.failure(new RSException(Network.IO_ERROR));
            }
            final StreamParser<ChangeDeviceNameResult> streamParser = streamParserFactory.create(ChangeDeviceNameParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<SendLiveViewResult> sendLiveView(String server, String page, int port, String guid, String width, String height, String imagePath, String filePath) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("GUID", guid);
            hashMap.put("ImageWidth", width);
            hashMap.put("ImageHeight", height);
            hashMap.put("ImagePath", imagePath);
            InputStream is = getConnectStream(getPageUrl(server, port) + page, hashMap, filePath);
            if (is == null) {
                return Result.Companion.failure(new RSException((Network.IO_ERROR)));
            }

            final StreamParser<SendLiveViewResult> streamParser = streamParserFactory.create(SendLiveViewParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<KnoxKeyResult> requestKnoxEnterpriseKey(String appVersion) {
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("mAppver", appVersion);

        try {
            InputStream is = getConnectStream(getFullUrl("/services/api/agent/knox_info"), requestMap, false);
            if (is == null) {
                throw new RSException(Network.IO_ERROR);
            }
            final StreamParser<KnoxKeyResult> streamParser = streamParserFactory.create(KnoxKeyParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<AgentSessionResult> agentSessionResult(String guid, String result, String sessionIP, String sessionPort) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            WebCrypto crypto = webCryptoFactory.create();

            hashMap.put("GUID", guid);
            hashMap.put("connresult", result);
            hashMap.put("AIP", crypto.encrypt(GlobalStatic.getLocalIP()));
            hashMap.put("MAddr", crypto.encrypt(GlobalStatic.getMacAddress(context)));
            hashMap.put("OSName", "Android" + android.os.Build.VERSION.RELEASE);
            hashMap.put("SessionIP", sessionIP);
            hashMap.put("SessionPort", sessionPort);

            hashMap.put("Version", GlobalStatic.APPVERSION_NAME);
            hashMap.put("RSVFlag", "80");
            //			hashMap.put("SetupKey", );
            //			hashMap.put("InstallId", );
            hashMap.put("Subnet", GlobalStatic.getSubnetmask(context));
            if (!AgentBasicInfo.RV_AGENT_RVOEMTYPE.equals("15")) { //skons custom
                RLog.d("skcustom 15");
                hashMap.put("PCName", android.os.Build.MODEL);
            }

            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.RV_AGENT_SESSIONRESULT_PAGE), hashMap);
            if (is == null) {
                throw new RSException(Network.IO_ERROR);
            }
            final StreamParser<AgentSessionResult> streamParser = streamParserFactory.create(AgentSessionResultParser.class);
            return streamParser.parse(is);
        } catch (RSException rse) {
            RLog.e(rse);
            return Result.Companion.failure(rse);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<ConnectAgreeResult> agentConnectAgreeResult(String guid, String logKey, String result, String url) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();

            hashMap.put("GUID", guid);
            hashMap.put("logkey", logKey);
            hashMap.put("agree", result);

            isSuccess = false;
            InputStream is = getPostConnectStream(getServerPageUrl(url), hashMap);
            if (is == null) {
                return Result.Companion.failure(new RSException(Network.IO_ERROR));
            }

            StreamParser<ConnectAgreeResult> streamParser = streamParserFactory.create(ConnectAgreeParser.class);
            return streamParser.parse(is);
        } catch (RSException rse) {
            RLog.e(rse);
            return Result.Companion.failure(rse);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<NotifyConnectedResult> notifyConnected(String guid, String logKey, String engineType) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();

            hashMap.put("GUID", guid);
            hashMap.put("logkey", logKey);
            hashMap.put("engineType", engineType);

            isSuccess = false;
            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.RV_AGENT_RMTCALLCONNECT_PAGE), hashMap);
            if (is == null) {
                throw new RSException(Network.IO_ERROR);
            }
            final StreamParser<NotifyConnectedResult> streamParser = streamParserFactory.create(NotifyConnectedParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<NotifyDisconnectedResult> notifyDisconnected(String guid, String logKey) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();

            hashMap.put("GUID", guid);
            hashMap.put("logkey", logKey);

            isSuccess = false;
            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.RV_AGENT_RMTCALLDISCONNECT_PAGE), hashMap);
            if (is == null) {
                throw new RSException(Network.IO_ERROR);
            }

            final StreamParser<NotifyDisconnectedResult> streamParser = streamParserFactory.create(NotifyDisconnectedParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<FcmRegisterResult> sendFcmResistID(String guid, String resistID) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();

            hashMap.put("guid", guid);
            hashMap.put("fcmRegisterId", resistID);
            InputStream is = getPostConnectStream(getServerPageUrl(AgentBasicInfo.getFCMRegistChangeURL(context)), hashMap);
            if (is == null) {
                throw new RSException(Network.IO_ERROR);
            }
            final StreamParser<FcmRegisterResult> streamParser = streamParserFactory.create(FcmRegisterParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.e(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.e(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    public Result<AgentDeleteResult> requestAgentDelete(String guid, String webId, String webPass, String BizID) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            WebCrypto crypto = webCryptoFactory.create();

            hashMap.put("WI", crypto.encrypt(webId));
            hashMap.put("WP", crypto.encrypt(webPass));
            hashMap.put("BizID", BizID);
            hashMap.put("GUID", guid);

            InputStream is = getPostConnectStream(getServerPageUrl("/services/api/agent/delete"), hashMap);
            if (is == null) {
                return Result.Companion.failure(new RSException(Network.IO_ERROR));
            }
            final StreamParser<AgentDeleteResult> streamParser = streamParserFactory.create(AgentDeleteParser.class);
            return streamParser.parse(is);
        } catch (RSException e) {
            RLog.w(e);
            return Result.Companion.failure(e);
        } catch (Exception e) {
            RLog.w(e);
            return Result.Companion.failure(new RSException(RSErrorCode.UNKNOWN));
        }
    }

    private String getConsoleURL() {
        return apiVersionPrefix() + BASE_CONSOLE_URL;
    }

    private String apiVersionPrefix() {
        String apiVersion = AgentBasicInfo.getApiVersion(context);
        if (TextUtils.isEmpty(apiVersion)) {
            return "";
        }
        return "/" + apiVersion;
    }

    public String getApiVersion() {
        return AgentBasicInfo.getApiVersion(context);
    }

    public void setApiVersion(String apiVersion) {
        AgentBasicInfo.setApiVersion(context, apiVersion);
    }

    private void setProtocolInfo(String url) {
        if (url.contains("https")) {
            protocol = "https";
            webServerPort = "443";
        } else {
            protocol = "http";
            webServerPort = "80";
        }
    }

    private String getFullUrl(String pass) {
        String ret = "";
        setProtocolInfo(webServerIP);
        if (webServerIP.contains("http")) {
            //			ret = webServerIP + ":" + webServerPort + pass;
            ret = webServerIP + pass;
        } else {
            ret = protocol + "://" + webServerIP + ":" + webServerPort + pass;
        }
        return ret;
    }


    public String getServerPageUrl(String pass) {
        return GlobalStatic.connectionInfo.getServerPageURL(pass);
    }

    private String getPageUrl(String pass, int port) {
        String serverAddr = "";

        if (port == 80) {
            if (!pass.contains("http://")) {
                serverAddr = "http://";
            }

        } else if (port == 443) {
            if (!pass.contains("https://")) {
                serverAddr = "https://";
            }
        } else {
            return "http://" + pass + ":" + port;
        }

        return serverAddr + pass;
    }

    private InputStream getConnectStream(String url, HashMap<String, String> hashMap, Boolean isPost) throws RSException {
        return getConnectStream(url, hashMap, isPost, false);
    }

    private InputStream getConnectStream(String url, HashMap<String, String> hashMap, Boolean isPost, boolean isUseAccessToken) throws RSException {
        try {
            WebReadStream webReadStream;
            synchronized (this) {
                webReadStream = webStreamFactory
                        .setRequestParams(url, hashMap, isPost)
                        .setUseAccessToken(isUseAccessToken)
                        .create();
            }
            return webReadStream.getStream();
        } catch (IOException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
            throw new RSException(Network.IO_ERROR);
        }
    }

    private InputStream getPostConnectStream(String serverAddr, HashMap<String, String> hashMap) throws RSException {
        return getConnectStream(serverAddr, hashMap, true);
    }

    private InputStream getConnectStream(String serverAddr, HashMap<String, String> hashMap, String filePath) throws RSException {
        try {
            WebReadStream webReadStream;
            synchronized (this) {
                webReadStream = webStreamFactory
                        .setMultiPart(serverAddr, hashMap, new String[]{filePath})
                        .create();
            }
            return webReadStream.getStream();
        } catch (IOException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException e) {
            throw new RSException(Network.IO_ERROR);
        }
    }
}
