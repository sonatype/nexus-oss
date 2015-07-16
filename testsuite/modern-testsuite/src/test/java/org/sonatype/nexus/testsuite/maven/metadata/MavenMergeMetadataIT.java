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

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
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
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Metadata rebuild IT.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenMergeMetadataIT
    extends MavenITSupport
{
  private static final String GROUP_ID = "group";

  private static final String ARTIFACT_ID = "artifact";

  private static final String METADATA_PATH = GROUP_ID.replace('.', '/') + "/" + ARTIFACT_ID + "/maven-metadata.xml";

  private static final MetadataXpp3Writer writer = new MetadataXpp3Writer();

  private static final MetadataXpp3Reader reader = new MetadataXpp3Reader();

  private Repository mr1;

  private Repository mr2;

  private Repository mr3;

  private Repository mr4;

  private Repository mavenGroup;

  @Before
  public void prepare() throws Exception {
    if ((mr1 = repositoryManager.get("mr1")) == null) {
      mr1 = repositoryManager.create(hostedConfig("mr1", VersionPolicy.RELEASE));
    }
    if ((mr2 = repositoryManager.get("mr2")) == null) {
      mr2 = repositoryManager.create(hostedConfig("mr2", VersionPolicy.RELEASE));
    }
    if ((mr3 = repositoryManager.get("mr3")) == null) {
      mr3 = repositoryManager.create(hostedConfig("mr3", VersionPolicy.RELEASE));
    }
    if ((mr4 = repositoryManager.get("mr4")) == null) {
      mr4 = repositoryManager.create(hostedConfig("mr4", VersionPolicy.RELEASE));
    }
    if (repositoryManager.get("mavenGroup") != null) {
      repositoryManager.delete("mavenGroup");
    }
  }

  private void deployMetadataWithVersionTo(String version, Repository repository) throws Exception {
    Metadata metadata = get(repository);
    if (metadata == null) {
      metadata = new Metadata();
      metadata.setModelVersion("1.1.0");
      metadata.setGroupId(GROUP_ID);
      metadata.setArtifactId(ARTIFACT_ID);
      metadata.setVersioning(new Versioning());
    }
    metadata.getVersioning().addVersion(version);
    StringWriter sw = new StringWriter();
    writer.write(sw, metadata);
    final Maven2Client client = createAdminMaven2Client(repository.getName());
    HttpResponse response = client.put(METADATA_PATH, new StringEntity(sw.toString(), ContentType.TEXT_XML));
    assertThat(response.getStatusLine().getStatusCode(), equalTo(201));
  }

  @Nullable
  private Metadata get(final Repository repository) throws Exception {
    final Maven2Client client = createAdminMaven2Client(repository.getName());
    HttpResponse response = client.get(METADATA_PATH);
    if (response.getStatusLine().getStatusCode() == 200) {
      Metadata metadata = reader.read(response.getEntity().getContent());
      EntityUtils.consume(response.getEntity());
      return metadata;
    }
    return null;
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
    deployMetadataWithVersionTo("1.0", mr1);
    deployMetadataWithVersionTo("2.0", mr2);
    deployMetadataWithVersionTo("3.0", mr3);
    deployMetadataWithVersionTo("4.0", mr4);
    mavenGroup = repositoryManager.create(
        groupConfig(
            "mavenGroup",
            mr1.getName(),
            mr2.getName(),
            mr3.getName(),
            mr4.getName()
        )
    );
    Metadata metadata = get(mavenGroup);
    assertThat(metadata, notNullValue());
    assertThat(metadata.getVersioning(), notNullValue());
    assertThat(metadata.getVersioning().getVersions(), hasItems("1.0", "2.0", "3.0", "4.0"));

    mavenGroup = updateGroup(mavenGroup, mr1, mr3);

    metadata = get(mavenGroup);
    assertThat(metadata, notNullValue());
    assertThat(metadata.getVersioning(), notNullValue());
    assertThat(metadata.getVersioning().getVersions(), hasItems("1.0", "3.0"));

    mavenGroup = updateGroup(mavenGroup, mr1, mr2, mr3);

    metadata = get(mavenGroup);
    assertThat(metadata, notNullValue());
    assertThat(metadata.getVersioning(), notNullValue());
    assertThat(metadata.getVersioning().getVersions(), hasItems("1.0", "2.0", "3.0"));
  }

  @Test
  public void mergeMetadataOnChange() throws Exception {
    deployMetadataWithVersionTo("1.0", mr1);
    deployMetadataWithVersionTo("2.0", mr2);
    deployMetadataWithVersionTo("3.0", mr3);
    deployMetadataWithVersionTo("4.0", mr4);
    mavenGroup = repositoryManager.create(
        groupConfig(
            "mavenGroup",
            mr1.getName(),
            mr2.getName(),
            mr3.getName(),
            mr4.getName()
        )
    );

    Metadata metadata = get(mavenGroup);
    assertThat(metadata, notNullValue());
    assertThat(metadata.getVersioning(), notNullValue());
    assertThat(metadata.getVersioning().getVersions(), hasItems("1.0", "2.0", "3.0", "4.0"));

    deployMetadataWithVersionTo("1.1", mr1);

    metadata = get(mavenGroup);
    assertThat(metadata, notNullValue());
    assertThat(metadata.getVersioning(), notNullValue());
    assertThat(metadata.getVersioning().getVersions(), hasItems("1.0", "1.1", "2.0", "3.0", "4.0"));

    deployMetadataWithVersionTo("4.1", mr4);

    metadata = get(mavenGroup);
    assertThat(metadata, notNullValue());
    assertThat(metadata.getVersioning(), notNullValue());
    assertThat(metadata.getVersioning().getVersions(), hasItems("1.1", "2.0", "3.0", "4.0", "4.1"));

    createAdminMaven2Client(mr3.getName()).delete(METADATA_PATH);

    metadata = get(mavenGroup);
    assertThat(metadata, notNullValue());
    assertThat(metadata.getVersioning(), notNullValue());
    assertThat(metadata.getVersioning().getVersions(), hasItems("1.1", "2.0", "4.0", "4.1"));
  }
}
