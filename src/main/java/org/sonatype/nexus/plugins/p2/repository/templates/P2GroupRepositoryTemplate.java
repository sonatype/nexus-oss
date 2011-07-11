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
package org.sonatype.nexus.plugins.p2.repository.templates;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.plugins.p2.repository.P2ContentClass;
import org.sonatype.nexus.plugins.p2.repository.group.P2GroupRepository;
import org.sonatype.nexus.plugins.p2.repository.group.P2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;

public class P2GroupRepositoryTemplate
    extends AbstractRepositoryTemplate
{
    public P2GroupRepositoryTemplate( final P2RepositoryTemplateProvider provider, final String id,
                                      final String description )
    {
        super( provider, id, description, new P2ContentClass(), P2GroupRepository.class );
    }

    public P2GroupRepositoryConfiguration getExternalConfiguration( final boolean forWrite )
    {
        return (P2GroupRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration().getConfiguration(
            forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        final CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( GroupRepository.class.getName() );
        repo.setProviderHint( P2GroupRepository.ROLE_HINT );

        final Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        final P2GroupRepositoryConfiguration exConf = new P2GroupRepositoryConfiguration( ex );

        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );

        final CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration( getTemplateProvider().getApplicationConfiguration(), repo,
                new CRepositoryExternalConfigurationHolderFactory<P2GroupRepositoryConfiguration>()
                {
                    @Override
                    public P2GroupRepositoryConfiguration createExternalConfigurationHolder( final CRepository config )
                    {
                        return new P2GroupRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
                    }
                } );

        return result;
    }
}