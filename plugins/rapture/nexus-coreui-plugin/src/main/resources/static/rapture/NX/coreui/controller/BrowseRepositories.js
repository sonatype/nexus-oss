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
 * Browse repositories controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.BrowseRepositories', {
  extend: 'Ext.app.Controller',

  views: [
    'repository.RepositoryBrowseFeature',
    'repository.RepositoryBrowseInfoTabPanel',
    'repository.RepositoryBrowseList',
    'repository.RepositoryBrowseTabs'
  ],
  refs: [
    {
      ref: 'list',
      selector: 'nx-coreui-repository-browse-list'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'browse',
      path: '/Repository/Standard',
      description: 'Browse standard repositories',
      view: { xtype: 'nx-coreui-repository-browse-feature' },
      iconName: 'feature-repository',
      weight: 10,
      authenticationRequired: false,
      visible: function () {
        return NX.Permissions.check('nexus:repositories', 'read');
      }
    });

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.refresh
        }
      },
      component: {
        'nx-coreui-repository-browse-list': {
          beforerender: me.load,
          selectionchange: me.onSelectionChange,
          itemdblclick: me.onItemDblClick
        },
        'nx-coreui-repository-browse-list button[action=admin]': {
          afterrender: me.bindAdminButton,
          click: me.navigateToAdminMode
        }
      }
    });
  },

  refresh: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.load(list);
    }
  },

  load: function (list) {
    list.getStore().load();
  },

  onSelectionChange: function (selectionModel, selected) {
    var me = this,
        list = me.getList();

    list.setTitle(selected[0].get('name'));
    list.fireEvent('selection', list, selected[0]);
  },

  onItemDblClick: function () {
    var me = this,
        list = me.getList();

    list.collapse();
  },

  /**
   * @protected
   * Show 'Admin' when user has 'delete' permission.
   */
  bindAdminButton: function (button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:repositories', 'update')
        ),
        {
          satisfied: button.show,
          unsatisfied: button.hide,
          scope: button
        }
    );
    button.mon(
        NX.Conditions.and(
            NX.Conditions.gridHasSelection('nx-coreui-repository-browse-list')
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  navigateToAdminMode: function () {
    var me = this,
        list = me.getList();

    NX.Bookmarks.navigateTo(NX.Bookmarks.fromSegments([
      'admin/repository/repositories', list.getSelectionModel().getSelection()[0].getId()
    ]));
  }

});