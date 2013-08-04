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

package org.sonatype.nexus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.targets.TargetRegistry;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.DirectoryWalkListener;
import org.codehaus.plexus.util.DirectoryWalker;
import org.codehaus.plexus.util.FileUtils;

/**
 * Tests that needs some repo content and are Maven related.
 *
 * @author cstamas
 */
public abstract class AbstractMavenRepoContentTests
    extends NexusAppTestSupport
{
  protected DefaultNexus defaultNexus;

  protected NexusConfiguration nexusConfiguration;

  protected RepositoryRegistry repositoryRegistry;

  protected MavenRepository snapshots;

  protected MavenRepository releases;

  protected MavenRepository apacheSnapshots;

  protected MavenProxyRepository central;

  protected TargetRegistry targetRegistry;

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();

    getLoggerManager().setThresholds(Logger.LEVEL_DEBUG);

    defaultNexus = (DefaultNexus) lookup(Nexus.class);

    nexusConfiguration = lookup(NexusConfiguration.class);

    repositoryRegistry = lookup(RepositoryRegistry.class);

    targetRegistry = lookup(TargetRegistry.class);

    // get a snapshots hosted repo
    snapshots = (MavenRepository) repositoryRegistry.getRepository("snapshots");

    // get a releases hosted repo
    releases = (MavenRepository) repositoryRegistry.getRepository("releases");

    apacheSnapshots = (MavenRepository) repositoryRegistry.getRepository("apache-snapshots");

    central = (MavenProxyRepository) repositoryRegistry.getRepository("central");
  }

  @Override
  protected void tearDown()
      throws Exception
  {
    super.tearDown();
  }

  protected boolean loadConfigurationAtSetUp() {
    return false;
  }

  public Nexus getNexus() {
    return defaultNexus;
  }

  public void fillInRepo()
      throws Exception
  {
    final File sourceSnapshotsRoot =
        new File(getBasedir(), "src/test/resources/reposes/snapshots").getAbsoluteFile();

    final URL snapshotsRootUrl = new URL(snapshots.getLocalUrl());

    final File snapshotsRoot = new File(snapshotsRootUrl.toURI()).getAbsoluteFile();

    copyDirectory(sourceSnapshotsRoot, snapshotsRoot);

    final File sourceReleasesRoot = new File(getBasedir(), "src/test/resources/reposes/releases");

    final URL releaseRootUrl = new URL(releases.getLocalUrl());

    final File releasesRoot = new File(releaseRootUrl.toURI());

    copyDirectory(sourceReleasesRoot, releasesRoot);

    final File sourceApacheSnapshotsRoot = new File(getBasedir(), "src/test/resources/reposes/apache-snapshots");

    final URL apacheSnapshotsRootUrl = new URL(apacheSnapshots.getLocalUrl());

    final File apacheSnapshotsRoot = new File(apacheSnapshotsRootUrl.toURI());

    copyDirectory(sourceApacheSnapshotsRoot, apacheSnapshotsRoot);

    // This above is possible, since SnapshotRemover is not using index, hence we can manipulate the content
    // "from behind"

    // but clear caches
    ResourceStoreRequest root = new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT);
    snapshots.expireCaches(root);
    releases.expireCaches(root);
    apacheSnapshots.expireCaches(root);

    // make apache-snapshots point to local fake repo
    ((MavenProxyRepository) apacheSnapshots).setRemoteUrl("http://localhost:" +
        super.getContainer().getContext().get(PROXY_SERVER_PORT) + "/apache-snapshots/");
    ((MavenProxyRepository) apacheSnapshots).setDownloadRemoteIndexes(false);
    nexusConfiguration.saveConfiguration();
    // saving the configuration will trigger an re-indexing task for apache-snapshots
    // wait for this tasks to run
    // waiting was introduced as a fix for NEXUS-4530 but will benefit all subclasses of this test as it will
    // avoid unexpected behavior
    wairForAsyncEventsToCalmDown();
    waitForTasksToStop();
  }

  protected File retrieveFile(MavenRepository repo, String path)
      throws Exception
  {
    File root = new File(new URL(repo.getLocalUrl()).toURI());

    File result = new File(root, path);

    if (result.exists()) {
      return result;
    }

    throw new FileNotFoundException("File with path '" + path + "' in repository '" + repo.getId()
        + "' does not exist!");
  }

  protected void copyDirectory(final File from, final File to)
      throws IOException
  {
    DirectoryWalker w = new DirectoryWalker();

    w.setBaseDir(from);

    w.addSCMExcludes();

    w.addDirectoryWalkListener(new DirectoryWalkListener()
    {
      public void debug(String message) {
      }

      public void directoryWalkStarting(File basedir) {
      }

      public void directoryWalkStep(int percentage, File file) {
        if (!file.isFile()) {
          return;
        }

        try {
          String path = file.getAbsolutePath().substring(from.getAbsolutePath().length());

          FileUtils.copyFile(file, new File(to, path));
        }
        catch (IOException e) {
          throw new IllegalStateException("Cannot copy dirtree.", e);
        }
      }

      public void directoryWalkFinished() {
      }
    });

    w.scan();
  }

}
