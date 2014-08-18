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
 * Search controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Search', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Bookmarks'
  ],

  stores: [
    'SearchFilter',
    'SearchCriteria',
    'SearchResult',
    'SearchResultVersion'
  ],
  models: [
    'SearchFilter'
  ],

  views: [
    'search.SearchFeature',
    'search.SearchResultDetails',
    'search.SearchResultList',
    'search.SearchResultVersionList',
    'search.TextSearchCriteria',
    'search.SaveSearchFilter'
  ],

  refs: [
    { ref: 'searchFeature', selector: 'nx-searchfeature' },
    { ref: 'searchResult', selector: 'nx-coreui-search-result-list' },
    { ref: 'searchResultDetails', selector: 'nx-searchfeature #searchResultDetails' },
    { ref: 'searchResultVersion', selector: 'nx-coreui-search-result-version-list' },
    { ref: 'storageFileContainer', selector: 'nx-searchfeature nx-coreui-repositorybrowse-storagefilecontainer' },
    { ref: 'quickSearch', selector: 'nx-header-panel #quicksearch' }
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'search-default': {
        file: 'magnifier.png',
        variants: ['x16', 'x32']
      },
      'search-component': {
        file: 'box_front.png',
        variants: ['x16', 'x32']
      },
      'search-component-detail': {
        file: 'box_front_open.png',
        variants: ['x16', 'x32']
      },
      'search-folder': {
        file: 'folder_search.png',
        variants: ['x16', 'x32']
      },
      'search-saved': {
        file: 'magnifier.png',
        variants: ['x16', 'x32']
      }
    });

    me.getApplication().getFeaturesController().registerFeature({
      path: '/Search/Saved',
      mode: 'browse',
      group: true,
      iconName: 'search-folder',
      weight: 500,
      visible: function() {
        return NX.Permissions.check('nexus:repositories', 'read');
      }
    });

    me.getSearchFilterStore().each(function(model) {
      if (model.getId() === 'keyword') {
        me.getApplication().getFeaturesController().registerFeature({
          mode: 'browse',
          path: '/Search',
          description: 'Search for components',
          group: true,
          view: { xtype: 'nx-searchfeature', searchFilter: model, bookmarkEnding: '' },
          iconName: 'search-default',
          weight: 20,
          expanded: false,
          visible: function() {
            return NX.Permissions.check('nexus:repositories', 'read');
          }
        });
      }
      else {
        me.getApplication().getFeaturesController().registerFeature({
          mode: 'browse',
          path: '/Search/' + (model.get('readOnly') ? '' : 'Saved/') + model.get('name'),
          view: { xtype: 'nx-searchfeature', searchFilter: model, bookmarkEnding: '/' + model.getId() },
          iconName: 'search-default',
          description: model.get('description'),
          authenticationRequired: false,
          visible: function() {
            return NX.Permissions.check('nexus:repositories', 'read');
          }
        });
      }
    });

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.onRefresh
        },
        '#Bookmarking': {
          navigate: me.navigateTo
        }
      },
      component: {
        'nx-searchfeature': {
          afterrender: me.initCriterias
        },
        'nx-searchfeature menuitem[action=add]': {
          click: me.addCriteria
        },
        'nx-searchfeature component[searchCriteria=true]': {
          search: me.onSearchCriteriaChange,
          searchcleared: me.onSearchCriteriaChange,
          removed: me.removeCriteria
        },
        'nx-coreui-search-result-list': {
          selectionchange: me.onSearchResultSelectionChange
        },
        'nx-coreui-search-result-version-list': {
          selectionchange: me.onSearchResultVersionSelectionChange
        },
        'nx-searchfeature button[action=save]': {
          click: me.showSaveSearchFilterWindow
        },
        'nx-coreui-search-save button[action=add]': {
          click: me.saveSearchFilter
        },
        'nx-main #quicksearch': {
          afterrender: me.bindQuickSearch,
          search: me.onQuickSearch,
          searchcleared: me.onQuickSearch
        }
      }
    });
  },

  /**
   * @private
   * Show quick search when user has 'nexus:repositories:read' permission.
   */
  bindQuickSearch: function(quickSearch) {
    quickSearch.up('panel').mon(
        NX.Conditions.isPermitted('nexus:repositories', 'read'),
        {
          satisfied: quickSearch.show,
          unsatisfied: quickSearch.hide,
          scope: quickSearch
        }
    );
  },

  /**
   * @private
   * Initialize search criterias (filters) on navigation.
   */
  navigateTo: function() {
    var me = this;

    if (me.getSearchFeature()) {
      me.initCriterias();
    }
  },

  /**
   * @private
   * Initialize search criterias (filters) based on filter definition and (eventual) bookmarked criterias.
   */
  initCriterias: function() {
    var me = this,
        searchPanel = me.getSearchFeature(),
        searchFilter = searchPanel.searchFilter,
        searchCriteriaPanel = searchPanel.down('#criteria'),
        searchCriteriaStore = me.getSearchCriteriaStore(),
        addCriteriaMenu = [],
        bookmarkSegments = NX.Bookmarks.getBookmark().segments,
        bookmarkValues = {},
        criterias = {},
        searchCriteria;

    if (bookmarkSegments && (bookmarkSegments.length > 1)
        && Ext.String.endsWith(NX.Bookmarks.getBookmark().segments[0], 'search' + searchPanel.bookmarkEnding)) {
      Ext.Array.each(Ext.Array.slice(bookmarkSegments, 1), function(segment) {
        var split = segment.split('=');
        if (split.length === 2) {
          bookmarkValues[split[0]] = decodeURIComponent(split[1]);
        }
      });
    }

    searchCriteriaPanel.removeAll();
    me.getSearchResultStore().removeAll();
    me.getSearchResultStore().clearFilter(true);

    if (searchFilter && searchFilter.get('criterias')) {
      Ext.Array.each(Ext.Array.from(searchFilter.get('criterias')), function(criteria) {
        criterias[criteria['id']] = { value: criteria['value'], hidden: criteria['hidden'] };
      });
    }
    Ext.Object.each(bookmarkValues, function(key, value) {
      var existingCriteria = criterias[key];
      if (existingCriteria) {
        existingCriteria['value'] = value;
      }
      else {
        criterias[key] = { value: value };
      }
    });

    Ext.Object.each(criterias, function(id, criteria) {
      var criteriaModel = searchCriteriaStore.getById(id);

      if (criteriaModel) {
        var cmpClass = Ext.ClassManager.getByAlias('widget.nx-searchcriteria-' + criteriaModel.getId());
        if (!cmpClass) {
          cmpClass = Ext.ClassManager.getByAlias('widget.nx-searchcriteria-text');
        }
        searchCriteria = searchCriteriaPanel.add(cmpClass.create(Ext.apply(criteriaModel.get('config'), {
          criteriaId: criteriaModel.getId(),
          value: criteria['value'],
          hidden: criteria['hidden']
        })));
        if (searchCriteria.value) {
          me.applyFilter(searchCriteria, false);
        }
      }
    });

    searchCriteriaStore.each(function(criteria) {
      addCriteriaMenu.push({
        text: criteria.get('config').fieldLabel,
        criteria: criteria,
        action: 'add'
      });
    });

    searchCriteriaPanel.add({
      xtype: 'button',
      itemId: 'addButton',
      text: 'More Criteria',
      glyph: 'xf055@FontAwesome' /* fa-plus-circle */,
      menu: addCriteriaMenu
    });

    // HACK: fire a fake event to force paging toolbar to refresh
    me.getSearchResultStore().fireEvent('load', me);
    me.getSearchResultStore().filter();
  },

  /**
   * @private
   * Add a criteria.
   * @param menuitem selected criteria menu item
   */
  addCriteria: function(menuitem) {
    var me = this,
        searchPanel = me.getSearchFeature(),
        searchCriteriaPanel = searchPanel.down('#criteria'),
        addButton = searchCriteriaPanel.down('#addButton'),
        criteria = menuitem.criteria,
        cmpClass = Ext.ClassManager.getByAlias('widget.nx-searchcriteria-' + criteria.getId());

    if (!cmpClass) {
      cmpClass = Ext.ClassManager.getByAlias('widget.nx-searchcriteria-text');
    }
    searchCriteriaPanel.remove(addButton, false);
    searchCriteriaPanel.add(cmpClass.create(
        Ext.apply(criteria.get('config'), { criteriaId: criteria.getId(), removable: true })
    ));
    searchCriteriaPanel.add(addButton);
  },

  /**
   * @private
   * Remove a criteria.
   * @param searchCriteria removed search criteria
   */
  removeCriteria: function(searchCriteria) {
    var me = this,
        searchPanel = me.getSearchFeature(),
        searchCriteriaPanel = searchPanel.down('#criteria');

    me.applyFilter({ criteriaId: searchCriteria.criteriaId }, true);
    searchCriteriaPanel.remove(searchCriteria);
  },

  /**
   * @private
   * Start searching on criteria value changed.
   * @param searchCriteria changed criteria
   */
  onSearchCriteriaChange: function(searchCriteria) {
    var me = this;
    me.applyFilter(searchCriteria, true);
  },

  /**
   * @private
   * Search on refresh.
   */
  onRefresh: function() {
    var me = this;

    if (me.getSearchFeature()) {
      me.getSearchResultStore().filter();
    }
  },

  /**
   * @private
   * Synchronize store filters with search criteria.
   * @param searchCriteria criteria to be synced
   * @param apply if filter should be applied on store ( = remote call)
   */
  applyFilter: function(searchCriteria, apply) {
    var me = this,
        store = me.getSearchResultStore(),
        filter = searchCriteria.filter;

    if (filter && Ext.isFunction(filter) && !(filter instanceof Ext.util.Filter)) {
      filter = searchCriteria.filter();
    }

    if (filter) {
      store.addFilter(Ext.apply(filter, { id: searchCriteria.criteriaId }), apply);
    }
    else {
      // TODO code bellow is a workaround stores not removing filters when remoteFilter = true
      store.removeFilter(searchCriteria.criteriaId);
      if (store.filters.removeAtKey(searchCriteria.criteriaId) && apply) {
        if (store.filters.length) {
          store.filter();
        }
        else {
          store.clearFilter();
        }
        store.fireEvent('filterchange', store, store.filters.items);
      }
    }

    if (apply) {
      me.onSearchResultSelectionChange(me.getSearchResult().getSelectionModel(), []);
      me.bookmarkFilters();
    }
  },

  /**
   * @private
   * Show details and load version of selected search result.
   * @param selectionModel search result grid selection model
   * @param selected selected search result
   */
  onSearchResultSelectionChange: function(selectionModel, selected) {
    var me = this,
        searchResultModel = selected[0],
        searchResultVersion = me.getSearchResultVersion(),
        searchResultDetails = me.getSearchResultDetails(),
        searchResultVersionStore = me.getSearchResultVersionStore();

    me.onSearchResultVersionSelectionChange(me.getSearchResultVersion().getSelectionModel(), []);
    if (searchResultModel) {
      searchResultDetails.items.get(0).hide();
      searchResultDetails.items.get(1).show();
      searchResultDetails.items.get(1).items.get(1).showInfo({
        'Group': searchResultModel.get('groupId'),
        'Name': searchResultModel.get('artifactId'),
        'Format': searchResultModel.get('format')
      });
      searchResultVersion.show();
      searchResultVersionStore.clearFilter(true);
      searchResultVersionStore.addFilter(me.getSearchResultStore().filters.items, false);
      searchResultVersionStore.addFilter([
        {
          property: 'groupid',
          value: searchResultModel.get('groupId')
        },
        {
          property: 'artifactid',
          value: searchResultModel.get('artifactId')
        }
      ]);
    }
    else {
      searchResultDetails.items.get(0).show();
      searchResultDetails.items.get(1).hide();
      searchResultVersion.hide();
    }
  },

  /**
   * @private
   * Show storage file of selected version of search result.
   * @param selectionModel search result grid selection model
   * @param selected selected search result
   */
  onSearchResultVersionSelectionChange: function(selectionModel, selected) {
    var me = this,
        searchResultVersionModel = selected[0],
        storageFileContainer = me.getStorageFileContainer();

    if (searchResultVersionModel) {
      storageFileContainer.showStorageFile(
          searchResultVersionModel.get('repositoryId'),
          searchResultVersionModel.get('path'),
          searchResultVersionModel.get('type')
      );
      storageFileContainer.expand();
    }
    else {
      storageFileContainer.showStorageFile();
    }
  },

  /**
   * @private
   * Show "Save Search Filter" window.
   */
  showSaveSearchFilterWindow: function() {
    Ext.widget('nx-coreui-search-save');
  },

  /**
   * @private
   * Save a search filter.
   * @param {Ext.button.Button} button 'Add' button from "Save Search Filter"
   */
  saveSearchFilter: function(button) {
    var me = this,
        win = button.up('window'),
        values = button.up('form').getValues(),
        criterias = [],
        model;

    Ext.Array.each(Ext.ComponentQuery.query('nx-searchfeature component[searchCriteria=true]'), function(cmp) {
      criterias.push({
        id: cmp.criteriaId,
        value: cmp.getValue(),
        hidden: cmp.hidden
      });
    });

    model = me.getSearchFilterModel().create(Ext.apply(values, {
      id: values.name,
      criterias: criterias,
      readOnly: false
    }));

    me.getSearchFilterStore().add(model);

    me.getApplication().getFeaturesController().registerFeature({
      path: '/Search/' + (model.get('readOnly') ? '' : 'Saved/') + model.get('name'),
      mode: 'browse',
      view: { xtype: 'nx-searchfeature', searchFilter: model },
      iconName: 'search-saved',
      description: model.get('description'),
      authenticationRequired: false
    });

    me.getController('Menu').refreshTree();
    NX.Bookmarks.navigateTo(NX.Bookmarks.fromToken('browse/search/saved/' + model.get('name')));

    win.close();
  },

  /**
   * @private
   * Bookmark search values.
   */
  bookmarkFilters: function() {
    var me = this,
        segments = [NX.Bookmarks.getBookmark().getSegment(0)];

    Ext.Array.each(Ext.ComponentQuery.query('nx-searchfeature component[searchCriteria=true]'), function(cmp) {
      if (cmp.getValue() && !cmp.isHidden()) {
        segments.push(cmp.criteriaId + '=' + encodeURIComponent(cmp.getValue()));
      }
    });

    NX.Bookmarks.bookmark(NX.Bookmarks.fromSegments(segments), me);
  },

  /**
   * @private
   * @param {NX.ext.SearchBox} quickSearch search box
   * @param {String} searchValue search value
   */
  onQuickSearch: function(quickSearch, searchValue) {
    var me = this,
        searchFeature = me.getSearchFeature();

    if (!searchFeature || (searchFeature.searchFilter.getId() !== 'keyword')) {
      if (searchValue) {
        NX.Bookmarks.navigateTo(
            NX.Bookmarks.fromSegments(['browse/search', 'keyword=' + encodeURIComponent(searchValue)]),
            me
        );
      }
    }
    else {
      searchFeature.down('#criteria component[criteriaId=keyword]').setValue(searchValue);
    }
  }

});