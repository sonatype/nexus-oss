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

package org.sonatype.appcontext.publisher;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A publisher that publishes Application Context to SLF4J Log on first enabled level (will try DEBUG, INFO, WARN in
 * this order). If none of those are enabled, will do nothing.
 *
 * @author cstamas
 */
public class Slf4jLoggerEntryPublisher
    extends AbstractStringDumpingEntryPublisher
    implements EntryPublisher
{
  private final Logger logger;

  public Slf4jLoggerEntryPublisher() {
    this(LoggerFactory.getLogger(AppContext.class));
  }

  public Slf4jLoggerEntryPublisher(final Logger logger) {
    this.logger = Preconditions.checkNotNull(logger);
  }

  public void publishEntries(final AppContext context) {
    final String dump = "\n" + getDumpAsString(context);
    if (logger.isDebugEnabled()) {
      logger.debug(dump);
    }
    else if (logger.isInfoEnabled()) {
      logger.info(dump);
    }
    else if (logger.isWarnEnabled()) {
      logger.warn(dump);
    }
  }
}
