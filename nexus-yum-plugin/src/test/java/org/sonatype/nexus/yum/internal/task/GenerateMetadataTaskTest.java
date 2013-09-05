/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
import java.util.concurrent.ExecutionException;

import org.sonatype.scheduling.SchedulerTask;

import org.junit.Test;

public class GenerateMetadataTaskTest
    extends GenerateMetadataTaskTestSupport
{

  private static final File PATH_NOT_EXISTS = new File("/data/path/not/exists");

  private static final String SNAPSHOTS = "snapshots";

  private static final String VERSION = "2.2-2";

  private static final String BASE_URL = "http://localhost:8080/nexus/content/snapshots";

  private static final String BASE_VERSIONED_URL = "http://localhost:8080/nexus/service/local/yum/snapshots/"
      + VERSION;

  private static final String NO_REPO_URL = null;

  private static final String NO_VERSION = null;

  private static final String NO_ADDED_FILE = null;

  private static final boolean SINGLE_RPM_PER_DIRECTORY = true;

  @Test
  public void shouldFilterForSpecificVersion()
      throws Exception
  {
    executeJob(createTask(
        rpmsDir(),
        BASE_URL,
        repoData(),
        BASE_VERSIONED_URL,
        SNAPSHOTS,
        VERSION,
        NO_ADDED_FILE,
        true
    ));
    assertThatYumMetadataAreTheSame(repoData(), "filtering");
  }

  @Test(expected = ExecutionException.class)
  public void shouldNotCreateRepoIfPathNotExists()
      throws Exception
  {
    executeJob(createTask(
        PATH_NOT_EXISTS,
        BASE_URL,
        repoData(),
        SNAPSHOTS
    ));
  }

  protected GenerateMetadataTask createTask(final File rpmDir,
                                            final String rpmUrl,
                                            final File repoDir,
                                            final String repoUrl,
                                            final String repositoryId,
                                            final String version,
                                            final String addedFile,
                                            final boolean singleRpmPerDirectory)
      throws Exception
  {
    GenerateMetadataTask yumTask = (GenerateMetadataTask) lookup(SchedulerTask.class, GenerateMetadataTask.ID);

    yumTask.setRepositoryId(repositoryId);
    yumTask.setRepoDir(repoDir);
    yumTask.setRepoUrl(repoUrl);
    yumTask.setRpmDir(rpmDir.getAbsolutePath());
    yumTask.setRpmUrl(rpmUrl);
    yumTask.setVersion(version);
    yumTask.setAddedFiles(addedFile);
    yumTask.setSingleRpmPerDirectory(singleRpmPerDirectory);

    return yumTask;
  }

  protected GenerateMetadataTask createTask(final File rpmDir,
                                            final String rpmUrl,
                                            final File repoDir,
                                            final String id)
      throws Exception
  {
    return createTask(
        rpmDir, rpmUrl, repoDir, NO_REPO_URL, id, NO_VERSION, NO_ADDED_FILE, SINGLE_RPM_PER_DIRECTORY
    );
  }

}
