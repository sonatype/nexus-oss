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
 * SMTP System Settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.SmtpSettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-system-smtp-settings',
  requires: [
    'NX.Conditions'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'nx-settingsform',
        settingsFormSuccessMessage: 'SMTP system settings $action',
        api: {
          load: 'NX.direct.coreui_SmtpSettings.read',
          submit: 'NX.direct.coreui_SmtpSettings.update'
        },
        editableCondition: NX.Conditions.isPermitted('nexus:settings', 'update'),
        editableMarker: 'You do not have permission to configure SMTP',

        items: [
          {
            xtype: 'label',
            html: '<p>Nexus can send email notifications for various events.</p>' +
                '<p>To enable sending email please configure a SMTP server.</p>'
          },
          {
            xtype: 'nx-email',
            name: 'systemEmail',
            fieldLabel: 'System email address',
            helpText: 'Email address used as the FROM address for all email notifications.'
          },
          {
            xtype: 'combo',
            name: 'connectionType',
            fieldLabel: 'SMTP server type',
            helpText: 'Connection level security to be used with SMTP server. Use any of the SSL/TLS provided solutions for greater security.',
            emptyText: 'select a connection type',
            editable: false,
            store: [
              ['PLAIN', 'Plain SMTP'],
              ['SSL', 'Secure SMTP via SSL'],
              ['TLS', 'Secure SMTP via TLS']
            ],
            queryMode: 'local',
            useTrustStore: function (combo) {
              var form = combo.up('form');
              if (combo.getValue() === 'SSL') {
                return {
                  name: 'useTrustStore',
                  host: form.down('#host'),
                  port: form.down('#port')
                };
              }
              return undefined;
            }
          },
          {
            name: 'host',
            itemId: 'host',
            fieldLabel: 'Hostname',
            helpText: 'The host name of an SMTP server.'
          },
          {
            xtype: 'numberfield',
            name: 'port',
            itemId: 'port',
            fieldLabel: 'SMTP server port',
            helpText: 'The port the SMTP server is listening on.',
            minValue: 1,
            maxValue: 65536,
            allowDecimals: false,
            allowExponential: false
          },
          {
            name: 'username',
            allowBlank: true,
            fieldLabel: 'Username',
            helpText: 'The username used to access the SMTP server.'
          },
          {
            xtype: 'nx-password',
            name: 'password',
            fieldLabel: 'SMTP password',
            helpText: 'The password used to access the SMTP server.',
            allowBlank: true
          }
        ]
      }
    ];

    me.callParent(arguments);

    me.items.get(0).getDockedItems('toolbar[dock="bottom"]')[0].add({
      xtype: 'button', text: 'Verify SMTP connection', formBind: true, action: 'verify', glyph: 'xf003@FontAwesome' /* fa-envelope-o */
    });
  }
});