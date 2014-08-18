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
 * Security anonymous settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.security.AnonymousSettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-security-anonymous-settings',
  requires: [
    'NX.Conditions'
  ],

  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'nx-settingsform',
        settingsFormSuccessMessage: 'Anonymous security settings $action',
        api: {
          load: 'NX.direct.coreui_AnonymousSettings.read',
          submit: 'NX.direct.coreui_AnonymousSettings.update'
        },
        editableCondition: NX.Conditions.isPermitted('nexus:settings', 'update'),
        editableMarker: 'You do not have permission to configure anonymous user',
        items: [
          {
            xtype: 'label',
            html: '<p>Nexus can be configured to allow or disallow anonymous users from accessing the system.</p>' +
                '<p>If only authenticated users should be allowed to use this server, disable anonymous access.</p>'
          },
          {
            xtype: 'checkbox',
            name: 'enabled',
            value: true,
            boxLabel: 'Allow anonymous users to access the server',
            listeners: {
              change: me.handleUseCustomUser,
              afterrender: me.handleUseCustomUser
            }
          },
          {
            xtype: 'nx-optionalfieldset',
            title: 'Customize anonymous user credentials',
            itemId: 'useCustomUser',
            checkboxToggle: true,
            checkboxName: 'useCustomUser',
            collapsed: true,
            items: [
              {
                xtype: 'label',
                html: '<p>In rare cases the <em>anonymous</em> username and password may be overridden; this is normally not needed.</p>'
              },
              {
                xtype: 'textfield',
                name: 'username',
                fieldLabel: 'Username',
                helpText: 'The username that will be used to authenticate anonymous users against the security realm.',
                emptyText: 'anonymous',
                allowBlank: false
              },
              {
                xtype: 'nx-password',
                name: 'password',
                fieldLabel: 'Password',
                helpText: 'The password that will be used to authenticate anonymous users against the security realm.',
                emptyText: 'password',
                allowBlank: false
              }
            ]
          }
        ]
      }
    ];

    me.callParent(arguments);
  },

  handleUseCustomUser: function (checkbox) {
    var useCustomUser = checkbox.up('form').down('#useCustomUser');

    if (checkbox.getValue()) {
      useCustomUser.enable();
    }
    else {
      useCustomUser.collapse();
      useCustomUser.disable();
    }
  }

});