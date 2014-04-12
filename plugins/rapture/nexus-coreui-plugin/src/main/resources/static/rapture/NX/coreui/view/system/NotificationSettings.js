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
 * Notification System Settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.NotificationSettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-system-notification-settings',

  items: [
    {
      xtype: 'nx-settingsform',
      settingsFormSuccessMessage: 'Notifications system settings $action',
      api: {
        load: 'NX.direct.coreui_NotificationSettings.read',
        submit: 'NX.direct.coreui_NotificationSettings.update'
      },
      editableCondition: NX.Conditions.isPermitted('nexus:settings', 'update'),
      editableMarker: 'You do not have permission to configure notifications',

      items: [
        {
          xtype: 'label',
          html: '<p>E-mail notifications.</p>'
        },
        {
          xtype: 'checkbox',
          name: 'enabled',
          fieldLabel: 'Enable E-mail notifications'
        },
        {
          xtype: 'nx-valueset',
          name: 'notifyEmails',
          fieldLabel: 'Notify email addresses',
          emptyText: 'enter an email',
          input: {
            xtype: 'nx-email'
          },
          sorted: true,
          allowBlank: true
        },
        {
          xtype: 'label',
          html: '<p>Notify users in roles.</p>'
        },
        {
          xtype: 'nx-itemselector',
          name: 'notifyRoles',
          buttons: ['add', 'remove'],
          fromTitle: 'Roles',
          toTitle: 'Notify users in roles',
          store: 'Role',
          valueField: 'id',
          displayField: 'name',
          delimiter: null,
          allowBlank: true
        }
      ]
    }
  ]

});