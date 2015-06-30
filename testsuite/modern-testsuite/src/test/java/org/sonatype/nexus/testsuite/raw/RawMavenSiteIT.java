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
package org.sonatype.nexus.testsuite.raw;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;

import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.asString;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.status;

/**
 * Tests deployment of a maven site to a raw hosted repository.
 */
@ExamReactorStrategy(PerClass.class)
public class RawMavenSiteIT
    extends MavenSiteTestSupport
{
  private Repository repository;

  private RawClient client;

  @Before
  public void createHostedRepository() throws Exception {
    final Configuration configuration = hostedConfig("test-raw-repo");
    repository = createRepository(configuration);
    client = client(repository);
  }

  @Test
  public void deploySimpleSite() throws Exception {
    log.info("deploySimpleSite() starting");
    mvn("testproject", "version", repository.getName(), "clean", "site:site", "site:deploy");

    final HttpResponse index = client.get("index.html");

    assertThat(status(index), is(HttpStatus.OK));
    assertThat(asString(index), containsString("About testproject"));
  }
}
