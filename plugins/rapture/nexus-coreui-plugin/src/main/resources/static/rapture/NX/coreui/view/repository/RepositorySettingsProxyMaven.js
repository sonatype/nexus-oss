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
 * Maven proxy repository settings form.
 *
 * @since 3.0
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
  settingsFormSuccessMessage: function(data) {
    return 'Repository updated: ' + data['id'];
  },

  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'combo',
        name: 'repositoryPolicy',
        itemId: 'repositoryPolicy',
        fieldLabel: 'Repository Policy',
        helpText: 'Repositories can store either all Release artifacts or all Snapshot artifacts.',
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
        helpText: 'This is the location of the remote repository being proxied. Only HTTP/HTTPs urls are currently supported.',
        emptyText: 'enter an URL'
      },
      {
        xtype: 'checkbox',
        name: 'downloadRemoteIndexes',
        fieldLabel: 'Download Remote Indexes',
        helpText: 'Indicates if the index stored on the remote repository should be downloaded and used for local searches.',
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'autoBlockActive',
        fieldLabel: 'Auto Blocking Enabled',
        helpText: 'Flag to enable Auto Blocking for this proxy repository. If enabled, Nexus will auto-block outbound connections on this repository if remote peer is detected as unreachable/unresponsive. Auto-blocked repositories will still try to detect remote peer availability, and will auto-unblock the proxy if remote peer detected as reachable/healthy. Auto-blocked repositories behaves exactly the same as user blocked proxy repositories, except they will auto-unblock themselves too.',
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'fileTypeValidation',
        fieldLabel: 'File Content Validation',
        helpText: 'Flag to check the remote file contents to see if it is valid. (e.g. not html error page), handy when you cannot enable strict checksum checking.',
        value: true
      },
      {
        xtype: 'combo',
        name: 'checksumPolicy',
        fieldLabel: 'Checksum Policy',
        helpText: 'The checksum policy for this repository: Ignore: Do not check remote checksums. Warn: Log a warning if the checksum is bad but serve the artifact anyway. (Default...there are currently known checksum errors on Central). StrictIfExists: Do not serve the artifact if the checksum exists but is invalid. Strict: Require that a checksum exists on the remote repository and that it is valid.',
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
        helpText: 'This controls if users can browse the contents of the repository via their web browser.',
        value: true
      },
      {
        xtype: 'checkbox',
        name: 'exposed',
        fieldLabel: 'Publish URL',
        helpText: 'This controls if the repository is published on a URL, if this field is false you will not be able to access this repository remotely.',
        value: true
      },
      {
        xtype: 'numberfield',
        name: 'notFoundCacheTTL',
        fieldLabel: 'Not Found Cache TTL',
        helpText: 'This controls how long to cache the fact that a file was not found in the repository.',
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
        helpText: 'This controls how long to cache the artifacts in the repository before rechecking the remote repository. In a release repository, this value should be -1 (infinite) as release artifacts should not change.',
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
        helpText: 'This controls how long to cache the metadata in the repository before rechecking the remote repository. Unlike artifact max age, this value should not be infinite or Maven will not discover new artifact releases.',
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
        helpText: 'Repositories may contain resources that are neither artifacts identified by GAV coordinates or metadata. This value controls how long to cache such items in the repository before rechecking the remote repository.',
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
