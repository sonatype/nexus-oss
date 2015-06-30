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
package org.sonatype.nexus.testsuite.nuget;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.Nonnull;

import com.sonatype.nexus.repository.nuget.internal.proxy.NugetProxyRecipe;

import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.io.DirSupport;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.testsuite.nuget.dispatch.FineGrainedDispatch;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.Content;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.eclipse.jetty.http.PathMap;
import org.junit.After;
import org.junit.Before;

import static org.sonatype.tests.http.server.fluent.Behaviours.error;
import static org.sonatype.tests.http.server.fluent.Behaviours.file;

/*
* Support for Nuget proxy ITs
 */
public abstract class NugetProxyITSupport
    extends NugetITSupport
{

  protected static final String REMOTE_NUGET_REPO_PATH = "nuget";

  protected Server proxyServer;

  protected FineGrainedDispatch dispatch;

  private File tempDirectory;

  @Nonnull
  protected Configuration proxyConfig(final String name, final String remoteUrl) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(NugetProxyRecipe.NAME);
    config.setOnline(true);

    final NestedAttributesMap proxy = config.attributes("proxy");
    proxy.set("remoteUrl", remoteUrl);
    proxy.set("artifactMaxAge", 5);

    NestedAttributesMap storage = config.attributes("storage");
    storage.set("blobStoreName", BlobStoreManager.DEFAULT_BLOBSTORE_NAME);

    return config;
  }

  @Before
  public void startProxyServer()
      throws Exception
  {
    PathMap.setPathSpecSeparators(":");

    proxyServer = Server.withPort(0)
        .serve("/*").withBehaviours(error(200))
        .start();

    dispatch = new FineGrainedDispatch(proxyServer, REMOTE_NUGET_REPO_PATH + "/*");
  }

  @After
  public void stopProxyServer()
      throws Exception
  {
    if (proxyServer != null) {
      proxyServer.stop();
    }
  }

  @Before
  public void initTempDirectory() throws Exception {
    String subdir = Integer.toString(new Random().nextInt(50000));
    tempDirectory = resolveBaseFile("target/proxy-content/" + subdir).getAbsoluteFile();
    log.info("Writing temp files to {}", tempDirectory);

    DirSupport.mkdir(tempDirectory.toPath());
  }

  protected File tempFile(final String filename) {
    return new File(tempDirectory, filename).getAbsoluteFile();
  }

  /**
   * Takes a test file representing a nuget XML response of some sort, and adapts it to the specific URL of the test
   * proxy server.
   */
  @Nonnull
  protected Content remoteFeed(final String path) throws Exception {
    return file(createNugetXml(resolveTestFile(path)));
  }

  /**
   * Takes a template file and customizes its content for the current running instance of the proxy server.
   */
  protected File createNugetXml(final File original) throws Exception {

    String originalContent = Files.toString(original, Charsets.UTF_8);

    Map<String, String> replacements = new HashMap<>();

    replacements.put("${host}", proxyServer.getUrl().getHost());
    replacements.put("${port}", Integer.toString(proxyServer.getPort()));
    replacements.put("${repository}", REMOTE_NUGET_REPO_PATH);

    String s = applyReplacements(replacements, originalContent);

    final File replaceFile = tempFile(original.getName());
    Files.write(s, replaceFile, Charsets.UTF_8);

    return replaceFile;
  }

  private String applyReplacements(final Map<String, String> replacements, String s) {
    for (Entry<String, String> replacement : replacements.entrySet()) {
      s = s.replace(replacement.getKey(), replacement.getValue());
    }
    return s;
  }


}
