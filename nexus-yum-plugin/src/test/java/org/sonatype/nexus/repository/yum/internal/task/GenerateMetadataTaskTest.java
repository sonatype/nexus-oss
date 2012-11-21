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
package org.sonatype.nexus.repository.yum.internal.task;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.sonatype.nexus.repository.yum.internal.support.RepositoryTestUtils.PACKAGE_CACHE_DIR;
import static org.sonatype.nexus.repository.yum.internal.support.RepositoryTestUtils.REPODATA_DIR;
import static org.sonatype.nexus.repository.yum.internal.support.RepositoryTestUtils.RPM_BASE_FILE;
import static org.sonatype.nexus.repository.yum.internal.support.RepositoryTestUtils.TARGET_DIR;
import static org.sonatype.nexus.repository.yum.internal.support.RepositoryTestUtils.assertRepository;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

public class GenerateMetadataTaskTest
    extends GenerateMetdataTaskTestSupport
{

    private static final File PATH_NOT_EXISTS = new File( "/data/path/not/exists" );

    private static final String SNAPSHOTS = "snapshots";

    private static final String VERSION = "2.2-2";

    private static final String BASE_URL = "http://localhost:8080/nexus/content/snapshots";

    private static final String BASE_VERSIONED_URL = "http://localhost:8080/nexus/service/local/yum/snapshots/"
        + VERSION;

    @Before
    public void removeTestDirectories()
        throws Exception
    {
        deleteDirectory( PACKAGE_CACHE_DIR );
        deleteDirectory( REPODATA_DIR );
    }

    @Test
    public void shouldCreateRepo()
        throws Exception
    {
        executeJob( createTask(
            RPM_BASE_FILE,
            BASE_URL,
            TARGET_DIR,
            SNAPSHOTS
        ) );
        assertRepository( REPODATA_DIR, "default" );
    }

    @Test
    public void shouldFilterForSpecificVersion()
        throws Exception
    {
        executeJob( createTask(
            RPM_BASE_FILE,
            BASE_URL,
            TARGET_DIR,
            BASE_VERSIONED_URL,
            SNAPSHOTS,
            VERSION,
            null,
            true
        ) );
        assertRepository( REPODATA_DIR, "filtering" );
    }

    @Test( expected = ExecutionException.class )
    public void shouldNotCreateRepoIfPathNotExists()
        throws Exception
    {
        executeJob( createTask(
            PATH_NOT_EXISTS,
            BASE_URL,
            TARGET_DIR,
            SNAPSHOTS
        ) );
    }

}
