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
 * HealthCheck search contribution controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.HealthCheckSearch', {
  extend: 'Ext.app.Controller',
  requires: [
    'Ext.grid.column.Column',
    'NX.Conditions',
    'NX.util.Url'
  ],

  models: [
    'SearchResult',
    'SearchResultVersion'
  ],
  stores: [
    'SearchResult',
    'SearchResultVersion'
  ],
  refs: [
    { ref: 'searchResultVersion', selector: 'nx-coreui-search-result-version-list' },
    { ref: 'searchResultDetails', selector: 'nx-coreui-search-result-details' }
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.listen({
      store: {
        '#SearchResult': {
          load: me.setHealthCheckSearchResultFields
        },
        '#SearchResultVersion': {
          load: me.loadHealthCheckSearchResultVersion
        }
      },
      component: {
        'nx-coreui-search-result-version-list': {
          afterrender: me.bindHealthCheckColumns
        }
      }
    });
  },

  /**
   * @private
   * Sets Health Check fields on search results.
   */
  setHealthCheckSearchResultFields: function() {
    var me = this;

    me.getSearchResultStore().each(function(searchResultModel) {
      searchResultModel.set('healthCheckLoading', true);
      searchResultModel.commit();
    });
  },

  /**
   * @private
   * Load Health Check details if search result version grid is active.
   */
  loadHealthCheckSearchResultVersion: function() {
    var me = this,
        searchResultDetails = me.getSearchResultDetails().down('#secondaryInfo'),
        searchResultModel,
        groupId = undefined, artifactId = undefined, versions = [];

    if (me.getSearchResultVersion()) {
      me.getSearchResultVersionStore().each(function(searchResultVersionModel) {
        groupId = searchResultVersionModel.get('groupId');
        artifactId = searchResultVersionModel.get('artifactId');
        if (versions.indexOf(searchResultVersionModel.get('version')) === -1) {
          versions.push(searchResultVersionModel.get('version'));
        }
      });
      if (groupId && artifactId) {
        searchResultModel = me.getSearchResultStore().getById(groupId + ':' + artifactId);
        searchResultModel.set('healthCheckLoading', true);
        searchResultModel.commit();
        searchResultDetails.showInfo({ 'Most popular version': me.renderMostPopularVersion(searchResultModel) });
        me.getSearchResultVersion().getView().refresh();
        NX.direct.healthcheck_Search.read(groupId, artifactId, versions, function(response) {
          searchResultModel.set('healthCheckLoading', false);
          if (Ext.isObject(response) && response.success) {
            searchResultModel.beginEdit();
            searchResultModel.set('healthCheckCapped', response.data['capped']);
            searchResultModel.set('healthCheckDisabled', response.data['disabled']);
            searchResultModel.set('healthCheckError', response.data['error']);
            searchResultModel.set('healthCheckMostPopularVersion', response.data['mostPopularVersion']);
            searchResultModel.endEdit();
            searchResultDetails.showInfo({ 'Most popular version': me.renderMostPopularVersion(searchResultModel) });
            Ext.Object.each(response.data.versions, function(key, value) {
              var searchResultVersionModel = me.getSearchResultVersionStore().getById(key);
              if (searchResultVersionModel) {
                searchResultVersionModel.beginEdit();
                searchResultVersionModel.set('healthCheckAge', value['age']);
                searchResultVersionModel.set('healthCheckPopularity', value['popularity']);
                searchResultVersionModel.set('healthCheckSecurityAlerts', value['securityAlerts']);
                searchResultVersionModel.set('healthCheckCriticalSecurityAlerts', value['criticalSecurityAlerts']);
                searchResultVersionModel.set('healthCheckSevereSecurityAlerts', value['severeSecurityAlerts']);
                searchResultVersionModel.set('healthCheckModerateSecurityAlerts', value['moderateSecurityAlerts']);
                searchResultVersionModel.set('healthCheckLicenseThreat', value['licenseThreat']);
                searchResultVersionModel.set('healthCheckLicenseThreatName', value['licenseThreatName']);
                searchResultVersionModel.endEdit(true);
              }
            });
          }
          else {
            searchResultModel.set('healthCheckError', true);
          }
          me.getSearchResultVersion().getView().refresh();
        });
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
          header: 'Age',
          dataIndex: 'healthCheckAge',
          groupable: false,
          width: 60,
          renderer: Ext.bind(me.renderAgeColumn, me)
        }),
        Ext.create('Ext.grid.column.Column', {
          header: 'Popularity',
          dataIndex: 'healthCheckPopularity',
          groupable: false,
          width: 90,
          renderer: Ext.bind(me.renderPopularityColumn, me)
        })
      ];
      grid.headerCt.add(grid.healthCheckColumns);
      grid.fireEvent('healthcheckcolumnsadded', grid);
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
   * Render most popular version field.
   * @param {NX.coreui.model.SearchResult} searchResultModel search result model
   * @returns {string} rendered value
   */
  renderMostPopularVersion: function(searchResultModel) {
    var me = this,
        result, metadata = {};

    result = me.renderPreconditions(searchResultModel, metadata);
    if (!result) {
      result = searchResultModel.get('healthCheckMostPopularVersion');
      if (!result) {
        result = me.renderNotAvailable(metadata);
      }
    }
    return '<div ' + (metadata.tdAttr || '') + '>' + result + '</div>';
  },

  /**
   * @private
   * Render age column.
   * @param {number} value age
   * @param metadata column metadata
   * @param {NX.coreui.model.SearchResultVersion} searchResultVersionModel search result version model
   * @returns {string} rendered value
   */
  renderAgeColumn: function(value, metadata, searchResultVersionModel) {
    var me = this,
        result, age, dayAge;

    result = me.renderPreconditions(searchResultVersionModel, metadata);
    if (!result) {
      age = searchResultVersionModel.get('healthCheckAge');
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
      if (!result) {
        result = me.renderNotAvailable(metadata);
      }
    }
    return result;
  },

  /**
   * @private
   * Render popularity column.
   * @param {number} value popularity
   * @param metadata column metadata
   * @param {NX.coreui.model.SearchResultVersion} searchResultVersionModel search result version model
   * @returns {string} rendered value
   */
  renderPopularityColumn: function(value, metadata, searchResultVersionModel) {
    var me = this,
        result, popularity;

    result = me.renderPreconditions(searchResultVersionModel, metadata);
    if (!result) {
      popularity = searchResultVersionModel.get('healthCheckPopularity');
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
      if (!result) {
        result = me.renderNotAvailable(metadata);
      }
    }
    return result;
  },

  /**
   * @private
   * Render value based on preconditions.
   * @param {NX.coreui.model.SearchResult|NX.coreui.model.SearchResultVersion} model search result / version model
   * @param metadata column metadata
   * @returns {*} rendered value
   */
  renderPreconditions: function(model, metadata) {
    var me = this,
        searchResultModel = me.getSearchResultStore().getById(
            model.get('groupId') + ':' + model.get('artifactId')
        );

    // FIXME: resolve use of undefined "opaqueWarning" css class and potentially replace icons with glyphs

    if (searchResultModel.get('healthCheckLoading')) {
      return 'Loading...';
    }
    else if (searchResultModel.get('healthCheckDisabled')) {
      metadata.tdAttr = 'data-qtip="The age and popularity data is only available once Repository Health Check (RHC) has been enabled."';
      return '<img class="opaqueWarning" src="' + me.imageUrl('information.png') + '">';
    }
    else if (searchResultModel.get('healthCheckError')) {
      metadata.tdAttr = 'data-qtip="Error retrieving data from Sonatype CLM"';
      return '<img class="opaqueWarning" src="' + me.imageUrl('exclamation.gif') + '">';
    }
    else if (searchResultModel.get('healthCheckCapped') || (model && model.get('capped'))) {
      metadata.tdAttr = 'data-qtip="The query limit for age and popularity data has been reached. Contact Sonatype support to extend current quota limits."';
      return '<img class="opaqueWarning" src="' + me.imageUrl('warning.gif') + '">';
    }
    return undefined;
  },

  /**
   * @private
   * Render a not available value (no data).
   * @param metadata column metadata
   * @returns {string} rendered value
   */
  renderNotAvailable: function(metadata) {
    var me = this;
    metadata.tdAttr = 'data-qtip="CLM data is not available for this component.<br/>Internal components are not covered in Sonatype CLM."';
    return '<span class="fa fa-ban"/>';
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