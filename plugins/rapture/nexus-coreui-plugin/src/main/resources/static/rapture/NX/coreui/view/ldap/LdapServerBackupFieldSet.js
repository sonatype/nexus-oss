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
 * LDAP Server "Backup Mirror" field set.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerBackupFieldSet', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-ldapserver-backup-fieldset',

  defaults: {
    xtype: 'textfield',
    allowBlank: false
  },

  items: [
    {
      xtype: 'checkbox',
      name: 'backupMirrorEnabled',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_BACKUP_USE'),
      helpText: NX.I18n.get('ADMIN_LDAP_BACKUP_USE_HELP'),
      listeners: {
        change: function (checkbox, newValue) {
          checkbox.up('panel').showOrHide('backupMirror', newValue);
        }
      }
    },
    {
      xtype: 'combo',
      name: 'backupMirrorProtocol',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_HELP'),
      emptyText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_PLACEHOLDER'),
      editable: false,
      store: [
        ['ldap', NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_PLAIN_ITEM')],
        ['ldaps', NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_SECURE_ITEM')]
      ],
      queryMode: 'local',
      backupMirror: [true]
    },
    {
      name: 'backupMirrorHost',
      itemId: 'backupMirrorHost',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_HOST'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_HOST_HELP'),
      backupMirror: [true]
    },
    {
      xtype: 'numberfield',
      name: 'backupMirrorPort',
      itemId: 'backupMirrorPort',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_PORT'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PORT_HELP'),
      minValue: 1,
      maxValue: 65535,
      allowDecimals: false,
      allowExponential: false,
      backupMirror: [true]
    }
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.callParent(arguments);

    me.showOrHide('backupMirror', false);
  },

  /**
   * @private
   * Show & enable or hide and disable components that have attributes that matches the specified value.
   * @param attribute name of attribute
   * @param value to be matched in order to show
   */
  showOrHide: function (attribute, value) {
    var me = this,
        form = me.up('form'),
        components = me.query('component[' + attribute + ']');

    Ext.iterate(components, function (component) {
      if (value && component[attribute].indexOf(value) > -1) {
        component.enable();
        component.show();
      }
      else {
        component.disable();
        component.hide();
      }
    });
    if (form) {
      form.isValid();
    }
  }

});
