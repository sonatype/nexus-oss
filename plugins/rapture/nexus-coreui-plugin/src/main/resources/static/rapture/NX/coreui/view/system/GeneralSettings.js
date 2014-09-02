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
 * General System Settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.GeneralSettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-system-general-settings',
  requires: [
    'NX.Conditions',
    'NX.util.Url'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'nx-settingsform',
        settingsFormSuccessMessage: 'General system settings $action',
        api: {
          load: 'NX.direct.coreui_GeneralSettings.read',
          submit: 'NX.direct.coreui_GeneralSettings.update'
        },
        editableCondition: NX.Conditions.isPermitted('nexus:settings', 'update'),
        editableMarker: 'You do not have permission to configure general settings',

        items: [
          {
            xtype: 'label',
            html: '<p>Set the base URL for the server.  This is primarily used when generating email notifications.</p>'
          },
          {
            xtype: 'textfield',
            name: 'baseUrl',
            itemId: 'baseUrl',
            fieldLabel: 'Base URL',
            helpText: 'This is the Base URL of the Nexus web application.  i.e. http://localhost:8081/',
            allowBlank: true,
            vtype: 'url',
            emptyText: NX.util.Url.baseUrl
          },
          {
            xtype: 'label',
            html: '<p>Optionally force the base URL which will be used by all server responses.</p>' +
                '<p>This is an advanced feature and only is applicable to rare cases.  Improper usage of this setting could negatively impact users of the system.</p>'
          },
          {
            xtype: 'checkbox',
            name: 'forceBaseUrl',
            itemId: 'forceBaseUrl',
            boxLabel: 'Force base URL',
            disabled: true
          }
        ]
      }
    ];

    me.callParent();
  }

});