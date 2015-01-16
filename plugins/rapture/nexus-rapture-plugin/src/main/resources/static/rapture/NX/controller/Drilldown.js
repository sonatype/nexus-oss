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
 * Abstract Master/Detail controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.Drilldown', {
  extend: 'Ext.app.Controller',
  requires: [
    // many impls use this
    'NX.view.info.Panel',
    'NX.view.info.Entry',
    'NX.Conditions',
    'NX.Dialogs',
    'NX.Bookmarks',
    'NX.view.drilldown.Drilldown',
    'NX.view.drilldown.Item'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'drilldown.Drilldown',
    'drilldown.Details'
  ],

  permission: undefined,

  getDescription: Ext.emptyFn,

  // List of xtypes
  masters: null,

  /**
   * @cfg {Function} optional function to be called on delete
   */
  deleteModel: undefined,

  onLaunch: function () {
    var me = this;
    me.getApplication().getIconController().addIcons({
      'drilldown-info': {
        file: 'information.png',
        variants: ['x16', 'x32']
      },
      'drilldown-warning': {
        file: 'warning.png',
        variants: ['x16', 'x32']
      }
    });
  },

  /**
   * @private
   */
  init: function () {
    var me = this,
        componentListener = {};

    // Normalize lists into an array
    if (!me.masters) {
      me.masters = [];
    } else if (!Ext.isArray(me.masters)) {
      me.masters = [me.masters];
    }

    // Add event handlers to each list
    for (var i = 0; i < me.masters.length; ++i) {
      componentListener[me.masters[i]] = {
        afterrender: me.onAfterRender,
        selection: me.onSelection,
        cellclick: me.onCellClick
      };
    }

    // If there’s at least one list, add event handlers to buttons and tabs
    if (me.masters.length > 0) {

      // New button
      componentListener[me.masters[0] + ' ^ nx-drilldown button[action=new]'] = {
        afterrender: me.bindNewButton
      };

      // Detail tabs
      componentListener[me.masters[0] + ' ^ nx-drilldown nx-drilldown-details > tabpanel'] = {
        tabchange: function() {
          // Get the model for the last master
          var segments = NX.Bookmarks.getBookmark().getSegments().slice(1);
          var lists = me.getLists();
          var modelId = segments[lists.length - 1];
          var model = lists[lists.length - 1].getStore().getById(modelId);

          // Bookmark it. The tab (if selected) will be added automatically
          me.bookmark(model);
        }
      };

      // Delete button
      if (me.deleteModel) {
        componentListener[me.masters[0] + ' ^ nx-drilldown button[action=delete]'] = {
          afterrender: me.bindDeleteButton,
          click: me.onDelete
        };
      }
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

  /**
   * @private
   * Return references to all of the master views
   */
  getLists: function() {
    var me = this,
      feature = me.getFeature(),
      lists = [];

    if (feature) {
      for (var i = 0; i < me.masters.length; ++i) {
        lists.push(feature.down(me.masters[i]));
      }
    }

    return lists;
  },

  /**
   * @private
   * Whenever the first list loads, trigger a navigation event
   */
  onAfterRender: function () {
    var me = this,
      lists = me.getLists();

    // Trigger navigation when the first list loads
    lists[0].mon(lists[0].getStore(), 'load', me.onStoreLoad, me);

    me.loadStore(Ext.emptyFn);
  },

  /**
   * @public
   * Prompts a reset/reload of the first list in the drilldown
   *
   * @param cb Call this once the store has loaded
   */
  loadStore: function (cb) {
    var me = this,
        lists = me.getLists();

    lists[0].getStore().clearFilter();
    lists[0].getStore().load(cb);
  },

  /**
   * @public
   * @param modelId The model to bookmark and/or navigate to
   * @param navigate Navigate to the model, or just select it?
   */
  loadStoreAndSelect: function (modelId, navigate) {
    var me = this,
      lists = me.getLists(),
      model;

    me.loadStore(function(records, operations, success) {
      // Find the model belonging to this id, and bookmark it
      for (var i = 0; i < lists.length; ++i) {
        model = lists[i].getStore().getById(modelId);
        if (model) {
          me.bookmark(model);
          if (navigate) {
            me.navigateTo(NX.Bookmarks.getBookmark());
          } else {
            me.onModelChanged(model);
          }
          break;
        }
      }
    });
  },

  /**
   * @private
   * Once all lists have loaded, navigate to the current bookmark
   */
  onStoreLoad: function () {
    var me = this,
      lists = me.getLists();

    // Return if no lists exist
    if (!lists.length) {
      return;
    }

    // Make sure all lists have loaded
    for (var i = 0; i < lists.length; ++i) {
      if (!lists[i]) {
        return;
      }
    }

    me.navigateTo(NX.Bookmarks.getBookmark());
  },

  /**
   * @public
   */
  reselect: function () {
    var me = this,
        lists = me.getLists();

    if (lists.length) {
      me.navigateTo(NX.Bookmarks.getBookmark());
    }
  },

  /**
   * @private
   */
  refreshList: function () {
    var me = this,
        lists = me.getLists();

    if (lists.length) {
      me.loadStore(Ext.emptyFn);
    }
  },

  /**
   * @private
   * When a list item is clicked, display the new view and update the bookmark
   */
  onCellClick: function(list, td, cellIndex, model) {
    var me = this;

    me.loadView(list, model, true);
    me.bookmark(model);
  },

  /**
   * @private
   * A model changed, focus on the new row and update the name of the related drilldown
   */
  onModelChanged: function (model) {
    var me = this,
        lists = me.getLists(),
        feature = me.getFeature();

    if (model) {
      // Find the list to which this model belongs, and focus on it
      for (var i = 0; i < lists.length; ++i) {
        if (lists[i].getView().getNode(model)) {
          lists[i].getSelectionModel().select([model], false, true);
          feature.setItemName(i + 1, me.getDescription(model));
          break;
        }
      }
    }
  },

  /**
   * @public
   * Make the detail view appear, update models and bookmarks
   */
  loadView: function (list, model, animate) {
    var me = this,
      lists = me.getLists(),
      feature = me.getFeature();

    // No model specified, go to the root view
    if (!model) {
      feature.showChild(0, animate);
    }

    // Model specified, find the associated list and show that
    for (var i = 0; i < lists.length; ++i) {
      if (list === lists[i].getView() && model) {
        lists[i].fireEvent("selection", list, model);
        me.onModelChanged(model);

        // Set all child bookmarks
        for (var j = 0; j <= i; ++j) {
          feature.setItemBookmark(j, NX.Bookmarks.fromSegments(NX.Bookmarks.getBookmark().getSegments().slice(0, j + 1)), me);
        }

        // Show the next view in line
        feature.showChild(i + 1, animate);
        break;
      }
    }
  },

  /**
   * @public
   * Make the create wizard appear
   *
   * @param index The zero-based step in the create wizard
   * @param animate Whether to animate the panel into view
   * @param cmp An optional component to load
   */
  loadCreateWizard: function (index, animate, cmp) {
    var me = this,
      feature = me.getFeature();

    // Reset all non-root bookmarks
    for (var i = 1; i <= index; ++i) {
      feature.setItemBookmark(i, null);
    }

    // Show the specified step in the wizard
    feature.showCreateWizard(index, animate, cmp);
  },

  /**
   * @private
   * Bookmark specified model / selected tab.
   */
  bookmark: function (model) {
    var me = this,
        lists = me.getLists(),
        feature = me.getFeature(),
        tabs = feature.down('nx-drilldown-details'),
        bookmark = NX.Bookmarks.getBookmark().getSegments(),
        segments = [],
        selectedTabBookmark,
        index;

    // Add the root element of the bookmark
    segments.push(bookmark.shift());

    // Find all parent models and add them to the bookmark array
    for (index = 0; index < lists.length; ++index) {
      if (!lists[index].getView().getNode(model)) {
        segments.push(bookmark.shift());
      } else {
        // All done adding parents
        break;
      }
    }

    // Add the currently selected model to the bookmark array
    if (model) {
      segments.push(encodeURIComponent(model.getId()));

      // Is this the last list model? And is a tab selected? If so, add it.
      if (tabs && index == lists.length - 1) {
        selectedTabBookmark = tabs.getBookmarkOfSelectedTab();
        if (selectedTabBookmark) {
          segments.push(selectedTabBookmark);
        }
      }
    }

    NX.Bookmarks.bookmark(NX.Bookmarks.fromSegments(segments), me);
  },

  /**
   * @public
   * @param {NX.Bookmark} bookmark to navigate to
   */
  navigateTo: function (bookmark) {
    var me = this,
        lists = me.getLists(),
        list_ids, tab_id = null, model, modelId, index;

    if (lists.length && bookmark) {

      me.logDebug('Navigate to: ' + bookmark.getSegments().join(':'));
      list_ids = bookmark.getSegments().slice(1, lists.length + 1);

      if (list_ids.length > lists.length) {
        // The last ID refers to a tab
        tab_id = list_ids.pop();
      }

      if (list_ids.length || tab_id) {

        // Select rows in all parent lists
        for (index = 0; index < list_ids.length; ++index) {
          modelId = decodeURIComponent(list_ids[index]);

          // Select rows
          model = lists[index].getStore().getById(modelId);
          if (model) {
            lists[index].fireEvent("selection", lists[index], model);
            me.onModelChanged(model);
          }

          // If this is the last list, load its data and attach a callback (if necessary)
          if (index == list_ids.length - 1) {
            me.dataLoadedCallback(lists[index], modelId, tab_id);

            // If the data isn’t loaded yet, return here when it is. Only do this for sub-lists,
            // otherwise, the load event on the first list will trigger navigateTo in an infinite loop
            if (!lists[index].getStore().getById(modelId) && index > 0) {
              lists[index].getStore().load({
                scope: me,
                callback: function () { me.dataLoadedCallback(lists[index], modelId, tab_id); }
              });
            }
            break;
          }
        }
      }
      else {
        lists[0].getSelectionModel().deselectAll();
        me.loadView(lists[0].getView(), null, false);
      }
    }
  },

  /**
   * @private
   * Once a model is loaded, call this to display the related view, selecting any tabs as needed
   */
  dataLoadedCallback: function(list, modelId, tabId) {
    var me = this,
      feature = this.getFeature();

    if (list && list.isVisible()) {
      // Show the referenced view
      modelId = decodeURIComponent(modelId);
      me.loadView(list.getView(), list.getStore().getById(modelId), false);

      // Is a tab specified?
      if (tabId) {
        feature.down('nx-drilldown-details').setActiveTabByBookmark(tabId);
      }
    }
  },

  /**
   * @private
   *
   * FIXME: wire this to work with multiple list views
   */
  onDelete: function () {
    var me = this,
        selection = me.getLists()[0].getSelectionModel().getSelection(),
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
