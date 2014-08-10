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
 * HealthCheck search contribution controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.HealthCheckSearch', {
  extend: 'Ext.app.Controller',

  stores: [
    'HealthCheckSearchResultVersion',
    'SearchResultVersion'
  ],
  refs: [
    { ref: 'searchResultVersion', selector: 'nx-coreui-search-result-version-list' }
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.listen({
      controller: {
        '#Permissions': {
          changed: me.refreshHealthCheckColumns
        }
      },
      store: {
        '#SearchResultVersion': {
          beforeload: me.clearHealthCheckSearchResultVersion,
          load: me.loadHealthCheckSearchResultVersion
        },
        '#HealthCheckSearchResultVersion': {
          refresh: me.refreshHealthCheckColumns
        }
      },
      component: {
        'nx-coreui-search-result-version-list': {
          afterrender: me.bindHealthCheckColumns
        }
      }
    });
  },

  clearHealthCheckSearchResultVersion: function() {
    var me = this;
    me.getHealthCheckSearchResultVersionStore().removeAll();
  },

  /**
   * @private
   * Load Health Check details if search result version grid is active.
   */
  loadHealthCheckSearchResultVersion: function() {
    var me = this,
        searchResultVersion = me.getSearchResultVersion(),
        groupId, artifactId, versions = [];

    if (searchResultVersion) {
      me.getSearchResultVersionStore().each(function(searchResultVersionModel) {
        groupId = searchResultVersionModel.get('groupId');
        artifactId = searchResultVersionModel.get('artifactId');
        if (versions.indexOf(searchResultVersionModel.get('version')) == -1) {
          versions.push(searchResultVersionModel.get('version'));
        }
      });
      if (groupId && artifactId) {
        me.getHealthCheckSearchResultVersionStore().load(groupId, artifactId, versions);
        me.refreshHealthCheckColumns();
      }
    }
  },

  /**
   * @private
   * Add/Remove Health Check columns based on nexus:healthcheck:read permission.
   * @param {NX.coreui.view.search.SearchResultVersionList} grid search result version grid
   */
  bindHealthCheckColumns: function(grid) {
    var me = this;
    grid.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted("nexus:healthcheck", "read")
        ),
        {
          satisfied: Ext.pass(me.addHealthCheckColumns, grid),
          unsatisfied: Ext.pass(me.removeHealthCheckColumns, grid),
          scope: me
        }
    );
  },

  /**
   * @private
   * Add Health Check columns to search result version grid.
   * @param {NX.coreui.view.search.SearchResultVersionList} grid search result version grid
   */
  addHealthCheckColumns: function(grid) {
    var me = this,
        view = grid.getView();

    if (!grid.healthCheckColumns) {
      grid.healthCheckColumns = [
        Ext.create('Ext.grid.column.Column', {
          id: 'age',
          header: 'Age',
          renderer: Ext.bind(me.renderAgeColumn, me)
        }),
        Ext.create('Ext.grid.column.Column', {
          id: 'popularity',
          header: 'Popularity',
          renderer: Ext.bind(me.renderPopularityColumn, me)
        })
      ];
      grid.headerCt.add(grid.healthCheckColumns);
      view.refresh();
    }
  },

  /**
   * @private
   * Remove Health Check columns from search result version grid.
   * @param {NX.coreui.view.search.SearchResultVersionList} grid search result version grid
   */
  removeHealthCheckColumns: function(grid) {
    if (grid.healthCheckColumns) {
      grid.headerCt.remove(grid.healthCheckColumns);
      grid.getView().refresh();
      delete grid.healthCheckColumns;
    }
  },

  /**
   * @private
   * Refresh search result version grid view.
   */
  refreshHealthCheckColumns: function() {
    var me = this,
        searchResultVersion = me.getSearchResultVersion();

    if (searchResultVersion) {
      searchResultVersion.getView().refresh();
    }
  },

  renderAgeColumn: function(value, metadata, searchResultVersionModel) {
    var me = this,
        healthCheckModel = me.getHealthCheckSearchResultVersionStore().getById(searchResultVersionModel.get('version')),
        result, age, dayAge;

    result = me.buildCommonText(healthCheckModel, false);
    if (!result) {
      if (healthCheckModel) {
        age = healthCheckModel.get('age');
        if (age === 0 || age > 0) {
          // convert millis to a day count
          dayAge = age / (1000 * 60 * 60 * 24);

          if (dayAge > 364) {
            result = (dayAge / 365).toFixed(1) + ' yrs';
          }
          else {
            result = dayAge.toFixed(0) + ' d';
          }
        }
      }
      if (!result) {
        result = me.notAvailable();
      }
    }
    return result;
  },

  renderPopularityColumn: function(value, metadata, searchResultVersionModel) {
    var me = this,
        healthCheckModel = me.getHealthCheckSearchResultVersionStore().getById(searchResultVersionModel.get('version')),
        result, popularity;

    result = me.buildCommonText(healthCheckModel, false);
    if (!result) {
      if (healthCheckModel) {
        popularity = healthCheckModel.get('popularity');
        if (popularity === 0 || popularity > 0) {
          if (popularity > 100) {
            popularity = 100;
          }
          result = '<img src="' + me.imageUrl('bar_start.gif')
              + '" style="vertical-align: middle;"/><img src="' + me.imageUrl('blue_body.gif')
              + '" width="' + popularity + '%"'
              + ' height="8px" style="vertical-align: middle;"/><img src="' + me.imageUrl('blue_border.gif')
              + '" style="vertical-align: middle;"/>';
        }
      }
      if (!result) {
        result = me.notAvailable();
      }
    }
    return result;
  },

  buildCommonText: function(healthCheckModel, alerts) {
    var me = this,
        store = me.getHealthCheckSearchResultVersionStore();

    if (alerts && !me.rhcAlertData) {
      return '<img src="' + me.imageUrl('locked.png') + '">';
    }
    else if (store.loading) {
      return 'Loading...';
    }
    else if (store.status['disabled']) {
      return '<img class="opaqueWarning" src="' + me.imageUrl('information.png') + '">';
    }
    else if (store.status['error']) {
      return '<img class="opaqueWarning" src="' + me.imageUrl('exclamation.gif') + '">';
    }
    else if (store.status['capped'] || (healthCheckModel && healthCheckModel.get(alerts ? 'cappedAlerts' : 'capped'))) {
      return '<img class="opaqueWarning" src="' + me.imageUrl('warning.gif') + '">';
    }
    return undefined;
  },

  notAvailable: function() {
    var me = this;
    return '<img class="opaqueWarning" src="' + me.imageUrl('na.png') + '">';
  },

  /**
   * @private
   * Calculate image url.
   * @param name of image
   * @returns {string} image url
   */
  imageUrl: function(name) {
    return NX.util.Url.urlOf('static/rapture/resources/images/' + name);
  }

});