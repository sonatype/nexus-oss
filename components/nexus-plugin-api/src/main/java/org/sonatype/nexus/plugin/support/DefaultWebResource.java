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

import org.sonatype.nexus.internal.DevModeResources;
import org.sonatype.nexus.web.WebResource;
import org.sonatype.nexus.web.WebResource.CacheControl;

/**
 * Default {@link WebResource} implementation.
 */
public class DefaultWebResource
    implements WebResource, CacheControl
{
  private final URL resourceURL;

  private final String path;

  private final String contentType;

  private final boolean shouldCache;

  private volatile URLConnection urlConnection;

  public DefaultWebResource(final URL url, final String path, final String contentType) {
    URL overrideUrl = DevModeResources.getResourceIfOnFileSystem(path);
    this.resourceURL = overrideUrl != null ? overrideUrl : url;
    this.path = path;
    this.contentType = contentType;
    this.shouldCache = overrideUrl == null;
  }

  protected synchronized boolean checkConnection() {
    if (urlConnection == null) {
      try {
        urlConnection = resourceURL.openConnection();
      }
      catch (IOException e) {
        // ignore it?
        urlConnection = null;
      }
    }

    return urlConnection != null;
  }

  public String getPath() {
    if (path != null) {
      return path;
    }
    else {
      return resourceURL.getPath();
    }
  }

  public long getSize() {
    if (checkConnection()) {
      return urlConnection.getContentLength();
    }
    else {
      return -1;
    }
  }

  public String getContentType() {
    if (contentType != null) {
      return contentType;
    }
    else if (checkConnection()) {
      return urlConnection.getContentType();
    }
    else {
      return null;
    }
  }

  public InputStream getInputStream() throws IOException {
    if (checkConnection()) {
      InputStream is = urlConnection.getInputStream();
      urlConnection = null;
      return is;
    }
    else {
      throw new IOException("Invalid resource: " + resourceURL);
    }
  }

  public Long getLastModified() {
    if (!shouldCache) {
      return System.currentTimeMillis();
    }
    if (checkConnection()) {
      return urlConnection.getLastModified();
    }
    return null;
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
