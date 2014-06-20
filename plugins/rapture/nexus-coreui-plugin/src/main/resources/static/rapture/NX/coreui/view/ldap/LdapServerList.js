/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
