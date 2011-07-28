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
package org.sonatype.nexus.plugins.mavenbridge.internal.guice;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.maven.model.resolution.ModelResolver;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.sisu.maven.bridge.internal.ModelResolverFactory;
import org.sonatype.sisu.maven.bridge.resolvers.EmptyRemoteModelResolver;

@Singleton
public class NexusModelResolverFactory
    implements ModelResolverFactory
{
    private final RepositorySystem repositorySystem;

    private final RemoteRepositoryManager remoteRepositoryManager;

    @Inject
    public NexusModelResolverFactory( final RepositorySystem repositorySystem,
                                      final RemoteRepositoryManager remoteRepositoryManager )
    {
        this.repositorySystem = repositorySystem;
        this.remoteRepositoryManager = remoteRepositoryManager;
    }

    @Override
    public ModelResolver getModelResolver( RepositorySystemSession session )
    {
        return new EmptyRemoteModelResolver( repositorySystem, session, remoteRepositoryManager );
    }
}
