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
 * Abstract Master/Detail controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.MasterDetail', {
  extend: 'Ext.app.Controller',
  requires: [
    // many impls use this
    'NX.view.info.Panel',
    'NX.view.info.Entry',
    'NX.Conditions',
    'NX.Dialogs',
    'NX.Bookmarks'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'masterdetail.Panel',
    'masterdetail.Tabs'
  ],

  permission: undefined,

  getDescription: Ext.emptyFn,

  /**
   * @cfg {Function} optional function to be called on delete
   */
  deleteModel: undefined,

  onLaunch: function () {
    var me = this;
    me.getApplication().getIconController().addIcons({
      'masterdetail-info': {
        file: 'information.png',
        variants: ['x16', 'x32']
      },
      'masterdetail-warning': {
        file: 'warning.png',
        variants: ['x16', 'x32']
      }
    });
  },

  init: function () {
    var me = this,
        componentListener = {};

    componentListener[me.list] = {
      afterrender: me.onAfterRender,
      selection: me.onSelection,
      cellclick: me.onCellClick
    };
    componentListener[me.list + ' button[action=new]'] = {
      afterrender: me.bindNewButton
    };
    componentListener[me.list + ' ^ nx-masterdetail-panel nx-masterdetail-tabs > tabpanel'] = {
      tabchange: function() {
        var selected = me.getList().getSelectionModel().getSelection();
        me.bookmark(selected.length === 1 ? selected[0] : null);
      }
    };

    // bind to a delete button if delete function defined
    if (me.deleteModel) {
      componentListener[me.list + ' ^ nx-masterdetail-panel nx-masterdetail-tabs button[action=delete]'] = {
        afterrender: me.bindDeleteButton,
        click: me.onDelete
      };
    }

    me.listen({
      component: componentListener,
      controller: {
        '#Bookmarking': {
          navigate: me.navigateTo
        },
        '#Refresh': {
          refresh: me.refreshList
        }
      }
    });

    if (me.icons) {
      me.getApplication().getIconController().addIcons(me.icons);
    }
    if (me.features) {
      me.getApplication().getFeaturesController().registerFeature(me.features, me);
    }
  },

  loadStore: function () {
    var me = this,
        list = me.getList();

    if (list) {
      list.getStore().load();
    }
  },

  loadStoreAndSelect: function (modelId) {
    var me = this;

    if (modelId) {
      me.bookmarkAt(modelId);
    }

    me.loadStore();
  },

  onStoreLoad: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.navigateTo(NX.Bookmarks.getBookmark());
    }
  },

  reselect: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.navigateTo(NX.Bookmarks.getBookmark());
    }
  },

  refreshList: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.loadStore();
    }
  },

  onAfterRender: function () {
    var me = this,
        list = me.getList();

    list.mon(list.getStore(), 'load', me.onStoreLoad, me);
    me.loadStore();
  },

  onCellClick: function(list, td, cellIndex, model) {
    var me = this;

    me.loadView(list, model, true);
    me.bookmark(model);
  },

  onModelChanged: function (model) {
    var me = this,
        list = me.getList(),
        masterdetail = list.up('nx-masterdetail-panel');

    if (model) {
      list.getView().focusRow(model);
      masterdetail.setDescription(me.getDescription(model));
    }
    else {
      masterdetail.setDescription('Empty selection');
    }
  },

  /**
   * Make the detail view appear, update models and bookmarks
   */
  loadView: function (list, model, animate) {
    var me = this;

    if (model) {
      me.getList().fireEvent("selection", list, model);
      me.onModelChanged(model);
      me.getList().up('#nx-drilldown').setItemBookmark(0, NX.Bookmarks.fromToken(NX.Bookmarks.getBookmark().getSegment(0)), me);
      me.getList().up('#nx-drilldown').showChild(1, animate);
    } else {
      me.getList().up('#nx-drilldown').showChild(0, animate);
    }
  },

  /**
   * Bookmark current selected model / selected tab.
   */
  bookmark: function (model) {
    var me = this,
        modelId;

    if (model && model.getId) {
      modelId = model.getId();
    }

    me.bookmarkAt(modelId);
  },

  /**
   * Bookmark specified model / selected tab.
   */
  bookmarkAt: function (modelId) {
    var me = this,
        list = me.getList(),
        tabs = list.up('nx-masterdetail-panel').down('nx-masterdetail-tabs'),
        bookmark = NX.Bookmarks.fromToken(NX.Bookmarks.getBookmark().getSegment(0)),
        segments = [],
        selectedTabBookmark;

    if (modelId) {
      segments.push(encodeURIComponent(modelId));
      selectedTabBookmark = tabs.getBookmarkOfSelectedTab();
      if (selectedTabBookmark) {
        segments.push(selectedTabBookmark);
      }
      bookmark.appendSegments(segments);
    }
    NX.Bookmarks.bookmark(bookmark, me);
  },

  /**
   * @public
   * @param {NX.Bookmark} bookmark to navigate to
   */
  navigateTo: function (bookmark) {
    var me = this,
        list = me.getList(),
        store, modelId, tabBookmark, model, tabs;

    if (list && bookmark) {
      modelId = bookmark.getSegment(1);
      tabBookmark = bookmark.getSegment(2);
      if (modelId) {
        modelId = decodeURIComponent(modelId);
        me.logDebug('Navigate to: ' + modelId + (tabBookmark ? ":" + tabBookmark : ''));
        store = list.getStore();
        model = store.getById(modelId);
        if (model) {
          list.getSelectionModel().deselectAll(true);
          list.getSelectionModel().select(model, false, true);
          list.getView().focusRow(model);
          me.loadView(list, model, false);
          list.fireEvent('selectionchange', list, [model]);
        }
        if (tabBookmark) {
          list.up('nx-masterdetail-panel').down('nx-masterdetail-tabs').setActiveTabByBookmark(tabBookmark);
        }
      }
      else {
        list.getSelectionModel().deselectAll();
        me.loadView(list, null, false);
      }
    }
  },

  onDelete: function () {
    var me = this,
        selection = me.getList().getSelectionModel().getSelection(),
        description;

    if (Ext.isDefined(selection) && selection.length > 0) {
      description = me.getDescription(selection[0]);
      NX.Dialogs.askConfirmation('Confirm deletion?', description, function () {
        me.deleteModel(selection[0]);
        me.bookmark(null);
      }, {scope: me});
    }
  },

  /**
   * @returns {Ext.data.Model} selected model if there is a selection, undefined otherwise
   */
  selectedModel: function () {
    var me = this,
        list = me.getList(),
        selection, model;

    if (list) {
      selection = list.getSelectionModel().getSelection();
      if (selection.length) {
        model = selection[0];
      }
    }
    return model;
  },

  /**
   * @protected
   * Enable 'New' when user has 'create' permission.
   */
  bindNewButton: function (button) {
    var me = this;
    button.mon(
        NX.Conditions.isPermitted(me.permission, 'create'),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @protected
   * Enable 'Delete' when user has 'delete' permission.
   */
  bindDeleteButton: function (button) {
    var me = this;
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted(me.permission, 'delete')
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  }

});
