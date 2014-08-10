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
 * Add LDAP Server window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-ldapserver-add',
  requires: [
    'NX.Conditions'
  ],

  title: 'Create new LDAP server',
  defaultFocus: 'name',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = {
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
    };

    me.callParent(arguments);

    me.items.get(0).getDockedItems('toolbar[dock="bottom"]')[0].add(
        { xtype: 'button', text: 'Verify connection', formBind: true, action: 'verifyconnection' },
        { xtype: 'button', text: 'Verify user mapping', formBind: true, action: 'verifyusermapping' },
        { xtype: 'button', text: 'Verify login', formBind: true, action: 'verifylogin' }
    );
  }
});
