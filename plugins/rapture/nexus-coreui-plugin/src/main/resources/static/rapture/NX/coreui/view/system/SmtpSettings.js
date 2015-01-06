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
 * SMTP System Settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.SmtpSettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-system-smtp-settings',
  requires: [
    'NX.Conditions',
    'NX.I18n'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'nx-settingsform',
        settingsFormSuccessMessage: NX.I18n.get('ADMIN_SMTP_UPDATE_SUCCESS'),
        api: {
          load: 'NX.direct.coreui_SmtpSettings.read',
          submit: 'NX.direct.coreui_SmtpSettings.update'
        },
        editableCondition: NX.Conditions.isPermitted('nexus:settings', 'update'),
        editableMarker: NX.I18n.get('ADMIN_SMTP_UPDATE_ERROR'),

        items: [
          {
            xtype: 'nx-email',
            name: 'systemEmail',
            fieldLabel: NX.I18n.get('ADMIN_SMTP_EMAIL'),
            helpText: NX.I18n.get('ADMIN_SMTP_EMAIL_HELP')
          },
          {
            xtype: 'combo',
            name: 'connectionType',
            itemId: 'connectionType',
            fieldLabel: NX.I18n.get('ADMIN_SMTP_TYPE'),
            helpText: NX.I18n.get('ADMIN_SMTP_TYPE_HELP'),
            emptyText: NX.I18n.get('ADMIN_SMTP_TYPE_PLACEHOLDER'),
            editable: false,
            store: [
              ['PLAIN', NX.I18n.get('ADMIN_SMTP_TYPE_PLAIN_ITEM')],
              ['SSL', NX.I18n.get('ADMIN_SMTP_TYPE_SSL_ITEM')],
              ['TLS', NX.I18n.get('ADMIN_SMTP_TYPE_TLS_ITEM')]
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
            fieldLabel: NX.I18n.get('ADMIN_SMTP_HOSTNAME'),
            listeners: {
              change: function(){
                var cType = this.up('form').down('#connectionType');
                cType.fireEvent('change', cType, cType.getValue(), cType.getValue());
              }
            }
          },
          {
            xtype: 'numberfield',
            name: 'port',
            itemId: 'port',
            fieldLabel: NX.I18n.get('ADMIN_SMTP_PORT'),
            minValue: 1,
            maxValue: 65536,
            allowDecimals: false,
            allowExponential: false
          },
          {
            name: 'username',
            allowBlank: true,
            fieldLabel: NX.I18n.get('ADMIN_SMTP_USERNAME'),
          },
          {
            xtype: 'nx-password',
            name: 'password',
            fieldLabel: NX.I18n.get('ADMIN_SMTP_PASSWORD'),
            allowBlank: true
          }
        ]
      }
    ];

    me.callParent(arguments);

    me.items.get(0).getDockedItems('toolbar[dock="bottom"]')[0].add({
      xtype: 'button', text: NX.I18n.get('ADMIN_SMTP_VERIFY_BUTTON'), formBind: true, action: 'verify', glyph: 'xf003@FontAwesome' /* fa-envelope-o */
    });
  }
});
