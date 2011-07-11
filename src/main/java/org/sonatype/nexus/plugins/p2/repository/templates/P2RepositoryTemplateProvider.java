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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.plugins.p2.repository.group.P2GroupRepository;
import org.sonatype.nexus.plugins.p2.repository.proxy.P2ProxyRepository;
import org.sonatype.nexus.plugins.p2.repository.updatesite.UpdateSiteRepository;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateProvider;

@Component( role = TemplateProvider.class, hint = P2RepositoryTemplateProvider.PROVIDER_ID )
public class P2RepositoryTemplateProvider
    extends AbstractRepositoryTemplateProvider
    implements Initializable
{
    public static final String PROVIDER_ID = "p2-repository";

    private static final String P2_PROXY = "p2_proxy";

    private static final String P2_UPDATE_SITE = "p2_updatesite";

    private static final String P2_GROUP = "p2_group";

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Override
    public TemplateSet getTemplates()
    {
        final TemplateSet templates = new TemplateSet( null );

        try
        {
            templates.add( new P2ProxyRepositoryTemplate( this, P2_PROXY, "P2 Proxy Repository" ) );
            templates.add( new UpdateSiteRepositoryTemplate( this, P2_UPDATE_SITE, "P2 Update Site Proxy Repository" ) );
            templates.add( new P2GroupRepositoryTemplate( this, P2_GROUP, "P2 Repository Group" ) );
        }
        catch ( final Exception e )
        {
            // will not happen
        }

        return templates;
    }

    @Override
    public void initialize()
        throws InitializationException
    {
        repositoryTypeRegistry.registerRepositoryTypeDescriptors( new RepositoryTypeDescriptor( Repository.class,
            P2ProxyRepository.ROLE_HINT, "repositories" ) );

        repositoryTypeRegistry.registerRepositoryTypeDescriptors( new RepositoryTypeDescriptor( Repository.class,
            UpdateSiteRepository.ROLE_HINT, "repositories" ) );

        repositoryTypeRegistry.registerRepositoryTypeDescriptors( new RepositoryTypeDescriptor( GroupRepository.class,
            P2GroupRepository.ROLE_HINT, "groups" ) );

    }
}
