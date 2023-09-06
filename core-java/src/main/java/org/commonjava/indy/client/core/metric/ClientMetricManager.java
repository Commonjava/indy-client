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
package org.commonjava.indy.client.core.metric;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.commonjava.indy.client.core.inject.ClientMetricSet;
import org.commonjava.o11yphant.otel.OtelConfiguration;
import org.commonjava.o11yphant.otel.OtelTracePlugin;
import org.commonjava.o11yphant.trace.SpanFieldsDecorator;
import org.commonjava.o11yphant.trace.TraceManager;
import org.commonjava.o11yphant.trace.TracerConfiguration;
import org.commonjava.o11yphant.trace.spi.O11yphantTracePlugin;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@SuppressWarnings( { "unused" } )
public class ClientMetricManager
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private ClientTracerConfiguration configuration;

    private TraceManager traceManager;

    private final ClientTrafficClassifier classifier = new ClientTrafficClassifier();

    @ClientMetricSet
    private final ClientGoldenSignalsMetricSet metricSet = new ClientGoldenSignalsMetricSet();

    public ClientMetricManager()
    {
    }

    public ClientMetricManager( SiteConfig siteConfig )
    {
        buildConfig( siteConfig );
        buildTraceManager();
    }

    public ClientMetricManager( TracerConfiguration existedTraceConfig )
    {
        buildTraceConfig( existedTraceConfig );
        buildTraceManager();
    }

    public ClientMetricManager( TraceManager traceManager )
    {
        buildTraceConfig( traceManager.getConfig() );
        this.traceManager = traceManager;
    }

    private void buildTraceConfig( TracerConfiguration existedTraceConfig )
    {
        this.configuration = new ClientTracerConfiguration();
        this.configuration.setEnabled( existedTraceConfig.isEnabled() );
        this.configuration.setConsoleTransport( existedTraceConfig.isConsoleTransport() );
        this.configuration.setBaseSampleRate( existedTraceConfig.getBaseSampleRate() );
        this.configuration.setCpNames( existedTraceConfig.getCPNames() );
        this.configuration.setFields( existedTraceConfig.getFieldSet() );
        this.configuration.setEnvironmentMappings( existedTraceConfig.getEnvironmentMappings() );
        if ( existedTraceConfig instanceof OtelConfiguration )
        {
            OtelConfiguration existedOtelConfig = (OtelConfiguration) existedTraceConfig;
            this.configuration.setGrpcUri( existedOtelConfig.getGrpcEndpointUri() );
            this.configuration.setGrpcHeaders( existedOtelConfig.getGrpcHeaders() );
            this.configuration.setGrpcResources( existedOtelConfig.getResources() );
        }
    }

    private void buildTraceManager()
    {
        if ( this.configuration.isEnabled() )
        {
            O11yphantTracePlugin plugin = new OtelTracePlugin( configuration, configuration );
            if ( StringUtils.isNotBlank( configuration.getGrpcEndpointUri() ) )
            {
                plugin = new OtelTracePlugin( configuration, configuration );
            }
            this.traceManager = new TraceManager( plugin, new SpanFieldsDecorator(
                    Collections.singletonList( new ClientGoldenSignalsSpanFieldsInjector( metricSet ) ) ),
                                                  configuration );
        }
    }

    public ClientMetrics register( HttpUriRequest request )
    {
        logger.debug( "Client honey register: {}", request.getURI().getPath() );
        List<String> functions = classifier.calculateClassifiers( request );

        return new ClientMetrics( configuration.isEnabled(), request, functions, metricSet );
    }

    private void buildConfig( SiteConfig siteConfig )
    {
        this.configuration = new ClientTracerConfiguration();
        this.configuration.setEnabled( siteConfig.isMetricEnabled() );
        this.configuration.setBaseSampleRate( siteConfig.getBaseSampleRate() );
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
        return traceManager == null ? Optional.empty() : Optional.of( traceManager );
    }
}
