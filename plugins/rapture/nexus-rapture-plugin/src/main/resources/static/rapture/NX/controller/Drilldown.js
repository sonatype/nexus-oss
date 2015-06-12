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
    } else if (!Ext.isArray(me.masters)) {
      me.masters = [me.masters];
    }

    // Add event handlers to each list
    for (var i = 0; i < me.masters.length; ++i) {
      componentListener[me.masters[i]] = {
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
          var lists = me.getFeature().query('nx-drilldown-master');
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
          //refresh: me.refreshList
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
        lists = me.getFeature().query('nx-drilldown-master');

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
      index = me.getFeature().query('nx-drilldown-master').indexOf(list.up('grid'));

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
        lists = me.getFeature().query('nx-drilldown-master'),
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
      lists = me.getFeature().query('nx-drilldown-master'),
      feature = me.getFeature();

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
        lists = me.getFeature().query('nx-drilldown-master'),
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
      if (model && !lists[index].getView().getNode(model)) {
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
        lists = me.getFeature().query('nx-drilldown-master'),
        list_ids, tab_id = null, model, modelId, index;

    if (lists.length && bookmark) {
      //<if debug>
      me.logDebug('Navigate to: ' + bookmark.getSegments().join(':'));
      //</if>

      list_ids = bookmark.getSegments().slice(1);

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
            lists[index].fireEvent('selection', lists[index], model);
            me.onModelChanged(index, model);
          }

          // If this is the last list, load its data and attach a callback (if necessary)
          if (index == list_ids.length - 1) {

            // If the data isn’t loaded yet, return here when it is
            if (!lists[index].getStore().getById(modelId)) {
              lists[index].getStore().load(function () {
                me.dataLoadedCallback(index, modelId, tab_id);
              });
            } else {
              me.dataLoadedCallback(index, modelId, tab_id);
            }
            break;
          }
        }
      }
      else {
        lists[0].getSelectionModel().deselectAll();
        me.loadView(0, false);
      }
    }
  },

  /**
   * @private
   * Once a model is loaded, call this to display the related view, selecting any tabs as needed
   */
  dataLoadedCallback: function(index, modelId, tabId) {
    var me = this,
      feature = this.getFeature(),
      lists = me.getFeature().query('nx-drilldown-master'),
      list = lists[index],
      model;

    if (list && list.isVisible() && !list.getStore().isLoading()) {
      // Show the referenced view
      modelId = decodeURIComponent(modelId);
      model = list.getStore().getById(modelId);

      // Is a tab specified?
      if (tabId) {
        feature.down('nx-drilldown-details').setActiveTabByBookmark(tabId);
      }

      me.loadView(index + 1, false, model);
    }
  },

  /**
   * @private
   *
   * FIXME: wire this to work with multiple list views
   */
  onDelete: function () {
    var me = this,
        selection = me.getFeature().query('nx-drilldown-master')[0].getSelectionModel().getSelection(),
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
        NX.Conditions.and(
            NX.Conditions.isPermitted(me.permission + ':delete')
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  }

});
