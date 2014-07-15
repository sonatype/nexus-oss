/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.testsuite.ssl;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.sonatype.nexus.ssl.client.Certificates;
import com.sonatype.nexus.ssl.client.TrustStore;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.capabilities.client.Capabilities;
import org.sonatype.nexus.client.core.subsystem.ServerConfiguration;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runners.Parameterized;

import static org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy.Strategy.EACH_TEST;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.goodies.common.Varargs.$;

/**
 * Support for ssl integration tests.
 *
 * @since 1.0
 */
@NexusStartAndStopStrategy(EACH_TEST)
public class SSLITSupport
    extends NexusRunningParametrizedITSupport
{

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return firstAvailableTestParameters(
        systemTestParameters(),
        testParameters(
            $("${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip")
        )
    ).load();
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  public SSLITSupport(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Override
  protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
    return configuration
        .setLogLevel("org.sonatype.siesta", "TRACE")
        .setLogLevel("com.sonatype.nexus.ssl", "DEBUG")
        .setLogLevel("org.sonatype.sisu.goodies.ssl", "DEBUG")
        .setLogLevel("org.sonatype.licensing", "TRACE")
        .addPlugins(
            artifactResolver().resolvePluginFromDependencyManagement(
                "org.sonatype.nexus.plugins", "nexus-ssl-plugin"
            )
        );
  }

  public static String uniqueName(final String prefix) {
    return prefix + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
  }

  public Repositories repositories() {
    return client().getSubsystem(Repositories.class);
  }

  public Content content() {
    return client().getSubsystem(Content.class);
  }

  public TrustStore truststore() {
    return client().getSubsystem(TrustStore.class);
  }

  public Certificates certificates() {
    return client().getSubsystem(Certificates.class);
  }

  public Capabilities capabilities() {
    return client().getSubsystem(Capabilities.class);
  }

  public ServerConfiguration config() {
    return client().getSubsystem(ServerConfiguration.class);
  }

}
