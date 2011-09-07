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
package org.sonatype.nexus.obr.templates;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.obr.ObrContentClass;
import org.sonatype.nexus.obr.proxy.ObrProxyRepository;
import org.sonatype.nexus.obr.proxy.ObrRepository;
import org.sonatype.nexus.obr.proxy.ObrRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;

public class ObrProxyRepositoryTemplate
    extends AbstractRepositoryTemplate
{
    public ObrProxyRepositoryTemplate( final ObrRepositoryTemplateProvider provider, final String id,
                                       final String description )
    {
        super( provider, id, description, new ObrContentClass(), ObrProxyRepository.class );
    }

    public ObrRepositoryConfiguration getExternalConfiguration( final boolean forWrite )
    {
        return (ObrRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration().getConfiguration(
            forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        final CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( Repository.class.getName() );
        repo.setProviderHint( ObrRepository.ROLE_HINT );

        repo.setRemoteStorage( new CRemoteStorage() );
        repo.getRemoteStorage().setProvider(
            getTemplateProvider().getRemoteProviderHintFactory().getDefaultHttpRoleHint() );
        repo.getRemoteStorage().setUrl( "http://some-remote-repository/repo-root/obr.xml" );

        final Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        final ObrRepositoryConfiguration exConf = new ObrRepositoryConfiguration( ex );

        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );
        repo.setNotFoundCacheTTL( 1440 );

        final CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration( getTemplateProvider().getApplicationConfiguration(), repo,
                new CRepositoryExternalConfigurationHolderFactory<ObrRepositoryConfiguration>()
                {
                    public ObrRepositoryConfiguration createExternalConfigurationHolder( final CRepository config )
                    {
                        return new ObrRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
                    }
                } );

        return result;
    }
}
