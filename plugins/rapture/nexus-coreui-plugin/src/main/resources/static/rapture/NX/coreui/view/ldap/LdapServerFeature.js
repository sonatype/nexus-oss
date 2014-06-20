/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * LDAP Server feature panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerFeature', {
  extend: 'NX.view.masterdetail.Panel',
  alias: 'widget.nx-coreui-ldapserver-feature',

  list: 'nx-coreui-ldapserver-list',

  iconName: 'ldapserver-default',

  tabs: [
    { xtype: 'nx-coreui-ldapserver-connection', title: 'Connection' },
    { xtype: 'nx-coreui-ldapserver-backup', title: 'Backup Mirror' },
    { xtype: 'nx-coreui-ldapserver-userandgroup', title: 'User & Group' }
  ]

});
