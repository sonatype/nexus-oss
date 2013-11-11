/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package org.sonatype.nexus.velocity;

import org.sonatype.sisu.goodies.common.Loggers;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;

/**
 * LogChute backed by SLF4J. Copied from SISU Velocity.
 *
 * @author cstamas
 * @since 2.8.0
 */
public class Slf4jLogChute
    implements LogChute
{
  /**
   * The SLF4J Logger instance to use for logging.
   */
  private final Logger logger;

  /**
   * A flag to redirect Velocity INFO level to DEBUG level. Velocity is kinda chatty in INFO level, that is not
   * always
   * what we need.
   */
  private final boolean redirectVelocityInfoToDebug;

  public Slf4jLogChute() {
    this(Loggers.getLogger(VelocityEngine.class), true);
  }

  public Slf4jLogChute(final Logger logger, final boolean redirectVelocityInfoToDebug) {
    if (logger == null) {
      throw new NullPointerException("Passed in logger is null!");
    }
    this.logger = logger;
    this.redirectVelocityInfoToDebug = redirectVelocityInfoToDebug;
  }

  public Logger getLogger() {
    return logger;
  }

  public void init(final RuntimeServices srv)
      throws Exception
  {
    // nothing
  }

  public boolean isLevelEnabled(final int level) {
    switch (level) {
      case TRACE_ID:
        return logger.isTraceEnabled();
      case DEBUG_ID:
        return logger.isDebugEnabled();
      case INFO_ID:
        return redirectVelocityInfoToDebug ? logger.isDebugEnabled() : logger.isInfoEnabled();
      case WARN_ID:
        return logger.isWarnEnabled();
      case ERROR_ID:
        return logger.isErrorEnabled();
      default:
        // huh?
        return level > INFO_ID;
    }
  }

  public void log(final int level, final String msg) {
    switch (level) {
      case TRACE_ID:
        logger.trace(msg);
        break;
      case DEBUG_ID:
        logger.debug(msg);
        break;
      case INFO_ID:
        if (redirectVelocityInfoToDebug) {
          logger.debug(msg);
        }
        else {
          logger.info(msg);
        }
        break;
      case WARN_ID:
        logger.warn(msg);
        break;
      case ERROR_ID:
        logger.error(msg);
        break;
      default:
        // huh?
        logger.info(msg);
    }
  }

  public void log(final int level, final String msg, final Throwable t) {
    switch (level) {
      case TRACE_ID:
        logger.trace(msg, t);
        break;
      case DEBUG_ID:
        logger.debug(msg, t);
        break;
      case INFO_ID:
        if (redirectVelocityInfoToDebug) {
          logger.debug(msg, t);
        }
        else {
          logger.info(msg, t);
        }
        break;
      case WARN_ID:
        logger.warn(msg, t);
        break;
      case ERROR_ID:
        logger.error(msg, t);
        break;
      default:
        // huh?
        logger.info(msg, t);
    }
  }
}
