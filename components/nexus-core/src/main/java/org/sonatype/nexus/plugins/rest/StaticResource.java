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

import java.io.IOException;
import java.io.InputStream;

/**
 * This is an abstraction for static resources that the NexusResourceBundle wants to "contribute" to Nexus Web App.
 *
 * @author cstamas
 */
public interface StaticResource
{
  String getPath();

  String getContentType();

  long getSize();

  /**
   * @return the last time the resource was modified or null if the last modified time is unknown
   */
  Long getLastModified();

  InputStream getInputStream()
      throws IOException;
}
