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

package org.sonatype.nexus.plugins.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @deprecated pending removal
 */
@Deprecated
public class ExternalStaticResource
    implements StaticResource
{
  private File file;

  private String path;

  private String contentType;

  public ExternalStaticResource(File file, String path, String contentType) {
    this.file = file;
    this.path = path;
    this.contentType = contentType;
  }

  public String getContentType() {
    return contentType;
  }

  public InputStream getInputStream()
      throws IOException
  {
    return new FileInputStream(file);
  }

  public String getPath() {
    return path;
  }

  public long getSize() {
    return file.length();
  }

  public Long getLastModified() {
    return file.lastModified();
  }
}
