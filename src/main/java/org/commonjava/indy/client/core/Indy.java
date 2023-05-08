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
package org.commonjava.indy.client.core;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.indy.client.modules.IndyContentClientModule;
import org.commonjava.indy.client.modules.IndyStoresClientModule;
import org.commonjava.o11yphant.trace.TracerConfiguration;
import org.commonjava.util.jhttpc.auth.PasswordManager;
import org.commonjava.util.jhttpc.model.SiteConfig;
import java.io.Closeable;
import java.util.*;

@SuppressWarnings( "unused" )
public class Indy
        implements Closeable
{

    public static final String HEADER_COMPONENT_ID = "component-id";

    private String apiVersion;

    private IndyClientHttp http;

    private final Set<IndyClientModule> moduleRegistry;

    @Deprecated
    public Indy(final String baseUrl, final IndyClientModule... modules )
            throws IndyClientException
    {
        this( null, null, Arrays.asList( modules ), IndyClientHttp.defaultSiteConfig( baseUrl ) );
    }

    @Deprecated
    public Indy(final String baseUrl, final IndyClientAuthenticator authenticator, final IndyClientModule... modules )
            throws IndyClientException
    {
        this( authenticator, null, Arrays.asList( modules ), IndyClientHttp.defaultSiteConfig( baseUrl ) );
    }

    @Deprecated
    public Indy( final String baseUrl, final ObjectMapper mapper, final IndyClientModule... modules )
            throws IndyClientException
    {
        this( null, mapper, Arrays.asList( modules ), IndyClientHttp.defaultSiteConfig( baseUrl ) );
    }

    @Deprecated
    public Indy(final String baseUrl, final IndyClientAuthenticator authenticator, final ObjectMapper mapper,
                final IndyClientModule... modules )
            throws IndyClientException
    {
        this( authenticator, mapper, Arrays.asList( modules ), IndyClientHttp.defaultSiteConfig( baseUrl ) );
    }

    @Deprecated
    public Indy(final String baseUrl, final Collection<IndyClientModule> modules )
            throws IndyClientException
    {
        this( null, null, modules, IndyClientHttp.defaultSiteConfig( baseUrl ) );
    }

    @Deprecated
    public Indy(final String baseUrl, final IndyClientAuthenticator authenticator,
                final Collection<IndyClientModule> modules )
            throws IndyClientException
    {
        this( authenticator, null, modules, IndyClientHttp.defaultSiteConfig( baseUrl ) );
    }

    @Deprecated
    public Indy(final String baseUrl, final ObjectMapper mapper, final Collection<IndyClientModule> modules )
            throws IndyClientException
    {
        this( null, mapper, modules, IndyClientHttp.defaultSiteConfig( baseUrl ) );
    }

    @Deprecated
    public Indy(final String baseUrl, final IndyClientAuthenticator authenticator, final ObjectMapper mapper,
                final Collection<IndyClientModule> modules )
            throws IndyClientException
    {
        this( authenticator, mapper, modules, IndyClientHttp.defaultSiteConfig( baseUrl ) );
    }

    @Deprecated
    public Indy(final IndyClientAuthenticator authenticator, final ObjectMapper mapper,
                final Collection<IndyClientModule> modules, SiteConfig location )
            throws IndyClientException
    {
        this( location, authenticator, mapper,
              modules == null ? new IndyClientModule[0] : modules.toArray( new IndyClientModule[0] ) );
    }

    @Deprecated
    public Indy(final SiteConfig location, final IndyClientAuthenticator authenticator, final ObjectMapper mapper,
                final IndyClientModule... modules )
            throws IndyClientException
    {
        this( location, authenticator, mapper, Collections.emptyMap(), modules );
    }

    /**
     *
     * @param location -
     * @param authenticator -
     * @param mapper -
     * @param mdcCopyMappings a map of fields to copy from LoggingMDC to http request headers where key=MDCMey and value=headerName
     * @param modules -
     * @throws IndyClientException -
     * @deprecated - since 3.1.0, we have new {@link Builder} to set up the indy client, so please use it instead in future
     */
    @Deprecated
    public Indy(final SiteConfig location, final IndyClientAuthenticator authenticator, final ObjectMapper mapper,
                final Map<String, String> mdcCopyMappings, final IndyClientModule... modules )
            throws IndyClientException
    {
        this.http = new IndyClientHttp( authenticator, mapper == null ? new ObjectMapper() : mapper, location,
                                        getApiVersion(), mdcCopyMappings );

        this.moduleRegistry = new HashSet<>();

        setupStandardModules();
        for ( final IndyClientModule module : modules )
        {
            module.setup( this, http );
            moduleRegistry.add( module );
        }

    }

    /**
     * @deprecated - since 3.1.0, we have new {@link Builder} to set up the indy client, so please use it instead in future
     */
    @Deprecated
    public Indy(SiteConfig location, PasswordManager passwordManager, IndyClientModule... modules )
            throws IndyClientException
    {
        this( location, passwordManager, null, modules );
    }

    /**
     * @deprecated - since 3.1.0, we have new {@link Builder} to set up the indy client, so please use it instead in future
     */
    @Deprecated
    public Indy(SiteConfig location, PasswordManager passwordManager, ObjectMapper objectMapper,
                IndyClientModule... modules )
            throws IndyClientException
    {
        this.http =
                new IndyClientHttp( passwordManager, objectMapper == null ? new ObjectMapper() : objectMapper,
                                    location, getApiVersion() );

        this.moduleRegistry = new HashSet<>();

        setupStandardModules();
        for ( final IndyClientModule module : modules )
        {
            module.setup( this, http );
            moduleRegistry.add( module );
        }
    }

    private Indy(final Set<IndyClientModule> modules )
    {
        this.moduleRegistry = new HashSet<>();
        moduleRegistry.addAll( modules );
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private Set<IndyClientModule> moduleRegistry;

        private PasswordManager passwordManager;

        private SiteConfig location;

        private ObjectMapper objectMapper;

        private IndyClientAuthenticator authenticator;

        private Map<String, String> mdcCopyMappings;

        private TracerConfiguration existedTraceConfig;

        private Builder()
        {
        }

        public Builder setModules( Set<IndyClientModule> modules )
        {
            this.moduleRegistry = modules;
            return this;
        }

        public Builder setModules( IndyClientModule... modules )
        {
            this.moduleRegistry = new HashSet<>();
            Collections.addAll( moduleRegistry, modules );
            return this;
        }

        public Builder setPasswordManager( PasswordManager passwordManager )
        {
            this.passwordManager = passwordManager;
            return this;
        }

        public Builder setLocation( SiteConfig location )
        {
            this.location = location;
            return this;
        }

        public Builder setObjectMapper( ObjectMapper objectMapper )
        {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder setExistedTraceConfig( TracerConfiguration existedTraceConfig )
        {
            this.existedTraceConfig = existedTraceConfig;
            return this;
        }

        public Builder setAuthenticator( IndyClientAuthenticator authenticator )
        {
            this.authenticator = authenticator;
            return this;
        }

        public Builder setMdcCopyMappings( Map<String, String> mdcCopyMappings )
        {
            this.mdcCopyMappings = mdcCopyMappings;
            return this;
        }

        public Indy build()
                throws IndyClientException
        {
            Set<IndyClientModule> modules = this.moduleRegistry == null ? new HashSet<>() : this.moduleRegistry;
            final Indy indy = new Indy( modules );
            if ( this.objectMapper == null )
            {
                this.objectMapper = new ObjectMapper();
            }

            indy.http = IndyClientHttp.builder()
                                      .setAuthenticator( this.authenticator )
                                      .setApiVersion( indy.getApiVersion() )
                                      .setLocation( this.location )
                                      .setPasswordManager( this.passwordManager )
                                      .setExistedTraceConfig( this.existedTraceConfig )
                                      .setMdcCopyMappings( this.mdcCopyMappings )
                                      .setObjectMapper( this.objectMapper )
                                      .build();
            indy.setupStandardModules();
            for ( final IndyClientModule module : this.moduleRegistry )
            {
                module.setup( indy, indy.http );
            }

            return indy;
        }
    }

    public void setupExternal( final IndyClientModule module )
    {
        setup( module );
    }

    /**
     * Not used since migration to jHTTPc library
     */
    @Deprecated
    public Indy connect()
    {
        return this;
    }

    @Override
    public void close()
    {
        http.close();
    }

    public IndyStoresClientModule stores()
            throws IndyClientException
    {
        return module( IndyStoresClientModule.class );
    }

    public IndyContentClientModule content()
            throws IndyClientException
    {
        return module( IndyContentClientModule.class );
    }

    public <T extends IndyClientModule> T module( final Class<T> type )
            throws IndyClientException
    {
        for ( final IndyClientModule module : moduleRegistry )
        {
            if ( type.isInstance( module ) )
            {
                return type.cast( module );
            }
        }

        throw new IndyClientException( "Module not found: %s.", type.getName() );
    }

    public boolean hasModule( Class<?> type )
    {
        for ( final IndyClientModule module : moduleRegistry )
        {
            if ( type.isInstance( module ) )
            {
                return true;
            }
        }

        return false;
    }

    public String getBaseUrl()
    {
        return http.getBaseUrl();
    }

    private void setupStandardModules()
    {
        final Set<IndyClientModule> standardModules = new HashSet<>();
        standardModules.add( new IndyStoresClientModule() );
        standardModules.add( new IndyContentClientModule() );

        for ( final IndyClientModule module : standardModules )
        {
            setup( module );
        }
    }

    private void setup( IndyClientModule module )
    {
        module.setup( this, http );
        moduleRegistry.add( module );

        Iterable<Module> serMods = module.getSerializerModules();
        if ( serMods != null )
        {
            http.getObjectMapper().registerModules( serMods );
        }
    }

    // DA, Builder, Orchestrator, etc. If available, this will be sent as a request header.
    public void setComponentId( String componentId )
    {
        http.addDefaultHeader( HEADER_COMPONENT_ID, componentId );
    }

    // Default headers will be sent along with each request
    public void addDefaultHeader( String key, String value )
    {
        http.addDefaultHeader( key, value );
    }

    public String getApiVersion()
    {
        return apiVersion;
    }
}
