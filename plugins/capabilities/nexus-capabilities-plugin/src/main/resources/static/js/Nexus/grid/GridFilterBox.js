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
NX.define('Nexus.grid.GridFilterBox', {
  extend: 'Ext.Container',

  /**
   * @cfg {String} regexp modifiers (defaults to 'i' = case insensitive).
   */
  modifiers: 'i',

  /**
   * @cfg {Ext.grid.GridPanel} grid that should filtered
   */
  grid: undefined,

  /**
   * @cfg {Number} delay between keystrokes and actual filtering (defaults to 50).
   */
  filterDelay: 100,

  /**
   * @override
   */
  initComponent: function () {
    var self = this,
        icons = Nexus.capabilities.Icons;

    self.filterField = NX.create('Ext.form.TextField', {
      enableKeyEvents: true,
      listeners: {
        keyup: {
          fn: function () {
            clearTimeout(self.searchTimeout);
            self.searchTimeout = self.filterGrid.defer(self.filterDelay, self);
          },
          scope: self
        }
      }
    });

    Ext.apply(self, {
      layout: 'fit',
      items: [
        self.filterField
      ],
      listeners: {
        render: {
          fn: function () {
            self.grid.on('reconfigure', self.onGridReconfigured, self);
            self.bindToStore(self.grid.getStore());
          },
          scope: self
        }
      }
    });

    self.icon = NX.create('Nexus.Image', {
      src: icons.get('magnifier_grey').path,
      height: 12,
      width: 12,
      style: {
        marginRight: '4px'
      }
    });

    self.clearButton = NX.create('Ext.Button', {
      iconCls: icons.get('cross_grey').cls,
      scope: self,
      handler: self.clearFilter
    });

    Nexus.grid.GridFilterBox.superclass.initComponent.call(self, arguments);
  },

  /**
   * @override
   */
  destroy: function () {
    var self = this;
    self.grid.removeListener('reconfigure', self.onGridReconfigured, self);
    self.unbindFromStore(self.grid.getStore());
  },

  /**
   * Clears the filter.
   */
  clearFilter: function () {
    var self = this;

    self.filterField.setValue(undefined);
    self.filterGrid();

    // reset clear button to grey on clear filter
    self.clearButton.setIconClass(Nexus.capabilities.Icons.get('cross_grey').cls);
  },

  /**
   * Filters the grid on current filter box value.
   */
  filterGrid: function () {
    var self = this,
        shouldClearFilter = true,
        regexp, filterFields;

    // when filtering set the icon to color
    self.clearButton.setIconClass(Nexus.capabilities.Icons.get('cross').cls);

    self.grid.getStore().clearFilter();
    if (self.filterField.getValue() && self.filterField.getValue().length > 0) {

      if (self.grid.getStore().getCount() > 0 && self.grid.view.emptyTextWhileFiltering) {
        if (!self.grid.view.emptyTextBackup) {
          self.grid.view.emptyTextBackup = self.grid.view.emptyText;
        }
        self.grid.view.emptyText = self.grid.view.emptyTextWhileFiltering.replaceAll(
            '{criteria}', self.filterField.getValue()
        );
      }

      regexp = new RegExp(self.filterField.getValue(), self.modifiers);
      filterFields = self.filterFieldNames();

      if (filterFields && filterFields.length > 0) {
        shouldClearFilter = false;

        self.grid.getStore().filterBy(function (record) {
          for (var i = 0; i < filterFields.length; i++) {
            if (filterFields[i]) {
              if (self.matches(regexp, record, filterFields[i], self.extractValue(record, filterFields[i]))) {
                return true;
              }
            }
          }
          return false;
        }, self);
      }
    }

    if (shouldClearFilter) {
      if (self.grid.view.emptyTextBackup) {
        self.grid.view.emptyText = self.grid.view.emptyTextBackup;
      }
      self.grid.getStore().clearFilter();
    }
  },

  /**
   * @protected
   * Returns true if the field value is defined and matches regexp.
   * @param regexp to match
   * @param record record that was used to extract the value to be matched
   * @param fieldName filter field name that was used to extract the value to be matched
   * @param fieldValue to me matched
   */
  matches: function (regexp, record, fieldName, fieldValue) {
    return fieldValue && regexp.test(fieldValue);
  },

  /**
   * Returns the value to be matched of a record given field name
   * @param record  from which the value to be matched should be extracted
   * @param fieldName
   * @returns {*} record field value or null if there is no field with provided name
   */
  extractValue: function (record, fieldName) {
    return record.data[fieldName];
  },

  /**
   * Returns the dataIndex property of all grid columns.
   * @returns {Array} of fields names to be matched
   */
  filterFieldNames: function () {
    var self = this,
        columnModel = self.grid.getColumnModel(),
        columns,
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
  },

  /**
   * @private
   * Handles reconfiguration of grid.
   * @param grid that was reconfigured
   * @param store new store
   */
  onGridReconfigured: function (grid, store) {
    var self = this;
    self.unbindFromStore(grid.getStore());
    if (store) {
      self.bindToStore(store);
    }
  },

  /**
   * @private
   * Remove itself as listener from provided store.
   * @param store to remove itself from
   */
  unbindFromStore: function (store) {
    var self = this;
    if (store) {
      store.removeListener('load', self.filterGrid, self);
      store.removeListener('save', self.filterGrid, self);
    }
  },

  /**
   * @private
   * Register itself as listener of load events on provided store.
   * @param store to register itself to
   */
  bindToStore: function (store) {
    var self = this;
    if (store) {
      store.on('load', self.filterGrid, self);
      store.on('save', self.filterGrid, self);
    }
  }

});