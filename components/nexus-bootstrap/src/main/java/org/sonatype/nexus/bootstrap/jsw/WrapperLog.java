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

package org.sonatype.nexus.bootstrap.jsw;

import org.tanukisoftware.wrapper.WrapperManager;

import static org.tanukisoftware.wrapper.WrapperManager.WRAPPER_LOG_LEVEL_ADVICE;
import static org.tanukisoftware.wrapper.WrapperManager.WRAPPER_LOG_LEVEL_DEBUG;
import static org.tanukisoftware.wrapper.WrapperManager.WRAPPER_LOG_LEVEL_ERROR;
import static org.tanukisoftware.wrapper.WrapperManager.WRAPPER_LOG_LEVEL_FATAL;
import static org.tanukisoftware.wrapper.WrapperManager.WRAPPER_LOG_LEVEL_INFO;
import static org.tanukisoftware.wrapper.WrapperManager.WRAPPER_LOG_LEVEL_STATUS;
import static org.tanukisoftware.wrapper.WrapperManager.WRAPPER_LOG_LEVEL_WARN;

/**
 * Helper to emit messages via the JSW wrapper log stream.
 *
 * @since 2.1
 */
public class WrapperLog
{
  public static void log(final int level, final String message) {
    WrapperManager.log(level, message);
  }

  public static void debug(final String message) {
    log(WRAPPER_LOG_LEVEL_DEBUG, message);
  }

  public static void info(final String message) {
    log(WRAPPER_LOG_LEVEL_INFO, message);
  }

  public static void status(final String message) {
    log(WRAPPER_LOG_LEVEL_STATUS, message);
  }

  public static void warn(final String message) {
    log(WRAPPER_LOG_LEVEL_WARN, message);
  }

  public static void error(final String message) {
    log(WRAPPER_LOG_LEVEL_ERROR, message);
  }

  public static void fatal(final String message) {
    log(WRAPPER_LOG_LEVEL_FATAL, message);
  }

  public static void fatal(final String message, final Throwable cause) {
    log(WRAPPER_LOG_LEVEL_FATAL, message);
    cause.printStackTrace();
  }

  public static void advice(final String message) {
    log(WRAPPER_LOG_LEVEL_ADVICE, message);
  }
}
