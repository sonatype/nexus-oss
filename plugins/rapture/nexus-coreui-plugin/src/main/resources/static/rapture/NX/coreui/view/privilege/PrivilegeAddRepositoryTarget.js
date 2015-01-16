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
    'NX.coreui.model.Reference',
    'NX.I18n'
  ],
  ui: 'nx-inset',

  defaultFocus: 'name',

  initComponent: function () {
    var me = this;

    me.repositoryStore = Ext.create('NX.coreui.store.RepositoryReference');
    me.mon(me.repositoryStore, 'load', function (store) {
      store.add(Ext.create('NX.coreui.model.Reference', { id: '', name: NX.I18n.get('ADMIN_PRIVILEGES_CREATE_ALL') }));
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
      editableMarker: NX.I18n.get('ADMIN_PRIVILEGES_CREATE_ERROR'),

      buttons: [
        { text: NX.I18n.get('ADMIN_PRIVILEGES_LIST_NEW_BUTTON'), action: 'add', formBind: true, ui: 'nx-primary' },
        { text: NX.I18n.get('GLOBAL_DIALOG_ADD_CANCEL_BUTTON'), handler: function () {
          this.up('nx-drilldown').showChild(0, true);
        }}
      ],

      items: [
        {
          name: 'name',
          itemId: 'name',
          fieldLabel: NX.I18n.get('ADMIN_PRIVILEGES_CREATE_NAME')
        },
        {
          name: 'description',
          fieldLabel: NX.I18n.get('ADMIN_PRIVILEGES_CREATE_DESCRIPTION')
        },
        {
          xtype: 'combo',
          name: 'repositoryId',
          itemId: 'repositoryId',
          fieldLabel: NX.I18n.get('ADMIN_PRIVILEGES_CREATE_REPOSITORY'),
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
          fieldLabel: NX.I18n.get('ADMIN_PRIVILEGES_CREATE_TARGET'),
          emptyText: NX.I18n.get('ADMIN_PRIVILEGES_CREATE_TARGET_PLACEHOLDER'),
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
