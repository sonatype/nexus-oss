/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.maven.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.Generator;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.XmlGenerator;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.ZipGenerator;
import org.sonatype.sisu.goodies.common.ByteSize;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.sonatype.nexus.testsuite.maven.concurrency.generators.Generator.generatedEntity;

/**
 * Maven hosted swarm: multiple clients asking, while others are deploying the SAME artifact from a hosted repository.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenHostedSwarmIT
    extends MavenHotspotITSupport
{
  private static final String RELEASE_XML_ARTIFACT_PATH = "groupId/artifactId/1.0/artifactId-1.0.xml";

  private static final String RELEASE_ZIP_ARTIFACT_PATH = "groupId/artifactId/1.0/artifactId-1.0.zip";

  private void hostedSwarm(final String path,
                           final Generator generator,
                           final Supplier<ByteSize> lengthSupplier) throws Exception
  {
    final Configuration configuration = hostedConfig(testName.getMethodName(), VersionPolicy.RELEASE);
    Repository repository = repositoryManager.create(configuration);
    final Maven2Client repositoryClient = createAdminMaven2Client(repository.getName());

    // create clients
    final List<Callable<HttpResponse>> clients = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      clients.add(new UriPut(repositoryClient, path, generator, lengthSupplier));
    }
    for (int i = 0; i < 15; i++) {
      clients.add(new UriGet(repositoryClient, path));
    }

    // put initial as we don't know will GET or PUT "win"
    final HttpEntity entity = generatedEntity(generator, lengthSupplier.get());
    HttpResponse response = repositoryClient.put(path, entity);
    assertThat(response.getStatusLine().getStatusCode(), equalTo(201));

    assertAllHttpResponseIs2xx(performSwarm(clients));
  }

  @Test
  public void smallZip() throws Exception {
    hostedSwarm(RELEASE_ZIP_ARTIFACT_PATH, new ZipGenerator(), Suppliers.ofInstance(ByteSize.kiloBytes(1)));
  }

  @Test
  public void mediumZip() throws Exception {
    hostedSwarm(RELEASE_ZIP_ARTIFACT_PATH, new ZipGenerator(), Suppliers.ofInstance(ByteSize.kiloBytes(100)));
  }

  @Test
  public void largeZip() throws Exception {
    hostedSwarm(RELEASE_ZIP_ARTIFACT_PATH, new ZipGenerator(), Suppliers.ofInstance(ByteSize.megaBytes(5)));
  }

  @Test
  public void smallXml() throws Exception {
    hostedSwarm(RELEASE_XML_ARTIFACT_PATH, new XmlGenerator(), Suppliers.ofInstance(ByteSize.kiloBytes(1)));
  }

  @Test
  public void mediumXml() throws Exception {
    hostedSwarm(RELEASE_XML_ARTIFACT_PATH, new XmlGenerator(), Suppliers.ofInstance(ByteSize.kiloBytes(10)));
  }

  @Test
  public void largeXml() throws Exception {
    hostedSwarm(RELEASE_XML_ARTIFACT_PATH, new XmlGenerator(), Suppliers.ofInstance(ByteSize.kiloBytes(500)));
  }
}
