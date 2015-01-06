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
 * Versions / search results grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.search.SearchResultVersionList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-search-result-version-list',
  requires: [
    'NX.I18n'
  ],

  store: 'SearchResultVersion',

  allowDeselect: true,

  viewConfig: {
    emptyText: 'No versions found',
    deferEmptyText: false
  },

  columns: [
    {
      xtype: 'nx-iconcolumn',
      dataIndex: 'type',
      width: 36,
      iconVariant: 'x16',
      iconNamePrefix: 'repository-item-type-',
      iconName: function (value) {
        if (NX.getApplication().getIconController().findIcon('repository-item-type-' + value, 'x16')) {
          return value;
        }
        return 'default';
      }
    },
    {
      header: NX.I18n.get('BROWSE_SEARCH_VERSIONS_VERSION_COLUMN'),
      dataIndex: 'versionOrder',
      flex: 1,
      renderer: function (value, metadata, model) {
        return model.get('version');
      }
    },
    { header: NX.I18n.get('BROWSE_SEARCH_VERSIONS_FILE_COLUMN'), dataIndex: 'name', flex: 2.5 },
    { header: NX.I18n.get('BROWSE_SEARCH_VERSIONS_REPOSITORY_COLUMN'), dataIndex: 'repositoryName', flex: 1 }
  ],

  features: [
    {
      ftype: 'grouping',
      groupHeaderTpl: '{columnName}: {name}'
    }
  ]

});
