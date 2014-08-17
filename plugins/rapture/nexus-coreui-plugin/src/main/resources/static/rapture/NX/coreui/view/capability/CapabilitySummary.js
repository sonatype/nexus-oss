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
 * Capability "Summary" panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.capability.CapabilitySummary', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-capability-summary',
  requires: [
    'NX.Conditions'
  ],

  title: 'Summary',

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
          var description = 'Capability updated: ' + data['typeName'];
          if (data['description']) {
            description += ' - ' + data['description'];
          }
          return description;
        },
        editableCondition: NX.Conditions.isPermitted('nexus:capabilities', 'update'),
        editableMarker: 'You do not have permission to update capabilities',
        items: [
          {
            xtype: 'hiddenfield',
            name: 'id'
          },
          {
            xtype: 'textarea',
            fieldLabel: 'Notes',
            helpText: "Optional notes about configured capability.",
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
