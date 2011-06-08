/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.proxy;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = P2ProxyRepositoryConfigurator.class )
public class P2ProxyRepositoryConfigurator
    extends AbstractProxyRepositoryConfigurator
{

    @Requirement( hint = "P2ChecksumContentValidator" )
    private ItemContentValidator checksumValidator;

    @Requirement( hint = "Pack200ContentValidator" )
    private ItemContentValidator pack200Validator;

    @Override
    public void doApplyConfiguration( Repository repository, ApplicationConfiguration configuration,
                                      CRepositoryCoreConfiguration coreConfiguration )
        throws ConfigurationException
    {
        super.doApplyConfiguration( repository, configuration, coreConfiguration );

        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            ProxyRepository proxy = repository.adaptToFacet( ProxyRepository.class );

            proxy.getItemContentValidators().put( "checksum", checksumValidator );
            proxy.getItemContentValidators().put( "pack200", pack200Validator );
        }
    }
}
