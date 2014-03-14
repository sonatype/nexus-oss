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
Ext.define('NX.coreui.view.system.PluginList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-system-pluginlist',

  store: 'PluginInfo',

  columns: [
    {
      xtype: 'iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function() { return 'plugin-default'; }
    },
    {header: 'Name', dataIndex: 'name', flex: 1},
    {header: 'Version', dataIndex: 'version', flex: 1},
    {header: 'Description', dataIndex: 'description', flex: 1}
  ],

  plugins: [ 'gridfilterbox' ]
});
