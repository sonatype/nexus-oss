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
  extend: 'NX.controller.MasterDetail',

  list: 'nx-coreui-repositorybrowse-list',

  stores: [
    'BrowseManagedRepository',
    'BrowseStandardRepository'
  ],
  views: [
    'repositorybrowse.BrowseManagedRepositoryFeature',
    'repositorybrowse.BrowseManagedRepositoryList',
    'repositorybrowse.BrowseStandardRepositoryFeature',
    'repositorybrowse.BrowseStandardRepositoryList'
  ],
  refs: [
    { ref: 'list', selector: 'nx-coreui-repositorybrowse-list' }
  ],
  icons: [
    {
      'repository-managed': {
        file: 'database_yellow.png',
        variants: ['x16', 'x32']
      }
    }
  ],
  features: [
    {
      mode: 'browse',
      path: '/Repository/Standard',
      description: 'Browse standard repositories',
      view: { xtype: 'nx-coreui-repositorybrowse-standard-feature' },
      weight: 10,
      authenticationRequired: false,
      iconConfig: {
        file: 'database.png',
        variants: ['x16', 'x32']
      },
      visible: function() {
        return NX.Permissions.check('nexus:repositories', 'read');
      }
    },
    {
      mode: 'browse',
      path: '/Repository/Managed',
      description: 'Browse managed repositories',
      view: { xtype: 'nx-coreui-repositorybrowse-managed-feature' },
      weight: 300,
      iconConfig: {
        file: 'database_yellow.png',
        variants: ['x16', 'x32']
      },
      visible: function() {
        return NX.Permissions.check('nexus:repositories', 'read');
      }
    }
  ],
  permission: 'nexus:repositories',

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.callParent(arguments);

    me.listen({
      component: {
        'nx-coreui-repositorybrowse-list button[action=admin]': {
          afterrender: me.bindAdminButton,
          click: me.navigateToAdminMode
        }
      }
    });
  },

  /**
   * @override
   */
  getDescription: function(model) {
    return model.get('name');
  },

  /**
   * @private
   * Show 'Admin' when user has 'update' permission.
   */
  bindAdminButton: function(button) {
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
            NX.Conditions.gridHasSelection('nx-coreui-repositorybrowse-list')
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @private
   * Navigate to same repository in admin mode.
   */
  navigateToAdminMode: function(button) {
    var list = button.up('grid');

    NX.Bookmarks.navigateTo(NX.Bookmarks.fromSegments([
      'admin/repository/repositories', list.getSelectionModel().getSelection()[0].getId()
    ]));
  }

});