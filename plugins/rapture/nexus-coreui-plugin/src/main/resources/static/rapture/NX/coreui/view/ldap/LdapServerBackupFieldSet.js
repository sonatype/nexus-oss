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
      fieldLabel: 'Use Backup Mirror',
      helpText: 'Check this box if a backup mirror should be used.',
      listeners: {
        change: function (checkbox, newValue) {
          checkbox.up('panel').showOrHide('backupMirror', newValue);
        }
      }
    },
    {
      xtype: 'combo',
      name: 'backupMirrorProtocol',
      fieldLabel: 'Protocol',
      helpText: 'Use plain text (ldap://) or secure (ldaps://) connection.',
      emptyText: 'select a connection type',
      editable: false,
      store: [
        ['ldap', 'Plain connection (ldap)'],
        ['ldaps', 'Secure Connection (ldaps)']
      ],
      queryMode: 'local',
      backupMirror: [true]
    },
    {
      name: 'backupMirrorHost',
      itemId: 'backupMirrorHost',
      fieldLabel: 'Host',
      helpText: 'The host name of the backup LDAP server.',
      backupMirror: [true]
    },
    {
      xtype: 'numberfield',
      name: 'backupMirrorPort',
      itemId: 'backupMirrorPort',
      fieldLabel: 'Port',
      helpText: 'The port the backup LDAP server is listening on (ldap - 389, ldaps - 636).',
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
