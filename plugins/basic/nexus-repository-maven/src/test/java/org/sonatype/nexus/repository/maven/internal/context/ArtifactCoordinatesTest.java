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
package org.sonatype.nexus.repository.maven.internal.context;

import org.sonatype.nexus.repository.maven.internal.context.Coordinates.HashType;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ArtifactCoordinatesTest
    extends TestSupport
{

  @Test
  public void simpleTest() {
    final ArtifactCoordinates coordinates = new ArtifactCoordinates(
        "/org/eclipse/jetty/jetty-io/8.1.16.v20140903/jetty-io-8.1.16.v20140903.pom",
        "jetty-io-8.1.16.v20140903.pom",
        null,
        "org.eclipse.jetty",
        "jetty-io",
        "8.1.16.v20140903",
        "8.1.16.v20140903",
        null,
        "pom",
        null
    );

    assertThat(coordinates.isSubordinate(), is(false));
    ArtifactCoordinates coordinatesSha1 = coordinates.hash(HashType.SHA1);
    assertThat(coordinatesSha1.isSubordinate(), is(true));
    assertThat(coordinatesSha1.subordinateOf(), equalTo(coordinates));
  }
}
