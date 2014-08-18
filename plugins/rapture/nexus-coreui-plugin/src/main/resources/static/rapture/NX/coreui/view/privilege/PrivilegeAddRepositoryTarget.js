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
 * Privilege add window (for repository target).
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.privilege.PrivilegeAddRepositoryTarget', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-privilege-add-repositorytarget',
  requires: [
    'NX.Conditions',
    'NX.coreui.store.RepositoryReference',
    'NX.coreui.store.RepositoryTarget',
    'NX.coreui.model.Reference'
  ],

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
          helpText: 'The name of this privilege.',
          emptyText: 'enter a name'
        },
        {
          name: 'description',
          fieldLabel: 'Description',
          helpText: 'The description of this privilege.',
          emptyText: 'enter a description'
        },
        {
          xtype: 'combo',
          name: 'repositoryId',
          itemId: 'repositoryId',
          fieldLabel: 'Repository',
          helpText: 'The repository or repository group this privilege will be associated with.',
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
          helpText: 'The Repository Target that will be applied with this privilege.',
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
