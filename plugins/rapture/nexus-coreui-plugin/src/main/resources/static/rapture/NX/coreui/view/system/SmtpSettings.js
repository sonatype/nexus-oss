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
/**
 * SMTP System Settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.SmtpSettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-system-smtp-settings',

  items: [
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
          html: '<p>SMTP settings.</p>'
        },
        {
          xtype: 'nx-email',
          name: 'systemEmail',
          fieldLabel: 'System E-mail'
        },
        {
          name: 'host',
          itemId: 'host',
          fieldLabel: 'Host'
        },
        {
          xtype: 'numberfield',
          name: 'port',
          itemId: 'port',
          fieldLabel: 'Port',
          minValue: 1,
          maxValue: 65536,
          allowDecimals: false,
          allowExponential: false
        },
        {
          name: 'username',
          allowBlank: true,
          fieldLabel: 'Username'
        },
        {
          xtype: 'nx-password',
          name: 'password',
          fieldLabel: 'Password',
          allowBlank: true
        },
        {
          xtype: 'combo',
          name: 'connectionType',
          fieldLabel: 'Connection',
          emptyText: 'select a connection type',
          editable: false,
          store: [
            ['PLAIN', 'Use plain SMTP'],
            ['SSL', 'Use secure SMTP (SSL)'],
            ['TLS', 'Use STARTTLS negotiation (TLS)']
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
            return undefined
          }
        }
      ]
    }
  ],

  initComponent: function () {
    var me = this;

    me.callParent(arguments);

    me.items.get(0).getDockedItems('toolbar[dock="bottom"]')[0].add({
      xtype: 'button', text: 'Verify SMTP connection', formBind: true, action: 'verify', glyph: 'xf003@FontAwesome' /* fa-envelope-o */
    });
  }

});