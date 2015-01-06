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
 * Repository target "Settings" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositorytarget.RepositoryTargetSettingsForm', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-repositorytarget-settings-form',
  requires: [
    'NX.Conditions',
    'NX.I18n'
  ],

  api: {
    submit: 'NX.direct.coreui_RepositoryTarget.update'
  },
  settingsFormSuccessMessage: function(data) {
    return NX.I18n.get('ADMIN_TARGETS_UPDATE_SUCCESS') + data['name'];
  },

  editableMarker: NX.I18n.get('ADMIN_TARGETS_UPDATE_ERROR'),

  initComponent: function() {
    var me = this;

    me.editableCondition = me.editableCondition || NX.Conditions.isPermitted('nexus:targets', 'update');

    me.items = [
      {
        xtype: 'hiddenfield',
        name: 'id'
      },
      {
        xtype: 'textfield',
        name: 'name',
        itemId: 'name',
        fieldLabel: NX.I18n.get('ADMIN_TARGETS_SETTINGS_NAME')
      },
      {
        xtype: 'combo',
        name: 'format',
        fieldLabel: NX.I18n.get('ADMIN_TARGETS_SETTINGS_TYPE'),
        helpText: NX.I18n.get('ADMIN_TARGETS_SETTINGS_TYPE_HELP'),
        emptyText: NX.I18n.get('ADMIN_TARGETS_SETTINGS_TYPE_PLACEHOLDER'),
        editable: false,
        store: 'RepositoryFormat',
        queryMode: 'local',
        displayField: 'name',
        valueField: 'id'
      },
      {
        xtype: 'nx-valueset',
        name: 'patterns',
        itemId: 'patterns',
        fieldLabel: NX.I18n.get('ADMIN_TARGETS_SETTINGS_PATTERNS'),
        helpText: NX.I18n.get('ADMIN_TARGETS_SETTINGS_PATTERNS_HELP'),
        emptyText: NX.I18n.get('ADMIN_TARGETS_SETTINGS_PATTERNS_PLACEHOLDER'),
        input: {
          xtype: 'nx-regexp'
        },
        sorted: true
      }
    ];

    me.callParent(arguments);
  }
});
