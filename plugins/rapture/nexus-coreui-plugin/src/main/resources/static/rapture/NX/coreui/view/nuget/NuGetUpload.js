/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext, NX*/

/**
 * NuGet upload panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.nuget.NuGetUpload', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-nuget-upload',

  items: {
    xtype: 'nx-coreui-upload-file',
    api: {
      submit: 'NX.direct.nuget_Upload.uploadPackages'
    },
    entryName: 'package'
  },

  initComponent: function() {
    var me = this;

    me.callParent(arguments);

    me.down('form').insert(0, [
      {
        xtype: 'combo',
        name: 'repositoryId',
        fieldLabel: 'Repository',
        helpText: 'Select the repository where packages will be uploaded.',
        emptyText: 'select repository',
        queryMode: 'local',
        displayField: 'name',
        valueField: 'id',
        store: 'NuGetUploadRepositoryHosted',
        editable: false
      }
    ]);
  }
});
