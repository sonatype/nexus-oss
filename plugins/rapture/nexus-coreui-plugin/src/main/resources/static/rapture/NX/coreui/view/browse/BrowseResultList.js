/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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
 * Search results grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.browse.BrowseResultList', {
  extend: 'NX.view.drilldown.Master',
  alias: ['widget.nx-coreui-browse-result-list', 'widget.nx-coreui-healthcheck-result-list'],
  requires: [
    'NX.I18n'
  ],

  config: {
    stateful: true,
    stateId: 'nx-coreui-browse-result-list'
  },

  store: 'Component',

  // Prevent the store from automatically loading
  loadStore: Ext.emptyFn,

  style: {
    'background-color': '#F4F4F4'
  },

  viewConfig: {
    emptyText: NX.I18n.get('Browse_BrowseResultList_EmptyText_View'),
    deferEmptyText: false
  },

  columns: [
    {
      xtype: 'nx-iconcolumn',
      dataIndex: 'id',
      width: 36,
      iconVariant: 'x16',
      iconName: function(value) {
        if (value === 'unattached') {
          return 'browse-unattached';
        }
        return 'browse-component';
      }
    },
    {
      text: NX.I18n.get('Browse_BrowseResultList_Name_Column'),
      dataIndex: 'name',
      stateId: 'name',
      flex: 3,
      renderer: function(value) {
        if (!value) {
          return NX.I18n.get('Browse_BrowseResultList_Name_Column_Unattached');
        }
        return value;
      }
    },
    {
      text: NX.I18n.get('Browse_BrowseResultList_Group_Column'),
      dataIndex: 'group',
      stateId: 'group',
      flex: 4,
      renderer: NX.ext.grid.column.Renderers.optionalData
    },
    {
      text: NX.I18n.get('Browse_BrowseResultList_Version_Column'),
      dataIndex: 'version',
      stateId: 'version',
      flex: 1,
      renderer: NX.ext.grid.column.Renderers.optionalData
    }
  ],

  dockedItems: [{
    xtype: 'toolbar',
    dock: 'top',
    cls: 'nx-actions nx-borderless',
    items: [
      '->',
      {
        xtype: 'nx-searchbox',
        itemId: 'filter',
        emptyText: NX.I18n.get('Grid_Plugin_FilterBox_Empty'),
        width: 200
      }
    ]
  }]

});
