/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.yum.internal.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.plexus.config.Strategies;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Component( role = M2YumRepositoryTypeRegistrator.class, instantiationStrategy = Strategies.LOAD_ON_START )
@Singleton
public class M2YumRepositoryTypeRegistratorImpl
    implements M2YumRepositoryTypeRegistrator
{
    private static final Logger LOG = LoggerFactory.getLogger( M2YumRepositoryTypeRegistratorImpl.class );

    @Inject
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Inject
    public void registerRepositoryType()
    {
        LOG.info( "Try register my M2YumRepository and M2YumGroupRepository to the RepositoryTypeRegistry" );
        repositoryTypeRegistry.registerRepositoryTypeDescriptors( m2yumDescriptor() );
        repositoryTypeRegistry.registerRepositoryTypeDescriptors( m2yumGroupDescriptor() );
    }

    private RepositoryTypeDescriptor m2yumDescriptor()
    {
        return new RepositoryTypeDescriptor( Repository.class, M2YumRepository.ID, "repositories",
            RepositoryType.UNLIMITED_INSTANCES );
    }

    private RepositoryTypeDescriptor m2yumGroupDescriptor()
    {
        return new RepositoryTypeDescriptor( GroupRepository.class, M2YumGroupRepository.ID, "groups",
            RepositoryType.UNLIMITED_INSTANCES );
    }
}
