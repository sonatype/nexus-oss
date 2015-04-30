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
 * Search results grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.search.SearchResultList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-search-result-list',
  requires: [
    'NX.I18n'
  ],

  config: {
    stateful: true,
    stateId: 'nx-coreui-search-result-list'
  },

  store: 'SearchResult',

  style: {
    'background-color': '#F4F4F4'
  },

  viewConfig: {
    emptyText: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_EMPTY_STATE'),
    deferEmptyText: false
  },

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function () {
        return 'search-component';
      }
    },
    { header: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_NAME_COLUMN'), dataIndex: 'name', stateId: 'name', flex: 3 },
    { header: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_GROUP_COLUMN'), dataIndex: 'group', stateId: 'group', flex: 4 },
    { header: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_VERSION_COLUMN'), dataIndex: 'version', stateId: 'version', flex: 1 },
    { header: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_FORMAT_COLUMN'), dataIndex: 'format', stateId: 'format', width: 70 },
    {
      header: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_REPOSITORY_COLUMN'),
      dataIndex: 'repositoryName',
      stateId: 'repositoryName',
      hidden: true
    }
  ],

  dockedItems: [
    {
      xtype: 'pagingtoolbar',
      store: 'SearchResult',
      dock: 'top',
      displayInfo: false
    }
  ]

});
