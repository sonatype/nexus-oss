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

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.AbstractConfigurableTemplate;

public abstract class AbstractRepositoryTemplate
    extends AbstractConfigurableTemplate
    implements RepositoryTemplate
{
    private final AbstractRepositoryTemplateProvider provider;

    private final ContentClass contentClass;

    private final Class<?> mainFacet;

    private ConfigurableRepository configurableRepository;

    public AbstractRepositoryTemplate( AbstractRepositoryTemplateProvider provider, String id, String description,
                                       ContentClass contentClass, Class<?> mainFacet )
    {
        super( provider, id, description );

        this.provider = provider;

        this.contentClass = contentClass;

        if ( mainFacet != null )
        {
            this.mainFacet = mainFacet;
        }
        else
        {
            this.mainFacet = Repository.class;
        }
    }

    @Override
    public boolean targetFits( Object clazz )
    {
        return super.targetFits( clazz ) || targetIsClassAndFitsClass( clazz, getMainFacet() )
            || ( targetIsClassAndFitsClass( clazz, getContentClass().getClass() ) || getContentClass().equals( clazz ) );
    }

    @Override
    public AbstractRepositoryTemplateProvider getTemplateProvider()
    {
        return provider;
    }

    public ContentClass getContentClass()
    {
        return contentClass;
    }

    public Class<?> getMainFacet()
    {
        return mainFacet;
    }

    public ConfigurableRepository getConfigurableRepository()
    {
        if ( configurableRepository == null )
        {
            configurableRepository = new ConfigurableRepository();

            try
            {
                configurableRepository.configure( getCoreConfiguration() );
            }
            catch ( ConfigurationException e )
            {
                // will not happen, since ConfigurableRepository will not validate!
                // TODO: get rid of this exception from here
            }
        }
        return configurableRepository;
    }

    public Repository create()
        throws ConfigurationException, IOException
    {
        getCoreConfiguration().validateChanges();

        // to merge in user changes to CoreConfiguration
        getCoreConfiguration().commitChanges();

        // create a repository
        Repository repository =
            getTemplateProvider().createRepository( getCoreConfiguration().getConfiguration( false ) );

        // reset the template
        setCoreConfiguration( null );

        // return the result
        return repository;
    }

    public String getRepositoryProviderRole()
    {
        return getCoreConfiguration().getConfiguration( false ).getProviderRole();
    }

    public String getRepositoryProviderHint()
    {
        return getCoreConfiguration().getConfiguration( false ).getProviderHint();
    }

    public CRepositoryCoreConfiguration getCoreConfiguration()
    {
        // we may do this, since we predefined the initCoreConfiguration(), see below
        return (CRepositoryCoreConfiguration) super.getCoreConfiguration();
    }

    // ==

    @Override
    protected abstract CRepositoryCoreConfiguration initCoreConfiguration();
}
