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
/**
 * Search controller.
 * FIXME move to core ui
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Search', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Bookmarks'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  stores: [
    'SearchFilter',
    'SearchCriteria',
    'SearchResult'
  ],
  models: [
    'SearchFilter'
  ],

  views: [
    'search.SearchFeature',
    'search.TextSearchCriteria',
    'search.SaveSearchFilter'
  ],

  refs: [
    {
      ref: 'searchFeature',
      selector: 'nx-searchfeature'
    },
    {
      ref: 'quickSearch',
      selector: 'nx-header-panel #quicksearch'
    },
    {
      ref: 'componentDetail',
      selector: 'nx-coreui-component-detail'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'search-default': {
        file: 'magnifier.png',
        variants: ['x16', 'x32']
      }
    });

    me.getApplication().getFeaturesController().registerFeature({
      path: '/Saved',
      mode: 'search',
      group: true,
      iconName: 'search-default',
      weight: 500
    });

    me.getSearchFilterStore().each(function (model) {
      me.getApplication().getFeaturesController().registerFeature({
        mode: 'search',
        path: '/' + (model.get('readOnly') ? '' : 'Saved/') + model.get('name'),
        view: { xtype: 'nx-searchfeature', searchFilter: model },
        iconName: model.get('iconName') ? model.get('iconName') : 'search-default',
        description: model.get('description'),
        authenticationRequired: false
      });
    });

    me.listen({
      component: {
        'nx-searchfeature': {
          afterrender: me.initCriterias
        },
        'nx-searchfeature menuitem[action=add]': {
          click: me.addCriteria
        },
        'nx-searchfeature component[searchCriteria=true]': {
          search: me.onSearchCriteriaChange,
          searchcleared: me.onSearchCriteriaChange
        },
        'nx-searchfeature grid': {
          selectionchange: me.onSelectionChange
        },
        'nx-searchfeature button[action=save]': {
          click: me.showSaveSearchFilterWindow
        },
        'nx-coreui-search-save button[action=add]': {
          click: me.saveSearchFilter
        },
        'nx-main #quicksearch': {
          search: me.onQuickSearch
        }
      }
    });

  },

  initCriterias: function () {
    var me = this,
        searchPanel = me.getSearchFeature(),
        searchFilter = searchPanel.searchFilter,
        searchCriteriaPanel = searchPanel.down('#criteria'),
        searchCriteriaStore = me.getSearchCriteriaStore(),
        addCriteriaMenu = [],
        bookmarkSegments = NX.Bookmarks.getBookmark().segments,
        bookmarkValues = {},
        searchCriteria;

    if (bookmarkSegments && bookmarkSegments.length > 1) {
      Ext.Array.each(Ext.Array.slice(bookmarkSegments, 1), function (segment) {
        var split = segment.split('=');
        if (split.length == 2) {
          bookmarkValues[split[0]] = decodeURIComponent(split[1]);
        }
      });
    }

    me.getSearchResultStore().removeAll();
    me.getSearchResultStore().clearFilter(true);

    if (searchFilter && searchFilter.get('criterias')) {
      Ext.Array.each(Ext.Array.from(searchFilter.get('criterias')), function (criteriaRef) {
        var criteria = searchCriteriaStore.getById(criteriaRef.id);

        if (criteria) {
          var cmpClass = Ext.ClassManager.getByAlias('widget.nx-searchcriteria-' + criteria.getId());
          if (!cmpClass) {
            cmpClass = Ext.ClassManager.getByAlias('widget.nx-searchcriteria-text');
          }
          searchCriteria = searchCriteriaPanel.add(cmpClass.create(Ext.apply(criteria.get('config'), {
            criteriaId: criteria.getId(),
            value: bookmarkValues[criteria.getId()] || criteriaRef.value,
            hidden: criteriaRef.hidden
          })));
          if (criteriaRef.value) {
            me.applyFilter(searchCriteria, false);
          }
        }
      });
    }

    searchCriteriaStore.each(function (criteria) {
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

    me.getSearchResultStore().filter();
  },

  addCriteria: function (menuitem) {
    var me = this,
        searchPanel = me.getSearchFeature(),
        searchCriteria = searchPanel.down('#criteria'),
        addButton = searchCriteria.down('#addButton'),
        criteria = menuitem.criteria,
        cmpClass = Ext.ClassManager.getByAlias('widget.nx-searchcriteria-' + criteria.getId());

    if (!cmpClass) {
      cmpClass = Ext.ClassManager.getByAlias('widget.nx-searchcriteria-text');
    }
    searchCriteria.remove(addButton, false);
    searchCriteria.add(cmpClass.create(Ext.apply(criteria.get('config'), { criteriaId: criteria.getId() })));
    searchCriteria.add(addButton);
  },

  onSearchCriteriaChange: function (searchCriteria) {
    var me = this;
    me.applyFilter(searchCriteria, true);
  },

  applyFilter: function (searchCriteria, apply) {
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
      // store.removeFilter(searchCriteria.criteriaId);
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
  },

  onSelectionChange: function (selectionModel, selected) {
    var me = this;
    me.getComponentDetail().setComponent(selected[0] ? selected[0].data : undefined);
  },

  showSaveSearchFilterWindow: function () {
    Ext.widget('nx-coreui-search-save');
  },

  saveSearchFilter: function (button) {
    var me = this,
        win = button.up('window'),
        values = button.up('form').getValues(),
        criterias = [],
        model;

    Ext.Array.each(Ext.ComponentQuery.query('nx-searchfeature component[searchCriteria=true]'), function (cmp) {
      criterias.push({
        id: cmp.criteriaId,
        value: cmp.getValue(),
        hidden: cmp.hidden
      })
    });

    model = me.getSearchFilterModel().create(Ext.apply(values, {
      id: values.name,
      criterias: criterias,
      readOnly: false
    }));

    me.getSearchFilterStore().add(model);

    me.getApplication().getFeaturesController().registerFeature({
      path: '/' + (model.get('readOnly') ? '' : 'Saved/') + model.get('name'),
      mode: 'search',
      view: { xtype: 'nx-searchfeature', searchFilter: model },
      iconConfig: {
        name: model.get('iconName') ? model.get('iconName') : 'feature-search'
      },
      description: model.get('description'),
      authenticationRequired: false
    });

    me.getController('Menu').refreshTree();
    NX.Bookmarks.navigateTo(NX.Bookmarks.fromToken('search/saved/' + model.get('name')))

    win.close();
  },

  /**
   * @private
   * @param {NX.ext.SearchBox} quickSearch search box
   * @param {String} searchValue search value
   */
  onQuickSearch: function (quickSearch, searchValue) {
    var me = this,
        searchFeature = me.getSearchFeature();

    if (!searchFeature || (searchFeature.searchFilter.getId() !== 'keyword')) {
      NX.Bookmarks.navigateTo(NX.Bookmarks.fromSegments(
          ['search/keyword', 'keyword=' + encodeURIComponent(searchValue)]
      ));
    }
    else {
      searchFeature.down('#criteria component[criteriaId=keyword]').setValue(searchValue);
    }
  }

});