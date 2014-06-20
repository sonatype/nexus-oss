/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Add LDAP Server window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-ldapserver-add',

  title: 'Create new LDAP server',
  defaultFocus: 'name',

  items: {
    xtype: 'nx-settingsform',
    api: {
      submit: 'NX.direct.ldap_LdapServer.create'
    },
    settingsFormSuccessMessage: function (data) {
      return 'LDAP server created: ' + data['name'];
    },
    editableCondition: NX.Conditions.isPermitted('security:ldapconfig', 'create'),
    editableMarker: 'You do not have permission to create LDAP servers',
    items: {
      xtype: 'tabpanel',
      plain: true,
      items: [
        { xtype: 'nx-coreui-ldapserver-connection-fieldset', title: 'Connection' },
        { xtype: 'nx-coreui-ldapserver-backup-fieldset', title: 'Backup Mirror' },
        { xtype: 'nx-coreui-ldapserver-userandgroup-fieldset', title: 'User & Group' }
      ]
    }
  },

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.callParent(arguments);

    me.items.get(0).getDockedItems('toolbar[dock="bottom"]')[0].add(
        { xtype: 'button', text: 'Verify connection', formBind: true, action: 'verifyconnection' },
        { xtype: 'button', text: 'Verify user mapping', formBind: true, action: 'verifyusermapping' },
        { xtype: 'button', text: 'Verify login', formBind: true, action: 'verifylogin' }
    );
  }

});
