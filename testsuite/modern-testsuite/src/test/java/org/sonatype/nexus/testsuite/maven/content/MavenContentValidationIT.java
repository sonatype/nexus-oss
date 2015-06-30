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
package org.sonatype.nexus.testsuite.maven.content;

import java.io.File;

import javax.inject.Inject;

import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;
import org.sonatype.tests.http.server.fluent.Behaviours;
import org.sonatype.tests.http.server.fluent.Server;

import com.google.common.net.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.contains;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

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
        mavenBundle("org.sonatype.http-testing-harness", "server-provider").versionAsInProject()
    );
  }

  private static final byte[] EMPTY_ZIP = {
      80, 75, 05, 06, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00
  };

  @Inject
  private RepositoryManager repositoryManager;

  @Inject
  private LogManager logManager;

  private Repository mavenCentral;

  private Server upstream;

  private Maven2Client client;

  @Before
  public void setupMavenDebugStorage() {
    logManager.setLoggerLevel("org.sonatype.nexus.repository.storage", LoggerLevel.DEBUG);
  }

  @Before
  public void prepare() throws Exception {
    upstream = Server.withPort(0)
        // Abbot
        .serve("/abbot/abbot/0.13.0/abbot-0.13.0.pom").withBehaviours(Behaviours.content(
            "<project>\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "  <groupId>abbot</groupId>\n" +
                "  <artifactId>abbot</artifactId>\n" +
                "  <name>Abbot</name>\n" +
                "  <version>0.13.0</version>\n" +
                "</project>", "text/xml"))
        .serve("/abbot/abbot/0.13.0/abbot-0.13.0.pom.sha1").withBehaviours(Behaviours.content(
            "deb955763d21951b4ac9fc462c8104c15c86e1ee  /home/projects/maven/repository-staging/to-ibiblio/maven2/abbot/abbot/0.13.0/abbot-0.13.0.pom",
            "text/plain"))
        .serve("/abbot/abbot/0.13.0/abbot-0.13.0.pom.md5").withBehaviours(Behaviours.content(
            "b4a3754bf13bb87475c0dbf45860e049  /home/projects/maven/repository-staging/to-ibiblio/maven2/abbot/abbot/0.13.0/abbot-0.13.0.pom",
            "text/plain"))

        .serve("/abbot/abbot/0.13.0/abbot-0.13.0.jar").withBehaviours(Behaviours.content(
            EMPTY_ZIP, "application/java-archive"))
        .serve("/abbot/abbot/0.13.0/abbot-0.13.0.jar.asc").withBehaviours(Behaviours.content(
            "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "\n" +
                "iQEcBAABAgAGBQJUrEJtAAoJECx7EvKlEeMlJSQH/Rl5iK33P+stWYLoYAbThMDZ\n" +
                "NMfY4Ga3m8o6WqLLUBrzNFEiVlski9XyIgHS+oWxKneL9oTH4zvhhc+3GcDO/Xsk\n" +
                "VX4mB3r7efn91152X47Xg8aqp8syhKuBL1sPIIJNvWrfKJ4FbiZzh5/1ZQJF1wJz\n" +
                "8lATmpcVhi8ji3/TxOOVa2A1CorEAJph5C34FNQzVXE6WH5gQSCnoYFh+xTgvQj/\n" +
                "NiNBvM5/8RWqjP5TPWilDu6pq2qSpMeB6EAMsle4vi5I4Jlcuih4Fjyd3okWVP1w\n" +
                "bmyVZKaOAGqY0eXFc4u6SKfID783CRG0tWqhkB5Vi0LFuJp1TC9rqz7oyqwxjUs=\n" +
                "=RNoG\n" +
                "-----END PGP SIGNATURE-----", "text/plain"))
        .serve("/abbot/abbot/0.13.0/abbot-0.13.0.jar.sha1").withBehaviours(Behaviours.content(
            "596d91e67631b0deb05fb685d8d1b6735f3e4f60", "text/plain"))
        .serve("/abbot/abbot/0.13.0/abbot-0.13.0.jar.md5").withBehaviours(Behaviours.content(
            "28bed71e03d65aeea000333f421f9304", "text/plain"))


        .serve("/abbot/abbot/0.13.0/maven-metadata.xml").withBehaviours(Behaviours.content(
            "<metadata>\n" +
                "<groupId>abbot</groupId>\n" +
                "<artifactId>abbot</artifactId>\n" +
                "<version>0.13.0</version>\n" +
                "</metadata>", "text/xml"))
        .serve("/abbot/abbot/0.13.0/maven-metadata.xml.sha1").withBehaviours(Behaviours.content(
            "018086a59b3ba83fb60cdf583b662d33961d1883  /home/maven/repository-staging/to-ibiblio/maven2/abbot/abbot/0.13.0/maven-metadata.xml",
            "text/plain"))
        .serve("/abbot/abbot/0.13.0/maven-metadata.xml.md5").withBehaviours(Behaviours.content(
            "a07b6cf5ab281542eee0425117a79234  ./abbot/abbot/0.13.0/maven-metadata.xml", "text/plain"))

            // Bad Abbot
        .serve("/badabbot/abbot/0.13.0/abbot-0.13.0.pom").withBehaviours(Behaviours.content(
            EMPTY_ZIP, "text/xml"))
        .serve("/badabbot/abbot/0.13.0/abbot-0.13.0.jar").withBehaviours(Behaviours.content(
            "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <title>Hi there</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "  This is a page\n" +
                "  a simple page\n" +
                "</body>\n" +
                "</html>", "application/java-archive"))
        .serve("/badabbot/abbot/0.13.0/maven-metadata.xml").withBehaviours(Behaviours.content(
            "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <title>Hi there</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "  This is a page\n" +
                "  a simple page\n" +
                "</body>\n" +
                "</html>", "text/xml"))
        .serve("/badabbot/abbot/0.13.0/maven-metadata.xml.sha1").withBehaviours(Behaviours.content(
            "This is not a hash, this is rubbish declared as XML", "text/xml"))
        .start();

    Repository repo = repositoryManager.get("maven-central");
    assertThat(repo, notNullValue());
    Configuration mavenCentralConfiguration = repo.getConfiguration();
    mavenCentralConfiguration.attributes("storage").set("strictContentTypeValidation", Boolean.TRUE.toString());
    mavenCentralConfiguration.attributes("proxy").set("remoteUrl", "http://localhost:" + upstream.getPort() + "/");
    mavenCentral = repositoryManager.update(mavenCentralConfiguration);

    client = new Maven2Client(HttpClients.custom().build(), HttpClientContext.create(),
        resolveUrl(nexusUrl, "/repository/" + mavenCentral.getName() + "/").toURI());
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
  public void positiveTestWithLayoutOverrides() throws Exception {
    // Note: upstream server uses different MIME types (imitates Central), but Maven repo after validation enforces layout types!
    get200("abbot/abbot/0.13.0/abbot-0.13.0.pom", "application/xml");
    get200("abbot/abbot/0.13.0/abbot-0.13.0.pom.sha1", "text/plain");
    get200("abbot/abbot/0.13.0/abbot-0.13.0.pom.md5", "text/plain");
    get200("abbot/abbot/0.13.0/abbot-0.13.0.jar", "application/java-archive");
    get200("abbot/abbot/0.13.0/abbot-0.13.0.jar.asc", "application/pgp-signature");
    get200("abbot/abbot/0.13.0/abbot-0.13.0.jar.sha1", "text/plain");
    get200("abbot/abbot/0.13.0/abbot-0.13.0.jar.md5", "text/plain");
    get200("abbot/abbot/0.13.0/maven-metadata.xml", "application/xml");
    get200("abbot/abbot/0.13.0/maven-metadata.xml.sha1", "text/plain");
    get200("abbot/abbot/0.13.0/maven-metadata.xml.md5", "text/plain");

    // verify log contains traces of format overrides
    final File logfile = logManager.getLogFile("nexus.log");
    assertThat(logfile, exists());
    assertThat(logfile, contains("Content /abbot/abbot/0.13.0/abbot-0.13.0.pom.xml declared as text/xml, determined as application/xml"));
  }

  @Test
  public void negativeTest() throws Exception {
    // Note: upstream server uses different MIME types (imitates Central), but Maven repo after validation enforces layout types!
    get404("badabbot/abbot/0.13.0/abbot-0.13.0.pom", allOf(containsString("Detected content type"), containsString("expected [application/xml")));
    get404("badabbot/abbot/0.13.0/abbot-0.13.0.jar", allOf(containsString("Detected content type"), containsString("expected [application/java-archive")));
    get404("badabbot/abbot/0.13.0/maven-metadata.xml", allOf(containsString("Detected content type"), containsString("expected [application/xml")));
    get404("badabbot/abbot/0.13.0/maven-metadata.xml.sha1", containsString("Not a Maven2 digest"));
  }

  private void get200(final String path, final String contentType) throws Exception {
    HttpResponse response = client.get(path);
    assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
    assertThat(response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue(), equalTo(contentType));
    EntityUtils.consume(response.getEntity());
  }

  private void get404(final String path, final Matcher<String> errorPageMatcher) throws Exception {
    HttpResponse response = client.get(path);
    assertThat(response.getStatusLine().getStatusCode(), equalTo(404));
    String errorPage = EntityUtils.toString(response.getEntity());
    assertThat(errorPage, errorPageMatcher);
  }
}
