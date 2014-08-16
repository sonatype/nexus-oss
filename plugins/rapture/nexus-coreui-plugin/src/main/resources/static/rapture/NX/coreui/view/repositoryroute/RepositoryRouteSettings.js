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
 * Repository route settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositoryroute.RepositoryRouteSettings', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-repositoryroute-settings',
  requires: [
    'NX.Conditions',
    'NX.coreui.store.RepositoryReference',
    'NX.coreui.model.Reference'
  ],

  api: {
    submit: 'NX.direct.coreui_RepositoryRoute.update'
  },
  settingsFormSuccessMessage: function (data) {
    return 'Repository route updated: ' + data['pattern'];
  },
  editableMarker: 'You do not have permission to update routes',

  models: [
    'Reference'
  ],

  initComponent: function () {
    var me = this;

    me.editableCondition = NX.Conditions.isPermitted('nexus:routes', 'update');

    me.groupStore = Ext.create('NX.coreui.store.RepositoryReference', { remoteFilter: true });
    me.mon(me.groupStore, 'load', function (store) {
      store.add(Ext.create('NX.coreui.model.Reference', { id: '*', name: 'All Repository Groups' }));
    });
    me.groupStore.filter({ property: 'type', value: 'group' });

    me.repositoryStore = Ext.create('NX.coreui.store.RepositoryReference');
    me.repositoryStore.load();

    me.items = [
      {
        xtype: 'hiddenfield',
        name: 'id'
      },
      {
        xtype: 'nx-regexp',
        name: 'pattern',
        fieldLabel: 'URL pattern',
        helpText: "A regular expression used to match the artifact path. The path is everything after /nexus/content/ so it will include the group or repository name. .* is used to specify all paths. '.*/com/some/company/.*' will match any artifact with 'com.some.company' as the group id or artifact id.",
        emptyText: 'enter a pattern'
      },
      {
        xtype: 'combo',
        name: 'mappingType',
        itemId: 'mappingType',
        fieldLabel: 'Rule Type',
        helpText: 'There are three types of rules: Inclusive (if the pattern matches, only use the repositories listed below), Exclusive (exclude the repositories listed below) and Blocking (block URLs matching the pattern).',
        emptyText: 'select a type',
        editable: false,
        store: [
          ['BLOCKING', 'Blocking'],
          ['INCLUSION', 'Inclusive'],
          ['EXCLUSION', 'Exclusive']
        ],
        queryMode: 'local'
      },
      {
        xtype: 'combo',
        name: 'groupId',
        itemId: 'groupId',
        fieldLabel: 'Repository Group',
        helpText: 'A repository group this route applies to.',
        emptyText: 'select a group',
        editable: false,
        store: me.groupStore,
        queryMode: 'local',
        displayField: 'name',
        valueField: 'id'
      },
      {
        xtype: 'nx-itemselector',
        name: 'mappedRepositoriesIds',
        itemId: 'mappedRepositoriesIds',
        fieldLabel: 'Repositories',
        helpText: 'Ordered list of repositories which will be searched to locate a particular artifact.',
        buttons: ['up', 'add', 'remove', 'down'],
        fromTitle: 'Available Repositories',
        toTitle: 'Ordered Route Repositories',
        store: me.repositoryStore,
        valueField: 'id',
        displayField: 'name',
        delimiter: null
      }
    ];

    me.callParent(arguments);
  }
});
