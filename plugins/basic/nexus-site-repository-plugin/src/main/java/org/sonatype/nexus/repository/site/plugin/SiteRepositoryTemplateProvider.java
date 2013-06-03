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
package org.sonatype.nexus.repository.site.plugin;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateProvider;

import static com.google.common.base.Preconditions.checkNotNull;

@Named( SiteRepositoryTemplateProvider.PROVIDER_ID )
@Singleton
public class SiteRepositoryTemplateProvider
    extends AbstractRepositoryTemplateProvider
    implements Initializable
{

    public static final String PROVIDER_ID = "site-repository";

    private final RepositoryTypeRegistry repositoryTypeRegistry;

    @Inject
    public SiteRepositoryTemplateProvider( final RepositoryTypeRegistry repositoryTypeRegistry )
    {
        this.repositoryTypeRegistry = checkNotNull( repositoryTypeRegistry );
    }

    public TemplateSet getTemplates()
    {
        TemplateSet templates = new TemplateSet( null );

        try
        {
            templates.add( new SiteRepositoryTemplate( this, SiteRepository.ID, "Site (hosted)" ) );
        }
        catch ( Exception e )
        {
            // will not happen
        }

        return templates;
    }

    public void initialize()
        throws InitializationException
    {
        repositoryTypeRegistry.registerRepositoryTypeDescriptors( new RepositoryTypeDescriptor(
            WebSiteRepository.class, "site", "sites" ) );
    }
}
