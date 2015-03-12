/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.index;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.AbstractApplicationStatusSource;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.NxApplication;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.events.EventSubscriberHost;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.TaskScheduler;
import org.sonatype.nexus.security.subject.FakeAlmightySubject;
import org.sonatype.nexus.test.NexusTestSupport;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.lucene.search.Query;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.SearchType;
import org.apache.maven.index.context.IndexingContext;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.sisu.plexus.PlexusSpaceModule;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.URLClassSpace;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractIndexerManagerTest
    extends NexusTestSupport
{
  protected DefaultIndexerManager indexerManager;

  protected RepositoryRegistry repositoryRegistry;

  protected M2Repository central;

  protected MavenRepository releases;

  protected MavenRepository snapshots;

  protected MavenRepository apacheSnapshots;

  @Override
  protected void customizeModules(final List<Module> modules) {
    super.customizeModules(modules);
    modules.add(new AbstractModule()
    {
      @Override
      protected void configure() {
        ThreadContext.bind(FakeAlmightySubject.forUserId("disabled-security"));
        bind(RealmSecurityManager.class).toInstance(mock(RealmSecurityManager.class));

        ApplicationStatusSource statusSource = mock(AbstractApplicationStatusSource.class);
        when(statusSource.getSystemStatus()).thenReturn(new SystemStatus());
        bind(ApplicationStatusSource.class).toInstance(statusSource);
      }
    });
  }

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();

    lookup(NxApplication.class).start();

    repositoryRegistry = lookup(RepositoryRegistry.class);
    indexerManager = (DefaultIndexerManager) lookup(IndexerManager.class);

    central = (M2Repository) repositoryRegistry.getRepository("central");
    releases = (MavenRepository) repositoryRegistry.getRepository("releases");
    snapshots = (MavenRepository) repositoryRegistry.getRepository("snapshots");
    apacheSnapshots = (MavenRepository) repositoryRegistry.getRepository("apache-snapshots");
  }

  @Override
  protected void tearDown()
      throws Exception
  {
    lookup(NxApplication.class).stop();
    super.tearDown();
  }

  protected void searchFor(String groupId, int expected)
      throws IOException
  {
    Query query = indexerManager.constructQuery(MAVEN.GROUP_ID, groupId, SearchType.EXACT);

    IteratorSearchResponse response;

    try {
      response = indexerManager.searchQueryIterator(query, null, null, null, null, false, null);
    }
    catch (NoSuchRepositoryException e) {
      // will not happen since we are not selecting a repo to search
      throw new IOException("Huh?");
    }

    try {
      ArrayList<ArtifactInfo> results = new ArrayList<ArtifactInfo>(response.getTotalHits());

      for (ArtifactInfo hit : response) {
        results.add(hit);
      }

      assertEquals("Query \"" + query + "\" returned wrong results: " + results + "!", expected, results.size());
    }
    finally {
      response.close();
    }
  }

  protected void searchForKeywordNG(String term, int expected)
      throws Exception
  {
    IteratorSearchResponse result =
        indexerManager.searchArtifactIterator(term, null, null, null, null, false, SearchType.SCORED, null);

    try {
      if (expected != result.getTotalHits()) {
        // dump the stuff
        StringBuilder sb = new StringBuilder("Found artifacts:\n");

        for (ArtifactInfo ai : result) {
          sb.append(ai.context).append(" : ").append(ai.toString()).append("\n");
        }

        fail(sb.toString() + "\nUnexpected result set size! We expected " + expected + " but got "
            + result.getTotalHits());
      }
    }
    finally {
      result.close();
    }
  }

  protected void searchFor(String groupId, int expected, String repoId)
      throws IOException, Exception
  {
    Query q = indexerManager.constructQuery(MAVEN.GROUP_ID, groupId, SearchType.EXACT);

    IteratorSearchResponse response = indexerManager.searchQueryIterator(q, repoId, null, null, null, false, null);
    try {
      ArrayList<ArtifactInfo> ais = new ArrayList<ArtifactInfo>(response.getTotalHits());

      for (ArtifactInfo ai : response) {
        ais.add(ai);
      }

      assertEquals(ais.toString(), expected, ais.size());
    }
    finally {
      response.close();
    }
  }

  protected void assertTemporatyContexts(final Repository repo)
      throws Exception
  {
    IndexingContext context =
        ((DefaultIndexerManager) indexerManager).getRepositoryIndexContext(repo.getId());
    File dir = context.getIndexDirectoryFile().getParentFile();

    File[] contextDirs = dir.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name) {
        return name.startsWith(repo.getId() + "-ctx");
      }
    });

    assertEquals(1, contextDirs.length);
  }

  @Override
  protected Module spaceModule() {
    return new PlexusSpaceModule(new URLClassSpace(getClassLoader()), BeanScanning.INDEX);
  }

  protected ApplicationConfiguration nexusConfiguration() {
    return lookup(ApplicationConfiguration.class);
  }

  public void fillInRepo()
      throws Exception
  {
    final File sourceSnapshotsRoot =
        new File(getBasedir(), "src/test/resources/reposes/snapshots").getAbsoluteFile();

    final URL snapshotsRootUrl = new URL(snapshots.getLocalUrl());

    final File snapshotsRoot = new File(snapshotsRootUrl.toURI()).getAbsoluteFile();

    FileUtils.copyDirectory(sourceSnapshotsRoot, snapshotsRoot, HiddenFileFilter.VISIBLE);

    final File sourceReleasesRoot = new File(getBasedir(), "src/test/resources/reposes/releases");

    final URL releaseRootUrl = new URL(releases.getLocalUrl());

    final File releasesRoot = new File(releaseRootUrl.toURI());

    FileUtils.copyDirectory(sourceReleasesRoot, releasesRoot, HiddenFileFilter.VISIBLE);

    final File sourceApacheSnapshotsRoot = new File(getBasedir(), "src/test/resources/reposes/apache-snapshots");

    final URL apacheSnapshotsRootUrl = new URL(apacheSnapshots.getLocalUrl());

    final File apacheSnapshotsRoot = new File(apacheSnapshotsRootUrl.toURI());

    FileUtils.copyDirectory(sourceApacheSnapshotsRoot, apacheSnapshotsRoot, HiddenFileFilter.VISIBLE);

    // This above is possible, since SnapshotRemover is not using index, hence we can manipulate the content
    // "from behind"

    // but clear caches
    ResourceStoreRequest root = new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT);
    snapshots.expireCaches(root);
    releases.expireCaches(root);
    apacheSnapshots.expireCaches(root);

    // make apache-snapshots point to local fake repo
