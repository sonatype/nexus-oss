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

package org.sonatype.nexus.webresources.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.web.WebResource;
import org.sonatype.nexus.web.WebResource.CacheControl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dev mode resource that is based on File.
 *
 * @since 2.8.0
 */
public class DevModeResource
    implements WebResource, CacheControl
{
  private final String path;

  private final String contentType;

  private final File file;

  public DevModeResource(final String path, final String contentType, final File file) {
    this.file = checkNotNull(file);
    this.path = checkNotNull(path);
    this.contentType = checkNotNull(contentType);
  }

  @Override
  public boolean shouldCache() {
    // do not cache dev mode resource
    return false;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public long getSize() {
    return file.length();
  }

  @Override
  public Long getLastModified() {
    return file.lastModified();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(file);
  }

  @Override
  public String toString() {
    return "DevModeResource{" +
        "path='" + path + '\'' +
        ", contentType='" + contentType + '\'' +
        ", file=" + file +
        '}';
  }
}
