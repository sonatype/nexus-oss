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
package org.sonatype.nexus.plugins.mavenbridge.internal;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.plugins.mavenbridge.NexusAether;
import org.sonatype.nexus.plugins.mavenbridge.workspace.NexusWorkspace;
import org.sonatype.nexus.proxy.maven.MavenRepository;

@Named
@Singleton
public class DefaultNexusAether
    implements NexusAether
{
    public static final String LOCAL_REPO_DIR = "maven2-local-repository";

    private final ApplicationConfiguration applicationConfiguration;

    private final RepositorySystem repositorySystem;

    @Inject
    public DefaultNexusAether( final ApplicationConfiguration applicationConfiguration,
                               final RepositorySystem repositorySystem )
    {
        this.applicationConfiguration = applicationConfiguration;
        this.repositorySystem = repositorySystem;
    }

    public NexusWorkspace createWorkspace( List<MavenRepository> participants )
    {
        if ( participants == null || participants.isEmpty() )
        {
            throw new IllegalArgumentException(
                "Participant repositories in NexusWorkspace must have at least one element!" );
        }

        return new NexusWorkspace( UUID.randomUUID().toString(), participants );
    }

    public NexusWorkspace createWorkspace( MavenRepository... participants )
    {
        if ( participants == null )
        {
            throw new IllegalArgumentException(
                "Participant repositories in NexusWorkspace must have at least one element!" );
        }

        return createWorkspace( Arrays.asList( participants ) );
    }

    public synchronized RepositorySystem getRepositorySystem()
    {
        return repositorySystem;
    }

    public DefaultRepositorySystemSession getDefaultRepositorySystemSession()
    {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        // can't aether work _without_ local repo?
        LocalRepository localRepo =
            new LocalRepository( applicationConfiguration.getWorkingDirectory( LOCAL_REPO_DIR ) );
        session.setLocalRepositoryManager( getRepositorySystem().newLocalRepositoryManager( localRepo ) );

        // session.setIgnoreMissingArtifactDescriptor( false );

        // session.setTransferListener( new ConsoleTransferListener( System.out ) );
        // session.setRepositoryListener( new ConsoleRepositoryListener( System.out ) );

        // session.setUpdatePolicy( "" );

        return session;
    }

    public DefaultRepositorySystemSession getNexusEnabledRepositorySystemSession( final NexusWorkspace nexusWorkspace,
                                                                                  final RepositoryListener listener )
    {
        return getNexusEnabledRepositorySystemSession( getDefaultRepositorySystemSession(), nexusWorkspace, listener );
    }

    public DefaultRepositorySystemSession getNexusEnabledRepositorySystemSession( final DefaultRepositorySystemSession session,
                                                                                  final NexusWorkspace nexusWorkspace,
                                                                                  final RepositoryListener listener )
    {
        session.setWorkspaceReader( nexusWorkspace.getWorkspaceReader() );
        session.setOffline( true );
        session.setRepositoryListener( listener );

        return session;
    }
}
