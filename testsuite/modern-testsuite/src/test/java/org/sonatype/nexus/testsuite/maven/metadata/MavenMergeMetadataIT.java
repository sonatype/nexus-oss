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
package org.sonatype.nexus.testsuite.maven.metadata;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Set;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.GeneratorBehaviour;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.MavenMetadataGenerator;
import org.sonatype.tests.http.server.fluent.Server;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * Metadata change detection IT.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenMergeMetadataIT
    extends MavenITSupport
{
  private static final String GROUP_ID = "group";

  private static final String ARTIFACT_ID = "artifact";

  private static final String METADATA_PATH = GROUP_ID + "/" + ARTIFACT_ID + "/maven-metadata.xml";

  private static final MetadataXpp3Writer writer = new MetadataXpp3Writer();

  private static final MetadataXpp3Reader reader = new MetadataXpp3Reader();

  private Server upstream;

  private Repository hostedRepository;

  private Repository proxyRepository;

  private Repository mavenGroup;

  @Before
  public void prepare() throws Exception {
    upstream = Server.withPort(0)
        .serve("/" + METADATA_PATH).withBehaviours(
            new GeneratorBehaviour(new MavenMetadataGenerator(defaultMetadata("1.0-proxy")))
        )
        .start();

    hostedRepository = repositoryManager.create(
        hostedConfig("hosted", VersionPolicy.RELEASE)
    );
    proxyRepository = repositoryManager.create(
        proxyConfig("proxy", "http://localhost:" + upstream.getPort() + "/", VersionPolicy.RELEASE)
    );

    deployMetadataWithVersionTo("1.0-hosted", hostedRepository);
    mavenGroup = repositoryManager.create(
        groupConfig(
            "mavenGroup",
            hostedRepository.getName(),
            proxyRepository.getName()
        )
    );
  }

  @After
  public void cleanup() throws Exception {
    repositoryManager.delete(mavenGroup.getName());
    repositoryManager.delete(proxyRepository.getName());
    repositoryManager.delete(hostedRepository.getName());
    upstream.stop();
  }

  private Metadata defaultMetadata() {
    Metadata metadata = new Metadata();
    metadata.setModelVersion("1.1.0");
    metadata.setGroupId(GROUP_ID);
    metadata.setArtifactId(ARTIFACT_ID);
    metadata.setVersioning(new Versioning());
    return metadata;
  }

  private Metadata defaultMetadata(String version) {
    Metadata metadata = defaultMetadata();
    metadata.getVersioning().getVersions().add(version);
    return metadata;
  }

  private void deployMetadataWithVersionTo(String version, Repository repository) throws Exception {
    Metadata metadata = get(repository);
    if (metadata == null) {
      metadata = defaultMetadata(version);
    }
    else if (!metadata.getVersioning().getVersions().contains(version)) {
      metadata.getVersioning().addVersion(version);
    }
    StringWriter sw = new StringWriter();
    writer.write(sw, metadata);
    final Maven2Client client = createAdminMaven2Client(repository.getName());
    HttpResponse response = client.put(METADATA_PATH, new StringEntity(sw.toString(), ContentType.TEXT_XML));
    assertThat(response.getStatusLine().getStatusCode(), equalTo(201));
  }

  private Metadata get(final Repository repository) throws Exception {
    final Maven2Client client = createAdminMaven2Client(repository.getName());
    HttpResponse response = client.get(METADATA_PATH);
    if (response.getStatusLine().getStatusCode() == 200) {
      Metadata metadata = reader.read(response.getEntity().getContent());
      EntityUtils.consume(response.getEntity());
      return metadata;
    }
    else if (response.getStatusLine().getStatusCode() == 404) {
      return defaultMetadata();
    }
    else {
      // this should fail the test
      throw new AssertionError("Unexpected response: " + response.getStatusLine());
    }
  }

  private Repository updateGroup(Repository group, Repository... members) throws Exception {
    Configuration conf = group.getConfiguration();
    final Set<String> memberNames = Sets
        .newHashSet(Iterables.transform(Arrays.asList(members), new Function<Repository, String>()
        {
          @Override
          public String apply(final Repository input) {
            return input.getName();
          }
        }));
    conf.attributes("group").set("memberNames", memberNames);
    return repositoryManager.update(conf);
  }

  @Test
  public void mergeMetadataOnMemberChange() throws Exception {
    Metadata metadata = get(mavenGroup);
    assertThat(metadata.getVersioning().getVersions(), contains("1.0-hosted", "1.0-proxy"));

    mavenGroup = updateGroup(mavenGroup, hostedRepository);

    metadata = get(mavenGroup);
    assertThat(metadata.getVersioning().getVersions(), contains("1.0-hosted"));

    mavenGroup = updateGroup(mavenGroup, proxyRepository);

    metadata = get(mavenGroup);
    assertThat(metadata.getVersioning().getVersions(), contains("1.0-proxy"));

    mavenGroup = updateGroup(mavenGroup, hostedRepository, proxyRepository);

    metadata = get(mavenGroup);
    assertThat(metadata.getVersioning().getVersions(), contains("1.0-hosted", "1.0-proxy"));
  }

  @Test
  public void mergeMetadataOnContentChange() throws Exception {
    Metadata metadata = get(mavenGroup);
    assertThat(metadata.getVersioning().getVersions(), contains("1.0-hosted", "1.0-proxy"));

    deployMetadataWithVersionTo("1.1-hosted", hostedRepository);

    metadata = get(mavenGroup);
    assertThat(metadata.getVersioning().getVersions(), contains("1.0-hosted", "1.0-proxy", "1.1-hosted"));

    createAdminMaven2Client(hostedRepository.getName()).delete(METADATA_PATH);

    metadata = get(mavenGroup);
    assertThat(metadata.getVersioning().getVersions(), contains("1.0-proxy"));
  }
}
