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
 * Feeds controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Feeds', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Permissions',
    'NX.Bookmarks',
    'NX.Conditions',
    'NX.Windows'
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
    {
      ref: 'list',
      selector: 'nx-coreui-feed-list'
    },
    {
      ref: 'entryList',
      selector: 'nx-coreui-feedentry-list'
    }
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
      description: 'System event feeds',
      view: { xtype: 'nx-coreui-feed-feature' },
      iconConfig: {
        file: 'feed.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('nexus:feeds', 'read');
      }
    });

    me.listen({
      controller: {
        '#Bookmarking': {
          navigate: me.navigateTo
        },
        '#Refresh': {
          refresh: me.loadFeeds
        }
      },
      store: {
        '#Feed': {
          load: me.onFeedLoad
        }
      },
      component: {
        'nx-coreui-feed-list': {
          beforerender: me.loadFeeds,
          selectionchange: me.onSelectionChange
        },
        'nx-coreui-feed-list button[action=subscribe]': {
          click: me.subscribe,
          afterrender: me.bindSubscribeButton
        }
      }
    });
  },

  /**
   * @private
   * Loads feeds store.
   */
  loadFeeds: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.getFeedStore().load();
    }
  },

  /**
   * @private
   * (Re)select bookmarked feed.
   */
  onFeedLoad: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.navigateTo(NX.Bookmarks.getBookmark());
    }
  },

  /**
   * @private
   */
  onSelectionChange: function (selectionModel, selected) {
    this.onFeedChanged(selected[0]);
  },

  /**
   * @private
   * Show/Hide, reload feed entries.
   * Bookmark selected entry.
   *
   * @param {NX.coreui.model.Feed} feedModel selected model
   */
  onFeedChanged: function (feedModel) {
    var me = this,
        entryList = me.getEntryList(),
        bookmark = NX.Bookmarks.fromToken(NX.Bookmarks.getBookmark().getSegment(0));

    if (feedModel) {
      entryList.setTitle(feedModel.get('name'));
      me.getFeedEntryStore().filter({ id: 'key', property: 'key', value: feedModel.get('key') });
      entryList.show();
      me.getList().getView().focusRow(feedModel);
      bookmark.appendSegments(encodeURIComponent(feedModel.getId()));
    }
    else {
      entryList.hide();
    }
    NX.Bookmarks.bookmark(bookmark, me);
  },

  /**
   * @private
   * @param {NX.Bookmark} bookmark to navigate to
   */
  navigateTo: function (bookmark) {
    var me = this,
        list = me.getList(),
        store, modelId, model;

    if (list && bookmark) {
      modelId = bookmark.getSegment(1);
      if (modelId) {
        modelId = decodeURIComponent(modelId);
        me.logDebug('Navigate to: ' + modelId);
        store = list.getStore();
        model = store.getById(modelId);
        if (model) {
          list.getSelectionModel().select(model, false, true);
          list.getView().focusRow(model);
          me.onFeedChanged(model);
        }
      }
      else {
        list.getSelectionModel().deselectAll();
      }
    }
  },

  /**
   * @private
   * Open a new tab targeting feed url.
   */
  subscribe: function () {
    var me = this,
        selection = me.getList().getSelectionModel().getSelection();

    if (selection.length) {
      NX.Windows.open(selection[0].get('url'));
    }
  },

  /**
   * @protected
   * Enable 'Subscribe' when a feed is selected.
   */
  bindSubscribeButton: function (button) {
    var me = this;
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