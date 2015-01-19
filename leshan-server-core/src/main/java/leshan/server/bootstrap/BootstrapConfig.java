/*
 * Copyright (c) 2013, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.server.bootstrap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import leshan.core.request.BindingMode;

/**
 * A client configuration to be pushed by a bootstrap operation
 */
public class BootstrapConfig {

    public Map<Integer, ServerConfig> servers = new HashMap<>();

    public Map<Integer, ServerSecurity> security = new HashMap<>();

    /** server configuration (object 1) */
    static public class ServerConfig {
        public int shortId;
        public int lifetime = 86400;
        public int defaultMinPeriod = 1;
        public Integer defaultMaxPeriod = null;
        public Integer disableTimeout = null;
        public boolean notifIfDisabled = true;
        public BindingMode binding = BindingMode.U;

        @Override
        public String toString() {
            return String
                    .format("ServerConfig [shortId=%s, lifetime=%s, defaultMinPeriod=%s, defaultMaxPeriod=%s, disableTimeout=%s, notifIfDisabled=%s, binding=%s]",
                            shortId, lifetime, defaultMinPeriod, defaultMaxPeriod, disableTimeout, notifIfDisabled,
                            binding);
        }
    }

    /** security configuration (object 0) */
    static public class ServerSecurity {
        public String uri;
        public boolean bootstrapServer = false;
        public SecurityMode securityMode;
        public byte[] publicKeyOrId = new byte[] {};
        public byte[] serverPublicKeyOrId = new byte[] {};
        public byte[] secretKey = new byte[] {};
        public SmsSecurityMode smsSecurityMode = SmsSecurityMode.NO_SEC;
        public byte[] smsBindingKeyParam = new byte[] {};
        public byte[] smsBindingKeySecret = new byte[] {};
        public String serverSmsNumber = ""; // spec says integer WTF?
        public Integer serverId;
        public int clientOldOffTime = 1;

        @Override
        public String toString() {
            return String
                    .format("ServerSecurity [uri=%s, bootstrapServer=%s, securityMode=%s, publicKeyOrId=%s, serverPublicKeyOrId=%s, secretKey=%s, smsSecurityMode=%s, smsBindingKeyParam=%s, smsBindingKeySecret=%s, serverSmsNumber=%s, serverId=%s, clientOldOffTime=%s]",
                            uri, bootstrapServer, securityMode, Arrays.toString(publicKeyOrId),
                            Arrays.toString(serverPublicKeyOrId), Arrays.toString(secretKey), smsSecurityMode,
                            Arrays.toString(smsBindingKeyParam), Arrays.toString(smsBindingKeySecret), serverSmsNumber,
                            serverId, clientOldOffTime);
        }
    }

    @Override
    public String toString() {
        return String.format("BootstrapConfig [servers=%s, security=%s]", servers, security);
    }

}
