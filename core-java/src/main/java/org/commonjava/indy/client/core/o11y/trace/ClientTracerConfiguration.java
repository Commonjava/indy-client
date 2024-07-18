/**
 * Copyright (C) 2023 Red Hat, Inc. (https://github.com/Commonjava/indy-client)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.client.core.o11y.trace;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ClientTracerConfiguration
{
    private static final Integer DEFAULT_BASE_SAMPLE_RATE = 100;

    private static final String DEFAULT_INDY_CLIENT_SERVICE_NAME = "indy-client";

    private boolean enabled;

    private String serviceName;

    private boolean consoleTransport;

    private String grpcUri;

    private Map<String, String> grpcHeaders = new HashMap<>();

    private Map<String, String> grpcResources = new HashMap<>();

    public boolean isEnabled()
    {
        return enabled;
    }

    public boolean isConsoleTransport()
    {
        return consoleTransport;
    }

    public Map<String, String> getGrpcHeaders()
    {
        return grpcHeaders;
    }

    public Map<String, String> getResources()
    {
        return grpcResources;
    }

    public String getServiceName()
    {
        return StringUtils.isBlank( serviceName ) ? DEFAULT_INDY_CLIENT_SERVICE_NAME : serviceName;
    }

    public String getGrpcEndpointUri()
    {
        return grpcUri == null ? DEFAULT_GRPC_URI : grpcUri;
    }

    public void setConsoleTransport( boolean consoleTransport )
    {
        this.consoleTransport = consoleTransport;
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }

    public void setGrpcUri( String grpcUri )
    {
        this.grpcUri = grpcUri;
    }

    public void setServiceName( String serviceName )
    {
        this.serviceName = serviceName;
    }

    public void setGrpcHeaders( Map<String, String> grpcHeaders )
    {
        this.grpcHeaders = grpcHeaders;
    }

    public void setGrpcResources( Map<String, String> grpcResources )
    {
        this.grpcResources = grpcResources;
    }

    String DEFAULT_GRPC_URI = "http://localhost:55680";

    public String getInstrumentationName()
    {
        return "org.commonjava.indy.client";
    }

    public String getInstrumentationVersion()
    {
        return "3.4.0";
    }

}
