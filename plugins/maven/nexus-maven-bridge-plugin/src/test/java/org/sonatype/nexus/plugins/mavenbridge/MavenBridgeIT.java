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
package org.sonatype.nexus.plugins.mavenbridge;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.sonatype.nexus.AbstractApplicationStatusSource;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.NxApplication;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.plugins.mavenbridge.internal.FileItemModelSource;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.security.subject.FakeAlmightySubject;
import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.tests.http.server.fluent.Behaviours;
import org.sonatype.tests.http.server.fluent.Server;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import junit.framework.Assert;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelSource;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.sisu.plexus.PlexusSpaceModule;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.URLClassSpace;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MavenBridgeIT
    extends NexusTestSupport
{
  protected NexusMavenBridge mavenBridge;

  protected RepositoryRegistry repositoryRegistry;

  private Server server;

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

    mavenBridge = lookup(NexusMavenBridge.class);

    repositoryRegistry = lookup(RepositoryRegistry.class);

    lookup(NxApplication.class).start();

    server = Server.withPort(0).serve("/*").withBehaviours(Behaviours.get(
        new File(getBasedir(), "src/test/resources/test-repo"))).start();

    for (MavenProxyRepository repo : repositoryRegistry.getRepositoriesWithFacet(MavenProxyRepository.class)) {
      repo.setRemoteUrl(server.getUrl().toExternalForm());
      ((AbstractMavenRepository)repo).commitChanges();
    }
  }

  @Override
  protected void tearDown()
      throws Exception
  {
    lookup(NxApplication.class).stop();

    super.tearDown();

    if (server != null) {
      server.stop();
    }
  }

  @Test
  public void testSimple()
      throws Exception
  {
    Assert.assertNotNull(mavenBridge);

    MavenRepository publicRepo = repositoryRegistry.getRepositoryWithFacet("public", MavenGroupRepository.class);

    ResourceStoreRequest req =
        new ResourceStoreRequest("/org/apache/maven/apache-maven/3.0-beta-1/apache-maven-3.0-beta-1.pom");

    StorageFileItem pomItem = (StorageFileItem) publicRepo.retrieveItem(req);

    ModelSource pomSource = new FileItemModelSource(pomItem);

    List<MavenRepository> participants =
        Arrays.asList(pomItem.getRepositoryItemUid().getRepository().adaptToFacet(MavenRepository.class));

    Model model = mavenBridge.buildModel(pomSource, participants);

    // very simple check: if interpolated/effective, license node is present, but if you look
    // at pom above that has no license node. Hence, if present, it means parent found and successfully calculated
    // effective
    Assert.assertTrue(model.getLicenses().size() > 0);

    // for debug
    //MavenXpp3Writer w = new MavenXpp3Writer();
    //w.write( System.out, model );
  }

  @Override
  protected Module spaceModule() {
    return new PlexusSpaceModule(new URLClassSpace(getClassLoader()), BeanScanning.INDEX);
  }
}
