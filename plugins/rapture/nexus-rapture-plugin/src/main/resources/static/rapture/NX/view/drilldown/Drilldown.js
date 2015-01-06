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

  /**
   * @cfg [autoHideTabHeader=true] automatically hide tabs (header) when there is only one tab
   */
  autoHideTabHeader: true,

  tabs: {
    xtype: 'nx-info-panel'
  },

  // List of masters to use when creating a new object
  createWizard: null,

  // List of masters to use (xtype objects)
  masters: null,

  // List of actions to use in the detail view
  actions: null,

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
      items = [],
      maxCount;

    // Normalize list of masters and create wizards
    me.masters = Ext.isArray(me.masters) ? me.masters : [me.masters];
    me.createWizard = Ext.isArray(me.createWizard) ? me.createWizard : [me.createWizard];

    // How many panels do we need?
    maxCount = (me.masters.length > me.createWizard.length ? me.masters.length : me.createWizard.length) + 1;

    // Add the detail panel to the masters array
    if (me.detail) {
      // Use a custom detail panel
      me.masters.push(me.detail);
    } else {
      // Use the default tab panel
      me.masters.push(
        {
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
          autoHideTabHeader: me.autoHideTabHeader,
          actions: Ext.isArray(me.actions) ? Ext.Array.clone(me.actions) : me.actions
        }
      );
    }

    // Stack all panels onto the items array
    for (var i = 0; i < maxCount; ++i) {
      items.push(
        {
          xtype: 'nx-drilldown-item',
          layout: 'card',
          items: [
            me.masters[i],
            (i > 0 ? me.createWizard[i - 1] : undefined)
          ]
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
   * Show the referenced step in the create wizard
   */
  showCreateWizard: function (index, animate, cmp) {
    var me = this;
    me.down('nx-drilldown-panel').showCreateWizard(index, animate, cmp);
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
