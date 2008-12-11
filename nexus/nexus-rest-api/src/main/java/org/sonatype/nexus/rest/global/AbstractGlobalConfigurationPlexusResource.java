/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.global;

import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.AuthenticationSettings;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.SmtpSettings;

/**
 * The base class for global configuration resources.
 * 
 * @author cstamas
 */
public abstract class AbstractGlobalConfigurationPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String SECURITY_OFF = "off";

    public static final String SECURITY_SIMPLE = "simple";
    
    public static final String SECURITY_CUSTOM = "custom";
    
    public static SmtpSettings convert( CSmtpConfiguration settings )
    {
        if ( settings == null )
        {
            return null;
        }
        
        SmtpSettings result = new SmtpSettings();
        
        result.setHost( settings.getHost() );
        
        result.setPassword( settings.getPassword() );
        
        result.setPort( settings.getPort() );
        
        result.setSslEnabled( settings.isSslEnabled() );
        
        result.setSystemEmailAddress( settings.getSystemEmailAddress() );
        
        result.setTlsEnabled( settings.isTlsEnabled() );
        
        result.setUsername( settings.getUsername() );
        
        return result;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static RemoteConnectionSettings convert( CRemoteConnectionSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }

        RemoteConnectionSettings result = new RemoteConnectionSettings();

        result.setConnectionTimeout( settings.getConnectionTimeout() / 1000 );

        result.setRetrievalRetryCount( settings.getRetrievalRetryCount() );

        result.setQueryString( settings.getQueryString() );

        result.setUserAgentString( settings.getUserAgentCustomizationString() );

        return result;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static RemoteHttpProxySettings convert( CRemoteHttpProxySettings settings )
    {
        if ( settings == null )
        {
            return null;
        }

        RemoteHttpProxySettings result = new RemoteHttpProxySettings();

        result.setProxyHostname( settings.getProxyHostname() );

        result.setProxyPort( settings.getProxyPort() );

        result.setAuthentication( convert( settings.getAuthentication() ) );

        return result;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static AuthenticationSettings convert( CRemoteAuthentication settings )
    {
        if ( settings == null )
        {
            return null;
        }

        AuthenticationSettings auth = new AuthenticationSettings();

        auth.setUsername( settings.getUsername() );

        auth.setPassword( settings.getPassword() );

        auth.setNtlmHost( settings.getNtlmHost() );

        auth.setNtlmDomain( settings.getNtlmDomain() );

        auth.setPrivateKey( settings.getPrivateKey() );

        auth.setPassphrase( settings.getPassphrase() );

        return auth;
    }

}