//    ((MavenProxyRepository) apacheSnapshots).setRemoteUrl("http://localhost:" +
//        getTestProperties().get(PROXY_SERVER_PORT) + "/apache-snapshots/");
    ((MavenProxyRepository) apacheSnapshots).setDownloadRemoteIndexes(false);
    nexusConfiguration().saveConfiguration();
    // saving the configuration will trigger an re-indexing task for apache-snapshots
    // wait for this tasks to run
    // waiting was introduced as a fix for NEXUS-4530 but will benefit all subclasses of this test as it will
    // avoid unexpected behavior
    waitForAsyncEventsToCalmDown();
    waitForTasksToStop();
  }

  protected void waitForAsyncEventsToCalmDown()
      throws Exception
  {
    EventSubscriberHost host = lookup(EventSubscriberHost.class);
    while (!host.isCalmPeriod()) {
      Thread.sleep(100);
    }
  }

  protected void waitForTasksToStop()
      throws Exception
  {
    // Give task a chance to start
    Thread.sleep(100);
    Thread.yield();

    TaskScheduler nexusScheduler = lookup(TaskScheduler.class);

    int counter = 0;
    while (nexusScheduler.getRunningTaskCount() > 0) {
      Thread.sleep(100);
      counter++;

      if (counter > 300) {
        System.out.println("TIMEOUT WAITING FOR TASKS TO COMPLETE!!!  Will kill them.");
        nexusScheduler.killAll();
        break;
      }
    }
  }
}