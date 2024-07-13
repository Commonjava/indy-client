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

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpanProvider
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    private final Tracer tracer;

    @SuppressWarnings( "PMD" )
    public SpanProvider( Tracer tracer )
    {
        this.tracer = tracer;
    }

    public SpanWrapper startClientSpan( String spanName )
    {
        return startClientSpan( spanName, null );
    }

    public SpanWrapper startClientSpan( String spanName, Context parentContext )
    {
        SpanBuilder spanBuilder = tracer.spanBuilder( spanName );
        Context ctx = Context.current();
        if ( parentContext != null )
        {
            ctx = parentContext;
            spanBuilder.setParent( ctx );
            logger.trace( "The span {} is using a parent context {}", spanName, ctx );
        }
        logger.trace( "Start a new client span {}", spanName );
        if ( ctx != null )
        {
            logger.trace( "The span {} is using a parent context {}", spanName, ctx );
            spanBuilder.setParent( ctx );
        }
        else
        {
            spanBuilder.setNoParent();
        }

        Span span = spanBuilder.setSpanKind( SpanKind.CLIENT ).startSpan();
        try (Scope ignored = span.makeCurrent())
        {
            logger.trace( "span with id {} started in trace {}", span.getSpanContext().getSpanId(),
                          span.getSpanContext().getTraceId() );
        }
        return new SpanWrapper( span );
    }
}
