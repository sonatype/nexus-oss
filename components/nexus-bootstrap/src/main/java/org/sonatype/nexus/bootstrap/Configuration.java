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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
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

  // TODO: expose, set/get, export?

  // TODO: installDir, workDir, tmpDir helpers?

  public Map<String, String> getProperties() {
    return properties;
  }

  public void load() throws Exception {
    File cwd = new File(".").getCanonicalFile();
    log.info("Current directory: {}", cwd);

    // Add the defaults as least important, is mandatory to be present
    addProperties("default.properties", true);

    // Add the nexus.properties, is mandatory to be present
    addProperties("/nexus.properties", true);

    // Add the nexus-test.properties, not mandatory to be present
    addProperties("/nexus-test.properties", false);

    // Always force basedir
    properties.put(BUNDLEBASEDIR_KEY, cwd.getAbsolutePath());

    // Resolve all entries
    interpolate();

    // Make some entries canonical
    canonicalizeEntry(NEXUS_WORK);

    // Ensure tmp directory is sane
    ensureTmpDirSanity();

    log.info("Properties:");
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      log.info("  {}='{}'", entry.getKey(), entry.getValue());
    }

    // Expose as system properties
    System.getProperties().putAll(properties);
  }

  private URL getResource(final String name) {
    return Configuration.class.getResource(name);
  }

  private Properties loadProperties(final Resource resource) throws IOException {
    assert resource != null;
    log.debug("Loading properties from: {}", resource);
    Properties props = new Properties();
    try (InputStream input = resource.getInputStream()) {
      props.load(input);
    }
    if (log.isDebugEnabled()) {
      for (Map.Entry<Object, Object> entry : props.entrySet()) {
        log.debug("  {}='{}'", entry.getKey(), entry.getValue());
      }
    }
    return props;
  }

  private void addProperties(final String resource, final boolean required) throws IOException {
    URL url = getResource(resource);
    if (url == null) {
      if (required) {
        log.error("Missing resource: {}", resource);
        throw new IOException("Missing resource: " + resource);
      }
      else {
        log.debug("Missing optional resource: {}", resource);
        return;
      }
    }

    Properties props = loadProperties(Resource.newResource(url));
    properties.putAll(props);
  }

  private void interpolate() throws Exception {
    Interpolator interpolator = new StringSearchInterpolator();
    interpolator.addValueSource(new MapBasedValueSource(properties));
    interpolator.addValueSource(new MapBasedValueSource(System.getProperties()));
    interpolator.addValueSource(new EnvarBasedValueSource());

    for (Entry<String,String> entry : properties.entrySet()) {
      properties.put(entry.getKey(), interpolator.interpolate(entry.getValue()));
    }
  }

  private void canonicalizeEntry(final String key) throws IOException {
    String value = properties.get(key);
    if (value == null) {
      log.warn("Unable to canonicalize null entry: {}", key);
      return;
    }
    File file = new File(value).getCanonicalFile();
    properties.put(key, file.getAbsolutePath());
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
