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
package org.sonatype.nexus.configuration;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.GlobalHttpProxySettings;
import org.sonatype.nexus.configuration.application.GlobalRemoteConnectionSettings;
import org.sonatype.nexus.configuration.application.SimpleApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

@Component( role = ApplicationConfiguration.class )
public class SimpleProxyApplicationConfiguration
    extends SimpleApplicationConfiguration
        implements Initializable
{
    private RemoteStorageContext remoteStorageContext = null;
    
    @Requirement
    private GlobalRemoteConnectionSettings globalRemoteConnectionSettings;
    
    @Requirement
    private GlobalHttpProxySettings globalHttpProxySettings;
    
    public void initialize()
        throws InitializationException
    {
        Configuration configuration = getConfigurationModel();

        configuration.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        configuration.getGlobalConnectionSettings().setConnectionTimeout( 1000 );
        configuration.getGlobalConnectionSettings().setRetrievalRetryCount( 3 );
        // configuration.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        configuration.setRouting( new CRouting() );
        configuration.setRepositoryGrouping( new CRepositoryGrouping() );

        // remote storage context
        remoteStorageContext = new DefaultRemoteStorageContext( null );

        try
        {
            globalRemoteConnectionSettings.configure( this );
            remoteStorageContext.setRemoteConnectionSettings( globalRemoteConnectionSettings );

            globalHttpProxySettings.configure( this );
            remoteStorageContext.setRemoteProxySettings( globalHttpProxySettings );
        }
        catch ( ConfigurationException e )
        {
            throw new InitializationException( "Error configuring nexus!", e );
        }   
    }
    
    @Override
    public RemoteStorageContext getGlobalRemoteStorageContext()
    {
        return remoteStorageContext;
    }
}
