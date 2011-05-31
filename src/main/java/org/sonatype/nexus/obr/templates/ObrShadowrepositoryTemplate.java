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
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.obr.ObrContentClass;
import org.sonatype.nexus.obr.shadow.ObrShadowRepository;
import org.sonatype.nexus.obr.shadow.ObrShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;

public class ObrShadowrepositoryTemplate
    extends AbstractRepositoryTemplate
{
    public ObrShadowrepositoryTemplate( final ObrRepositoryTemplateProvider provider, final String id,
                                        final String description )
    {
        super( provider, id, description, new ObrContentClass(), ObrShadowRepository.class );
    }

    public ObrShadowRepositoryConfiguration getExternalConfiguration( final boolean forWrite )
    {
        return (ObrShadowRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration().getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        final CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( ShadowRepository.class.getName() );
        repo.setProviderHint( ObrShadowRepository.ROLE_HINT );

        final Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        final ObrShadowRepositoryConfiguration exConf = new ObrShadowRepositoryConfiguration( ex );

        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );

        final CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration(
                                              getTemplateProvider().getApplicationConfiguration(),
                                              repo,
                                              new CRepositoryExternalConfigurationHolderFactory<ObrShadowRepositoryConfiguration>()
                                              {
                                                  public ObrShadowRepositoryConfiguration createExternalConfigurationHolder( final CRepository config )
                                                  {
                                                      return new ObrShadowRepositoryConfiguration(
                                                                                                   (Xpp3Dom) config.getExternalConfiguration() );
                                                  }
                                              } );

        return result;
    }
}
