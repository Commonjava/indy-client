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

import org.apache.http.Header;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.commonjava.util.jhttpc.HttpFactory;
import org.commonjava.util.jhttpc.HttpFactoryIfc;
import org.commonjava.util.jhttpc.JHttpCException;
import org.commonjava.util.jhttpc.auth.PasswordManager;
import org.commonjava.util.jhttpc.model.SiteConfig;

import java.io.IOException;
import java.util.List;

public class SpanningHttpFactory
        implements HttpFactoryIfc
{
    private final HttpFactory delegate;

    private final TraceManager traceManager;

    public SpanningHttpFactory( HttpFactory httpFactory, TraceManager traceManager )
    {
        super();
        delegate = httpFactory;
        this.traceManager = traceManager;
    }

    public PasswordManager getPasswordManager()
    {
        return delegate.getPasswordManager();
    }

    @Override
    public CloseableHttpClient createClient()
            throws JHttpCException
    {
        return new TracerHttpClient( delegate.createClient(), traceManager );
    }

    @Override
    public CloseableHttpClient createClient( SiteConfig location )
            throws JHttpCException
    {
        return new TracerHttpClient( delegate.createClient( location ), traceManager );
    }

    @Override
    public CloseableHttpClient createClient( SiteConfig location, List<Header> defaultHeaders )
            throws JHttpCException
    {
        return new TracerHttpClient( delegate.createClient( location, defaultHeaders ), traceManager );
    }

    @Override
    public HttpClientContext createContext()
            throws JHttpCException
    {
        return delegate.createContext();
    }

    @Override
    public HttpClientContext createContext( SiteConfig location )
            throws JHttpCException
    {
        return delegate.createContext( location );
    }

    @Override
    public void close()
            throws IOException
    {
        delegate.close();
    }

    @Override
    public boolean isShutdown()
    {
        return delegate.isShutdown();
    }

    @Override
    public boolean shutdownNow()
    {
        return delegate.shutdownNow();
    }

    @Override
    public boolean shutdownGracefully( long timeoutMillis )
            throws InterruptedException
    {
        return delegate.shutdownGracefully( timeoutMillis );
    }
}

