/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * Repository route form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositoryroute.RepositoryRouteSettingsForm', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-repositoryroute-settings-form',
  requires: [
    'NX.Conditions',
    'NX.coreui_legacy.store.RepositoryReference',
    'NX.coreui.model.Reference',
    'NX.I18n'
  ],

  api: {
    submit: 'NX.direct.coreui_RepositoryRoute.update'
  },
  settingsFormSuccessMessage: function(data) {
    return NX.I18n.get('ADMIN_ROUTING_UPDATE_SUCCESS') + data['pattern'];
  },
  editableMarker: NX.I18n.get('ADMIN_ROUTING_UPDATE_ERROR'),

  models: [
    'Reference'
  ],

  initComponent: function() {
    var me = this;

    me.editableCondition = me.editableCondition || NX.Conditions.isPermitted('nexus:routes', 'update');

    me.groupStore = Ext.create('NX.coreui_legacy.store.RepositoryReference', { remoteFilter: true });
    me.mon(me.groupStore, 'load', function(store) {
      store.add(Ext.create('NX.coreui.model.Reference', { id: '*', name: NX.I18n.get('ADMIN_ROUTING_SETTINGS_GROUP_ALL_ITEM') }));
    });
    me.groupStore.filter({ property: 'type', value: 'group' });

    me.repositoryStore = Ext.create('NX.coreui_legacy.store.RepositoryReference');
    me.repositoryStore.load();

    me.items = [
      {
        xtype: 'hiddenfield',
        name: 'id'
      },
      {
        xtype: 'nx-regexp',
        name: 'pattern',
        itemId: 'pattern',
        fieldLabel: NX.I18n.get('ADMIN_ROUTING_SETTINGS_URL'),
        helpText: NX.I18n.get('ADMIN_ROUTING_SETTINGS_URL_HELP'),
        emptyText: NX.I18n.get('ADMIN_ROUTING_SETTINGS_URL_PLACEHOLDER')
      },
      {
        xtype: 'combo',
        name: 'mappingType',
        itemId: 'mappingType',
        fieldLabel: NX.I18n.get('ADMIN_ROUTING_SETTINGS_RULE'),
        emptyText: NX.I18n.get('ADMIN_ROUTING_SETTINGS_RULE_PLACEHOLDER'),
        editable: false,
        store: [
          ['BLOCKING', NX.I18n.get('ADMIN_ROUTING_SETTINGS_BLOCKING_ITEM')],
          ['INCLUSION', NX.I18n.get('ADMIN_ROUTING_SETTINGS_INCLUSIVE_ITEM')],
          ['EXCLUSION', NX.I18n.get('ADMIN_ROUTING_SETTINGS_EXCLUSIVE_ITEM')]
        ],
        queryMode: 'local'
      },
      {
        xtype: 'combo',
        name: 'groupId',
        itemId: 'groupId',
        fieldLabel: NX.I18n.get('ADMIN_ROUTING_SETTINGS_GROUP'),
        emptyText: NX.I18n.get('ADMIN_ROUTING_SETTINGS_GROUP_PLACEHOLDER'),
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
        fieldLabel: NX.I18n.get('ADMIN_ROUTING_SETTINGS_REPOSITORIES'),
        helpText: NX.I18n.get('ADMIN_ROUTING_SETTINGS_REPOSITORIES_HELP'),
        buttons: ['up', 'add', 'remove', 'down'],
        fromTitle: NX.I18n.get('ADMIN_ROUTING_SETTINGS_AVAILABLE'),
        toTitle: NX.I18n.get('ADMIN_ROUTING_SETTINGS_ORDERED'),
        store: me.repositoryStore,
        valueField: 'id',
        displayField: 'name',
        delimiter: null
      }
    ];

    me.callParent(arguments);
  }
});
