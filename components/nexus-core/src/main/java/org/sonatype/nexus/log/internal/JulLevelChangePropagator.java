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

package org.sonatype.nexus.log.internal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.JULHelper;

/**
 * Adapter to standard {@link ch.qos.logback.classic.jul.LevelChangePropagator}
 * to work around bug with null levels.
 *
 * @since 2.7
 */
public class JulLevelChangePropagator
    extends ch.qos.logback.classic.jul.LevelChangePropagator
{
  public JulLevelChangePropagator() {
    // nop
  }

  public JulLevelChangePropagator(final LoggerContext context) {
    setContext(context);
  }

  @Override
  public boolean isResetResistant() {
    return true;
  }

  @Override
  public void onLevelChange(final Logger logger, final Level level) {
    if (level != null) {
      super.onLevelChange(logger, level);
    }
    else {
      java.util.logging.Logger julLogger = JULHelper.asJULLogger(logger);
      julLogger.setLevel(null);
    }
  }
}
