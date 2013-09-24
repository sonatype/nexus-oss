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

package org.sonatype.nexus.util;

import org.codehaus.plexus.PlexusContainer;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;

/**
 * Helper method to handle plexus "transition", to gracefully manage Plexus lifecycle while Core is being transitioned.
 * To be removed once Plexus is completely removed from Core!
 * 
 * @author cstamas
 * @since 2.7.0
 * 
 */
public class PlexusUtils
{
  private static final Logger logger = LoggerFactory.getLogger(PlexusUtils.class);

  private PlexusUtils() {
    // nope
  }

  /**
   * Silently "release" (see {@link PlexusContainer#release(Object)} method) a component, mimics Plexus behavior. It
   * suppresses all exceptions, but logs them.
   */
  public static void release(final Object component) {
    if (component instanceof Startable) {
      try {
        ((Startable) component).stop();
      }
      catch (Exception e) {
        logger.warn("Could not stop component {}", component, e);
      }
    }
    if ((component instanceof Disposable)) {
      try {
        ((Disposable) component).dispose();
      }
      catch (Exception e) {
        logger.warn("Could not dispose component {}", component, e);
      }
    }
  }
}
