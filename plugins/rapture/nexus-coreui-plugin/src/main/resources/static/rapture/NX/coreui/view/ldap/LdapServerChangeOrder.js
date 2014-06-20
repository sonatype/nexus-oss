/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Change LDAP Server ordering window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerChangeOrder', {
  extend: 'NX.view.ChangeOrderWindow',
  alias: 'widget.nx-coreui-ldapserver-changeorder',

  title: 'Change LDAP Servers ordering',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.store = Ext.create('NX.coreui.store.LdapServer', {
      sortOnLoad: true,
      sorters: { property: 'order', direction: 'ASC' }
    });
    me.store.load();

    me.callParent(arguments);
  }

});
