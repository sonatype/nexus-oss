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

package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;

import org.sonatype.nexus.plugins.rest.StaticResource;

/**
 * {@link StaticResource} contributed from a Nexus plugin.
 */
public final class PluginStaticResource
    implements StaticResource
{
  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private final URL resourceURL;

  private final String publishedPath;

  private final String contentType;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  public PluginStaticResource(final URL resourceURL, final String publishedPath, final String contentType) {
    this.resourceURL = resourceURL;
    this.publishedPath = publishedPath;
    this.contentType = contentType;
  }

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  public String getPath() {
    return publishedPath;
  }

  public String getContentType() {
    return contentType;
  }

  public long getSize() {
    try {
      return resourceURL.openConnection().getContentLength();
    }
    catch (final Throwable e) // NOPMD
    {
      // default to unknown size
    }
    return -1;
  }

  public InputStream getInputStream()
      throws IOException
  {
    return resourceURL.openStream();
  }

  public Long getLastModified() {
    try {
      final URLConnection urlConn = resourceURL.openConnection();
      if (urlConn instanceof JarURLConnection) {
        final JarEntry jarEntry = ((JarURLConnection) urlConn).getJarEntry();
        if (jarEntry != null) {
          return Long.valueOf(jarEntry.getTime());
        }
        // This is a jar, not an entry in a jar
      }
      return Long.valueOf(urlConn.getLastModified());
    }
    catch (final Throwable e) // NOPMD
    {
      return null; // default to unknown last modified time
    }
  }
}
