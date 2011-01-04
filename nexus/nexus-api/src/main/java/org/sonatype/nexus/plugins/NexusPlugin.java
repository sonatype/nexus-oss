/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
     * Called once during plugin lifecycle, on plugin installation. The plugin itself may perform some actions to finish
     * install (actions that are plugin specific).
     * 
     * @param context
     */
    void install( PluginContext context );

    /**
     * Called when Nexus environment activates the plugin. Called multiple times.
     * 
     * @param context
     */
    void init( PluginContext context );

    /**
     * Called only once during plugin lifecycle, on plugin deinstallation. Plugin should undo/clean-up changes made in
     * install() method.
     * 
     * @param context
     */
    void uninstall( PluginContext context );
}
