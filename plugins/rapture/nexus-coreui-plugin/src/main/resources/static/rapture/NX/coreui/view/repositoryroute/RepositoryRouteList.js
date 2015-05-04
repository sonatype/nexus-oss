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
 * Repository route grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositoryroute.RepositoryRouteList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-repositoryroute-list',
  requires: [
    'NX.I18n'
  ],

  config: {
    stateful: true,
    stateId: 'nx-coreui-repositoryroute-list'
  },

  store: 'RepositoryRoute',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function () {
        return 'repositoryroute-default';
      }
    },
    { header: NX.I18n.get('ADMIN_ROUTING_LIST_ROUTE_COLUMN'), dataIndex: 'pattern', stateId: 'pattern', flex: 1 },
    {
      header: NX.I18n.get('ADMIN_ROUTING_LIST_RULE_COLUMN'), dataIndex: 'mappingType', stateId: 'mappingType',
      renderer: function(val) {
        return {
          BLOCKING: NX.I18n.get('ADMIN_ROUTING_SETTINGS_BLOCKING_ITEM'),
          INCLUSION: NX.I18n.get('ADMIN_ROUTING_SETTINGS_INCLUSIVE_ITEM'),
          EXCLUSION: NX.I18n.get('ADMIN_ROUTING_SETTINGS_EXCLUSIVE_ITEM')
        }[val];
      }},
    { header: NX.I18n.get('ADMIN_ROUTING_LIST_GROUP_COLUMN'), dataIndex: 'groupName', stateId: 'groupName' },
    {
      header: NX.I18n.get('ADMIN_ROUTING_LIST_REPOSITORIES_COLUMN'),
      dataIndex: 'mappedRepositoriesNames',
      stateId: 'mappedRepositoriesNames',
      flex: 1
    }
  ],

  viewConfig: {
    emptyText: NX.I18n.get('ADMIN_ROUTING_LIST_EMPTY_STATE'),
    deferEmptyText: false
  },

  dockedItems: [{
    xtype: 'toolbar',
    dock: 'top',
    cls: 'nx-actions nx-borderless',
    items: [
      {
        xtype: 'button',
        text: NX.I18n.get('ADMIN_ROUTING_LIST_NEW_BUTTON'),
        glyph: 'xf055@FontAwesome' /* fa-plus-circle */,
        action: 'new',
        disabled: true
      }
    ]
  }],

  plugins: [{ ptype: 'gridfilterbox', emptyText: NX.I18n.get('ADMIN_ROUTING_LIST_FILTER_ERROR') }]

});
