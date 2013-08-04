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

package org.sonatype.nexus.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Similar to Plexus' AbstractLogEnabled, but using Slf4j and straight-forward stuff! Consider using
 * {@code LoggerFactory.getLogger(getClass() )} directly instead, since unsure about the "value" of this class.
 *
 * @author cstamas
 */
public abstract class AbstractLoggingComponent
{

  private final Logger logger;

  /**
   * Default constructor that creates logger for component upon instantiation.
   */
  protected AbstractLoggingComponent() {
    this.logger = checkNotNull(createLogger());
  }

  /**
   * Creates logger instance to be used with component instance. It might be overridden by subclasses to implement
   * alternative logger naming strategy. By default, this method does the "usual" fluff: {@code
   * LoggerFactory.getLogger(getClass())}.
   *
   * @return The Logger instance to be used by component for logging.
   */
  protected Logger createLogger() {
    return LoggerFactory.getLogger(getClass());
  }

  /**
   * Returns the Logger instance of this component. Never returns {@code null}.
   */
  protected Logger getLogger() {
    return logger;
  }
}
