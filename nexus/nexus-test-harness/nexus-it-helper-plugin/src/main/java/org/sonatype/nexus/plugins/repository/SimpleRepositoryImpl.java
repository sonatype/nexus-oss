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
package org.sonatype.nexus.plugins.repository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryKind;

@Component( role = SimpleRepository.class, hint="default" )
public class SimpleRepositoryImpl
    extends AbstractRepository
    implements SimpleRepository
{
    @Requirement( hint = SimpleContentClass.ID )
    private ContentClass contentClass;

    @Requirement
    private SimpleRepositoryConfigurator simpleRepositoryConfigurator;

    private final RepositoryKind repositoryKind = new DefaultRepositoryKind( SimpleRepository.class, null );

    @Override
    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }

    @Override
    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return simpleRepositoryConfigurator;
    }

    @Override
    protected SimpleRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (SimpleRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<SimpleRepositoryConfiguration> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<SimpleRepositoryConfiguration>()
        {
            public SimpleRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new SimpleRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    @Override
    public synchronized String sayHello()
    {
        int cnt = getExternalConfiguration( false ).getSaidHelloCount();

        getExternalConfiguration( true ).setSaidHelloCount( cnt++ );

        getLogger().info( String.format( "Saying \"Hello\" for %s time.", cnt ) );

        return "hello";
    }
}
