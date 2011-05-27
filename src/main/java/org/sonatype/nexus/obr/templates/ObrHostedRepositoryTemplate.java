/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.obr.templates;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.obr.ObrContentClass;
import org.sonatype.nexus.obr.proxy.ObrHostedRepository;
import org.sonatype.nexus.obr.proxy.ObrRepository;
import org.sonatype.nexus.obr.proxy.ObrRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;


public class ObrHostedRepositoryTemplate
    extends AbstractRepositoryTemplate
{
    public ObrHostedRepositoryTemplate( ObrRepositoryTemplateProvider provider, String id, String description )
    {
        super( provider, id, description, new ObrContentClass(), ObrHostedRepository.class );
    }

    public ObrRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (ObrRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration().getConfiguration(
            forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( Repository.class.getName() );
        repo.setProviderHint( ObrRepository.ROLE_HINT );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        ObrRepositoryConfiguration exConf = new ObrRepositoryConfiguration( ex );

        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE_ONCE.name() );
        repo.setNotFoundCacheTTL( 1440 );

        CRepositoryCoreConfiguration result = new CRepositoryCoreConfiguration(
            getTemplateProvider().getApplicationConfiguration(),
            repo,
            new CRepositoryExternalConfigurationHolderFactory<ObrRepositoryConfiguration>()
            {
                public ObrRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
                {
                    return new ObrRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
                }
            } );

        return result;
    }
}
