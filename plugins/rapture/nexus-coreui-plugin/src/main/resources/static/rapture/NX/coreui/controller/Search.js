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
    'SearchResult'
  ],
  models: [
    'SearchFilter'
  ],

  views: [
    'search.SearchFeature',
    'search.SearchTree',
    'search.TextSearchCriteria',
    'search.SaveSearchFilter'
  ],

  refs: [
    { ref: 'searchFeature', selector: 'nx-searchfeature' },
    { ref: 'searchTree', selector: 'nx-coreui-search-tree' },
    { ref: 'quickSearch', selector: 'nx-header-panel #quicksearch' },
    { ref: 'storageFileContainer', selector: 'nx-coreui-repositorybrowse-storagefilecontainer' }
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
      weight: 500
    });

    me.getSearchFilterStore().each(function(model) {
      if (model.getId() === 'keyword') {
        me.getApplication().getFeaturesController().registerFeature({
          mode: 'browse',
          path: '/Search',
          description: 'Search repositories',
          group: true,
          view: { xtype: 'nx-searchfeature', searchFilter: model, bookmarkEnding: '' },
          iconName: 'search-default',
          weight: 20,
          expanded: false
        });
      }
      else {
        me.getApplication().getFeaturesController().registerFeature({
          mode: 'browse',
          path: '/Search/' + (model.get('readOnly') ? '' : 'Saved/') + model.get('name'),
          view: { xtype: 'nx-searchfeature', searchFilter: model, bookmarkEnding: '/' + model.getId() },
          iconName: 'search-default',
          description: model.get('description'),
          authenticationRequired: false
        });
      }
    });

    me.listen({
      controller: {
        '#Bookmarking': {
          navigate: me.navigateTo
        }
      },
      store: {
        '#SearchResult': {
          beforeload: me.onSearchResultBeforeLoad,
          load: me.onSearchResultLoad
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
          searchcleared: me.onSearchCriteriaChange
        },
        'nx-coreui-search-tree': {
          select: me.onNodeSelected,
          beforeitemexpand: me.loadVersions,
        },
        'nx-searchfeature button[action=save]': {
          click: me.showSaveSearchFilterWindow
        },
        'nx-coreui-search-save button[action=add]': {
          click: me.saveSearchFilter
        },
        'nx-main #quicksearch': {
          search: me.onQuickSearch,
          searchcleared: me.onQuickSearch
        }
      }
    });
  },

  navigateTo: function() {
    var me = this;

    if (me.getSearchFeature()) {
      me.initCriterias();
    }
  },

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
        if (split.length == 2) {
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

    me.getSearchResultStore().filter();
  },

  addCriteria: function(menuitem) {
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

  onSearchCriteriaChange: function(searchCriteria) {
    var me = this;
    me.applyFilter(searchCriteria, true);
  },

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
      me.bookmarkFilters();
    }
  },

  onSearchResultBeforeLoad: function(store) {
    var me = this,
        tree = me.getSearchTree();

    if (tree) {
      tree.getEl().mask('Searching...');
    }
  },

  onSearchResultLoad: function(store) {
    var me = this,
        tree = me.getSearchTree(),
        root;

    if (tree) {
      root = tree.getRootNode();
      Ext.suspendLayouts();
      root.removeAll();
      store.each(function(searchResultModel) {
        root.appendChild({
          text: searchResultModel.getId(), leaf: false, children: me.versionsAsNodes(searchResultModel.get('versions'))
        });
      });
      Ext.resumeLayouts(true);
      tree.getEl().unmask();
    }
  },

  loadVersions: function(node) {
    var me = this,
        tree = me.getSearchTree(),
        searchResultStore = me.getSearchResultStore(),
        filters = [],
        segments = node.get('text').split(':');

    if (!node.childNodes.length) {
      filters.push({
        property: 'groupid',
        value: segments[0]
      });
      filters.push({
        property: 'artifactid',
        value: segments[1]
      });
      if (searchResultStore.filters.length) {
        searchResultStore.filters.each(function(filter) {
          if (filter.property !== 'groupid' && filter.property !== 'artifactid') {
            filters.push({ property: filter.property, value: filter.value });
          }
        });
      }
      tree.getEl().mask('Searching...');
      NX.direct.coreui_Search.readVersions({ filter: filters }, function(response) {
        if (Ext.isObject(response) && response.success && response.data && response.data.versions) {
          Ext.suspendLayouts();
          node.appendChild(me.versionsAsNodes(response.data.versions));
          Ext.resumeLayouts(true);
        }
        tree.getEl().unmask();
      });
    }
  },

  versionsAsNodes: function(versions) {
    var nodes;

    if (versions) {
      nodes = []
      Ext.Array.each(versions, function(version) {
        var versionNode = { text: version['version'], children: [] },
            itemsPerRepo = {}, nrOfRepos = 0;

        Ext.Array.each(version['items'], function(item) {
          if (!itemsPerRepo[item['repositoryId']]) {
            nrOfRepos++;
            itemsPerRepo[item['repositoryId']] = [];
          }
          itemsPerRepo[item['repositoryId']].push(Ext.apply({ text: item['name'], leaf: true }, item));
        });

        Ext.Object.each(itemsPerRepo, function(repository, items) {
          if (nrOfRepos == 1) {
            versionNode.children = items;
          }
          else {
            versionNode.children.push({ text: items[0]['repositoryName'], children: items });
          }
        });

        nodes.push(versionNode);
      });
    }
    return nodes;
  },

  onNodeSelected: function(tree, node) {
    var me = this;
    me.getStorageFileContainer().showStorageFile(
        node.get('repositoryId'),
        node.get('path')
    );
  },

  showSaveSearchFilterWindow: function() {
    Ext.widget('nx-coreui-search-save');
  },

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
      })
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
    NX.Bookmarks.navigateTo(NX.Bookmarks.fromToken('browse/search/saved/' + model.get('name')))

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