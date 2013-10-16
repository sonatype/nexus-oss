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

package org.sonatype.nexus.testsuite.repository.nexus5944;

import java.util.Collection;

import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;
import org.sonatype.nexus.testsuite.TwinNexusITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.goodies.common.Varargs.$;

/**
 * IT related to issue NEXUS-5944: Remote is Nexus, but browsing of proxied repo
 * is not allowed. Local Nexus' proxy repo should not auto block, it should work.
 */
@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_TEST)
public class Nexus5944BrowsingNotAllowedIT
    extends TwinNexusITSupport
{
  private static final Logger LOG = LoggerFactory.getLogger(Nexus5944BrowsingNotAllowedIT.class);

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return firstAvailableTestParameters(
        systemTestParameters(),
        testParameters($("${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip:bundle"))
    ).load();
  }

  public Nexus5944BrowsingNotAllowedIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Test
  public void remoteBrowsingNotAllowed() {
    // disable browsing on remote/central
    remoteRepositories().get(MavenProxyRepository.class, "central").disableBrowsing().save();
    // create local/central proxying remote/central
    localRepositories().create(MavenProxyRepository.class, "remote-central-proxy").asProxyOf(
        remoteRepositories().get(MavenProxyRepository.class, "central").contentUri()).withRepoPolicy("RELEASE").save();
    waitForRemoteToSettleDown();
    waitForLocalToSettleDown();

    assertThat("remote-central-proxy should not be autoblocked",
        !localRepositories().get(MavenProxyRepository.class, "remote-central-proxy").status().isAutoBlocked());

  }

}
