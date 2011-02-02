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
package org.sonatype.nexus.templates.repository;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.remote.RemoteProviderHintFactory;
import org.sonatype.nexus.templates.AbstractTemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;

/**
 * An abstract class for template providers that provides templates for Repositories.
 * 
 * @author cstamas
 */
public abstract class AbstractRepositoryTemplateProvider
    extends AbstractTemplateProvider<RepositoryTemplate>
{
    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private Nexus nexus;
    
    @Requirement
    private RemoteProviderHintFactory remoteProviderHintFactory;

    protected Repository createRepository( CRepository repository )
        throws ConfigurationException, IOException
    {
        return this.nexus.getNexusConfiguration().createRepository( repository );
    }
    
    public String getDefaultRemoteProviderHint()
    {
        return remoteProviderHintFactory.getDefaultRoleHint();
    }

    public Class<RepositoryTemplate> getTemplateClass()
    {
        return RepositoryTemplate.class;
    }

    public TemplateSet getTemplates( Object filter )
    {
        return getTemplates().getTemplates( filter );
    }

    public TemplateSet getTemplates( Object... filters )
    {
        return getTemplates().getTemplates( filters );
    }

    public ManuallyConfiguredRepositoryTemplate createManuallyTemplate( CRepositoryCoreConfiguration configuration )
    {
        ContentClass contentClass =
            repositoryTypeRegistry.getRepositoryContentClass(
                configuration.getConfiguration( false ).getProviderRole(),
                configuration.getConfiguration( false ).getProviderHint() );

        return new ManuallyConfiguredRepositoryTemplate( this, "manual", "Manually created template", contentClass,
            null, configuration );
    }
}
