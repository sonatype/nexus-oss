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
 * Capability grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.capability.CapabilityList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-capability-list',

  store: 'Capability',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconNamePrefix: 'capability-',
      dataIndex: 'state',
      hideable: false
    },
    { text: NX.I18n.get('ADMIN_CAPABILITIES_LIST_TYPE_COLUMN'), dataIndex: 'typeName', flex: 1 },
    { text: NX.I18n.get('ADMIN_CAPABILITIES_LIST_DESCRIPTION_COLUMN'), dataIndex: 'description', flex: 1, groupable: false },
    { text: NX.I18n.get('ADMIN_CAPABILITIES_LIST_NOTES_COLUMN'), dataIndex: 'notes', flex: 1 }
  ],

  viewConfig: {
    emptyText: 'No capabilities defined',
    deferEmptyText: false,
    getRowClass: function (record) {
      if (record.get('enabled') && !record.get('active')) {
        return 'nx-red-marker';
      }
    }
  },

  tbar: [
    {
      xtype: 'button',
      text: NX.I18n.get('ADMIN_CAPABILITIES_LIST_NEW_BUTTON'),
      action: 'new',
      disabled: true,
      glyph: 'xf055@FontAwesome' /* fa-plus-circle */
    }
  ],

  features: [
    {
      ftype: 'grouping',
      groupHeaderTpl: '{[values.name === "" ? "No " + values.columnName : values.name + " " + values.columnName]}'
    }
  ],

  plugins: [
    { ptype: 'gridfilterbox', emptyText: 'No capability matched criteria "$filter"' }
  ],

  /**
   * @override
   */
  initComponent: function (config) {
    var me = this;

    me.originalColumns = me.columns;

    me.callParent(arguments);
  }

});
