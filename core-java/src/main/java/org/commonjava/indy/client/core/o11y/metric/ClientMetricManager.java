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
package org.commonjava.indy.client.core.o11y.metric;

import org.apache.http.client.methods.HttpUriRequest;
import org.commonjava.indy.client.core.o11y.trace.ClientTracerConfiguration;
import org.commonjava.indy.client.core.o11y.trace.TraceManager;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class ClientMetricManager
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private ClientTracerConfiguration configuration;

    private TraceManager traceManager;

    private final ClientTrafficClassifier classifier = new ClientTrafficClassifier();

    public ClientMetricManager()
    {
    }

    public ClientMetricManager( SiteConfig siteConfig )
    {
        this.configuration = new ClientTracerConfiguration();
        this.configuration.setEnabled( siteConfig.isMetricEnabled() );
        buildTraceManager();
    }

    public ClientMetricManager( ClientTracerConfiguration tracerConfig )
    {
        this.configuration = tracerConfig;
        buildTraceManager();
    }

    private void buildTraceManager()
    {
        if ( this.configuration.isEnabled() )
        {
            this.traceManager = new TraceManager( configuration );
        }
    }

    public ClientMetrics register( HttpUriRequest request )
    {
        logger.debug( "Client honey register: {}", request.getURI().getPath() );
        List<String> functions = classifier.calculateClassifiers( request );

        return new ClientMetrics( configuration.isEnabled(), request, functions );
    }

    private String getEndpointName( String method, String pathInfo )
    {
        StringBuilder sb = new StringBuilder( method + "_" );
        String[] toks = pathInfo.split( "/" );
        for ( String s : toks )
        {
            if ( isBlank( s ) || "api".equals( s ) )
            {
                continue;
            }
            sb.append( s );
            if ( "admin".equals( s ) )
            {
                sb.append( "_" );
            }
            else
            {
                break;
            }
        }
        return sb.toString();
    }

    public Optional<TraceManager> getTraceManager()
    {
        return Optional.ofNullable( traceManager );
    }
}
