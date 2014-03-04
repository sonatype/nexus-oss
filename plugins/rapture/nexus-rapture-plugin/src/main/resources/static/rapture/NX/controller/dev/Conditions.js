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
/**
 * Developer Conditions controller.
 *
 * @since 2.8
 */
Ext.define('NX.controller.dev.Conditions', {
  extend: 'Ext.app.Controller',

  stores: [
    'NX.store.dev.Condition'
  ],
  views: [
    'dev.Conditions'
  ],

  refs: [
    {
      ref: 'showAll',
      selector: 'nx-dev-conditions #showAll'
    },
    {
      ref: 'devPanelTabs',
      selector: 'nx-dev-panel tabpanel'
    },
    {
      ref: 'devConditionsPanel',
      selector: 'nx-dev-panel nx-dev-conditions'
    }
  ],

  init: function () {
    var me = this;

    me.showAllFilter = Ext.create('Ext.util.Filter', {
      id: 'showAll',
      filterFn: function (record) {
        return Ext.isDefined(record.get('condition'));
      }
    });

    me.listen({
      controller: {
        '#State': {
          conditionboundedchanged: me.boundedChanged,
          conditionstatechanged: me.stateChanged
        }
      },
      component: {
        'nx-dev-conditions': {
          afterrender: me.showAll
        },
        'nx-dev-conditions #showAll': {
          change: me.showAll
        }
      }
    });
  },

  onLaunch: function () {
    var me = this,
        devPanelTab = me.getDevPanelTabs();

    if (devPanelTab) {
      devPanelTab.add({ xtype: 'nx-dev-conditions' });
    }
  },

  onDestroy: function () {
    var me = this,
        devConditionsPanel = me.getDevConditionsPanel();

    if (devConditionsPanel) {
      me.getDevPanelTabs().remove(devConditionsPanel);
    }
  },

  showAll: function () {
    var me = this,
        showAll = me.getShowAll().getValue(),
        store = me.getStore('NX.store.dev.Condition');

    if (showAll) {
      store.removeFilter(me.showAllFilter);
    }
    else {
      store.addFilter(me.showAllFilter);
    }
  },

  boundedChanged: function (condition) {
    var me = this,
        store = me.getStore('NX.store.dev.Condition'),
        model;

    if (condition.bounded) {
      store.add({ id: condition.id, condition: condition });
      store.filter();
    }
    else {
      model = store.getById(condition.id);
      if (model) {
        store.remove(model);
      }
    }
  },

  stateChanged: function (condition) {
    var me = this,
        store = me.getStore('NX.store.dev.Condition'),
        model = store.getById(condition.id);

    if (model) {
      model.set('satisfied', condition.isSatisfied());
      model.commit();
      store.filter();
    }
  }

});