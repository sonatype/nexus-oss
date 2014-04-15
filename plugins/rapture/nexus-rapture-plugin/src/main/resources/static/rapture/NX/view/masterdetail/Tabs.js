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
 * Master/Detail tabs.
 *
 * @since 3.0
 */
Ext.define('NX.view.masterdetail.Tabs', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-masterdetail-tabs',

  // HACK: For now make all detail panels light themed while we sort out the overall look of rapture
  ui: 'feature-detail',

  warningTpl: new Ext.XTemplate(
      '<div class="nx-masterdetail-warning">',
      '  <div>{icon}{text}</div>',
      '</div>',
      {
        compiled: true
      }
  ),

  initComponent: function () {
    var me = this,
        content = me.tabs;

    if (Ext.isArray(content)) {
      content = me.getTabsConfig(content);
    }
    else {
      content = Ext.apply({}, content, {title: undefined});
    }

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
      plain: true,
      items: items
    };
  },

  setDescription: function (description) {
    this.description = description;
    this.showTitle();
  },

  showWarning: function (message) {
    this.warning = message;
    this.showTitle();
  },

  clearWarning: function () {
    this.warning = undefined;
    this.showTitle();
  },

  showTitle: function () {
    var title = this.description;
    if (Ext.isDefined(this.warning)) {
      // TODO icon
      title += this.warningTpl.apply({
        text: this.warning
      });
    }
    this.setTitle(title);
  },

  addTab: function (tab) {
    var me = this,
        content = me.items.get(0);

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

  /**
   * @public
   * @returns {String} bookmark token of selected tab
   */
  getBookmarkOfSelectedTab: function () {
    var me = this,
        content = me.items.get(0),
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
        content = me.items.get(0);

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
