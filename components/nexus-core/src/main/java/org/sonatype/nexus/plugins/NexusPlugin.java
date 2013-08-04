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

package org.sonatype.nexus.plugins;

import javax.inject.Singleton;

import org.sonatype.plugin.ExtensionPoint;

/**
 * Extension point for Nexus "plugin entry point". A Nexus plugin bundle does not have to contain this, but if it needs
 * some special lifecycle tasks, then it should.
 *
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface NexusPlugin
{
  /**
   * Called once during plugin lifecycle, on plugin installation. The plugin itself may perform some actions to
   * finish
   * install (actions that are plugin specific).
   */
  void install(PluginContext context);

  /**
   * Called when Nexus environment activates the plugin. Called multiple times.
   */
  void init(PluginContext context);

  /**
   * Called only once during plugin lifecycle, on plugin deinstallation. Plugin should undo/clean-up changes made in
   * install() method.
   */
  void uninstall(PluginContext context);
}
