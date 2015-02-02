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
 * Capability "Summary" panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.capability.CapabilitySummary', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-capability-summary',
  requires: [
    'NX.Conditions',
    'NX.I18n'
  ],

  title: NX.I18n.get('ADMIN_CAPABILITIES_DETAILS_SUMMARY_TAB'),
  autoScroll: true,

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {
        layout: 'column',
        items: [
          {
            xtype: 'nx-info',
            columnWidth: 1
          }
        ]
      },
      {
        xtype: 'nx-settingsform',
        api: {
          submit: 'NX.direct.capability_Capability.updateNotes'
        },
        settingsFormSuccessMessage: function (data) {
          var description = NX.I18n.format('ADMIN_CAPABILITIES_UPDATE_SUCCESS', data['typeName']);
          if (data['description']) {
            description += ' - ' + data['description'];
          }
          return description;
        },
        editableCondition: NX.Conditions.isPermitted('nexus:capabilities', 'update'),
        editableMarker: NX.I18n.get('ADMIN_CAPABILITIES_UPDATE_ERROR'),
        items: [
          {
            xtype: 'hiddenfield',
            name: 'id'
          },
          {
            xtype: 'textarea',
            fieldLabel: NX.I18n.get('ADMIN_CAPABILITIES_SUMMARY_NOTES'),
            helpText: NX.I18n.get('ADMIN_CAPABILITIES_SUMMARY_NOTES_HELP'),
            name: 'notes',
            allowBlank: true,
            anchor: '100%'
          }
        ]
      }
    ];

    me.callParent();
  },

  /**
   * @public
   * Shows capability info.
   * @param {Object} info capability info object
   */
  showInfo: function (info) {
    this.down('nx-info').showInfo(info);
  }
});
