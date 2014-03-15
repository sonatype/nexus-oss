/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
 * System general settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.General', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-system-general',

  items: [
    {
      xtype: 'nx-settingsform',
      settingsFormSuccessMessage: 'General system settings $action',
      api: {
        load: 'NX.direct.coreui_SystemGeneral.read',
        submit: 'NX.direct.coreui_SystemGeneral.update'
      },
      editableCondition: NX.Conditions.isPermitted('nexus:settings', 'update'),
      editableMarker: 'You do not have permission to configure general settings',

      items: [
        {
          xtype: 'label',
          html: '<p>Set the base URL for the server.</p>'
        },
        {
          xtype: 'textfield',
          name: 'baseUrl',
          fieldLabel: 'Base URL',
          emptyText: NX.util.Url.baseUrl
        },
        {
          xtype: 'checkbox',
          name: 'forceBaseUrl',
          boxLabel: 'Force base URL'
        },
        {
          xtype: 'label',
          html: '<p>Version updates.</p>'
        },
        {
          xtype: 'checkbox',
          name: 'checkForUpdates',
          boxLabel: 'Check for new version updates'
        }
      ]
    }
  ]

});