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
Ext.define('NX.coreui.view.upload.MavenUpload', {
  extend: 'Ext.Panel',
  alias: 'widget.nx-coreui-upload-maven',

  layout: {
    type: 'vbox',
    align: 'stretch',
    pack: 'start'
  },

  maxWidth: 1024,

  style: {
    margin: '20px'
  },

  initComponent: function () {
    var me = this,
        store;

    store = Ext.create('NX.coreui.store.RepositoryOfType');
    store.load({
      params: {
        type: 'hosted',
        format: 'maven2'
      }
    });

    me.items = [
      {
        xtype: 'form',
        api: {
          submit: 'NX.direct.maven_Maven.upload'
        },

        defaults: {
          xtype: 'textfield',
          allowBlank: false
        },
        items: [
          {
            xtype: 'label',
            html: '<p>Select the repository where to upload and artifacts to be uploaded.</p>'
          },
          {
            xtype: 'combo',
            name: 'repository',
            fieldLabel: 'Repository',
            emptyText: 'select repository to upload to',
            queryMode: 'local',
            displayField: 'name',
            valueField: 'id',
            store: store,
            editable: false,
            width: 500
          },
          {
            xtype: 'label',
            html: '<p>Enter the following details, or leave them empty to be automatically detected based on selected artifacts.</p>'
          },
          {
            name: 'group',
            itemId: 'group',
            fieldLabel: 'Group',
            emptyText: 'enter a group name',
            width: 500
          },
          {
            name: 'artifact',
            itemId: 'artifact',
            fieldLabel: 'Artifact',
            emptyText: 'enter an artifact name',
            width: 500
          },
          {
            name: 'version',
            itemId: 'version',
            fieldLabel: 'Version',
            emptyText: 'enter a version',
            width: 500
          },
          {
            xtype: 'combo',
            name: 'packaging',
            itemId: 'packaging',
            fieldLabel: 'Packaging',
            emptyText: 'select or enter packaging',
            queryMode: 'local',
            displayField: 'name',
            valueField: 'id',
            store: ['pom', 'jar', 'ejb', 'war', 'ear', 'rar', 'par', 'maven-archetype', 'maven-plugin'],
            width: 500
          }
        ],

        buttonAlign: 'left',
        buttons: [
          { text: 'Upload', action: 'upload', ui: 'primary', formBind: true },
          { text: 'Discard', action: 'discard' }
        ]
      }
    ];

    me.callParent();
  }

});