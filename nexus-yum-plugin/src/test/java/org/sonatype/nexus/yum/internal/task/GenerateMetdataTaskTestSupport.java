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
package org.sonatype.nexus.yum.internal.task;

import java.io.File;
import javax.inject.Inject;

import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.yum.YumRepository;
import org.sonatype.nexus.yum.internal.support.YumNexusTestSupport;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

public abstract class GenerateMetdataTaskTestSupport
    extends YumNexusTestSupport
{

    protected static final String NO_REPO_URL = null;

    protected static final String NO_VERSION = null;

    protected static final String NO_ADDED_FILE = null;

    protected static final boolean SINGLE_RPM_PER_DIRECTORY = true;

    @Inject
    protected NexusScheduler nexusScheduler;

    protected YumRepository executeJob( final GenerateMetadataTask task )
        throws Exception
    {
        final ScheduledTask<YumRepository> scheduledTask = nexusScheduler.submit( GenerateMetadataTask.ID, task );
        return scheduledTask.get();
    }

    protected GenerateMetadataTask createTask( File rpmDir, String rpmUrl, File repoDir, String repoUrl,
                                               String repositoryId, String version, String addedFile,
                                               boolean singleRpmPerDirectory )
        throws Exception
    {
        GenerateMetadataTask yumTask = (GenerateMetadataTask) lookup( SchedulerTask.class, GenerateMetadataTask.ID );

        yumTask.setRepositoryId( repositoryId );
        yumTask.setRepoDir( repoDir );
        yumTask.setRepoUrl( repoUrl );
        yumTask.setRpmDir( rpmDir.getAbsolutePath() );
        yumTask.setRpmUrl( rpmUrl );
        yumTask.setVersion( version );
        yumTask.setAddedFiles( addedFile );
        yumTask.setSingleRpmPerDirectory( singleRpmPerDirectory );

        return yumTask;
    }

    protected GenerateMetadataTask createTask( File rpmDir, String rpmUrl, File repoDir, String id )
        throws Exception
    {
        return createTask(
            rpmDir, rpmUrl, repoDir, NO_REPO_URL, id, NO_VERSION, NO_ADDED_FILE, SINGLE_RPM_PER_DIRECTORY
        );
    }
}
