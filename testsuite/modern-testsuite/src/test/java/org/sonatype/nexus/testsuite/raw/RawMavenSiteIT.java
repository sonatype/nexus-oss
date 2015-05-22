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
package org.sonatype.nexus.testsuite.raw;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;

import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.asString;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.status;

/**
 * Tests deployment of a maven site to a raw hosted repository.
 */
public class RawMavenSiteIT
    extends MavenSiteTestSupport
{
  private Repository repository;

  private RawClient client;

  @org.ops4j.pax.exam.Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),
        wrappedBundle(maven("org.apache.maven.shared", "maven-verifier").versionAsInProject()),
        wrappedBundle(maven("org.apache.maven.shared", "maven-shared-utils").versionAsInProject()));
  }

  @Before
  public void createHostedRepository() throws Exception {
    final Configuration configuration = hostedConfig("test-raw-repo");
    repository = createRepository(configuration);
    client = client(repository);
  }

  @Test
  public void deploySimpleSite() throws Exception {
    mvn("testproject", "version", repository.getName(), "clean", "site:site", "site:deploy");

    final HttpResponse index = client.get("index.html");

    assertThat(status(index), is(HttpStatus.OK));
    assertThat(asString(index), containsString("About testproject"));
  }
}
