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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.extdirect.DirectComponent;
import org.sonatype.nexus.extdirect.DirectComponentSupport;
import org.sonatype.nexus.logging.LoggingConfigurator;
import org.sonatype.nexus.logging.LoggingPlugin;

import com.google.common.collect.Lists;
import com.softwarementors.extjs.djn.config.annotations.DirectAction;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Loggers {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = "logging_Loggers")
public class LoggersComponent
    extends DirectComponentSupport
{

  private final LoggingConfigurator configurator;

  @Inject
  public LoggersComponent(final LoggingConfigurator configurator) {
    this.configurator = checkNotNull(configurator, "configurator");
  }

  /**
   * Returns a list of configured loggers (never null).
   *
   * @see LoggingConfigurator#getLoggers()
   */
  @DirectMethod
  @RequiresPermissions(LoggingPlugin.PERMISSION_PREFIX_LOGGERS + "read")
  public List<LoggerXO> read() {
    return Lists.newArrayList(configurator.getLoggers());
  }

  /**
   * Sets the level of a logger.
   *
   * @param logger logger name/level (cannot be null)
   * @throws NullPointerException     If logger is null or level is empty
   * @throws IllegalArgumentException If logger name is null or empty
   */
  @DirectMethod
  @RequiresPermissions(LoggingPlugin.PERMISSION_PREFIX_LOGGERS + "update")
  public List<LoggerXO> update(final LoggerXO logger)
      throws Exception
  {
    checkNotNull(logger, "logger");
    checkNotNull(logger.getLevel(), "logger level");
    checkArgument(StringUtils.isNotEmpty(logger.getName()), "name cannot be empty");

    configurator.setLevel(logger.getName(), logger.getLevel());

    return Lists.newArrayList(logger);
  }

  /**
   * Un-sets the level of a logger.
   *
   * @param name logger name
   * @throws NullPointerException If name is null
   */
  @DirectMethod
  @RequiresPermissions(LoggingPlugin.PERMISSION_PREFIX_LOGGERS + "update")
  public List<LoggerXO> destroy(final String name)
      throws Exception
  {
    checkNotNull(name, "name");

    configurator.remove(name);

    return Lists.newArrayList();
  }

  /**
   * Resets all loggers to their default levels.
   */
  @DirectMethod
  @RequiresPermissions(LoggingPlugin.PERMISSION_PREFIX_LOGGERS + "update")
  public void reset()
      throws Exception
  {
    configurator.reset();
  }


}
