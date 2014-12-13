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
 * The foundation class for new drilldowns. Extend this.
 *
 * @since 3.0
 */
Ext.define('NX.view.drilldown.Drilldown', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-drilldown',
  itemId: 'nx-drilldown',

  requires: [
    'NX.Icons',
    'NX.view.drilldown.Panel'
  ],

  layout: 'fit',

  tabs: {
    xtype: 'nx-info-panel'
  },

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
      items = [];

    // Normalize list of masters
    me.masters = Ext.isArray(me.masters) ? me.masters : [me.masters];

    // Add masters
    for (var i = 0; i < me.masters.length; ++i) {
      items.push(
        {
          xtype: 'nx-drilldown-item',
          layout: 'fit',
          items: me.masters[i]
        }
      );
    }

    // Add details
    if (me.detail) {
      // Use a custom detail panel
      items.push({ xtype: 'nx-drilldown-item', layout: 'fit', items: me.detail });
    } else {
      // Use the default tab panel
      items.push(
        {
          xtype: 'nx-drilldown-item',

          layout: 'fit',

          items: {
            xtype: 'nx-drilldown-details',
            ui: 'drilldown-tabs',
            header: false,
            plain: true,

            layout: {
              type: 'vbox',
              align: 'stretch',
              pack: 'start'
            },
            tabs: Ext.isArray(me.tabs) ? Ext.Array.clone(me.tabs) : Ext.apply({}, me.tabs),
            actions: Ext.isArray(me.actions) ? Ext.Array.clone(me.actions) : Ext.apply({}, me.actions)
          }
        }
      );
    }

    // Initialize this component’s items
    me.items = [
      {
        xtype: 'container',
        items: [
          {
            xtype: 'nx-drilldown-panel',

            items: items
          }
        ]
      }
    ];

    me.callParent(arguments);

    if (Ext.isDefined(me.iconName)) {
      me.setDefaultIconName(me.iconName);
    }
  },

  /**
   * Shorthand to assign the same icon to all drilldown items, scaled appropriately.
   */
  setDefaultIconName: function (iconName) {
    var items = this.query('nx-drilldown-item');
    for (var i = 0; i < items.length; ++i) {
      items[i].setItemClass(NX.Icons.cls(iconName) + (i === 0 ? '-x32' : '-x16'));
    }
  },

  /**
   * Show the referenced drilldown item
   */
  showChild: function (index, animate) {
    var me = this;
    me.down('nx-drilldown-panel').showChild(index, animate);
  },

  /**
   * Set the name of the referenced drilldown item
   */
  setItemName: function (index, text) {
    var me = this;
    me.query('nx-drilldown-item')[index].setItemName(text);
  },

  /**
   * Set the icon class of the referenced drilldown item
   */
  setItemClass: function (index, cls) {
    var me = this;
    me.query('nx-drilldown-item')[index].setItemClass(cls);
  },

  /**
   * Set the bookmark of the breadcrumb segment associated with the referenced drilldown item
   */
  setItemBookmark: function (index, bookmark, scope) {
    var me = this;
    me.query('nx-drilldown-item')[index].setItemBookmark(bookmark, scope);
  },

  showInfo: function (message) {
    this.down('nx-drilldown-details').showInfo(message);
  },

  clearInfo: function () {
    this.down('nx-drilldown-details').clearInfo();
  },

  showWarning: function (message) {
    this.down('nx-drilldown-details').showWarning(message);
  },

  clearWarning: function () {
    this.down('nx-drilldown-details').clearWarning();
  },

  /**
   * Add a tab to the default detail panel
   *
   * Note: this will have no effect if a custom detail panel has been specified
   */
  addTab: function (tab) {
    var me = this;
    if (!me.detail) {
      this.down('nx-drilldown-details').addTab(tab);
    }
  },

  /**
   * Remove a panel from the default detail panel
   *
   * Note: this will have no effect if a custom detail panel has been specified
   */
  removeTab: function (tab) {
    var me = this;
    if (!me.detail) {
      this.down('nx-drilldown-details').removeTab(tab);
    }
  }
});
