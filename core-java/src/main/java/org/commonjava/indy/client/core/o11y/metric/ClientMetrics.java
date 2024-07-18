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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.commonjava.indy.client.core.o11y.trace.TraceManager.addFieldToCurrentSpan;
import static org.slf4j.LoggerFactory.getLogger;

public class ClientMetrics
        extends ClientMetricManager
        implements Closeable
{
    private static final String REQUEST_ERROR = "request-error";

    private static final String RESPONSE_ERROR = "response-error";

    private static final double NANOS_PER_MILLISECOND = 1E6;

    private static final String REQUEST_LATENCY_NS = "request-latency-ns";

    private static final String REQUEST_LATENCY_MILLIS = "latency_ms";

    private static final String TRAFFIC_TYPE = "traffic_type";

    private final boolean enabled;

    private final HttpUriRequest request;

    private final Collection<String> functions;

    private final Logger logger = getLogger( getClass().getName() );

    private final long start;

    private long end;

    public ClientMetrics( boolean enabled, HttpUriRequest request, Collection<String> functions )
    {
        this.enabled = enabled;
        this.request = request;
        this.functions = functions;
        this.start = System.nanoTime();

        if ( enabled )
        {
            logger.debug( "Client trace starting: {}", request.getURI().getPath() );
            Set<String> classifierTokens = new LinkedHashSet<>();
            functions.forEach( function -> {
                String[] parts = function.split( "\\." );
                classifierTokens.addAll( Arrays.asList( parts ).subList( 0, parts.length - 1 ) );
            } );

            String classification = StringUtils.join( classifierTokens, "," );

            RequestContextHelper.setContext( TRAFFIC_TYPE, classification );
            addFieldToCurrentSpan( TRAFFIC_TYPE, classification );
        }
    }

    public void registerErr( Object error )
    {
        if ( !enabled )
        {
            return;
        }

        logger.debug( "Client trace registerErr: {}", request.getURI().getPath() );
        if ( error instanceof Throwable )
        {
            String errorMsg = error.getClass().getSimpleName() + ": " + ( (Throwable) error ).getMessage();
            addFieldToCurrentSpan( REQUEST_ERROR, errorMsg );
        }
        else
        {
            addFieldToCurrentSpan( REQUEST_ERROR, error );
        }
    }

    public void registerEnd( HttpResponse response )
    {
        if ( !enabled || response == null )
        {
            return;
        }

        logger.debug( "Client trace registerEnd: {}", request.getURI().getPath() );
        boolean error = response.getStatusLine() != null && response.getStatusLine().getStatusCode() > 499;

        functions.forEach( function -> {
            if ( Arrays.stream( ClientMetricConstants.CLIENT_FUNCTIONS )
                       .collect( Collectors.toSet() )
                       .contains( function ) )
            {
                addFieldToCurrentSpan( function + ".latency", String.valueOf( end - start ) );
                if ( error )
                {
                    addFieldToCurrentSpan( function + "." + RESPONSE_ERROR, error );
                }
            }
        } );
    }

    @Override
    public void close()
    {
        if ( !enabled )
        {
            return;
        }

        logger.trace( "Client trace closing: {}", request.getURI().getPath() );

        this.end = RequestContextHelper.getRequestEndNanos() - RequestContextHelper.getRawIoWriteNanos();
        RequestContextHelper.setContext( REQUEST_LATENCY_NS, String.valueOf( end - start ) );
        RequestContextHelper.setContext( REQUEST_LATENCY_MILLIS, ( end - start ) / NANOS_PER_MILLISECOND );

        String pathInfo = request.getURI().getPath();

        addFieldToCurrentSpan( "path_info", pathInfo );
    }
}
