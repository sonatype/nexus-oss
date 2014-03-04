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
Ext.define('NX.coreui.view.repository.RepositorySettingsProxyMaven', {
  extend: 'NX.coreui.view.repository.RepositorySettings',
  alias: ['widget.nx-repository-settings-proxy-maven1', 'widget.nx-repository-settings-proxy-maven2'],
  requires: [
    'NX.coreui.view.AuthenticationSettings',
    'NX.coreui.view.HttpRequestSettings'
  ],

  api: {
    submit: 'NX.direct.coreui_Repository.updateProxyMaven'
  },
  settingsFormSuccessMessage: function (data) {
    return 'Repository updated: ' + data['id'];
  },

  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'combo',
        name: 'repositoryPolicy',
        itemId: 'repositoryPolicy',
        fieldLabel: 'Repository Policy',
        emptyText: 'select a policy',
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
      {
        xtype: 'nx-url',
        name: 'remoteStorageUrl',
        fieldLabel: 'Remote Storage Location',
        emptyText: 'enter an URL'
      },
      {
        xtype: 'checkbox',
        name: 'downloadRemoteIndexes',
        fieldLabel: 'Download Remote Indexes',
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'autoBlockActive',
        fieldLabel: 'Auto Blocking Enabled',
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'fileTypeValidation',
        fieldLabel: 'File Content Validation',
        value: true
      },
      {
        xtype: 'combo',
        name: 'checksumPolicy',
        fieldLabel: 'Checksum Policy',
        emptyText: 'select a policy',
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
        fieldLabel: 'Allow file browsing',
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'exposed',
        fieldLabel: 'Publish URL',
        value: true
      },
      {
        xtype: 'numberfield',
        name: 'notFoundCacheTTL',
        fieldLabel: 'Not Found Cache TTL',
        allowBlank: true
      },
      {
        xtype: 'numberfield',
        name: 'artifactMaxAge',
        fieldLabel: 'Artifact Max Age',
        allowBlank: true
      },
      {
        xtype: 'numberfield',
        name: 'metadataMaxAge',
        fieldLabel: 'Metadata Max Age',
        allowBlank: true
      },
      {
        xtype: 'numberfield',
        name: 'itemMaxAge',
        fieldLabel: 'Item Max Age',
        allowBlank: true
      },
      {
        xtype: 'nx-optionalfieldset',
        title: 'Authentication',
        checkboxToggle: true,
        checkboxName: 'authEnabled',
        collapsed: true,
        items: {
          xtype: 'nx-coreui-authenticationsettings'
        }
      },
      {
        xtype: 'nx-optionalfieldset',
        title: 'HTTP Request Settings',
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
