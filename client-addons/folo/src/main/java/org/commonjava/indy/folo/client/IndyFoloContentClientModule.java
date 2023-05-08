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
package org.commonjava.indy.folo.client;

import org.apache.commons.io.IOUtils;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.IndyClientModule;
import org.commonjava.indy.client.core.helper.HttpResources;
import org.commonjava.indy.client.core.helper.PathInfo;
import org.commonjava.indy.client.core.model.StoreKey;
import org.commonjava.indy.client.core.util.UrlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class IndyFoloContentClientModule
    extends IndyClientModule
{

    private static final String TRACKING_PATH = "/folo/track";

    public String trackingUrl( final String id, final StoreKey key )
    {
        return UrlUtils.buildUrl( http.getBaseUrl(), TRACKING_PATH, id, key.getPackageType(),
                                  key.getType().singularEndpointName(), key.getName() );
    }

    public boolean exists( final String trackingId, final StoreKey key, final String path )
        throws IndyClientException
    {
        return http.exists( UrlUtils.buildUrl( TRACKING_PATH, trackingId, key.getPackageType(),
                                               key.getType().singularEndpointName(), key.getName(), path ) );
    }

    public PathInfo store( final String trackingId, final StoreKey key, final String path, final InputStream stream )
            throws IndyClientException
    {
        http.putWithStream( UrlUtils.buildUrl( TRACKING_PATH, trackingId, key.getPackageType(),
                                               key.getType().singularEndpointName(), key.getName(), path ), stream );

        return getInfo( trackingId, key, path );
    }

    public PathInfo getInfo( final String trackingId, final StoreKey key, final String path )
        throws IndyClientException
    {
        final Map<String, String> headers = http.head(
                UrlUtils.buildUrl( TRACKING_PATH, trackingId, key.getPackageType(),
                                   key.getType().singularEndpointName(), key.getName(), path ) );

        return new PathInfo( headers );
    }

    public InputStream get( final String trackingId, final StoreKey key, final String path )
        throws IndyClientException
    {
        final HttpResources resources = http.getRaw( UrlUtils.buildUrl( TRACKING_PATH, trackingId, key.getPackageType(),
                                                                        key.getType().singularEndpointName(),
                                                                        key.getName(), path ) );

        if ( resources.getStatusCode() != 200 )
        {
            if ( resources.getStatusCode() == 404 )
            {
                return null;
            }

            IOUtils.closeQuietly( resources );
            throw new IndyClientException( "Response returned status: %s.", resources.getStatusLine() );
        }

        try
        {
            return resources.getResponseStream();
        }
        catch ( final IOException e )
        {
            throw new IndyClientException( "Failed to open response content stream: %s", e, e.getMessage() );
        }
    }

    public String contentUrl( final String trackingId, final StoreKey key, final String... path )
    {
        return UrlUtils.buildUrl( http.getBaseUrl(), aggregatePathParts( trackingId, key, path ) );
    }

    public String contentPath( final String trackingId, final StoreKey key, final String... path )
    {
        return UrlUtils.buildUrl( null, aggregatePathParts( trackingId, key, path ) );
    }

    private String[] aggregatePathParts( final String trackingId, final StoreKey key,
                                         final String... path )
    {
        final String[] parts = new String[path.length + 4];
        int i=0;
        parts[i++] = trackingId;
        parts[i++] = key.getPackageType();
        parts[i++] = key.getType().singularEndpointName();
        parts[i++] = key.getName();
        System.arraycopy( path, 0, parts, 4, path.length );

        return parts;
    }

}
