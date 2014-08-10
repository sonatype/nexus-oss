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
 * LDAP Server grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-ldapserver-list',

  store: 'LdapServer',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function () {
        return 'ldapserver-default';
      }
    },
    { header: 'Order', dataIndex: 'order', width: 80 },
    { header: 'Name', dataIndex: 'name', flex: 1 },
    { header: 'Url', dataIndex: 'url', flex: 1 }
  ],

  emptyText: 'No LDAP servers defined',

  tbar: [
    { xtype: 'button', text: 'New', glyph: 'xf055@FontAwesome' /* fa-plus-circle */, action: 'new', disabled: true },
    { xtype: 'button', text: 'Delete', glyph: 'xf056@FontAwesome' /* fa-minus-circle */, action: 'delete', disabled: true },
    { xtype: 'button', text: 'Change Order', glyph: 'xf162@FontAwesome' /* fa-sort-numeric-asc */, action: 'changeorder', disabled: true },
    { xtype: 'button', text: 'Clear Cache', glyph: 'xf014@FontAwesome' /* fa-trash-o */, action: 'clearcache', disabled: true }
  ],

  plugins: [
    { ptype: 'gridfilterbox', emptyText: 'No LDAP server matched criteria "$filter"' }
  ]

});
