/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rsupport.mobile.agent.modules.push.fcm;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.rsupport.mobile.agent.api.ApiService;
import com.rsupport.mobile.agent.constant.AgentBasicInfo;
import com.rsupport.mobile.agent.status.AgentStatus;

import org.koin.java.KoinJavaComponent;


public class MyFirebaseInstanceIDService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    public void onTokenRefresh(Context context) {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        sendRegistrationToServer(context, refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(Context context, String token) {
        AgentStatus agentStatus = KoinJavaComponent.get(AgentStatus.class);

        if (agentStatus.get() == AgentStatus.AGENT_STATUS_LOGIN || agentStatus.get() == AgentStatus.AGENT_STATUS_REMOTING) {
            String guid = AgentBasicInfo.getAgentGuid(context);
            KoinJavaComponent.get(ApiService.class).registerFcmId(guid, token);
        }

    }
}
