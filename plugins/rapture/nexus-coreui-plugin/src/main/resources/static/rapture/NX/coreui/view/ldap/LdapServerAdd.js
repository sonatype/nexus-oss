/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
    'NX.Conditions',
    'NX.I18n'
  ],

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
        return NX.I18n.get('ADMIN_LDAP_CREATE_SUCCESS') + data['name'];
      },
      editableCondition: NX.Conditions.isPermitted('security:ldapconfig', 'create'),
          editableMarker: NX.I18n.get('ADMIN_LDAP_CREATE_ERROR'),
          items: {
            xtype: 'tabpanel',
            ui: 'nx-light',
            items: [
              {
                xtype: 'nx-coreui-ldapserver-connection-fieldset',
                title: NX.I18n.get('ADMIN_LDAP_DETAILS_CONNECTION_TAB'),
                ui: 'nx-inset'
              },
              {
                xtype: 'nx-coreui-ldapserver-backup-fieldset',
                title: NX.I18n.get('ADMIN_LDAP_DETAILS_BACKUP_TAB'),
                ui: 'nx-inset'
              },
              {
                xtype: 'nx-coreui-ldapserver-userandgroup-fieldset',
                title: NX.I18n.get('ADMIN_LDAP_DETAILS_GROUP_TAB'),
                ui: 'nx-inset'
              }
            ]
      },

      buttons: [
        { text: NX.I18n.get('ADMIN_LDAP_LIST_NEW_BUTTON'), action: 'add', formBind: true, ui: 'nx-primary' },
        { text: NX.I18n.get('GLOBAL_DIALOG_ADD_CANCEL_BUTTON'), handler: function () {
          this.up('nx-drilldown').showChild(0, true);
        }}
      ]
    };

    me.callParent(arguments);

    me.items.get(0).getDockedItems('toolbar[dock="bottom"]')[0].add(
        { xtype: 'button', text: NX.I18n.get('ADMIN_LDAP_CONNECTION_VERIFY_BUTTON'), groupBind: 'connection', action: 'verifyconnection' },
        { xtype: 'button', text: NX.I18n.get('ADMIN_LDAP_GROUP_MAPPING_BUTTON'), formBind: true, action: 'verifyusermapping' },
        { xtype: 'button', text: NX.I18n.get('ADMIN_LDAP_GROUP_LOGIN_BUTTON'), formBind: true, action: 'verifylogin' }
    );
  }
});
