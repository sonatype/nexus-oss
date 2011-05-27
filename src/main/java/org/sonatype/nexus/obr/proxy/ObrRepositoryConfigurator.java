/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.obr.proxy;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = ObrRepositoryConfigurator.class )
public class ObrRepositoryConfigurator
    extends AbstractProxyRepositoryConfigurator
{
    @Override
    public void doApplyConfiguration( Repository repository, ApplicationConfiguration configuration,
                                      CRepositoryCoreConfiguration coreConfig )
        throws ConfigurationException
    {
        repository.setIndexable( false );

        super.doApplyConfiguration( repository, configuration, coreConfig );

        CRemoteStorage remoteStorage = coreConfig.getConfiguration( true ).getRemoteStorage();

        if ( remoteStorage != null )
        {
            // // FIXME: on the fly upgrade, if needed
            // // it will trigger if detects that nexus.xml contains remoteUrl _with_ OBR XML file
            // String[] siteAndPath = ObrUtils.splitObrSiteAndPath( remoteStorage.getUrl(), false );
            //
            // if ( siteAndPath[1] != null )
            // {
            // // upgrade needed!
            // ( (ObrProxyRepository) repository ).setObrPath( siteAndPath[1] );
            //
            // // write back the stripped URL
            // remoteStorage.setUrl( siteAndPath[0] );
            // }

            // FIXME: this should happen in this super's class: AbstractProxyRepositoryConfigurator
            try
            {
                ( (ObrProxyRepository) repository ).setRemoteUrl( remoteStorage.getUrl() );
            }
            catch ( StorageException e )
            {
                throw new ConfigurationException( "Cannot configure OBR Proxy Repository! " + remoteStorage.getUrl(), e );
            }
        }
    }
}
