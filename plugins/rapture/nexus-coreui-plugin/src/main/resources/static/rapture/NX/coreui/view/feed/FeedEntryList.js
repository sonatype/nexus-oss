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
 * Feed Entry grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.feed.FeedEntryList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-feedentry-list',
  requires: [
    'NX.Icons',
    'NX.I18n'
  ],

  store: 'FeedEntry',

  viewConfig: {
    emptyText: 'No entries',
    deferEmptyText: false
  },

  columns: [
    { header: NX.I18n.get('Feed_FeedEntryList_Title_Header'), dataIndex: 'title', flex: 2 },
    { header: NX.I18n.get('Feed_FeedEntryList_Date_Header'), dataIndex: 'published', flex: 1 }
  ],

  dockedItems: [
    {
      xtype: 'pagingtoolbar',
      store: 'FeedEntry',
      dock: 'top',
      displayInfo: true
    }
  ],

  plugins: [
    {
      ptype: 'rowexpander',
      rowBodyTpl: Ext.create('Ext.XTemplate',
          '<p>{content}</p>'
      )
    }
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.callParent();
  }
});
