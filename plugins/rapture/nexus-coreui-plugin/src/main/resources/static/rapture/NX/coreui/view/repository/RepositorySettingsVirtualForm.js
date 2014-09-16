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
 * Virtual repository "Settings" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositorySettingsVirtualForm', {
  extend: 'NX.coreui.view.repository.RepositorySettingsForm',
  alias: 'widget.nx-repository-settings-virtual-form',
  requires: [
    'NX.coreui.store.RepositoryReference'
  ],

  api: {
    submit: 'NX.direct.coreui_Repository.updateVirtual'
  },
  settingsFormSuccessMessage: function(data) {
    return 'Repository updated: ' + data['id'];
  },

  initComponent: function() {
    var me = this;

    me.repositoryStore = Ext.create('NX.coreui.store.RepositoryReference', { remoteFilter: true });
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
        fieldLabel: 'Allow file browsing',
        helpText: 'Allow users to browse the contents of the repository.',
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'exposed',
        fieldLabel: 'Publish URL',
        helpText: 'Expose the URL of the repository to users.',
        value: true
      },
      {
        xtype: 'combo',
        name: 'shadowOf',
        itemId: 'shadowOf',
        fieldLabel: 'Source repository',
        helpText: 'Physical repository being presented as a logical view by the repository.',
        emptyText: 'select a repository',
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
        fieldLabel: 'Synchronize on Startup',
        helpText: 'Rebuild virtual links when the server starts.',
        value: true
      }
    ];

    me.callParent(arguments);
  }

});
