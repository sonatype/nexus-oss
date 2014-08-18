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
 * Menu controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.Menu', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Bookmarks',
    'NX.controller.User',
    'NX.controller.Features',
    'NX.Permissions',
    'NX.Security',
    'NX.State'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'feature.Menu',
    'feature.NotFound',
    'feature.NotVisible',
    'header.DashboardMode',
    'header.SearchMode',
    'header.BrowseMode',
    'header.AdminMode'
  ],

  models: [
    'Feature'
  ],
  stores: [
    'Feature',
    'FeatureMenu',
    'FeatureGroup'
  ],

  refs: [
    {
      ref: 'featureMenu',
      selector: 'nx-feature-menu'
    },
    {
      ref: 'headerPanel',
      selector: 'nx-header-panel'
    }
  ],

  /**
   * @private
   * Current mode.
   */
  mode: undefined,

  /**
   * @private
   * Modes discovered by searching all buttons with a mode property.
   */
  availableModes: [],

  bookmarkingEnabled: true,

  /**
   * @private {String}
   * Current selected path
   */
  currentSelectedPath: undefined,

  /**
   * @private {Boolean}
   * True if menu should auto navigate to first available feature.
   */
  navigateToFirstFeature: false,

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'feature-notfound': {
        file: 'feed.png',
        variants: ['x16', 'x32']
      }
    });

    me.listen({
      controller: {
        '#Permissions': {
          changed: me.refreshMenu
        },
        '#State': {
          changed: me.onStateChange
        },
        '#Bookmarking': {
          navigate: me.navigateTo
        },
        '#User': {
          logout: me.onLogout
        }
      },
      component: {
        'nx-feature-menu': {
          select: me.onSelection,
          afterrender: me.refreshMenu
        },
        'nx-header-panel button[mode]': {
          click: me.onModeChanged
        },
        'button[mode]': {
          afterrender: me.registerModeButton,
          destroy: me.unregisterModeButton
        }
      },
      store: {
        '#Feature': {
          update: me.refreshMenu
        }
      }
    });

    me.addEvents(
        /**
         * @event featureselected
         * Fires when a feature is selected.
         * @param {NX.model.Feature} selected feature
         */
        'featureselected'
    );
  },

  /**
   * @public
   * @returns {NX.Bookmark} a bookmark for current selected feature (if any)
   */
  getBookmark: function () {
    var me = this,
        selection = me.getFeatureMenu().getSelectionModel().getSelection();

    return NX.Bookmarks.fromToken(selection.length ? selection[0].get('bookmark') : me.mode);
  },

  /**
   * @private
   */
  onSelection: function (panel, featureMenuModel) {
    var me = this,
        path = featureMenuModel.get('path');

    if ((path !== me.currentSelectedPath) || featureMenuModel.get('group')) {
      me.currentSelectedPath = path;

      //<if debug>
      me.logDebug('Selected feature: ' + path);
      //</if>

      if (!featureMenuModel.get('href')) {
        me.selectFeature(me.getFeatureStore().getById(featureMenuModel.get('path')));
        me.populateFeatureGroupStore(featureMenuModel);
        if (me.bookmarkingEnabled) {
          me.bookmark(featureMenuModel);
        }
      }
    }
  },

  /**
   * @private
   */
  selectFeature: function (featureModel) {
    var me = this,
        path;

    if (featureModel) {
      path = featureModel.get('path');
      if (path && path.length > 0) {
        me.fireEvent('featureselected', featureModel);
      }
    }
  },

  /**
   * Updates the **{@link NX.store.FeatureGroup} store with children of selected feature.
   *
   * @private
   * @param {NX.model.FeatureMenu} record
   */
  populateFeatureGroupStore: function (record) {
    var me = this,
        features = [],
        featureStore = me.getFeatureStore();

    // add all children of the record to the group store, but do not include the node for the current record
    record.eachChild(function (node) {
      node.cascadeBy(function (child) {
        features.push(featureStore.getById(child.get('path')));
      });
    });

    me.getFeatureGroupStore().loadData(features);
  },

  /**
   * @private
   */
  navigateTo: function (bookmark) {
    var me = this,
        node, mode, feature, menuBookmark;

    if (bookmark) {
      menuBookmark = bookmark.getSegment(0);

      //<if debug>
      me.logDebug('Navigate to: ' + menuBookmark);
      //</if>

      mode = me.getMode(bookmark);
      // if we are navigating to a new mode, sync it
      if (me.mode !== mode) {
        me.mode = mode;
        me.refreshModes();
      }
      if (menuBookmark) {
        node = me.getFeatureMenuStore().getRootNode().findChild('bookmark', menuBookmark, true);
      }
      // in case that we do not have a bookmark to navigate to or we have to navigate to first feature,
      // find the first feature
      if (!node && (!Ext.isDefined(menuBookmark) || me.navigateToFirstFeature)) {
        if (!me.mode) {
          me.selectFirstAvailableMode();
          me.refreshModes();
        }
        node = me.getFeatureMenuStore().getRootNode().firstChild;

        //<if debug>
        me.logDebug('Automatically selected: ' + node.get('bookmark'));
        //</if>
      }
      // select the bookmarked feature in menu, if available
      if (node) {
        me.bookmarkingEnabled = me.navigateToFirstFeature;
        me.navigateToFirstFeature = false;
        me.getFeatureMenu().selectPath(node.getPath('text'), 'text', undefined, function () {
          me.bookmarkingEnabled = true;
        });
      }
      else {
        delete me.currentSelectedPath;
        // if the feature to navigate to is not available in menu check out if is hidden (probably no permissions)
        if (menuBookmark) {
          feature = me.getFeatureStore().findRecord('bookmark', menuBookmark, 0, false, false, true);
        }
        me.getFeatureMenu().getSelectionModel().deselectAll();
        if (feature) {
          if (feature.get('authenticationRequired') && NX.Permissions.available()) {
            //<if debug>
            me.logDebug('Asking user to authenticate as feature exists but is not visible');
            //</if>
            NX.Security.askToAuthenticate();
          }
          me.selectFeature(me.createNotAvailableFeature(feature));
        }
        else {
          // as feature does not exist at all, show teh 403 like content
          me.selectFeature(me.createNotFoundFeature(menuBookmark));
        }
      }
    }
  },

  onLogout: function () {
    var me = this;
    me.navigateToFirstFeature = true;
  },

  /**
   * @private
   * On a state change check features visibility and trigger a menu refresh if necessary.
   */
  onStateChange: function () {
    var me = this,
        shouldRefresh = false;

    me.getFeatureStore().each(function (feature) {
      var visible, previousVisible;
      if (feature.get('mode') === me.mode) {
        visible = feature.get('visible')();
        previousVisible = me.getFeatureMenuStore().getRootNode().findChild('path', feature.get('path'), true) !== null;
        shouldRefresh = (visible !== previousVisible);
      }
      return !shouldRefresh;
    });

    if (shouldRefresh) {
      me.refreshMenu();
    }
  },

  /**
   * @private
   */
  bookmark: function (node) {
    var me = this,
        bookmark = node.get('bookmark');

    if (!(NX.Bookmarks.getBookmark().getSegment(0) === bookmark)) {
      NX.Bookmarks.bookmark(NX.Bookmarks.fromToken(bookmark), me);
    }
  },

  /**
   * @public
   * Refresh modes & feature menu.
   */
  refreshMenu: function () {
    var me = this;

    //<if debug>
    me.logDebug('Refreshing menu (mode ' + me.mode + ')');
    //</if>

    me.refreshVisibleModes();
    me.refreshTree();
    me.toggleMenu();
    me.navigateTo(NX.Bookmarks.getBookmark());
  },

  /**
   * @private
   * Refreshes modes buttons based on the fact that there are features visible for that mode or not.
   * In case that current mode is no longer visible, auto selects a new one.
   */
  refreshVisibleModes: function () {
    var me = this,
        visibleModes = [],
        feature;

    me.getFeatureStore().each(function (rec) {
      feature = rec.getData();
      if (feature.visible() && !feature.group && visibleModes.indexOf(feature.mode) === -1) {
        visibleModes.push(feature.mode);
      }
    });

    //<if debug>
    me.logDebug('Visible modes: ' + visibleModes);
    //</if>

    Ext.each(me.availableModes, function (button) {
      button.toggle(false, true);
      if (button.autoHide) {
        if (visibleModes.indexOf(button.mode) > -1) {
          button.show();
        }
        else {
          button.hide();
        }
      }
    });

    me.refreshModeButtons();
  },

  refreshModeButtons: function () {
    var me = this,
        headerPanel = me.getHeaderPanel(),
        modeButton;

    Ext.each(me.availableModes, function (button) {
      button.toggle(false, true);
    });

    if (me.mode) {
      modeButton = headerPanel.down('button[mode=' + me.mode + ']');
      if (!modeButton || modeButton.isHidden()) {
        delete me.mode;
      }
    }
    if (me.mode) {
      modeButton = headerPanel.down('button[mode=' + me.mode + ']');
      modeButton.toggle(true, true);
    }
  },

  /**
   * @private
   * Automatically expand/collapse menu if there is only one feature to display and button is configured to do so.
   */
  toggleMenu: function () {
    var me = this,
        menu = me.getFeatureMenu(),
        menuCollapsed = false,
        numberOfFeatures = me.getFeatureMenuStore().getRootNode().childNodes.length;

    if (me.mode) {
      menu.show();

      if (numberOfFeatures <= 1) {
        Ext.each(me.availableModes, function (button) {
          if ((me.mode === button.mode) && (button.collapseMenu === true)) {
            menuCollapsed = true;
          }
        });
      }

      // expand/collapse w/o animation to avoid problems with undefined panel placeholder.el
      if (menuCollapsed) {
        menu.collapse(undefined, false);
      }
      else {
        menu.expand(false);
      }
    }
    else {
      menu.hide();
    }
  },

  refreshTree: function () {
    var me = this,
        menuTitle = me.mode,
        groupsToRemove = [],
        feature, segments, parent, child, modeButton;

    //<if debug>
    me.logDebug('Refreshing tree (mode ' + me.mode + ')');
    //</if>

    Ext.suspendLayouts();

    modeButton = me.getHeaderPanel().down('button[mode=' + me.mode + ']');
    if (modeButton && modeButton.title) {
      menuTitle = modeButton.title;
    }
    me.getFeatureMenu().setTitle(menuTitle);

    me.getFeatureMenuStore().getRootNode().removeAll();

    // create leafs and all parent groups of those leafs
    me.getFeatureStore().each(function (rec) {
      feature = rec.getData();
      // iterate only visible features
      if ((me.mode === feature.mode) && feature.visible()) {
        segments = feature.path.split('/');
        parent = me.getFeatureMenuStore().getRootNode();
        for (var i = 2; i < segments.length; i++) {
          child = parent.findChild('text', segments[i], false);
          if (child) {
            if (i < segments.length - 1) {
              child.data = Ext.apply(child.data, {
                leaf: false
              });
            }
          }
          else {
            if (i < segments.length - 1) {
              // create the group
              child = parent.appendChild({
                text: segments[i],
                leaf: false,
                // expand the menu by default
                expanded: true
              });
            }
            else {
              // create the leaf
              child = parent.appendChild(Ext.apply(feature, {
                leaf: true,
                iconCls: NX.Icons.cls(feature.iconName, 'x16'),
                qtip: feature.description
              }));
            }
          }
          parent = child;
        }
      }
    });

    // remove all groups without children
    me.getFeatureMenuStore().getRootNode().cascadeBy(function (node) {
      if (node.get('group') && !node.hasChildNodes()) {
        groupsToRemove.push(node);
      }
    });
    Ext.Array.each(groupsToRemove, function (node) {
      node.parentNode.removeChild(node, true);
    });

    me.getFeatureMenuStore().sort([
      { property: 'weight', direction: 'ASC' },
      { property: 'text', direction: 'ASC' }
    ]);

    Ext.resumeLayouts(true);
  },

  createNotAvailableFeature: function (feature) {
    var me = this;
    return me.getFeatureModel().create({
      path: feature.get('path'),
      description: feature.get('description'),
      iconName: feature.get('iconName'),
      view: {
        xtype: 'nx-feature-notvisible',
        text: feature.get('text') + ' feature is not available as '
            + (NX.State.getValue('user') ? ' you do not have the required permissions' : ' you are not logged in')
      },
      visible: NX.controller.Features.alwaysVisible
    });
  },

  createNotFoundFeature: function (bookmark) {
    var me = this;
    return me.getFeatureModel().create({
      path: '/Not Found',
      description: bookmark,
      iconName: 'feature-notfound',
      view: {
        xtype: 'nx-feature-notfound',
        path: bookmark
      },
      visible: NX.controller.Features.alwaysVisible
    });
  },

  getMode: function (bookmark) {
    if (bookmark && bookmark.getSegment(0)) {
      return bookmark.getSegment(0).split('/')[0]
    }
    return undefined;
  },

  /**
   * @private
   */
  onModeChanged: function (button) {
    var me = this,
        mode = button.mode;

    me.changeMode(mode);
  },

  /**
   * @public
   * Change mode.
   * @param {String} mode to change to
   */
  changeMode: function (mode) {
    var me = this;

    //<if debug>
    me.logDebug('Mode changed: ' + mode);
    //</if>

    me.mode = mode;
    me.refreshTree();
    me.toggleMenu();
    me.navigateTo(NX.Bookmarks.fromToken(me.getFeatureMenuStore().getRootNode().firstChild.get('bookmark')));
    NX.Bookmarks.bookmark(me.getBookmark());
  },

  /**
   * @private
   * Register a mode button.
   */
  registerModeButton: function (button) {
    var me = this;

    me.availableModes.push(button);
  },

  /**
   * @private
   * Unregister a mode button.
   */
  unregisterModeButton: function (button) {
    var me = this;

    Ext.Array.remove(me.availableModes, button);
  },

  selectFirstAvailableMode: function () {
    var me = this;
    Ext.each(me.availableModes, function (button) {
      if (!button.isHidden()) {
        me.mode = button.mode;
        return false;
      }
      return true;
    });

    //<if debug>
    me.logDebug('Auto selecting mode: ' + me.mode);
    //</if>
  },

  refreshModes: function () {
    var me = this;
    me.refreshModeButtons();
    me.refreshTree();
    me.toggleMenu();
  }

});