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
package org.sonatype.nexus.plugins.plugin.console.ui;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.extdirect.DirectComponent;
import org.sonatype.nexus.extdirect.DirectComponentSupport;
import org.sonatype.nexus.plugins.plugin.console.PluginConsoleManager;
import org.sonatype.nexus.plugins.plugin.console.model.PluginInfo;

import com.softwarementors.extjs.djn.config.annotations.DirectAction;
import com.softwarementors.extjs.djn.config.annotations.DirectMethod;

/**
 * Plugin Console {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = "pluginconsole_PluginConsole")
public class PluginConsoleComponent
    extends DirectComponentSupport
{

  private final PluginConsoleManager pluginConsoleManager;

  @Inject
  public PluginConsoleComponent(final PluginConsoleManager pluginConsoleManager) {
    this.pluginConsoleManager = pluginConsoleManager;
  }

  @DirectMethod
  public List<PluginInfo> read() {
    return pluginConsoleManager.listPluginInfo();
  }

}
