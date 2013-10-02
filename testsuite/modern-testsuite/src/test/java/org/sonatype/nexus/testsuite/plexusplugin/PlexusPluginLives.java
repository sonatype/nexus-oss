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

package org.sonatype.nexus.testsuite.plexusplugin;

import java.util.Collection;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

import com.sun.jersey.api.client.ClientResponse;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runners.Parameterized;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy.Strategy.EACH_TEST;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.goodies.common.Varargs.$;

/**
 * Support for PlexusPlugin plugin integration tests.
 *
 * @since 2.7.0
 */
public class PlexusPluginLives
    extends PlexusPluginITSupport
{

  public PlexusPluginLives(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Test
  public void fetchAndValidatePlexusPluginResource() {
    final ClientResponse response =((JerseyNexusClient)client()).serviceResource("plexusplugin").get(ClientResponse.class);

    assertThat(response.getStatus(), equalTo(200));

  }

}
