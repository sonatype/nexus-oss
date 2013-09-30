/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global NX, Ext, Nexus*/

/**
 * A grid filter box.
 *
 * @since 2.7
 */
NX.define('Nexus.ext.GridFilterBox', {
  extend: 'Nexus.ext.StoreFilterBox',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  /**
   * @cfg {Ext.grid.GridPanel} grid that should filtered
   */
  filteredGrid: undefined,

  /**
   * @override
   */
  initComponent: function () {
    var self = this;

    Ext.apply(self, {
      filteredStore: self.filteredGrid.gridStore,
      filteredFields: self.extractColumnsWithDataIndex(self.filteredGrid.getColumnModel())
    });

    Nexus.ext.GridFilterBox.superclass.initComponent.call(self, arguments);

    self.on('render', function () {
      self.logDebug('Binding to grid ' + self.filteredGrid);
      self.filteredGrid.on('reconfigure', self.onGridReconfigured, self);
    });

    self.on('destroy', function () {
      self.logDebug('Unbinding from grid ' + self.filteredGrid);
      self.filteredGrid.removeListener('reconfigure', self.onGridReconfigured, self);
    });
    self.on('beforeFiltering', function () {
      if (self.filteredGrid.view.emptyTextWhileFiltering && self.filteredStore.getCount() > 0) {
        if (!self.filteredGrid.view.emptyTextBackup) {
          self.filteredGrid.view.emptyTextBackup = self.filteredGrid.view.emptyText;
        }
        self.filteredGrid.view.emptyText = self.filteredGrid.view.emptyTextWhileFiltering.replaceAll(
            '{criteria}', self.getSearchValue()
        );
      }
    });
    self.on('searchcleared', function () {
      if (self.filteredGrid.view.emptyTextBackup) {
        self.filteredGrid.view.emptyText = self.filteredGrid.view.emptyTextBackup;
      }
    });
  },

  /**
   * @private
   * Handles reconfiguration of grid.
   * @param grid that was reconfigured
   * @param store new store
   * @param columnModel new column model
   */
  onGridReconfigured: function (grid, store, columnModel) {
    var self = this;
    self.logDebug('Grid ' + self.filteredGrid + ' reconfigured, binding to new store');
    self.reconfigureStore(store, self.extractColumnsWithDataIndex(columnModel));
  },

  /**
   * Returns the dataIndex property of all grid columns.
   * @returns {Array} of fields names
   */
  extractColumnsWithDataIndex: function (columnModel) {
    var columns,
        filterFieldNames = [];

    if (columnModel) {
      columns = columnModel.getColumnsBy(function () {
        return true;
      });
      if (columns) {
        Ext.each(columns, function (column) {
          if (column.dataIndex) {
            filterFieldNames.push(column.dataIndex);
          }
        });
      }
    }

    if (filterFieldNames.length > 0) {
      return filterFieldNames;
    }
    return [];
  }

});