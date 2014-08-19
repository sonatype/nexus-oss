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
 * Master/Detail tabs.
 *
 * @since 3.0
 */
Ext.define('NX.view.masterdetail.Tabs', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-masterdetail-tabs',
  requires: [
    'NX.Icons',
    'NX.Bookmarks'
  ],

  // HACK: For now make all detail panels light themed while we sort out the overall look of rapture
  //ui: 'feature-detail',

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
        content = me.tabs;

    if (Ext.isArray(content)) {
      content = me.getTabsConfig(content);
    }
    else {
      content = Ext.apply({}, content, { title: undefined, flex: 1 });
    }
    content = Ext.Array.from(content);
    Ext.Array.insert(content, 0, [
      {
        xtype: 'panel',
        itemId: 'info',
        iconCls: NX.Icons.cls('masterdetail-info', 'x16'),
        hidden: true
      },
      {
        xtype: 'panel',
        itemId: 'warning',
        iconCls: NX.Icons.cls('masterdetail-warning', 'x16'),
        hidden: true
      }
    ]);

    Ext.apply(me, {
      items: content
    });

    me.description = me.title;

    me.callParent(arguments);

    me.on('afterrender', me.calculateBookmarks, me);
  },

  getTabsConfig: function (items) {
    return {
      xtype: 'tabpanel',
      activeTab: 0,
      layoutOnTabChange: true,
      flex: 1,
      items: items
    };
  },

  setDescription: function (description) {
    this.description = description;
    this.setTitle(description);
  },

  showInfo: function (message) {
    var me = this,
        infoPanel = me.down('>#info');

    infoPanel.setTitle(message);
    infoPanel.show();
  },

  clearInfo: function () {
    var me = this,
        infoPanel = me.down('>#info');

    infoPanel.hide();
  },

  showWarning: function (message) {
    var me = this,
        warningPanel = me.down('>#warning');

    warningPanel.setTitle(message);
    warningPanel.show();
  },

  clearWarning: function () {
    var me = this,
        warningPanel = me.down('>#warning');

    warningPanel.hide();
  },

  addTab: function (tab) {
    var me = this,
        content = me.items.get(2);

    if (content.isXType('tabpanel')) {
      me.tabs.push(tab);
      content.add(tab);
    }
    else {
      me.tabs = [me.tabs, tab];
      me.remove(content);
      me.add(me.getTabsConfig(me.tabs));
    }
    me.calculateBookmarks();
  },

  removeTab: function (tab) {
    var me = this,
        content = me.items.get(2);

    if (content.isXType('tabpanel')) {
      Ext.Array.remove(me.tabs, tab);
      content.remove(tab);
    }
    if (me.tabs.length === 1) {
      me.tabs = me.tabs[0];
      me.remove(content);
      me.add(me.tab);
    }
    me.calculateBookmarks();
  },

  /**
   * @public
   * @returns {String} bookmark token of selected tab
   */
  getBookmarkOfSelectedTab: function () {
    var me = this,
        content = me.items.get(2),
        selectedItem = content;

    if (content.isXType('tabpanel')) {
      selectedItem = content.getActiveTab();
    }
    return selectedItem.bookmark;
  },

  /**
   * @public
   * Finds a tab by bookmark & sets it active (if found).
   * @param {String} bookmark of tab to be activated
   */
  setActiveTabByBookmark: function (bookmark) {
    var me = this,
        tabpanel = me.down('> tabpanel'),
        tab = me.down('> tabpanel > panel[bookmark=' + bookmark + ']');

    if (tabpanel && tab) {
      tabpanel.setActiveTab(tab);
    }
  },

  /**
   * @private
   * Calculates bookmarks of all tabs based on tab title.
   */
  calculateBookmarks: function () {
    var me = this,
        content = me.items.get(2);

    if (content.isXType('tabpanel')) {
      content.items.each(function (tab) {
        if (tab.title) {
          tab.bookmark = NX.Bookmarks.encode(tab.title).toLowerCase();
        }
      });
    }
    else {
      if (content.title) {
        content.bookmark = NX.Bookmarks.encode(content.title).toLowerCase();
      }
    }
  }

});
