/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.componentviews;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import org.sonatype.nexus.componentviews.config.ViewConfig;

/**
 * A simplified abstraction of the request coming from the client.
 *
 * @since 3.0
 */
public interface ViewRequest
{
  enum HttpMethod
  {
    GET, PUT, DELETE;
  }

  /**
   * Provides an immutable version of the view's configuration, useful for shaping queries
   */
  ViewConfig getViewConfig();

  /**
   * A path starting with "/" that excludes the view name.
   */
  String getPath();

  /**
   * The HTTP method invoked (e.g. "GET", "PUT").
   */
  HttpMethod getMethod();

  /**
   * The content type of the content, for PUT and POST requests.
   */
  @Nullable
  String getContentType();

  /**
   * An input stream for the request body.
   */
  InputStream getInputStream() throws IOException;

  /**
   * Adds a value to the request context.
   */
  void setAttribute(String name, Object value);

  /**
   * Retrieves a value from the request context.
   */
  @Nullable
  <T> T getAttribute(String name);
}
