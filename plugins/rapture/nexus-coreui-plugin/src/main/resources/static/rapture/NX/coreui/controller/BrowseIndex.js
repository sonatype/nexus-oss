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
Ext.define('NX.coreui.controller.BrowseIndex', {
  extend: 'Ext.app.Controller',

  views: [
    'repository.RepositoryBrowseIndex',
    'repository.RepositoryBrowseIndexTree'
  ],
  refs: [
    {
      ref: 'feature',
      selector: 'nx-coreui-repository-browse-feature'
    },
    {
      ref: 'panel',
      selector: 'nx-coreui-repository-browse-index'
    },
    {
      ref: 'tree',
      selector: 'nx-coreui-repository-browse-index-tree'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.listen({
      component: {
        'nx-coreui-repository-browse-list': {
          selection: me.onSelection
        },
        'nx-coreui-repository-browse-index-tree': {
          select: me.onItemSelected
        }
      }
    });
  },

  onSelection: function (list, model) {
    var me = this,
        panel = me.getPanel(),
        tree;

    if (model.get('indexable') || model.get('downloadRemoteIndexes')) {
      if (!panel) {
        me.getFeature().addTab({ xtype: 'nx-coreui-repository-browse-index', title: 'Index' });
      }
      tree = me.getTree();
      tree.getStore().getRootNode().set('text', model.get('name'));
    }
    else {
      if (panel) {
        me.getFeature().removeTab(panel);
      }
    }
  },

  onItemSelected: function (tree, node) {
    var me = this,
        panel = me.getPanel();

    if (node.isRoot()) {
      panel.fireEvent('itemdeselected', panel);
    }
    else {
      panel.fireEvent('itemselected', panel, node.get('text'));
    }
  }

});