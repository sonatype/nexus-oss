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
 * LDAP Server "Backup Mirror" field set.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerBackupFieldSet', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-ldapserver-backup-fieldset',
  requires: [
    'NX.I18n'
  ],

  items: [
    {
      xtype: 'nx-optionalfieldset',
      title: NX.I18n.get('ADMIN_LDAP_BACKUP_USE'),
      checkboxToggle: true,
      checkboxName: 'backupMirrorEnabled',
      collapsed: true,

      defaults: {
        xtype: 'textfield',
        allowBlank: false
      },

      items: [
        {
          xtype: 'label',
          text: NX.I18n.get('ADMIN_LDAP_CONNECTION_ADDRESS'),
          style: {
            fontWeight: 'bold',
            display: 'block',
            marginTop: '10px',
            marginBottom: '5px'
          }
        },
        {
          xtype: 'label',
          text: NX.I18n.get('ADMIN_LDAP_CONNECTION_ADDRESS_HELP'),
          style: {
            fontSize: '10px',
            display: 'block',
            marginBottom: '1px'
          }
        },
        {
          xtype: 'combo',
          name: 'backupMirrorProtocol',
          cls: 'nx-float-left',
          blankText: 'Required',
          width: 85,
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
          xtype: 'label',
          cls: 'nx-float-left nx-interstitial-label',
          text: '://'
        },
        {
          name: 'backupMirrorHost',
          cls: 'nx-float-left',
          blankText: 'Required',
          width: 405,
          emptyText: NX.I18n.get('ADMIN_LDAP_CONNECTION_HOST_PLACEHOLDER'),
          itemId: 'backupMirrorHost',
          backupMirror: [true]
        },
        {
          xtype: 'label',
          cls: 'nx-float-left nx-interstitial-label',
          text: ':'
        },
        {
          xtype: 'numberfield',
          name: 'backupMirrorPort',
          cls: 'nx-float-left',
          blankText: 'Required',
          width: 75,
          emptyText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PORT_PLACEHOLDER'),
          itemId: 'backupMirrorPort',
          minValue: 1,
          maxValue: 65535,
          allowDecimals: false,
          allowExponential: false,
          backupMirror: [true]
        }
      ]
    }
  ]

});
