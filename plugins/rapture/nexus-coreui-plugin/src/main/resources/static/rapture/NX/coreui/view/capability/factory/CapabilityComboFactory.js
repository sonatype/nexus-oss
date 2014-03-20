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
/*global NX, Ext, Sonatype*/

/**
 * 'combo' factory.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.capability.factory.CapabilityComboFactory', {
  singleton: true,
  alias: [
    'nx.capability.factory.combo',
    'nx.capability.factory.combobox',
    'nx.capability.factory.repo',
    'nx.capability.factory.repo-or-group',
    'nx.capability.factory.repo-target'
  ],

  mixins: {
    logAware: 'NX.LogAware'
  },

  requires: [
    'NX.util.Url'
  ],

  /**
   * Map of stores / store url.
   * @private
   */
  stores: {},

  /**
   * Creates a combo.
   * @param formField capability type form field to create combo for
   * @returns {*} created combo (never null)
   */
  create: function (formField) {
    var me = this,
        ST = Ext.data.SortTypes,
        store,
        item = Ext.create('Ext.form.ComboBox', {
          xtype: 'combo',
          fieldLabel: formField.label,
          itemCls: formField.required ? 'required-field' : '',
          helpText: formField.helpText,
          name: formField.id,
          displayField: 'name',
          valueField: 'id',
          editable: false,
          forceSelection: true,
          queryMode: 'local',
          triggerAction: 'all',
          emptyText: 'Select...',
          selectOnFocus: false,
          allowBlank: formField.required ? false : true
        });

    if (formField.initialValue) {
      item.value = formField.initialValue;
    }
    if (formField.storePath) {
      store = me.stores[formField.storePath];
      if (!store) {
        store = Ext.create('Ext.data.Store', {

          proxy: {
            type: 'ajax',
            url: NX.util.Url.urlOf(formField.storePath),
            headers: {
              'accept': 'application/json'
            },
            reader: {
              type: 'json',
              root: formField.storeRoot,
              idProperty: formField.idMapping || 'id'
            }
          },

          fields: [
            { name: 'id', mapping: formField.idMapping || 'id' },
            { name: 'name', mapping: formField.nameMapping || 'name', sortType: ST.asUCString }
          ],

          sortInfo: {
            field: 'name',
            direction: 'ASC'
          },

          autoLoad: true
        });
        me.stores[formField.storePath] = store;
        me.logDebug("Caching store for " + store.proxy.url);
      }
      item.store = store;
    }
    return item;
  },

  /**
   * Evicts all cached stores (they will be recreated on demand).
   */
  evictCache: function () {
    var me = this;

    me.logDebug('Evicted all cached stores');
    me.stores = {};
  }

});