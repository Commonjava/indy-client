/**
 * Copyright (C) 2024 Red Hat, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Queue;
import java.util.function.BiConsumer;

public final class TraceManager
{
    private static final ThreadLocal<Queue<SpanWrapper>> ACTIVE_SPAN = new ThreadLocal<>();

    //    private static final String ACTIVE_SPAN_KEY = "active-trace-span";

    private final OtelProvider otelProvider;

    private final ClientTracerConfiguration config;

    private final Logger logger = LoggerFactory.getLogger( getClass().getName() );

    public TraceManager( ClientTracerConfiguration config )
    {
        this.otelProvider = new OtelProvider( config );
        this.config = config;
    }

    public Optional<SpanWrapper> startClientRequestSpan( String spanName, BiConsumer<String, String> spanInjector )
    {
        if ( !config.isEnabled() )
        {
            return Optional.empty();
        }

        SpanWrapper span = otelProvider.getSpanProvider().startClientSpan( spanName );
        if ( span != null )
        {
            otelProvider.injectContext( spanInjector, span );
            logger.trace( "Started span: {}", span.getSpanId() );
        }

        return Optional.ofNullable( span );
    }

    public static void addFieldToCurrentSpan( String name, Object value )
    {
        Logger logger = LoggerFactory.getLogger( TraceManager.class );
        Optional<SpanWrapper> current = SpanWrapper.current();
        current.ifPresent( span -> {
            if ( logger.isTraceEnabled() )
            {
                StackTraceElement[] st = Thread.currentThread().getStackTrace();
                logger.trace( "Adding field: {} with value: {} to span: {} from:\n  {}\n  {}", name, value,
                              span.getSpanId(), st[3], st[4] );
            }

            span.addField( name, value );
        } );

        if ( current.isEmpty() && logger.isTraceEnabled() )
        {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            logger.info( "NO ACTIVE SPAN for: {} from:\n  {}\n  {}", name, st[2], st[3] );
        }
    }

    public ClientTracerConfiguration getConfig()
    {
        return this.config;
    }
}
