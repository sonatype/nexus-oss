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
package org.sonatype.nexus.logging.ui;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.extdirect.DirectComponent;
import org.sonatype.nexus.extdirect.DirectComponentSupport;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.logging.LoggingPlugin;

import com.softwarementors.extjs.djn.config.annotations.DirectAction;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Log {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = "logging_Log")
public class LogComponent
    extends DirectComponentSupport
{

  private final LogManager logManager;

  @Inject
  public LogComponent(final LogManager logManager) {
    this.logManager = checkNotNull(logManager);
  }

  /**
   * Logs a message at INFO level.
   *
   * @param marker message to be logger (cannot be null/empty)
   * @throws NullPointerException     If marker is null
   * @throws IllegalArgumentException If marker message is null or empty
   */
  @DirectMethod
  @RequiresPermissions(LoggingPlugin.PERMISSION_PREFIX_LOGGERS + "update")
  public void mark(final MarkerXO marker)
      throws Exception
  {
    checkNotNull(marker);
    checkArgument(StringUtils.isNotEmpty(marker.getMessage()));

    // ensure that level for marking logger is enabled
    logManager.setLoggerLevel(log.getName(), LoggerLevel.INFO);

    String asterixes = StringUtils.repeat("*", marker.getMessage().length() + 4);
    log.info("\n"
        + asterixes + "\n"
        + "* " + marker.getMessage() + " *" + "\n"
        + asterixes
    );
  }

}
