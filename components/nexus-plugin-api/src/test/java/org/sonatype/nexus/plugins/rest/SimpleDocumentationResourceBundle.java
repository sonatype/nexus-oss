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
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.sonatype.nexus.mime.DefaultMimeSupport;
import org.sonatype.nexus.plugin.support.AbstractDocumentationResourceBundle;

public class SimpleDocumentationResourceBundle
    extends AbstractDocumentationResourceBundle
{
  public SimpleDocumentationResourceBundle() {
    super(new DefaultMimeSupport());
  }

  @Override
  public String getPluginId() {
    return "test";
  }

  @Override
  protected ZipFile getZipFile()
      throws IOException
  {
    final String file = new File(getClass().getResource("/docs.zip").getFile()).getCanonicalPath();
    try {
      return new ZipFile(file);
    }
    catch (ZipException e) {
      throw new IOException(e.getMessage() + ": " + file, e);
    }
  }

  @Override
  public String getDescription() {
    return "Simple Test";
  }

  @Override
  public String getPathPrefix() {
    return "test";
  }
}