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
package org.commonjava.indy.client.core.o11y.metric;

import org.commonjava.indy.client.core.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * The scope annotations (Thread, Header, MDC) tell where the constant is available/used. The static methods are used
 * to manage contextual state in both MDC and ThreadContext.
 */
public class RequestContextHelper
{
    private static final Logger logger = LoggerFactory.getLogger( RequestContextHelper.class );

    private static final String RAW_IO_WRITE_NANOS = "raw-io-write-nanos";

    private static final String END_NANOS = "latency-end-nanos";

    public static void setContext( final String key, final Object value )
    {
        MDC.put( key, String.valueOf( value ) );
        ThreadContext ctx = ThreadContext.getContext( true );
        logger.trace( "Setting value: '{}' = '{}' in ThreadContext: {}", key, value, ctx );
        ctx.computeIfAbsent( key, k -> value );
    }

    public static <T> T getContext( final String key, final T defaultValue )
    {
        ThreadContext ctx = ThreadContext.getContext( false );
        if ( ctx != null )
        {
            logger.trace( "Retrieving value for: '{}' from ThreadContext: {}", key, ctx );
            Object v = ctx.get( key );
            //noinspection unchecked
            return v == null ? defaultValue : (T) v;
        }

        return defaultValue;
    }

    public static long getRequestEndNanos()
    {
        return getContext( END_NANOS, System.nanoTime() );
    }

    public static long getRawIoWriteNanos()
    {
        return getContext( RAW_IO_WRITE_NANOS, 0L );
    }

}
