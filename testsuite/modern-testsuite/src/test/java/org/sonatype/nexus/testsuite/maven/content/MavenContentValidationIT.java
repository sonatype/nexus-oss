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
package org.sonatype.nexus.testsuite.maven.content;

import javax.inject.Inject;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;
import org.sonatype.nexus.testsuite.repository.FormatClientSupport;
import org.sonatype.tests.http.server.fluent.Behaviours;
import org.sonatype.tests.http.server.fluent.Server;

import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption.OverwriteMode;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

/**
 * Metadata content validation IT.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenContentValidationIT
    extends MavenITSupport
{
  @org.ops4j.pax.exam.Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),
        // TODO: This should be replaced with:
        // mavenBundle("org.sonatype.http-testing-harness", "server-provider").versionAsInProject()
        // ..once the http-testing-harness duplicate import of org.sonatype.tests.http.server.api is corrected.
        wrappedBundle(maven("org.sonatype.http-testing-harness", "server-provider").versionAsInProject())
            .overwriteManifest(OverwriteMode.FULL).instructions("DynamicImport-Package=*")
    );
  }

  @Inject
  private RepositoryManager repositoryManager;

  private Repository mavenCentral;

  private Server upstream;

  @Before
  public void prepare() throws Exception {
    upstream = Server.withPort(0)
        .serve("/abbot/abbot/0.13.0/abbot-0.13.0.pom").withBehaviours(Behaviours.content(
            "<project>\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "  <groupId>abbot</groupId>\n" +
                "  <artifactId>abbot</artifactId>\n" +
                "  <name>Abbot</name>\n" +
                "  <version>0.13.0</version>\n" +
                "</project>", "text/xml"))
        .start();

    Repository repo = repositoryManager.get("maven-central");
    assertThat(repo, notNullValue());
    Configuration mavenCentralConfiguration = repo.getConfiguration();
    mavenCentralConfiguration.attributes("proxy").set("remoteUrl", "http://localhost:" + upstream.getPort() + "/");
    mavenCentral = repositoryManager.update(mavenCentralConfiguration);
  }

  @After
  public void cleanup()
      throws Exception
  {
    if (upstream != null) {
      upstream.stop();
    }
  }

  @Test
  public void fetchPomVia() throws Exception {
    final FormatClientSupport client = new FormatClientSupport(HttpClients.custom().build(), HttpClientContext.create(),
        resolveUrl(nexusUrl, "/repository/" + mavenCentral.getName() + "/").toURI());

    HttpResponse response = client.get("abbot/abbot/0.13.0/abbot-0.13.0.pom");
    assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
    // TODO: nexus log should contain in DEBUG:
    // "Content /abbot/abbot/0.13.0/abbot-0.13.0.pom declared as text/xml, determined as application/xml"
    // Confirming Maven Format override upstream server preference
  }
}
