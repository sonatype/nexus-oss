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

package org.sonatype.nexus.timeline.feeds;

import java.util.Date;

/**
 * A class that encapsulates a Nexus System event, like boot, reconfiguration, etc.
 *
 * @author cstamas
 */
public class SystemEvent
    extends AbstractEvent
{
  public static final String TYPE = "SYSTEM";

  /**
   * System event action: boot
   */
  public static final String ACTION_BOOT = "BOOT";

  /**
   * System event action: configuration
   */
  public static final String ACTION_CONFIG = "CONFIG";

  /**
   * System event action: repository local status changes
   */
  public static final String ACTION_REPO_LSTATUS_CHANGES = "REPO_LSTATUS_CHANGES";

  /**
   * System event action: repository proxy status auto change
   */
  public static final String ACTION_REPO_PSTATUS_CHANGES = "REPO_PSTATUS_CHANGES";

  /**
   * System event action: repository proxy status auto change
   */
  public static final String ACTION_REPO_PSTATUS_AUTO_CHANGES = "REPO_PSTATUS_AUTO_CHANGES";

  public SystemEvent(final Date eventDate, final String action, final String message) {
    super(eventDate, action, message);
  }
}
