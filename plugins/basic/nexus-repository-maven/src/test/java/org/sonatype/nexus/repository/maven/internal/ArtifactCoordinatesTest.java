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
package org.sonatype.nexus.repository.maven.internal;

import org.sonatype.nexus.repository.maven.internal.ArtifactCoordinates.SignatureType;
import org.sonatype.nexus.repository.maven.internal.Coordinates.HashType;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2PathParser;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class ArtifactCoordinatesTest
    extends TestSupport
{
  @Test
  public void pom() {
    final Maven2PathParser pathParser = new Maven2PathParser();

    final String path = "/org/eclipse/jetty/jetty-io/8.1.16.v20140903/jetty-io-8.1.16.v20140903.pom";
    final Coordinates coordinates = pathParser.parsePath(path);
    assertThat(coordinates, is(notNullValue()));
    assertThat(coordinates, instanceOf(ArtifactCoordinates.class));

    final ArtifactCoordinates artifactCoordinates = (ArtifactCoordinates) coordinates;

    assertThat(artifactCoordinates.getPath(), equalTo(path));
    assertThat(artifactCoordinates.getFileName(), equalTo("jetty-io-8.1.16.v20140903.pom"));
    assertThat(artifactCoordinates.getHashType(), nullValue());
    assertThat(artifactCoordinates.getGroupId(), equalTo("org.eclipse.jetty"));
    assertThat(artifactCoordinates.getArtifactId(), equalTo("jetty-io"));
    assertThat(artifactCoordinates.getVersion(), equalTo("8.1.16.v20140903"));
    assertThat(artifactCoordinates.getBaseVersion(), equalTo(artifactCoordinates.getVersion()));
    assertThat(artifactCoordinates.getClassifier(), nullValue());
    assertThat(artifactCoordinates.getExtension(), equalTo("pom"));

    assertThat(coordinates.isSubordinate(), is(false));
    Coordinates coordinatesSha1 = coordinates.hash(HashType.SHA1);
    assertThat(coordinatesSha1.isSubordinate(), is(true));
    assertThat(coordinatesSha1.subordinateOf(), equalTo(coordinates));

    ArtifactCoordinates coordinatesAscSha1 = artifactCoordinates.signature(SignatureType.GPG).hash(HashType.SHA1);
    assertThat(coordinatesAscSha1.getPath(), equalTo(path + ".asc.sha1"));
    assertThat(coordinatesAscSha1.getFileName(), equalTo(coordinates.getFileName() + ".asc.sha1"));
    assertThat(coordinatesAscSha1.getExtension(), equalTo(artifactCoordinates.getExtension() + ".asc.sha1"));
    assertThat(coordinatesAscSha1.isSubordinate(), is(true));
    assertThat(coordinatesAscSha1.subordinateOf().subordinateOf(), equalTo(artifactCoordinates));
    assertThat(coordinatesAscSha1.main(), equalTo(artifactCoordinates));

    ArtifactCoordinates coordinates2 = coordinatesAscSha1.subordinateOf().subordinateOf();
    assertThat(coordinates2.getPath(), equalTo(path));
    assertThat(coordinates2.getFileName(), equalTo(coordinates.getFileName()));
    assertThat(coordinates2.getExtension(), equalTo(artifactCoordinates.getExtension()));
    assertThat(coordinates2.isSubordinate(), is(false));
  }

  @Test
  public void pomSnapshot() {
    final Maven2PathParser pathParser = new Maven2PathParser();

    final String path = "/org/eclipse/jetty/jetty-io/8.1.16-SNAPSHOT/jetty-io-8.1.16-20140903.180000-1.pom";
    final Coordinates coordinates = pathParser.parsePath(path);
    assertThat(coordinates, is(notNullValue()));
    assertThat(coordinates, instanceOf(ArtifactCoordinates.class));

    final ArtifactCoordinates artifactCoordinates = (ArtifactCoordinates) coordinates;

    assertThat(artifactCoordinates.getPath(), equalTo(path));
    assertThat(artifactCoordinates.getFileName(), equalTo("jetty-io-8.1.16-20140903.180000-1.pom"));
    assertThat(artifactCoordinates.getHashType(), nullValue());
    assertThat(artifactCoordinates.getGroupId(), equalTo("org.eclipse.jetty"));
    assertThat(artifactCoordinates.getArtifactId(), equalTo("jetty-io"));
    assertThat(artifactCoordinates.getVersion(), equalTo("8.1.16-20140903.180000-1"));
    assertThat(artifactCoordinates.getBaseVersion(), equalTo("8.1.16-SNAPSHOT"));
    assertThat(artifactCoordinates.getClassifier(), nullValue());
    assertThat(artifactCoordinates.getExtension(), equalTo("pom"));

    assertThat(coordinates.isSubordinate(), is(false));
    Coordinates coordinatesSha1 = coordinates.hash(HashType.SHA1);
    assertThat(coordinatesSha1.isSubordinate(), is(true));
    assertThat(coordinatesSha1.subordinateOf(), equalTo(coordinates));

    ArtifactCoordinates coordinatesAscSha1 = artifactCoordinates.signature(SignatureType.GPG).hash(HashType.SHA1);
    assertThat(coordinatesAscSha1.getPath(), equalTo(path + ".asc.sha1"));
    assertThat(coordinatesAscSha1.getFileName(), equalTo(coordinates.getFileName() + ".asc.sha1"));
    assertThat(coordinatesAscSha1.getExtension(), equalTo(artifactCoordinates.getExtension() + ".asc.sha1"));
    assertThat(coordinatesAscSha1.isSubordinate(), is(true));
    assertThat(coordinatesAscSha1.subordinateOf().subordinateOf(), equalTo(artifactCoordinates));
    assertThat(coordinatesAscSha1.main(), equalTo(artifactCoordinates));

    ArtifactCoordinates coordinates2 = coordinatesAscSha1.subordinateOf().subordinateOf();
    assertThat(coordinates2.getPath(), equalTo(path));
    assertThat(coordinates2.getFileName(), equalTo(coordinates.getFileName()));
    assertThat(coordinates2.getExtension(), equalTo(artifactCoordinates.getExtension()));
    assertThat(coordinates2.isSubordinate(), is(false));
  }
}
