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
 * Capability "Summary" panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.capability.CapabilitySummary', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-capability-summary',

  title: 'Summary',

  items: [
    {
      layout: 'column',
      items: [
        {
          xtype: 'nx-info',
          columnWidth: 1
        },
        {
          margin: '10 20 0 10',
          items: [
            { itemId: 'stateImage' },
            { itemId: 'stateLabel', margin: '5 0 0 0' }
          ]
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
  ],

  /**
   * @public
   * Shows capability info.
   * @param {Object} info capability info object
   */
  showInfo: function (info) {
    this.down('nx-info').showInfo(info);
  },

  /**
   * @public
   * Shows state image and text.
   * @param {String} state capability state
   */
  showState: function (state) {
    var me = this;

    me.down('#stateImage').update(NX.Icons.img('capability-' + state, 'x32'));
    me.down('#stateLabel').update('<b>' + Ext.String.capitalize(state) + '</b>');
  }

});
