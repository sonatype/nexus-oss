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
 * Feeds controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Feeds', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Permissions',
    'NX.Bookmarks',
    'NX.Conditions',
    'NX.Windows',
    'NX.I18n'
  ],
  masters: [
    'nx-coreui-feed-list'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },
  stores: [
    'Feed',
    'FeedEntry'
  ],
  views: [
    'feed.FeedFeature',
    'feed.FeedEntryList',
    'feed.FeedList'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-feed-feature' },
    { ref: 'list', selector: 'nx-coreui-feed-list' },
    { ref: 'entryList', selector: 'nx-coreui-feedentry-list' }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'feed-default': {
        file: 'feed.png',
        variants: ['x16', 'x32']
      }
    });

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'browse',
      path: '/Feeds',
      text: NX.I18n.get('Feeds_Text'),
      description: NX.I18n.get('Feeds_Description'),
      view: { xtype: 'nx-coreui-feed-feature' },
      iconConfig: {
        file: 'feed.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('nexus:feeds:read');
      }
    }, me);

    me.callParent();

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadFeeds
        }
      },
      component: {
        'nx-coreui-feed-list': {
          beforerender: me.loadFeeds
        },
        'nx-coreui-feed-feature button[action=subscribe]': {
          click: me.subscribe,
          afterrender: me.bindSubscribeButton
        }
      }
    });
  },

  /**
   * @override
   */
  getDescription: function(model) {
    return model.get('name');
  },

  /**
   * @private
   * Loads feeds store.
   */
  loadFeeds: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.getStore('Feed').load();
    }
  },

  /**
   * @private
   */
  onSelection: function (list, model) {
    if (model) {
      this.getStore('FeedEntry').filter({ id: 'key', property: 'key', value: model.get('key') });
    }
  },

  /**
   * @private
   * Open a new tab targeting feed url.
   */
  subscribe: function () {
    var selection = this.getList().getSelectionModel().getSelection();

    if (selection.length) {
      NX.Windows.open(selection[0].get('url'));
    }
  },

  /**
   * @protected
   * Enable 'Subscribe' when a feed is selected.
   */
  bindSubscribeButton: function (button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.gridHasSelection('nx-coreui-feed-list')
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  }

});
