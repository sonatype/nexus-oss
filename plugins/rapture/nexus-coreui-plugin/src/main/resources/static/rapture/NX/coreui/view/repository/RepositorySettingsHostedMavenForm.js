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
 * Maven hosted repository "Settings" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositorySettingsHostedMavenForm', {
  extend: 'NX.coreui.view.repository.RepositorySettingsForm',
  alias: [
    'widget.nx-repository-settings-hosted-maven1-form',
    'widget.nx-repository-settings-hosted-maven2-form'
  ],

  api: {
    submit: 'NX.direct.coreui_Repository.updateHostedMaven'
  },
  settingsFormSuccessMessage: function(data) {
    return 'Repository updated: ' + data['id'];
  },

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'combo',
        name: 'repositoryPolicy',
        itemId: 'repositoryPolicy',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_POLICY'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_POLICY_HELP'),
        emptyText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_POLICY_PLACEHOLDER'),
        editable: false,
        store: [
          ['RELEASE', 'Release'],
          ['SNAPSHOT', 'Snapshots']
        ],
        queryMode: 'local',
        readOnly: true,
        allowBlank: true,
        submitValue: false
      },
      { xtype: 'nx-coreui-repository-settings-localstorage' },
      {
        xtype: 'combo',
        name: 'writePolicy',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_HELP'),
        emptyText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_PLACEHOLDER'),
        editable: false,
        store: [
          ['ALLOW_WRITE', 'Allow Redeploy'],
          ['ALLOW_WRITE_ONCE', 'Disable Redeploy'],
          ['READ_ONLY', 'Read Only']
        ],
        queryMode: 'local'
      },
      {
        xtype: 'checkbox',
        name: 'browseable',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_BROWSING'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_BROWSING_HELP'),
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'indexable',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_SEARCH'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_SEARCH_HELP'),
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'exposed',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_PUBLISH'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_PUBLISH_HELP'),
        value: true
      }
    ];

    me.callParent(arguments);
  }

});
