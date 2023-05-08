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
package org.commonjava.indy.client.modules;

import com.fasterxml.jackson.core.type.TypeReference;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.IndyClientModule;
import org.commonjava.indy.client.util.UrlUtils;
import org.commonjava.indy.client.model.ArtifactStore;
import org.commonjava.indy.client.model.Group;
import org.commonjava.indy.client.model.HostedRepository;
import org.commonjava.indy.client.model.RemoteRepository;
import org.commonjava.indy.client.model.StoreKey;
import org.commonjava.indy.client.model.StoreType;
import org.commonjava.indy.client.model.store.StoreListingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndyStoresClientModule
    extends IndyClientModule
{
    public static final String ALL_PACKAGE_TYPES = "_all";

    public static final String STORE_BASEPATH = "admin/stores";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public <T extends ArtifactStore> T create( final T value, final String changelog, final Class<T> type )
        throws IndyClientException
    {
        value.setMetadata( ArtifactStore.METADATA_CHANGELOG, changelog );
        return http.postWithResponse( UrlUtils.buildUrl( STORE_BASEPATH, value.getPackageType(), value.getType()
                                                                       .singularEndpointName() ),
                                      value, type );
    }

    public boolean exists( final StoreKey key )
            throws IndyClientException
    {
        return http.exists( UrlUtils.buildUrl( STORE_BASEPATH, key.getPackageType(), key.getType().singularEndpointName(), key.getName() ) );
    }

    public void delete( final StoreKey key, final String changelog )
            throws IndyClientException
    {
        http.deleteWithChangelog( UrlUtils.buildUrl( STORE_BASEPATH, key.getPackageType(), key.getType().singularEndpointName(), key.getName() ), changelog );
    }

    public void delete( final StoreKey key, final String changelog, final boolean deleteContent )
                    throws IndyClientException
    {
        http.deleteWithChangelog(
                        UrlUtils.buildUrl( STORE_BASEPATH, key.getPackageType(), key.getType().singularEndpointName(),
                                           key.getName(), deleteContent ? "?deleteContent=true" : "" ), changelog );
    }

    public boolean update( final ArtifactStore store, final String changelog )
        throws IndyClientException
    {
        store.setMetadata( ArtifactStore.METADATA_CHANGELOG, changelog );
        return http.put( UrlUtils.buildUrl( STORE_BASEPATH, store.getPackageType(), store.getType()
                                                                           .singularEndpointName(), store.getName() ),
                         store );
    }

    public <T extends ArtifactStore> T load( StoreKey key, final Class<T> cls )
        throws IndyClientException
    {
        return http.get( UrlUtils.buildUrl( STORE_BASEPATH, key.getPackageType(), key.getType().singularEndpointName(), key.getName() ), cls );
    }

    public StoreListingDTO<HostedRepository> listHostedRepositories()
        throws IndyClientException
    {
        return http.get( UrlUtils.buildUrl( STORE_BASEPATH, ALL_PACKAGE_TYPES, StoreType.hosted.singularEndpointName() ),
                         new TypeReference<StoreListingDTO<HostedRepository>>()
                         {
                         } );
    }

    public StoreListingDTO<RemoteRepository> listRemoteRepositories()
        throws IndyClientException
    {
        return http.get( UrlUtils.buildUrl( STORE_BASEPATH, ALL_PACKAGE_TYPES, StoreType.remote.singularEndpointName() ),
                         new TypeReference<StoreListingDTO<RemoteRepository>>()
                         {
                         } );
    }

    public StoreListingDTO<Group> listGroups()
        throws IndyClientException
    {
        return http.get( UrlUtils.buildUrl( STORE_BASEPATH, ALL_PACKAGE_TYPES, StoreType.group.singularEndpointName() ),
                         new TypeReference<StoreListingDTO<Group>>()
                         {
                         } );
    }


    public StoreListingDTO<HostedRepository> listHostedRepositories( String packageType )
            throws IndyClientException
    {
        return http.get( UrlUtils.buildUrl( STORE_BASEPATH, packageType, StoreType.hosted.singularEndpointName() ),
                         new TypeReference<StoreListingDTO<HostedRepository>>()
                         {
                         } );
    }

    public StoreListingDTO<RemoteRepository> listRemoteRepositories( String packageType )
            throws IndyClientException
    {
        return http.get( UrlUtils.buildUrl( STORE_BASEPATH, packageType, StoreType.remote.singularEndpointName() ),
                         new TypeReference<StoreListingDTO<RemoteRepository>>()
                         {
                         } );
    }

    public StoreListingDTO<Group> listGroups( String packageType )
            throws IndyClientException
    {
        return http.get( UrlUtils.buildUrl( STORE_BASEPATH, packageType, StoreType.group.singularEndpointName() ),
                         new TypeReference<StoreListingDTO<Group>>()
                         {
                         } );
    }

    public StoreListingDTO<RemoteRepository> getRemoteByUrl( final String url, final String packageType )
            throws IndyClientException
    {
        return http.get(
                UrlUtils.buildUrl( STORE_BASEPATH, packageType, StoreType.remote.toString(), "query" ) + "/byUrl?url=" + url,
                new TypeReference<StoreListingDTO<RemoteRepository>>()
                {
                }  );
    }
}
