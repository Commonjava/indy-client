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
package org.commonjava.indy.client.core;

import org.apache.commons.io.IOUtils;
import org.commonjava.indy.client.core.module.IndyStoreQueryClientModule;
import org.commonjava.indy.client.core.module.IndyStoresClientModule;
import org.commonjava.indy.client.core.util.UrlUtils;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.model.core.RemoteRepository;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.commonjava.test.http.expect.ExpectationServer;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.commonjava.util.jhttpc.model.SiteConfigBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

import static org.commonjava.indy.client.core.util.UrlUtils.normalizePath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractIndyClientTest
{
    private static final String BASE_STORE_PATH = "/api/admin/stores";

    @Rule
    public ExpectationServer server = new ExpectationServer();

    private final IndyObjectMapper mapper = new IndyObjectMapper( false );

    private Indy client;

    @Before
    public void setUp()
    {
        final String serviceUrl = UrlUtils.buildUrl( server.getBaseUri(), "api" );

        SiteConfig config = new SiteConfigBuilder( "indy", serviceUrl ).withRequestTimeoutSeconds( 30 )
                                                                       .withMetricEnabled( false )
                                                                       .build();
        Collection<IndyClientModule> modules =
                Arrays.asList( new IndyStoresClientModule(), new IndyStoreQueryClientModule() );

        client = getClient( config, modules );
    }

    protected abstract Indy getClient(SiteConfig siteConfig, Collection<IndyClientModule> modules);

    @Test
    public void testGetStores()
            throws Exception
    {
        String path = normalizePath( BASE_STORE_PATH, "maven/remote/central" );
        server.expect( path, 200, readResource( "repo-service/remote-central.json" ) );
        StoreKey key = StoreKey.fromString( "maven:remote:central" );
        RemoteRepository remote = client.module( IndyStoresClientModule.class ).load( key, RemoteRepository.class );
        assertNotNull( remote );
        assertThat( remote.getKey(), equalTo( key ) );
        assertThat( remote.getType(), equalTo( StoreType.remote ) );

        path = normalizePath( BASE_STORE_PATH, "maven/hosted/local-deployments" );
        server.expect( path, 200, readResource( "repo-service/hosted-localdeploy.json" ) );
        key = StoreKey.fromString( "maven:hosted:local-deployments" );
        HostedRepository hosted = client.module( IndyStoresClientModule.class ).load( key, HostedRepository.class );
        assertNotNull( hosted );
        assertThat( hosted.getKey(), equalTo( key ) );
        assertThat( hosted.getType(), equalTo( StoreType.hosted ) );

        path = normalizePath( BASE_STORE_PATH, "maven/group/static" );
        server.expect( path, 200, readResource( "repo-service/group-static.json" ) );
        key = StoreKey.fromString( "maven:group:static" );
        Group group = client.module( IndyStoresClientModule.class ).load( key, Group.class );
        assertNotNull( group );
        assertThat( group.getKey(), equalTo( key ) );
        assertThat( group.getType(), equalTo( StoreType.group ) );
    }

    static String readResource( final String resourcePath )
            throws IOException
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource( resourcePath );
        if ( url != null )
        {
            return IOUtils.toString( url.openStream(), Charset.defaultCharset() );
        }
        throw new IOException( String.format( "File not exists: %s", resourcePath ) );
    }

}
