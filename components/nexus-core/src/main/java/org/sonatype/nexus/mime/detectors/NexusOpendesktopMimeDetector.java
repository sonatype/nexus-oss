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

package org.sonatype.nexus.mime.detectors;

import eu.medsea.mimeutil.detector.OpendesktopMimeDetector;

/**
 * Nexus specific {@link OpendesktopMimeDetector}. It detects file override and will use the override or the default
 * accordingly.
 *
 * @author cstamas
 * @since 2.7.0
 */
public class NexusOpendesktopMimeDetector
    extends OpendesktopMimeDetector
{
  public static final String DEFAULT_MIME_CACHE_PATH = "/usr/share/mime/mime.cache";

  public static String mimeCachePath;

  public NexusOpendesktopMimeDetector() {
    super(mimeCachePath);
  }
}
