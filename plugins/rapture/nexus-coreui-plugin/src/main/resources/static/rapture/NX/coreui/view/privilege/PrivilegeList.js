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
/*global Ext, NX*/

/**
 * Privilege grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.privilege.PrivilegeList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-privilege-list',
  requires: [
    'NX.Icons'
  ],

  store: 'Privilege',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      dataIndex: 'type',
      width: 36,
      iconVariant: 'x16',
      iconNamePrefix: 'privilege-'
    },
    { header: 'Name', dataIndex: 'name', flex: 1 },
    { header: 'Description', dataIndex: 'description', flex: 1 },
    { header: 'Type', dataIndex: 'typeName', flex: 1 },
    { header: 'Target', dataIndex: 'repositoryTargetName', flex: 1 },
    { header: 'Repository', dataIndex: 'repositoryName', flex: 1 },
    { header: 'Method', dataIndex: 'method', flex: 1 }
  ],

  emptyText: 'No privileges defined',

  plugins: [
    { ptype: 'gridfilterbox', emptyText: 'No privilege matched criteria "$filter"' }
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.tbar = [
      { xtype: 'button', text: 'New', glyph: 'xf055@FontAwesome' /* fa-plus-circle */, action: 'new', disabled: true,
        menu: [
          { text: 'Repository Target Privilege', action: 'newrepositorytarget', iconCls: NX.Icons.cls('privilege-target',
              'x16') }
        ]
      },
      { xtype: 'button', text: 'Delete', glyph: 'xf056@FontAwesome' /* fa-minus-circle */, action: 'delete', disabled: true }
    ];

    me.callParent();
  }
});
