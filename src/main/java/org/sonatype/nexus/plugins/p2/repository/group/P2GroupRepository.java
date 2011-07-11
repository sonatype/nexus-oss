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
package org.sonatype.nexus.plugins.p2.repository.group;

import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.p2.repository.P2ContentClass;
import org.sonatype.nexus.plugins.p2.repository.P2Repository;
import org.sonatype.nexus.plugins.p2.repository.metadata.P2MetadataSource;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;

@Component( role = GroupRepository.class, hint = P2GroupRepository.ROLE_HINT, instantiationStrategy = "per-lookup", description = "Eclipse P2 Artifacts" )
public class P2GroupRepository
    extends AbstractGroupRepository
    implements P2Repository, GroupRepository
{

    public static final String ROLE_HINT = "p2";

    @Requirement( hint = P2ContentClass.ID )
    private ContentClass contentClass;

    @Requirement( role = P2MetadataSource.class, hint = "group" )
    private P2MetadataSource<P2GroupRepository> metadataSource;

    @Requirement
    private P2GroupRepositoryConfigurator p2GroupRepositoryConfigurator;

    private RepositoryKind repositoryKind;

    @Override
    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    @Override
    public RepositoryKind getRepositoryKind()
    {
        if ( repositoryKind == null )
        {
            repositoryKind =
                new DefaultRepositoryKind( GroupRepository.class,
                    Arrays.asList( new Class<?>[] { P2GroupRepository.class } ) );
        }
        return repositoryKind;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return p2GroupRepositoryConfigurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<P2GroupRepositoryConfiguration>()
        {
            @Override
            public P2GroupRepositoryConfiguration createExternalConfigurationHolder( final CRepository config )
            {
                return new P2GroupRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    @Override
    protected StorageItem doRetrieveItem( final ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        final StorageItem item = metadataSource.doRetrieveItem( request, this );

        if ( item != null )
        {
            return item;
        }

        return super.doRetrieveItem( request );
    }

}
