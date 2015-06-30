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
 * Feed grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.feed.FeedList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-feed-list',
  requires: [
    'NX.I18n'
  ],

  config: {
    stateful: true,
    stateId: 'nx-coreui-feed-list'
  },

  store: 'Feed',

  /*
   * @override
   */

  initComponent: function() {
    var me = this;

    me.columns = [
      {
        xtype: 'nx-iconcolumn',
        width: 36,
        iconVariant: 'x16',
        iconName: function () {
          return 'feed-default';
        }
      },
      { header: NX.I18n.get('Feed_FeedList_Feed_Header'), dataIndex: 'name', stateId: 'name',flex: 1 },
      {
        xtype: 'nx-linkcolumn',
        header: NX.I18n.get('Feed_FeedList_URL_Header'),
        dataIndex: 'url',
        flex: 2
      }
    ];

    me.callParent(arguments);
  },

  viewConfig: {
    emptyText: 'No feeds defined',
    deferEmptyText: false
  },

  dockedItems: [{
    xtype: 'toolbar',
    dock: 'top',
    cls: 'nx-actions nx-borderless'
  }],

  plugins: [
    { ptype: 'gridfilterbox', emptyText: NX.I18n.get('Feed_FeedList_Filter_EmptyText') }
  ]

});
