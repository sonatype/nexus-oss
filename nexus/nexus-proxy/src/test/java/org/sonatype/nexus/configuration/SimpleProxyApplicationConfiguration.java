/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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

        // NEXUS-4521: littering "defaults" all over the place is unhealthy
        //configuration.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        //configuration.getGlobalConnectionSettings().setConnectionTimeout( 1000 );
        //configuration.getGlobalConnectionSettings().setRetrievalRetryCount( 3 );
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
