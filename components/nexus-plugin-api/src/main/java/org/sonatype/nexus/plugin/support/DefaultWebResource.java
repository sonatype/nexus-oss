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

package org.sonatype.nexus.plugin.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.sonatype.nexus.web.WebResource;

import com.google.common.base.Strings;

/**
 * Default {@link WebResource} implementation.
 */
public class DefaultWebResource
    implements WebResource
{
  private final URL resourceURL;

  private final String path;

  private final boolean shouldCache;

  private final String contentType;

  private final long size;

  private final long lastModified;

  public DefaultWebResource(final URL url, final String path, final String contentType) {
    this(url, path, contentType, true);
  }

  public DefaultWebResource(final URL url, final String path, final String contentType, final boolean shouldCache) {
    this.resourceURL = url;
    this.path = path;
    this.shouldCache = shouldCache;
    try {
      final URLConnection urlConnection = resourceURL.openConnection();
      try (final InputStream is = urlConnection.getInputStream()) {
        if (Strings.isNullOrEmpty(contentType)) {
          this.contentType = urlConnection.getContentType();
        }
        else {
          this.contentType = contentType;
        }
        this.size = urlConnection.getContentLengthLong();
        this.lastModified = urlConnection.getLastModified();
      }
    }
    catch (IOException e) {
      throw new IllegalArgumentException("Static resource " + url + " inaccessible", e);
    }
  }

  @Override
  public String getPath() {
    if (path != null) {
      return path;
    }
    else {
      return resourceURL.getPath();
    }
  }

  @Override
  public long getSize() {
    return size;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return resourceURL.openStream();
  }

  @Override
  public boolean shouldCache() {
    return shouldCache;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("DefaultWebResource [");
    if (path != null) {
      builder.append("path=");
      builder.append(path);
      builder.append(", ");
    }
    if (contentType != null) {
      builder.append("contentType=");
      builder.append(contentType);
    }
    builder.append("]");
    return builder.toString();
  }
}
