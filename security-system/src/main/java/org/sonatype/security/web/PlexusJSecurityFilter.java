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

import static org.jsecurity.util.StringUtils.clean;

import javax.servlet.FilterConfig;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.config.ConfigurationException;
import org.jsecurity.util.ClassUtils;
import org.jsecurity.util.LifecycleUtils;
import org.jsecurity.web.config.WebConfiguration;
import org.jsecurity.web.servlet.JSecurityFilter;

/**
 * Extension of JSecurityFilter that uses Plexus lookup to get the configuration, if any role param is given. Otherwise
 * it fallbacks to the standard stuff from JSecurityFilter.
 * 
 * @author cstamas
 */
public class PlexusJSecurityFilter
    extends JSecurityFilter
{
    public static final String CONFIG_ROLE = "configRole";

    public static final String CONFIG_ROLE_DEFAULT = PlexusWebConfiguration.class.getName();

    public static final String CONFIG_ROLE_HINT = "configRoleHint";

    private String configRole;

    private String configRoleHint;

    public PlexusJSecurityFilter()
    {
        // not setting configClassName explicitly, so we can use either configRole or configClassName

        this.configClassName = null;
    }

    protected void applyInitParams()
    {
        super.applyInitParams();

        FilterConfig config = getFilterConfig();

        configRole = clean( config.getInitParameter( CONFIG_ROLE ) );

        if ( configRole == null )
        {
            configRole = CONFIG_ROLE_DEFAULT;
        }

        configRoleHint = clean( config.getInitParameter( CONFIG_ROLE_HINT ) );
    }

    protected WebConfiguration configure()
    {
        WebConfiguration conf = null;

        if ( !StringUtils.isEmpty( configClassName ) )
        {
            // doin' the "old" way
            conf = (WebConfiguration) ClassUtils.newInstance( this.configClassName );
        }
        else if ( !StringUtils.isEmpty( configRole ) )
        {
            try
            {
                // doin' a lookup
                if ( StringUtils.isEmpty( configRoleHint ) )
                {
                    conf = (WebConfiguration) getPlexusContainer().lookup( configRole );
                }
                else
                {
                    conf = (WebConfiguration) getPlexusContainer().lookup( configRole, configRoleHint );
                }
            }
            catch ( ComponentLookupException e )
            {
                throw new ConfigurationException( "Could not lookup configuration!", e );
            }
        }
        else
        {
            // fallback to the "default" if nothing is given
            conf = (WebConfiguration) ClassUtils.newInstance( PlexusConfiguration.class.getName() );
        }

        applyFilterConfig( conf );
        applyUrlConfig( conf );
        applyEmbeddedConfig( conf );
        LifecycleUtils.init( conf );
        return conf;
    }

    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getAttribute( PlexusConstants.PLEXUS_KEY );
    }
}
