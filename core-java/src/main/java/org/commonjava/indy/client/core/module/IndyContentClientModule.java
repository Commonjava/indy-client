/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.indy.client.core.module;

import org.apache.commons.io.IOUtils;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.IndyClientModule;
import org.commonjava.indy.client.core.helper.HttpResources;
import org.commonjava.indy.client.core.helper.PathInfo;
import org.commonjava.indy.client.core.model.StoreKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static org.commonjava.indy.client.core.util.UrlUtils.buildUrl;

public class IndyContentClientModule
    extends IndyClientModule
{

    public static final String CHECK_CACHE_ONLY = "cache-only";

    private static final String CONTENT_BASE = "content";
    
    public String contentUrl( final StoreKey key, final String... path )
    {
        return buildUrl( http.getBaseUrl(), aggregatePathParts( key, path ) );
    }
    
    public String contentPath( final StoreKey key, final String... path )
    {
        return buildUrl( null, aggregatePathParts( key, path ) );
    }

    public void deleteCache( final StoreKey key, final String path ) // delete cached file for group/remote
                    throws IndyClientException
    {
        http.deleteCache( contentPath( key, path ) );
    }

    public void delete( final StoreKey key, final String path )
            throws IndyClientException
    {
        http.delete( contentPath( key, path ) );
    }
    
    public boolean exists( final StoreKey key, final String path )
            throws IndyClientException
    {
        return http.exists( contentPath( key, path ) );
    }

    public Boolean exists( StoreKey key, String path, boolean cacheOnly )
            throws IndyClientException
    {
        return http.exists( contentPath( key, path ),
                            () -> Collections.<String, String>singletonMap( CHECK_CACHE_ONLY,
                                                                            Boolean.toString( cacheOnly ) ) );
    }

    public void store( final StoreKey key, final String path, final InputStream stream )
            throws IndyClientException
    {
        http.putWithStream( contentPath( key, path ), stream );
    }

    public PathInfo getInfo( final StoreKey key, final String path )
        throws IndyClientException
    {
        final Map<String, String> headers = http.head( contentPath( key, path ) );
        return new PathInfo( headers );
    }
    
    public InputStream get( final StoreKey key, final String path )
            throws IndyClientException
    {
        final HttpResources resources = http.getRaw( contentPath( key, path ) );

        if ( resources.getStatusCode() != 200 )
        {
            IOUtils.closeQuietly( resources );
            if ( resources.getStatusCode() == 404 )
            {
                return null;
            }

            throw new IndyClientException( resources.getStatusCode(), "Response returned status: %s.",
                                           resources.getStatusLine() );
        }

        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.debug( "Returning stream that should contain: {} bytes", resources.getResponse().getFirstHeader( "Content-Length" ) );
        try
        {
            return resources.getResponseStream();
        }
        catch ( final IOException e )
        {
            IOUtils.closeQuietly( resources );
            throw new IndyClientException( "Failed to open response content stream: %s", e,
                                           e.getMessage() );
        }
    }

    private String[] aggregatePathParts( final StoreKey key, final String... path )
    {
        final String[] parts = new String[path.length + 4];
        int i=0;
        parts[i++] = CONTENT_BASE;
        parts[i++] = key.getPackageType();
        parts[i++] = key.getType().singularEndpointName();
        parts[i++] = key.getName();
        System.arraycopy( path, 0, parts, 4, path.length );

        return parts;
    }

}
