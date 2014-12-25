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
 * Maven proxy repository "Settings" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositorySettingsProxyMavenForm', {
  extend: 'NX.coreui.view.repository.RepositorySettingsForm',
  alias: [
    'widget.nx-repository-settings-proxy-maven1-form',
    'widget.nx-repository-settings-proxy-maven2-form'
  ],
  requires: [
    'NX.coreui.view.AuthenticationSettings',
    'NX.coreui.view.HttpRequestSettings'
  ],

  api: {
    submit: 'NX.direct.coreui_Repository.updateProxyMaven'
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
        xtype: 'nx-url',
        name: 'remoteStorageUrl',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_REMOTE'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_REMOTE_HELP'),
        emptyText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_REMOTE_PLACEHOLDER')
      },
      {
        xtype: 'checkbox',
        name: 'downloadRemoteIndexes',
        fieldLabel: NX.I18n.get('ADMIN_REPOSTIORIES_SETTINGS_DOWNLOAD'),
        helpText: NX.I18n.get('ADMIN_REPOSTIORIES_SETTINGS_DOWNLOAD_HELP'),
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'autoBlockActive',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_BLOCKING'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_BLOCKING_HELP'),
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'fileTypeValidation',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_FILE'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_FILE_HELP'),
        value: true
      },
      {
        xtype: 'combo',
        name: 'checksumPolicy',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CHECKSUM'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CHECKSUM_HELP'),
        emptyText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CHECKSUM_PLACEHOLDER'),
        editable: false,
        store: [
          ['IGNORE', 'Ignore'],
          ['WARN', 'Warn'],
          ['STRICT_IF_EXISTS', 'Strict If Exists'],
          ['STRICT', 'Strict']
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
        name: 'exposed',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_PUBLISH'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_PUBLISH_HELP'),
        value: true
      },
      {
        xtype: 'numberfield',
        name: 'notFoundCacheTTL',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_TTL'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_TTL_HELP'),
        minValue: -1,
        maxValue: 511000,
        allowDecimals: false,
        allowExponential: false,
        allowBlank: true
      },
      {
        xtype: 'numberfield',
        name: 'artifactMaxAge',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_ARTIFACT_AGE'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_ARTIFACT_AGE_HELP'),
        minValue: -1,
        maxValue: 511000,
        allowDecimals: false,
        allowExponential: false,
        allowBlank: true
      },
      {
        xtype: 'numberfield',
        name: 'metadataMaxAge',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_METADATA_AGE'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_METADATA_AGE_HELP'),
        minValue: -1,
        maxValue: 511000,
        allowDecimals: false,
        allowExponential: false,
        allowBlank: true
      },
      {
        xtype: 'numberfield',
        name: 'itemMaxAge',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_ITEM_AGE'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_ITEM_AGE_HELP'),
        minValue: -1,
        maxValue: 511000,
        allowDecimals: false,
        allowExponential: false,
        allowBlank: true
      },
      {
        xtype: 'nx-optionalfieldset',
        title: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_AUTHENTICATION'),
        checkboxToggle: true,
        checkboxName: 'authEnabled',
        collapsed: true,
        items: {
          xtype: 'nx-coreui-authenticationsettings'
        }
      },
      {
        xtype: 'nx-optionalfieldset',
        title: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_HTTP'),
        checkboxToggle: true,
        checkboxName: 'httpRequestSettings',
        collapsed: true,
        items: {
          xtype: 'nx-coreui-httprequestsettings'
        }
      }
    ];

    me.callParent(arguments);
  }

});
