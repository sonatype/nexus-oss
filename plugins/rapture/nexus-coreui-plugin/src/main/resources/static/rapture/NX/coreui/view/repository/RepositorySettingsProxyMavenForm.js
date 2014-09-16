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
        fieldLabel: 'Repository Policy',
        helpText: 'Maven repositories can store either release or snapshot artifacts.',
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
      { xtype: 'nx-coreui-repository-settings-localstorage' },
      {
        xtype: 'nx-url',
        name: 'remoteStorageUrl',
        fieldLabel: 'Remote Storage Location',
        helpText: 'Location of the remote repository being proxied.',
        emptyText: 'enter an URL'
      },
      {
        xtype: 'checkbox',
        name: 'downloadRemoteIndexes',
        fieldLabel: 'Download Remote Indexes',
        helpText: 'Allow downloading of remote repository indexes.',
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'autoBlockActive',
        fieldLabel: 'Auto Blocking Enabled',
        helpText: 'Auto-block outbound connections on the repository if remote peer is detected as unreachable/unresponsive.',
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'fileTypeValidation',
        fieldLabel: 'File Content Validation',
        helpText: 'Enable content validation for remote contents.',
        value: true
      },
      {
        xtype: 'combo',
        name: 'checksumPolicy',
        fieldLabel: 'Checksum Policy',
        helpText: 'Repository checksum policy.',
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
        xtype: 'numberfield',
        name: 'notFoundCacheTTL',
        fieldLabel: 'Not Found Cache TTL',
        helpText: 'How long to cache the fact that a file was not found in the repository.',
        minValue: -1,
        maxValue: 511000,
        allowDecimals: false,
        allowExponential: false,
        allowBlank: true
      },
      {
        xtype: 'numberfield',
        name: 'artifactMaxAge',
        fieldLabel: 'Artifact Max Age',
        helpText: 'How long to cache the artifacts in the repository before rechecking the remote repository.  Release repositories should use -1.',
        minValue: -1,
        maxValue: 511000,
        allowDecimals: false,
        allowExponential: false,
        allowBlank: true
      },
      {
        xtype: 'numberfield',
        name: 'metadataMaxAge',
        fieldLabel: 'Metadata Max Age',
        helpText: 'How long to cache the metadata in the repository before rechecking the remote repository.',
        minValue: -1,
        maxValue: 511000,
        allowDecimals: false,
        allowExponential: false,
        allowBlank: true
      },
      {
        xtype: 'numberfield',
        name: 'itemMaxAge',
        fieldLabel: 'Item Max Age',
        helpText: 'How long to cache non-artifact and metadata items in the repository before rechecking the remote repository.',
        minValue: -1,
        maxValue: 511000,
        allowDecimals: false,
        allowExponential: false,
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
