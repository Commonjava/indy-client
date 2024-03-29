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

import org.apache.commons.io.IOUtils;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by jdcasey on 4/25/16.
 */
public class ValidationRuleDTOTest
{
    @Test
    public void jsonRoundTrip()
            throws IOException
    {
        try(InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "no-snapshots.groovy" ))
        {
            String spec = IOUtils.toString( stream );
            ValidationRuleDTO in = new ValidationRuleDTO( "test", spec );

            IndyObjectMapper mapper = new IndyObjectMapper( true );
            String json = mapper.writeValueAsString( in );

            ValidationRuleDTO out = mapper.readValue( json, ValidationRuleDTO.class );
            assertThat( out, notNullValue() );
            assertThat( out.getName(), equalTo( in.getName() ) );
            assertThat( out.getSpec(), equalTo( in.getSpec() ) );
            assertThat( out, equalTo( in ) );
        }
    }
}
