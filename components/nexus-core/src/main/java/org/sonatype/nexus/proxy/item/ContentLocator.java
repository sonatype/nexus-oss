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

package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Interface ContentLocator. Implements a strategy to fetch content of an item.
 *
 * @author cstamas
 */
public interface ContentLocator
{
  /**
   * Gets the content. It has to be closed by the caller explicitly.
   *
   * @return the content
   * @throws IOException Signals that an I/O exception has occurred.
   */
  InputStream getContent()
      throws IOException;

  /**
   * Returns the MIME type of the content.
   */
  String getMimeType();

  /**
   * Checks if is reusable.
   *
   * @return true, if is reusable
   */
  boolean isReusable();
}
