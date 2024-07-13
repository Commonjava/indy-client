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
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpanWrapper
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    private final Span span;

    private final Map<String, Double> inProgress = new HashMap<>();

    private final Map<String, Object> attributes = new HashMap<>();

    public SpanWrapper( Span span )
    {
        this.span = span;
    }

    public String getTraceId()
    {
        return span.getSpanContext().getTraceId();
    }

    public String getSpanId()
    {
        return span.getSpanContext().getSpanId();
    }

    public Map<String, Object> getFields()
    {
        return attributes;
    }

    public void close()
    {
        attributes.forEach( ( k, v ) -> span.setAttribute( k, (String) v ) );
        inProgress.forEach( span::setAttribute );

        SpanContext context = span.getSpanContext();
        logger.trace( "Closing span {} in trace {}", getSpanId(), getTraceId() );
        span.end();
    }

    public void setInProgressField( String key, Double value )
    {
        inProgress.put( key, value );
    }

    public Double getInProgressField( String key, Double defValue )
    {
        return inProgress.getOrDefault( key, defValue );
    }

    public synchronized void updateInProgressField( String key, Double value )
    {
        Double mappedVal = inProgress.getOrDefault( key, 0.0 );
        mappedVal += value;
        inProgress.put( key, mappedVal );
    }

    public void clearInProgressField( String key )
    {
        inProgress.remove( key );
    }

    public Map<String, Double> getInProgressFields()
    {
        return inProgress;
    }

    public Optional<Context> getSpanContext()
    {
        return Optional.ofNullable( Context.current().with( span ) );
    }

    public Scope makeCurrent()
    {
        return span.makeCurrent();
    }

    public String toString()
    {
        return span.toString();
    }

    public <T> void addField( String name, T value )
    {
        Logger logger = LoggerFactory.getLogger( TraceManager.class );

        if ( logger.isTraceEnabled() )
        {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            logger.trace( "Adding field: {} with value: {} to span: {} from:\n  {}\n  {}", name, value, getSpanId(),
                          st[3], st[4] );
        }

        attributes.put( name, String.valueOf( value ) );
    }

    public static Optional<SpanWrapper> current()
    {
        return Span.current() == null ? Optional.empty() : Optional.of( new SpanWrapper( Span.current() ) );
    }
}
