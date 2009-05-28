/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.web;

import static org.jsecurity.util.StringUtils.split;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletContext;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.jsecurity.JSecurityException;
import org.jsecurity.config.ConfigurationException;
import org.jsecurity.mgt.RealmSecurityManager;
import org.jsecurity.mgt.SecurityManager;
import org.jsecurity.realm.Realm;
import org.jsecurity.util.LifecycleUtils;
import org.jsecurity.web.config.IniWebConfiguration;
import org.jsecurity.web.filter.PathConfigProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.SecuritySystem;

@Component( role = PlexusWebConfiguration.class )
public class PlexusConfiguration
    extends IniWebConfiguration
    implements PlexusMutableWebConfiguration, Contextualizable
{
    private static final long serialVersionUID = -608021587325532351L;

    public static final String SECURITY_MANAGER_ROLE = "securityManagerRole";

    public static final String DEFAULT_SECURITY_SYSTEM_ROLE = RealmSecurityManager.class.getName();

    public static final String SECURITY_MANAGER_ROLE_HINT = "securityManagerRoleHint";

    public static final String DEFAULT_SECURITY_MANAGER_ROLE_HINT = "web";

    private static final Logger logger = LoggerFactory.getLogger( PlexusConfiguration.class );

    protected PlexusContainer plexusContainer;

    protected String securityManagerRole;

    protected String securityManagerRoleHint;

    protected Map<String, Filter> filters;

    protected Logger getLogger()
    {
        return logger;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        plexusContainer = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );

        // TODO: do somethign different
        // start up the security system before we load the security manager, so the configuraiton is set
        try
        {
            plexusContainer.lookup( SecuritySystem.class );
        }
        catch ( ComponentLookupException e )
        {
            this.getLogger().error( "Cannot lookup 'SecuritySystem', illegal state.", e );
            throw new ContextException( "Cannot lookup 'SecuritySystem', illegal state.", e );
        }
    }

    public String getSecurityManagerRole()
    {
        if ( securityManagerRole != null )
        {
            return securityManagerRole;
        }
        else
        {
            return DEFAULT_SECURITY_SYSTEM_ROLE;
        }
    }

    public void setSecurityManagerRole( String securityManagerRole )
    {
        this.securityManagerRole = securityManagerRole;
    }

    public String getSecurityManagerRoleHint()
    {
        if ( securityManagerRoleHint != null )
        {
            return securityManagerRoleHint;
        }
        else
        {
            return DEFAULT_SECURITY_MANAGER_ROLE_HINT;
        }
    }

    public void setSecurityManagerRoleHint( String securityManagerRoleHint )
    {
        this.securityManagerRoleHint = securityManagerRoleHint;
    }

    @Override
    public void init()
        throws JSecurityException
    {
        String role = getFilterConfig().getInitParameter( SECURITY_MANAGER_ROLE );

        if ( role != null )
        {
            setSecurityManagerRole( role );
        }

        String roleHint = getFilterConfig().getInitParameter( SECURITY_MANAGER_ROLE_HINT );

        if ( roleHint != null )
        {
            setSecurityManagerRoleHint( roleHint );
        }

        super.init();
    }

    protected PlexusContainer getPlexusContainer()
    {
        // to remain backward compatible, where PlexusConfiguration was not Plexus component, co
        // Contexualizable would not be noticed, get Plexus from servletContext attrs
        if ( plexusContainer == null )
        {
            ServletContext servletContext = getFilterConfig().getServletContext();

            plexusContainer = (PlexusContainer) servletContext.getAttribute( PlexusConstants.PLEXUS_KEY );
        }

        return plexusContainer;
    }

    @Override
    protected SecurityManager createDefaultSecurityManager()
    {
        return createSecurityManager( null );
    }

    @Override
    protected SecurityManager createSecurityManager( Map<String, Map<String, String>> sections )
    {
        return getOrCreateSecurityManager( getPlexusContainer(), sections );
    }

    protected SecurityManager getOrCreateSecurityManager( PlexusContainer container,
        Map<String, Map<String, String>> sections )
    {
        SecurityManager securityManager = null;

        try
        {
            securityManager = (RealmSecurityManager) container.lookup(
                getSecurityManagerRole(),
                getSecurityManagerRoleHint() );

            getLogger().info(
                "SecurityManager with role='" + getSecurityManagerRole() + "' and roleHint='"
                    + getSecurityManagerRoleHint() + "' found in Plexus." );
        }
        catch ( ComponentLookupException e )
        {
            // getLogger().info(
            // "Could not lookup SecurityManager with role='" + getSecurityManagerRole() + "' and roleHint='"
            // + getSecurityManagerRoleHint() + "'. Will look for Realms..." );
            getLogger().warn(
                "Could not lookup SecurityManager with role='" + getSecurityManagerRole() + "' and roleHint='"
                    + getSecurityManagerRoleHint() + "'. Will look for Realms...",
                e );

            securityManager = null;
        }

        if ( securityManager == null )
        {
            securityManager = createDefaultSecurityManagerFromRealms( container, sections );
        }

        if ( securityManager == null )
        {
            String msg = "There is no component with role "
                + SecurityManager.class.getName()
                + " available in the "
                + "Plexus Context. If your security manager uses different role and roleHint, you can specify those with this filter's '"
                + SECURITY_MANAGER_ROLE + "' and '" + SECURITY_MANAGER_ROLE_HINT + "' init-params.";

            throw new JSecurityException( msg );
        }
        return securityManager;
    }

    protected SecurityManager createDefaultSecurityManagerFromRealms( PlexusContainer container,
        Map<String, Map<String, String>> sections )
    {
        SecurityManager securityManager = null;

        // Create security manager according to superclass
        securityManager = super.createSecurityManager( sections );

        try
        {
            List<Realm> realms = container.lookupList( Realm.class );

            if ( !realms.isEmpty() )
            {
                if ( securityManager instanceof RealmSecurityManager )
                {
                    RealmSecurityManager realmSM = (RealmSecurityManager) securityManager;

                    realmSM.setRealms( realms );
                }
                else
                {
                    getLogger().warn(
                        "Attempted to set realms declared in Plexus Context on SecurityManager, but was not of "
                            + "type RealmSecurityManager - instead was of type: "
                            + securityManager.getClass().getName() );
                }
            }
        }
        catch ( ComponentLookupException e )
        {
            getLogger().warn( "Attempted to lookup realms declared in Plexus Context but found none", e );
        }

        LifecycleUtils.init( securityManager );

        return securityManager;
    }

    // DYNA CONFIG

    @Override
    protected void afterSecurityManagerSet( Map<String, Map<String, String>> sections )
    {
        // filters section:
        Map<String, String> section = sections.get( FILTERS );

        // changed: we need the prepped filters for later
        filters = getFilters( section );

        // urls section:
        section = sections.get( URLS );
        this.chains = createChains( section, filters );

        initFilters( this.chains );
    }

    @Override
    public Map<String, List<Filter>> createChains( Map<String, String> urls, Map<String, Filter> filters )
    {
        if ( urls == null || urls.isEmpty() )
        {
            if ( this.getLogger().isDebugEnabled() )
            {
                this.getLogger().debug( "No urls to process." );
            }
            return null;
        }
        if ( filters == null || filters.isEmpty() )
        {
            if ( this.getLogger().isDebugEnabled() )
            {
                this.getLogger().debug( "No filters to process." );
            }
            return null;
        }

        if ( this.getLogger().isTraceEnabled() )
        {
            this.getLogger().trace( "Before url processing." );
        }

        Map<String, List<Filter>> pathChains = new LinkedHashMap<String, List<Filter>>( urls.size() );

        for ( Map.Entry<String, String> entry : urls.entrySet() )
        {
            String path = entry.getKey();
            String value = entry.getValue();

            // externalized this part to enable later reuse
            List<Filter> pathFilters = getPathFilters( path, value );

            if ( !pathFilters.isEmpty() )
            {
                pathChains.put( path, pathFilters );
            }
        }

        if ( pathChains.isEmpty() )
        {
            return null;
        }

        return pathChains;
    }

    protected List<Filter> getPathFilters( String path, String value )
    {
        if ( this.getLogger().isDebugEnabled() )
        {
            this.getLogger().debug( "Processing path [" + path + "] with value [" + value + "]" );
        }

        List<Filter> pathFilters = new ArrayList<Filter>();

        // parse the value by tokenizing it to get the resulting filter-specific config entries
        //
        // e.g. for a value of
        //
        // "authc, roles[admin,user], perms[file:edit]"
        //
        // the resulting token array would equal
        //
        // { "authc", "roles[admin,user]", "perms[file:edit]" }
        //
        String[] filterTokens = split( value, ',', '[', ']', true, true );

        // each token is specific to each filter.
        // strip the name and extract any filter-specific config between brackets [ ]
        for ( String token : filterTokens )
        {
            String[] nameAndConfig = token.split( "\\[", 2 );
            String name = nameAndConfig[0];
            String config = null;

            if ( nameAndConfig.length == 2 )
            {
                config = nameAndConfig[1];
                // if there was an open bracket, there was a close bracket, so strip it too:
                config = config.substring( 0, config.length() - 1 );
            }

            // now we have the filter name, path and (possibly null) path-specific config. Let's apply them:
            Filter filter = filters.get( name );
            if ( filter == null )
            {
                String msg = "Path [" + path + "] specified a filter named '" + name + "', but that "
                    + "filter has not been specified in the [" + FILTERS + "] section.";
                throw new ConfigurationException( msg );
            }
            if ( filter instanceof PathConfigProcessor )
            {
                if ( this.getLogger().isDebugEnabled() )
                {
                    this.getLogger().debug(
                        "Applying path [" + path + "] to filter [" + name + "] " + "with config [" + config + "]" );
                }
                ( (PathConfigProcessor) filter ).processPathConfig( path, config );
            }

            pathFilters.add( filter );
        }

        return pathFilters;
    }

    public void addProtectedResource( String pathPattern, String filterExpression )
        throws SecurityConfigurationException
    {
        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Adding new protected resource with path='" + pathPattern + "' and filterExpression='"
                        + filterExpression + "'" );
            }

            if ( chains == null )
            {
                // create a map if not
                chains = new LinkedHashMap<String, List<Filter>>();
            }

            chains.remove( pathPattern );

            chains.put( pathPattern, getPathFilters( pathPattern, filterExpression ) );
        }
        catch ( Exception e )
        {
            throw new SecurityConfigurationException( "Could not apply changes!", e );
        }
    }

    public void protectedResourcesAdded()
    {
        initFilters( chains );
    }
}
