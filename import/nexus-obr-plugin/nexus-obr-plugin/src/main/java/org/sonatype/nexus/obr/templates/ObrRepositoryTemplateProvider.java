/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
            templates.add( new ObrProxyRepositoryTemplate( this, OBR_PROXY, "OBR (proxy)" ) );
            templates.add( new ObrShadowrepositoryTemplate( this, OBR_SHADOW, "OBR (virtual)" ) );
            templates.add( new ObrGroupRepositoryTemplate( this, OBR_GROUP, "OBR (group)" ) );
            templates.add( new ObrHostedRepositoryTemplate( this, OBR_HOSTED, "OBR (hosted)" ) );
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
