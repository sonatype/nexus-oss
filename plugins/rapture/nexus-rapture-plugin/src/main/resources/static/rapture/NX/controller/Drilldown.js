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
 * Abstract Master/Detail controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.Drilldown', {
  extend: 'Ext.app.Controller',
  requires: [
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

  /**
   * @protected
   * Get the human-readable name of a model
   */
  getDescription: Ext.emptyFn,

  /**
   * @override
   * An array of xtypes which represent the masters available to this drilldown
   */
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
   * @override
   */
  init: function () {
    var me = this,
        componentListener = {};

    // Normalize lists into an array
    if (!me.masters) {
      me.masters = [];
    }

    // Add event handlers to each list
    for (var i = 0; i < me.masters.length; ++i) {
      componentListener[me.masters[i]] = {
        selection: me.onSelection,
        cellclick: me.onCellClick
      };
    }

    // New button
    componentListener['nx-drilldown button[action=new]'] = {
      afterrender: me.bindNewButton
    };

    // Delete button
    componentListener[me.masters[0] + ' ^ nx-drilldown button[action=delete]'] = {
      afterrender: me.bindDeleteButton,
      click: me.onDelete
    };

    me.listen({
      component: componentListener,
      controller: {
        '#Bookmarking': {
          navigate: me.navigateTo
        },
        '#Refresh': {
          refresh: me.reselect
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
   * @public
   */
  reselect: function () {
    var me = this,
        lists = Ext.ComponentQuery.query('nx-drilldown-master');

    if (lists.length) {
      me.navigateTo(NX.Bookmarks.getBookmark());
    }
  },

  /**
   * @private
   * When a list item is clicked, display the new view and update the bookmark
   */
  onCellClick: function(list, td, cellIndex, model, tr, rowIndex, e) {
    var me = this,
      index = Ext.ComponentQuery.query('nx-drilldown-master').indexOf(list.up('grid'));

    //if the cell target is a link, let it do it's thing
    if(e.getTarget('a')) {
      return false;
    }
    me.loadView(index + 1, true, model);
  },

  /**
   * @private
   * A model changed, focus on the new row and update the name of the related drilldown
   */
  onModelChanged: function (index, model) {
    var me = this,
        lists = Ext.ComponentQuery.query('nx-drilldown-master'),
        feature = me.getFeature();

    lists[index].getSelectionModel().select([model], false, true);
    feature.setItemName(index + 1, me.getDescription(model));
  },

  /**
   * @public
   * Make the detail view appear
   *
   * @param index The zero-based view to load
   * @param animate Whether to animate the panel into view
   * @param model An optional record to select
   */
  loadView: function (index, animate, model) {
    var me = this,
      lists = Ext.ComponentQuery.query('nx-drilldown-master'),
      feature = me.getFeature();

    // Don’t load the view if the feature is not ready
    if (!feature) {
      return;
    }

    // Model specified, select it in the previous list
    if (model && index > 0) {
      lists[index - 1].fireEvent('selection', lists[index - 1], model);
      me.onModelChanged(index - 1, model);
    }
    // Set all child bookmarks
    for (var i = 0; i <= index; ++i) {
      feature.setItemBookmark(i, NX.Bookmarks.fromSegments(NX.Bookmarks.getBookmark().getSegments().slice(0, i + 1)), me);
    }

    // Show the next view in line
    feature.showChild(index, animate);
    me.bookmark(model);
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
        lists = Ext.ComponentQuery.query('nx-drilldown-master'),
        bookmark = NX.Bookmarks.getBookmark().getSegments(),
        segments = [],
        index = 0;

    // Add the root element of the bookmark
    segments.push(bookmark.shift());

    // Find all parent models and add them to the bookmark array
    while (index < lists.length && model && !lists[index].getView().getNode(model)) {
      segments.push(bookmark.shift());
      ++index;
    }

    // Add the currently selected model to the bookmark array
    if (model) {
      segments.push(encodeURIComponent(model.getId()));
    }

    // Set the bookmark
    NX.Bookmarks.bookmark(NX.Bookmarks.fromSegments(segments), me);
  },

  /**
   * @public
   * @param {NX.Bookmark} bookmark to navigate to
   */
  navigateTo: function (bookmark) {
    var me = this,
        feature = me.getFeature(),
        lists = Ext.ComponentQuery.query('nx-drilldown-master'),
        list_ids = bookmark.getSegments().slice(1),
        index, list_ids, modelId, store;

    if (feature && lists.length && list_ids.length) {
      //<if debug>
      me.logDebug('Navigate to: ' + bookmark.getSegments().join(':'));
      //</if>

      modelId = decodeURIComponent(list_ids.pop());
      index = list_ids.length;
      store = lists[index].getStore();

      if (store.isLoading()) {
        // The store hasn’t yet loaded, load it when ready
        me.mon(store, 'load', function() {
          me.selectModel(index, modelId);
          me.mun(store, 'load');
        });
      } else {
        me.selectModel(index, modelId);
      }
    } else {
      me.loadView(0, false);
    }
  },

  /**
   * @private
   * @param index of the list which owns the model
   * @param model to select
   */
  selectModel: function (index, modelId) {
    var me = this,
        lists = Ext.ComponentQuery.query('nx-drilldown-master'),
        store = lists[index].getStore(),
        model;

    // getById() throws an error if a model ID is found, but not cached, check for content first
    if (store.getCount()) {
      model = store.getById(modelId);
      lists[index].fireEvent('selection', lists[index], model);
      me.onModelChanged(index, model);
      me.loadView(index + 1, false, model);
    }
  },

  /**
   * @private
   */
  onDelete: function () {
    var me = this,
        selection = Ext.ComponentQuery.query('nx-drilldown-master')[0].getSelectionModel().getSelection(),
        description;

    if (Ext.isDefined(selection) && selection.length > 0) {
      description = me.getDescription(selection[0]);
      NX.Dialogs.askConfirmation('Confirm deletion?', description, function () {
        me.deleteModel(selection[0]);

        // Reset the bookmark
        NX.Bookmarks.bookmark(NX.Bookmarks.fromToken(NX.Bookmarks.getBookmark().getSegment(0)));
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
        NX.Conditions.isPermitted(me.permission + ':create'),
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
        NX.Conditions.isPermitted(me.permission + ':delete'),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  }

});