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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.obr.group.ObrGroupRepository;
import org.sonatype.nexus.obr.proxy.ObrRepository;
import org.sonatype.nexus.obr.shadow.ObrShadowRepository;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateProvider;

@Component( role = TemplateProvider.class, hint = ObrRepositoryTemplateProvider.PROVIDER_ID )
public class ObrRepositoryTemplateProvider
    extends AbstractRepositoryTemplateProvider
    implements Initializable
{
    public static final String PROVIDER_ID = "obr-repository";

    private static final String OBR_PROXY = "obr_proxy";

    private static final String OBR_HOSTED = "obr_hosted";

    private static final String OBR_SHADOW = "obr_shadow";

    private static final String OBR_GROUP = "obr_group";

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    public TemplateSet getTemplates()
    {
        final TemplateSet templates = new TemplateSet( null );

        try
        {
            templates.add( new ObrProxyRepositoryTemplate( this, OBR_PROXY, "OBR Proxy Repository" ) );
            templates.add( new ObrShadowrepositoryTemplate( this, OBR_SHADOW, "OBR Shadow Repository" ) );
            templates.add( new ObrGroupRepositoryTemplate( this, OBR_GROUP, "OBR Group Repository" ) );
            templates.add( new ObrHostedRepositoryTemplate( this, OBR_HOSTED, "OBR Hosted Repository" ) );
        }
        catch ( final Exception e )
        {
            // will not happen
        }

        return templates;
    }

    public void initialize()
        throws InitializationException
    {
        repositoryTypeRegistry.registerRepositoryTypeDescriptors( new RepositoryTypeDescriptor(
                                                                                                Repository.class,
                                                                                                ObrRepository.ROLE_HINT,
                                                                                                "repositories" ) );

        repositoryTypeRegistry.registerRepositoryTypeDescriptors( new RepositoryTypeDescriptor(
                                                                                                ShadowRepository.class,
                                                                                                ObrShadowRepository.ROLE_HINT,
                                                                                                "shadows" ) );

        repositoryTypeRegistry.registerRepositoryTypeDescriptors( new RepositoryTypeDescriptor(
                                                                                                GroupRepository.class,
                                                                                                ObrGroupRepository.ROLE_HINT,
                                                                                                "groups" ) );
    }
}
