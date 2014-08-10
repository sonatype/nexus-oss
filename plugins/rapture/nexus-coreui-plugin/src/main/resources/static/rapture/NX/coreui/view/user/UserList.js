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
 * User grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.user.UserList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-user-list',

  store: 'User',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function () {
        return 'user-default';
      }
    },
    {header: 'User Id', dataIndex: 'id'},
    {header: 'Realm', dataIndex: 'realm'},
    {header: 'First name', dataIndex: 'firstName'},
    {header: 'Last name', dataIndex: 'lastName', width: 300},
    {header: 'Email', dataIndex: 'email', width: 250},
    {header: 'Status', dataIndex: 'status'}
  ],

  emptyText: 'No users defined',

  tbar: [
    '<b>Source:</b>',
    { xtype: 'button', text: 'Default', action: 'filter', menu: [] },
    '-',
    { xtype: 'button', text: 'New', glyph: 'xf055@FontAwesome' /* fa-plus-circle */, action: 'new', disabled: true },
    { xtype: 'button', text: 'Delete', glyph: 'xf056@FontAwesome' /* fa-minus-circle */, action: 'delete', disabled: true }
  ],

  plugins: [
    { ptype: 'gridfilterbox', emptyText: 'No user matched criteria "$filter"' }
  ]

});
