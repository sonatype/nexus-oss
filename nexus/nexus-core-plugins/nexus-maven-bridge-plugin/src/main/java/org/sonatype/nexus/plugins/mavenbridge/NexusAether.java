/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.plugins.mavenbridge;

import java.util.List;

import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.nexus.plugins.mavenbridge.workspace.NexusWorkspace;
import org.sonatype.nexus.proxy.maven.MavenRepository;

/**
 * Component providing Nexus integrated Aether services, like Nexus-enabled Sessions and WorkspaceReaders. It also gives
 * a handle to (shared) RepositorySystem.
 * 
 * @author cstamas
 */
public interface NexusAether
{
    /**
     * Creates a nexus workspace containing the particiant MavenRepositories. These repositories will fed the Aether
     * engine with artifacts (probably causing some proxying to happen too, if needed). It may be a group or just a
     * bunch of repository (or any combination of them).
     * 
     * @param participants
     * @return
     */
    NexusWorkspace createWorkspace( List<MavenRepository> participants );

    /**
     * Creates a nexus workspace containing the particiant MavenRepositories. These repositories will fed the Aether
     * engine with artifacts (probably causing some proxying to happen too, if needed). It may be a group or just a
     * bunch of repository (or any combination of them).
     * 
     * @param participants
     * @return
     */
    NexusWorkspace createWorkspace( MavenRepository... participants );

    /**
     * Returns the shared repository system instance.
     */
    RepositorySystem getRepositorySystem();

    /**
     * Returns the most basic repository system session with local repository set only.
     * 
     * @param repositorySystem
     * @return
     */
    RepositorySystemSession getDefaultRepositorySystemSession();

    /**
     * Returns a special repository system session that is "nexus enabled" (using
     * {@link #getDefaultRepositorySystemSession()} returned session). Passed in nexus workspace controls what nexus
     * repository participates in this session. The returned session is set {@code OFFLINE} since passed in
     * NexusWorkspace feeds all the artifacts needed, and Aether should not go remote (reach out of Nexus instance).
     * 
     * @param repositorySystem
     * @param nexusWorkspace
     * @param listener
     * @return
     */
    RepositorySystemSession getNexusEnabledRepositorySystemSession( NexusWorkspace nexusWorkspace,
                                                                    RepositoryListener listener );

    /**
     * Returns a special repository system session that is "nexus enabled". Passed in nexus workspace controls what
     * nexus repository participates in this session. The returned session is set {@code OFFLINE} since passed in
     * NexusWorkspace feeds all the artifacts needed, and Aether should not go remote (reach out of Nexus instance).
     * 
     * @param repositorySystem
     * @param nexusWorkspace
     * @param listener
     * @return
     */
    RepositorySystemSession getNexusEnabledRepositorySystemSession( DefaultRepositorySystemSession session,
                                                                    NexusWorkspace nexusWorkspace,
                                                                    RepositoryListener listener );
}
