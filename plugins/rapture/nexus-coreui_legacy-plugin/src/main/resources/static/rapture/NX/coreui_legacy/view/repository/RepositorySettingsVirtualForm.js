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
 * Virtual repository "Settings" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui_legacy.view.repository.RepositorySettingsVirtualForm', {
  extend: 'NX.coreui_legacy.view.repository.RepositorySettingsForm',
  alias: 'widget.nx-repository-settings-virtual-form',
  requires: [
    'NX.coreui_legacy.store.RepositoryReference',
    'NX.I18n'
  ],

  api: {
    submit: 'NX.direct.coreui_legacy_Repository.updateVirtual'
  },
  settingsFormSuccessMessage: function(data) {
    return NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_UPDATE_SUCCESS') + data['id'];
  },

  initComponent: function() {
    var me = this;

    me.repositoryStore = Ext.create('NX.coreui_legacy.store.RepositoryReference', { remoteFilter: true });
    if (me.template['masterFormat']) {
      me.repositoryStore.filter({ property: 'format', value: me.template['masterFormat'] });
    }
    else {
      me.repositoryStore.filter({ property: 'format', value: '!' + me.template['format'] });
    }

    me.items = [
      {
        xtype: 'checkbox',
        name: 'browseable',
        fieldLabel: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_SETTINGS_BROWSING'),
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'exposed',
        fieldLabel: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_SETTINGS_PUBLISH'),
        value: true
      },
      {
        xtype: 'combo',
        name: 'shadowOf',
        itemId: 'shadowOf',
        fieldLabel: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_SETTINGS_SOURCE'),
        helpText: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_SETTINGS_SOURCE_HELP'),
        emptyText: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_SETTINGS_SOURCE_PLACEHOLDER'),
        editable: false,
        readOnly: true,
        store: me.repositoryStore,
        queryMode: 'local',
        displayField: 'name',
        valueField: 'id'
      },
      {
        xtype: 'checkbox',
        name: 'synchronizeAtStartup',
        fieldLabel: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_SETTINGS_SYNCHRONIZE'),
        helpText: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_SETTINGS_SYNCHRONIZE_HELP'),
        value: true
      }
    ];

    me.callParent(arguments);
  }

});
