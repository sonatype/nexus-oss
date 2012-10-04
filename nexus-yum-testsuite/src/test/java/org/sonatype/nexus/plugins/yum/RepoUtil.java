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
package org.sonatype.nexus.plugins.yum;

import static org.sonatype.nexus.plugins.yum.NameUtil.uniqueName;

import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;

public final class RepoUtil
{

    public static String createHostedRepo( final NexusClient client )
    {
        final String repoName = uniqueName();
        final Repositories repositories = client.getSubsystem( Repositories.class );
        final MavenHostedRepository repo = repositories.create( MavenHostedRepository.class, repoName );
        repo.settings().setId( repoName );
        repo.settings().setName( repoName );
        repo.withRepoPolicy( "RELEASE" ).withWritePolicy( "ALLOW_WRITE" );
        repo.save();
        return repoName;
    }

}
