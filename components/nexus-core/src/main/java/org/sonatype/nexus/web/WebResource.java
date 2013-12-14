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

package org.sonatype.nexus.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.annotation.Nullable;

/**
 * A resource to be exposed via web (http/https) protocols.
 *
 * @since 2.8
 */
public interface WebResource
{
  /**
   * The path where the resource is mounted under the servlet-context.
   */
  String getPath();

  /**
   * The content-type of the resource, or null if unknown.
   */
  @Nullable
  String getContentType();

  /**
   * The size of the content, or -1 if unknown.
   *
   * @see URLConnection#getContentLengthLong()
   */
  long getSize();

  /**
   * The last modified time, or 0 if unknown.
   *
   * @see URLConnection#getLastModified()
   */
  long getLastModified();

  /**
   * True if the resource should be cached.
   */
  boolean shouldCache();

  /**
   * Resource content stream.
   */
  InputStream getInputStream() throws IOException;
}
