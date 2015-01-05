/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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
    'NX.util.Url',
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
        settingsFormSuccessMessage: NX.I18n.get('ADMIN_GENERAL_UPDATE_SUCCESS'),
        api: {
          load: 'NX.direct.coreui_GeneralSettings.read',
          submit: 'NX.direct.coreui_GeneralSettings.update'
        },
        editableCondition: NX.Conditions.isPermitted('nexus:settings', 'update'),
        editableMarker: NX.I18n.get('ADMIN_GENERAL_UPDATE_ERROR'),

        items: [
          {
            xtype: 'textfield',
            name: 'baseUrl',
            itemId: 'baseUrl',
            fieldLabel: NX.I18n.get('ADMIN_GENERAL_URL'),
            helpText: NX.I18n.get('ADMIN_GENERAL_URL_HELP'),
            allowBlank: true,
            vtype: 'url',
            emptyText: NX.util.Url.baseUrl
          }
        ]
      }
    ];

    me.callParent();
  }

});
