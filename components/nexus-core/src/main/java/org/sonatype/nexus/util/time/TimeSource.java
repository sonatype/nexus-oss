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
package org.sonatype.nexus.util.time;

import org.joda.time.DateTime;

/**
 * An interface for obtaining the current time, used only so that time-dependent code can have its clock dependency
 * injected for unit-test purposes.
 *
 * @since 3.0
 */
public interface TimeSource
{
  /**
   * Get the current time as a {@link DateTime}.
   */
  DateTime currentTime();

  /**
   * Get the current time in milliseconds.
   */
  long currentTimeMillis();
}
