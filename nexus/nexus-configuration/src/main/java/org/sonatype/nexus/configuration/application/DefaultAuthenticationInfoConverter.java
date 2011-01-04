/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration.application;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.proxy.repository.ClientSSLRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;

@Component( role = AuthenticationInfoConverter.class )
public class DefaultAuthenticationInfoConverter
    implements AuthenticationInfoConverter
{
    public RemoteAuthenticationSettings convertAndValidateFromModel( CRemoteAuthentication model )
        throws ConfigurationException
    {
        if ( model != null )
        {
            doValidate( model );

            if ( StringUtils.isNotBlank( model.getKeyStore() ) || StringUtils.isNotBlank( model.getTrustStore() ) )
            {
                return new ClientSSLRemoteAuthenticationSettings( new File( model.getTrustStore() ), model
                    .getTrustStorePassword(), new File( model.getKeyStore() ), model.getKeyStorePassword() );
            }
            else if ( StringUtils.isNotBlank( model.getNtlmDomain() ) )
            {
                return new NtlmRemoteAuthenticationSettings( model.getUsername(), model.getPassword(), model
                    .getNtlmDomain(), model.getNtlmHost() );
            }
            else
            {
                return new UsernamePasswordRemoteAuthenticationSettings( model.getUsername(), model.getPassword() );
            }
        }
        else
        {
            return null;
        }
    }

    public CRemoteAuthentication convertToModel( RemoteAuthenticationSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }
        else
        {
            CRemoteAuthentication remoteAuthentication = new CRemoteAuthentication();

            if ( settings instanceof NtlmRemoteAuthenticationSettings )
            {
                NtlmRemoteAuthenticationSettings up = (NtlmRemoteAuthenticationSettings) settings;

                remoteAuthentication.setUsername( up.getUsername() );

                remoteAuthentication.setPassword( up.getPassword() );

                remoteAuthentication.setNtlmDomain( up.getNtlmDomain() );

                remoteAuthentication.setNtlmHost( up.getNtlmHost() );
            }
            else if ( settings instanceof UsernamePasswordRemoteAuthenticationSettings )
            {
                UsernamePasswordRemoteAuthenticationSettings up =
                    (UsernamePasswordRemoteAuthenticationSettings) settings;

                remoteAuthentication.setUsername( up.getUsername() );

                remoteAuthentication.setPassword( up.getPassword() );
            }
            else if ( settings instanceof ClientSSLRemoteAuthenticationSettings )
            {
                ClientSSLRemoteAuthenticationSettings cs = (ClientSSLRemoteAuthenticationSettings) settings;

                remoteAuthentication.setKeyStore( cs.getKeyStore().getAbsolutePath() );

                remoteAuthentication.setKeyStorePassword( cs.getKeyStorePassword() );

                remoteAuthentication.setTrustStore( cs.getTrustStore().getAbsolutePath() );

                remoteAuthentication.setTrustStorePassword( cs.getTrustStorePassword() );
            }
            else
            {
                // ??
            }

            return remoteAuthentication;
        }
    }

    // ==

    protected void doValidate( CRemoteAuthentication model )
        throws ConfigurationException
    {
        // FIXME: implement me
    }

}
