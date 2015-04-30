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
 * Bundle list.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.BundleList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-system-bundlelist',
  requires: [
    'NX.I18n'
  ],

  config: {
    stateful: true,
    stateId: 'nx-coreui-system-bundlelist'
  },

  store: 'Bundle',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function () {
        return 'bundle-default';
      }
    },
    {header: NX.I18n.get('ADMIN_BUNDLES_LIST_ID_COLUMN'), dataIndex: 'id', stateId: 'id', width: 60, resizable: false},
    {
      header: NX.I18n.get('ADMIN_BUNDLES_LIST_STATE_COLUMN'),
      dataIndex: 'state',
      stateId: 'state',
      width: 80,
      resizable: false
    },
    {
      header: NX.I18n.get('ADMIN_BUNDLES_LIST_START_LEVEL_COLUMN'),
      dataIndex: 'startLevel',
      stateId: 'startLevel',
      width: 60,
      resizable: false
    },
    {header: NX.I18n.get('ADMIN_BUNDLES_LIST_NAME_COLUMN'), dataIndex: 'name', stateId: 'name', flex: 2},
    {
      header: NX.I18n.get('ADMIN_BUNDLES_LIST_SYMBOLIC_NAME_COLUMN'),
      dataIndex: 'symbolicName',
      stateId: 'symbolicName',
      flex: 2,
      hidden: true
    },
    {header: NX.I18n.get('ADMIN_BUNDLES_LIST_VERSION_COLUMN'), dataIndex: 'version', stateId: 'version', flex: 1},
    {
      header: NX.I18n.get('ADMIN_BUNDLES_LIST_LOCATION_COLUMN'),
      dataIndex: 'location',
      stateId: 'location',
      hidden: true
    },
    {
      header: NX.I18n.get('ADMIN_BUNDLES_LIST_FRAGMENT_COLUMN'),
      dataIndex: 'fragment',
      stateId: 'fragment',
      hidden: true
    }
  ],

  dockedItems: [{
    xtype: 'toolbar',
    dock: 'top',
    cls: 'nx-actions nx-borderless'
  }],

  plugins: [
    {ptype: 'gridfilterbox', emptyText: NX.I18n.get('ADMIN_BUNDLES_LIST_FILTER_ERROR')}
  ]
});
