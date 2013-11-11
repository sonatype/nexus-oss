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

package org.sonatype.nexus.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.publisher.SystemPropertiesEntryPublisher;
import org.sonatype.appcontext.source.PropertiesEntrySource;
import org.sonatype.appcontext.source.StaticEntrySource;

import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nexus bootstrap configuration.
 *
 * @since 2.8
 */
public class Configuration
{
  private static final Logger log = LoggerFactory.getLogger(Configuration.class);

  private static final String BUNDLEBASEDIR_KEY = "bundleBasedir";

  private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

  private static final String NEXUS_WORK = "nexus-work";

  private final PropertyMap properties;

  public Configuration() {
    this.properties = new PropertyMap();
  }

  public void load() throws Exception {
    File cwd = new File(".").getCanonicalFile();
    log.info("Current directory: {}", cwd);

    // create app context request, with ID "nexus", without parent, and due to NEXUS-4520 add "plexus" alias too
    final AppContextRequest request = Factory.getDefaultRequest("nexus", null, Arrays.asList("plexus"));

    // Kill the default logging publisher that is installed
    request.getPublishers().clear();

    // NOTE: sources list is "ascending by importance", 1st elem in list is "weakest" and last elem in list is
    // "strongest" (overrides). Factory already created us some sources, so we are just adding to that list without
    // disturbing the order of the list (we add to list head and tail)

    // Add the defaults as least important, is mandatory to be present
    addProperties(request, "defaults", "default.properties", true);

    // NOTE: These are loaded as resources, and its expected that <install>/conf is included in the classpath

    // Add the nexus.properties, is mandatory to be present
    addProperties(request, "nexus", "/nexus.properties", true);

    // Add the nexus-test.properties, not mandatory to be present
    addProperties(request, "nexus-test", "/nexus-test.properties", false);

    // Ultimate source of "bundleBasedir" (hence, is added as last in sources list)
    // Now, that will be always overridden by value got from cwd and that seems correct to me
    request.getSources().add(new StaticEntrySource(BUNDLEBASEDIR_KEY, cwd.getAbsolutePath()));

    // we need to publish all entries coming from loaded properties
    request.getPublishers().add(new SystemPropertiesEntryPublisher(true));

    // when context created, the context is built and all publisher were invoked (system props set for example)
    AppContext context = Factory.create(request);

    // Make some entries canonical
    canonicalizeEntry(context, NEXUS_WORK);

    for (Map.Entry<String, Object> entry : context.flatten().entrySet()) {
      properties.put(entry.getKey(), String.valueOf(entry.getValue()));
    }

    ensureTmpDirSanity();

    log.info("Properties:");
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      log.info("  {}='{}'", entry.getKey(), entry.getValue());
    }
  }

  // TODO: expose, set/get, export?
  // TODO: installDir, workDir, tmpDir helpers?

  public Map<String, String> getProperties() {
    return properties;
  }

  private void canonicalizeEntry(final AppContext context, final String key) throws IOException {
    if (!context.containsKey(key)) {
      log.warn("Unable to canonicalize missing entry: {}, key");
      return;
    }
    String value = String.valueOf(context.get(key));
    File file = new File(value).getCanonicalFile();
    value = file.getAbsolutePath();
    context.put(key, value);
  }

  private Properties loadProperties(final Resource resource) throws IOException {
    assert resource != null;
    log.debug("Loading properties from: {}", resource);
    Properties props = new Properties();
    try (InputStream input = resource.getInputStream()) {
      props.load(input);
      if (log.isDebugEnabled()) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
          log.debug("  {}='{}'", entry.getKey(), entry.getValue());
        }
      }
    }
    return props;
  }

  private URL getResource(final String name) {
    // Now that Launcher is extend-able we'll need to load resources from common package
    return Launcher.class.getResource(name);
  }

  private Properties loadProperties(final String resource, final boolean required) throws IOException {
    URL url = getResource(resource);
    if (url == null) {
      if (required) {
        log.error("Missing resource: {}", resource);
        throw new IOException("Missing resource: " + resource);
      }
      else {
        log.debug("Missing optional resource: {}", resource);
      }
      return null;
    }
    else {
      return loadProperties(Resource.newResource(url));
    }
  }

  private void addProperties(final AppContextRequest request,
                             final String name,
                             final String resource,
                             final boolean required)
      throws IOException
  {
    Properties props = loadProperties(resource, required);
    if (props != null) {
      request.getSources().add(new PropertiesEntrySource(name, props));
    }
  }


  private void ensureTmpDirSanity() throws IOException {
    // Make sure that java.io.tmpdir points to a real directory
    String tmp = properties.get(JAVA_IO_TMPDIR, System.getProperty(JAVA_IO_TMPDIR, "tmp"));
    File dir = new File(tmp).getCanonicalFile();
    log.info("Temp directory: {}", dir);

    if (!dir.exists()) {
      Files.createDirectories(dir.toPath());
      log.debug("Created tmp dir: {}", dir);
    }
    else if (!dir.isDirectory()) {
      log.warn("Tmp dir is configured to a location which is not a directory: {}", dir);
    }

    // Ensure we can actually create a new tmp file
    File file = File.createTempFile("nexus-launcher", ".tmp");
    file.createNewFile();
    file.delete();

    properties.put(JAVA_IO_TMPDIR, dir.getAbsolutePath());
  }
}
