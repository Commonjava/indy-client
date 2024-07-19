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

import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.commonjava.util.jhttpc.auth.MemoryPasswordManager;
import org.commonjava.util.jhttpc.model.SiteConfig;

import java.util.Collection;
import java.util.Collections;

public class IndyClientWithoutTraceTest
        extends AbstractIndyClientTest
{
    @Override
    protected Indy getClient( SiteConfig siteConfig, Collection<IndyClientModule> modules )
    {
        try
        {

            final Indy.Builder builder = Indy.builder()
                                             .setLocation( siteConfig )
                                             .setObjectMapper( new IndyObjectMapper( Collections.emptySet() ) )
                                             .setMdcCopyMappings( Collections.emptyMap() )
                                             .setModules( modules.toArray( new IndyClientModule[0] ) );
            builder.setPasswordManager( new MemoryPasswordManager() );
            return builder.build();
        }
        catch ( IndyClientException e )
        {
            throw new RuntimeException( e );
        }
    }

}
