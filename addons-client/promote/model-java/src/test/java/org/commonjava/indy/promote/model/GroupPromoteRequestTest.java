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
package org.commonjava.indy.promote.model;

import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class GroupPromoteRequestTest
{

    @Test
    public void roundTripJSON_Basic()
        throws Exception
    {
        final IndyObjectMapper mapper = new IndyObjectMapper( true );

        final GroupPromoteRequest req =
            new GroupPromoteRequest( new StoreKey( StoreType.hosted, "source" ), "target" );

        final String json = mapper.writeValueAsString( req );

        System.out.println( json );

        final GroupPromoteRequest result = mapper.readValue( json, GroupPromoteRequest.class );

        assertThat( result.getSource(), equalTo( req.getSource() ) );
        assertThat( result.getTarget(), equalTo( req.getTarget() ) );
    }

}
