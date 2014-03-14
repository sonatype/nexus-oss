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
Ext.define('NX.controller.Search', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Bookmarks'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'Search'
  ],

  refs: [
    {
      ref: 'search',
      selector: 'nx-search'
    },
    {
      ref: 'quickSearch',
      selector: 'nx-header-panel #quicksearch'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'feature-search': {
        file: 'magnifier.png',
        variants: ['x16', 'x32']
      }
    });
    me.getApplication().getFeaturesController().registerFeature({
      path: '/Search',
      mode: 'search',
      view: { xtype: 'nx-search' },
      authenticationRequired: false
    });

    me.listen({
      component: {
        'nx-search': {
          afterrender: me.afterRender
        },
        'nx-header-panel #quicksearch': {
          search: me.search,
          searchcleared: me.clearSearch
        }
      }
    });
  },

  afterRender: function () {
    var me = this,
        quickSearch = me.getQuickSearch(),
        value = quickSearch.getValue();

    if (value) {
      me.search(quickSearch, value);
    }
  },

  search: function (quickSearch, value) {
    var me = this;

    if (me.getSearch()) {
      me.showSomeFun(
          ':)))) ... searching for "' + value + '" yield no results...',
          'Yet!',
          'Try again! ;-)'
      );
    }
    else {
      NX.Bookmarks.navigateTo(NX.Bookmarks.fromToken('search/search'), me);
    }
  },

  clearSearch: function () {
    var me = this;

    if (me.getSearch()) {
      me.showSomeFun(
          'Enter a value in quick search above ^',
          '',
          ''
      );
    }
  },

  showSomeFun: function (one, two, three) {
    var me = this,
        label1 = me.getSearch().down('#one'),
        label2 = me.getSearch().down('#two'),
        label3 = me.getSearch().down('#three');

    label1.setText(one);
    label2.setText('');
    label3.setText('');
    new Ext.util.DelayedTask(function () {
      label2.setText(two);
    }).delay(1000);
    new Ext.util.DelayedTask(function () {
      label3.setText(three);
    }).delay(2000);
  }

});