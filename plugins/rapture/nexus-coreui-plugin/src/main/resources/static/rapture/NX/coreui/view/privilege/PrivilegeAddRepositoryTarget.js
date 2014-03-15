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
 * Privilege add panel (for repository target).
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.privilege.PrivilegeAddRepositoryTarget', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-privilege-add-repositorytarget',

  title: 'Create new Repository Target Privilege',
  defaultFocus: 'name',

  initComponent: function () {
    var me = this;

    me.repositoryStore = Ext.create('NX.coreui.store.RepositoryReference');
    me.mon(me.repositoryStore, 'load', function (store) {
      store.add(Ext.create('NX.coreui.model.Reference', { id: '', name: 'All Repositories' }));
    });
    me.repositoryStore.load();

    me.targetStore = Ext.create('NX.coreui.store.RepositoryTarget');
    me.targetStore.load();

    me.items = {
      xtype: 'nx-settingsform',
      api: {
        submit: 'NX.direct.coreui_Privilege.createForRepositoryTarget'
      },
      editableCondition: NX.Conditions.isPermitted('security:privileges', 'create'),
      editableMarker: 'You do not have permission to create privileges',

      items: [
        {
          name: 'name',
          itemId: 'name',
          fieldLabel: 'Name',
          emptyText: 'enter a name'
        },
        {
          name: 'description',
          fieldLabel: 'Description',
          emptyText: 'enter a description'
        },
        {
          xtype: 'combo',
          name: 'repositoryId',
          itemId: 'repositoryId',
          fieldLabel: 'Repository',
          emptyText: 'select a repository',
          editable: false,
          store: me.repositoryStore,
          queryMode: 'local',
          displayField: 'name',
          valueField: 'id'
        },
        {
          xtype: 'combo',
          name: 'repositoryTargetId',
          itemId: 'repositoryTargetId',
          fieldLabel: 'Repository Target',
          emptyText: 'select a target',
          editable: false,
          store: me.targetStore,
          queryMode: 'local',
          displayField: 'name',
          valueField: 'id'
        }
      ]
    };

    me.callParent(arguments);
  }

});
