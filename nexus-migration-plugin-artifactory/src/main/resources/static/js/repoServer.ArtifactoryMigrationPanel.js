/*
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
Sonatype.repoServer.ArtifactoryMigrationPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Artifactory Import'
  };
  Ext.apply( this, config, defaultConfig );

  this.groupStore = new Ext.data.JsonStore( {
    id: 'groupId',
    autoLoad: false,
    sortInfo: { field: 'groupId', direction: 'ASC' },
    data: [],
    fields: [
      { name: 'groupId', sortType:Ext.data.SortTypes.asUCString },
      { name: 'repositoryTypeResolution' },
      { name: 'isMixed' },
      { 
        name: 'displayType',
        mapping: 'repositoryTypeResolution',
        convert: function( v, rec ) { return v.toLowerCase().replace( /_/g, ' ' ); }
      },
      { name: 'import', type: 'bool', defaultValue: true }
    ]
  } );

  this.repoStore = new Ext.data.JsonStore( {
    id: 'repositoryId',
    autoLoad: false,
    sortInfo: { field: 'repositoryId', direction: 'ASC' },
    data: [],
    fields: [
      { name: 'repositoryId', sortType:Ext.data.SortTypes.asUCString },
      { name: 'type' },
      { name: 'mapUrls', type: 'bool' },
      { name: 'copyCachedArtifacts', type: 'bool' },
      { name: 'similarRepositoryId' },
      { name: 'mergeSimilarRepository' },
      { name: 'isMixed' },
      { name: 'mixResolution' },
      { 
        name: 'displayType',
        mapping: 'type',
        convert: function( v, rec ) { return v.toLowerCase(); }
      },
      {
        name: 'displayMixedResolution',
        mapping: 'isMixed',
        convert: function( v, rec ) { return v ? rec.mixResolution.toLowerCase() : ''; }
      },
      { name: 'import', type: 'bool', defaultValue: true }
    ]
  } );

  this.userStore = new Ext.data.JsonStore( {
    id: 'id',
    autoLoad: false,
    sortInfo: { field: 'id', direction: 'ASC' },
    data: [],
    fields: [
      { name: 'id', sortType:Ext.data.SortTypes.asUCString },
      { name: 'email' },
      { name: 'isAdmin', type: 'bool' },
      { name: 'import', type: 'bool', defaultValue: true }
    ]
  } );

  var groupImportColumn = new Ext.grid.CheckColumn( {
    header: 'Import',
    dataIndex: 'import',
    width: 45
  } );

  var repoImportColumn = new Ext.grid.CheckColumn( {
    header: 'Import',
    dataIndex: 'import',
    width: 45
  } );

  var mapUrlsColumn = new Ext.grid.CheckColumn( {
    header: 'Map URLs',
    dataIndex: 'mapUrls',
    width: 70
  } );

  var copyCachedArtifactsColumn = new Ext.grid.CheckColumn( {
    header: 'Copy Cached Artifacts',
    dataIndex: 'copyCachedArtifacts',
    width: 120
  } );
  
  var mergeWithColumnClass = Ext.extend( Ext.grid.CheckColumn, {
    renderer: function( v, p, record, rowIndex ) {
      if ( record.data.similarRepositoryId ) {
        p.css += ' x-grid3-check-col-td'; 
        return '<div style="padding-left:16px; background-position: center left;" class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">' + record.data.similarRepositoryId + '</div>';
      }
      else {
        return '';
      }
    }
  } );
  
  var mergeWithColumn = new mergeWithColumnClass( {
    header: 'Merge With', 
    dataIndex: 'mergeSimilarRepository', 
    width: 120
  } );

  var userImportColumn = new Ext.grid.CheckColumn( {
    header: 'Import',
    dataIndex: 'import',
    width: 45
  } );

  var adminColumn = new Ext.grid.CheckColumn( {
    header: 'Admin',
    dataIndex: 'isAdmin',
    width: 45
  } );

  this.formPanel = new Ext.form.FormPanel( {
    region: 'center',
    trackResetOnLoad: true,
    autoScroll: true,
    border: false,
    frame: true,
    width: '100%',
    collapsible: false,
    collapsed: false,
    labelWidth: 175,
    layoutConfig: {
      labelSeparator: ''
    },
    
    items: [
      {
        style: 'padding: 10px;',
        cls: 'x-form-item',
        html: 'Import an existing Artifactory configuration into Nexus.<br/><br/>' +
          'Start by uploading a .zip file with a configuration backup. Once the upload is complete, ' +
          'you will be presented with a list of available repositories and import options.'
      },
      { 
        xtype: 'panel',
        layout: 'fit',
        items: [
          {
            xtype: 'fieldset',
            checkboxToggle: false,
            title: '1. Upload Artifactory Configuration',
            id: 'artifactory-import-step1-fieldset',
            anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
            autoHeight: true,
            layoutConfig: {
              labelSeparator: ''
            },
    
            items: [
              {
                style: 'padding-bottom: 10px',
                cls: 'x-form-item',
                html: 'Click "Browse" to upload a .zip file with your Artifactory configuration backup:'
              },
              {
                xtype: 'panel',
                layout: 'column',
                hideLabel: true,
                items: [
                  {
                    xtype: 'textfield',
                    name: 'filenameField',
                    readOnly: true,
                    columnWidth: .85
                  },
                  {
                    xtype: 'browsebutton',
                    text: 'Browse...',
                    id: 'artifactory-import-browse-button',
                    columnWidth: .1,
                    uploadPanel: this,
                    handler: function( b ) {
                      b.uploadPanel.fileInput = b.detachInputFile(); 
                      var filename = b.uploadPanel.fileInput.getValue();
                      b.uploadPanel.formPanel.find( 'name', 'filenameField' )[0].setValue( filename );
                      b.uploadPanel.uploadBackup();
                    }
                  }
                ] 
              }
            ]
          },
          {
            xtype: 'fieldset',
            checkboxToggle: false,
            title: '2. Customize Import',
            anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
            autoHeight: true,
            collapsed: true,
            layoutConfig: {
              labelSeparator: ''
            },
            id: 'artifactory-import-step2-fieldset',
    
            items: [
              {
                style: 'padding-bottom: 10px',
                cls: 'x-form-item',
                html: 'Select groups and repositories you wish to import:'
              },
              {
                height: 164,
                width: 'auto',
                autoScroll: true,
                layout: 'border',
                items: [ 
                  {
                    xtype: 'grid',
                    title: 'Groups',
                    region: 'center',
                    frame: true,
                    ds: this.groupStore,
                    sortInfo: { field: 'groupId', direction: 'asc' },
                    loadMask: true,
                    deferredRender: true,
                    plugins: groupImportColumn,
                    columns: [
                      groupImportColumn,
                      { header: 'Group ID', dataIndex: 'groupId', width: 200 },
                      { header: 'Type', dataIndex: 'displayType', width: 200 }
                    ]
                  }
                ]
              },
              {
                height: 200,
                width: 'auto',
                autoScroll: true,
                layout: 'border',
                items: [
                  {
                    xtype: 'grid',
                    title: 'Repositories',
                    region: 'center',
                    frame: true,
                    ds: this.repoStore,
                    sortInfo: { field: 'repositoryId', direction: 'asc' },
                    loadMask: true,
                    deferredRender: true,
                    plugins: [ repoImportColumn, mapUrlsColumn, copyCachedArtifactsColumn, mergeWithColumn ],
                    columns: [
                      repoImportColumn,
                      { header: 'Repository ID', dataIndex: 'repositoryId', width: 200 },
                      { header: 'Type', dataIndex: 'displayType', width: 55 },
                      mapUrlsColumn,
                      copyCachedArtifactsColumn,
                      { header: 'Releases/Snapshots', dataIndex: 'displayMixedResolution', width: 120 },
                      mergeWithColumn
                    ]
                  }
                ]
              },
              {
                height: 164,
                width: 'auto',
                autoScroll: true,
                layout: 'border',
                items: [
                  {
                    xtype: 'grid',
                    title: 'Users',
                    region: 'center',
                    frame: true,
                    ds: this.userStore,
                    sortInfo: { field: 'id', direction: 'asc' },
                    loadMask: true,
                    deferredRender: true,
                    plugins: [ userImportColumn, adminColumn ],
                    columns: [
                      userImportColumn,
                      { header: 'User ID', dataIndex: 'id', width: 200 },
                      { header: 'Email', dataIndex: 'email', width: 200 },
                      adminColumn
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ],
    buttons: [
      {
        text: 'Start Import',
        disabled: true
      },
      {
        text: 'Cancel',
        handler: this.cancelImport,
        scope: this
      }
    ]
  } );

  Sonatype.repoServer.ArtifactoryMigrationPanel.superclass.constructor.call( this, {
    autoScroll: false,
    layout: 'border',
    items: [
      this.formPanel
    ]
  } );
};

Ext.extend( Sonatype.repoServer.ArtifactoryMigrationPanel, Ext.Panel, {
  cancelImport: function() {
    Sonatype.view.mainTabPanel.remove( this.id, true );
  },

  loadImportData: function( data ) {
    this.importData = data;
    this.groupStore.loadData( data.groupsResolution );
    this.repoStore.loadData( data.repositoriesResolution );
    this.userStore.loadData( data.userResolution );

    var fieldset1 = this.findById( 'artifactory-import-step1-fieldset' );
    var fieldset2 = this.findById( 'artifactory-import-step2-fieldset' );
    fieldset2.expand();
    fieldset1.setWidth( fieldset2.getSize().width ); // weird layout issue, fieldset1 wouldn't resize without this
  },

  uploadBackup: function() {
    this.findById( 'artifactory-import-browse-button' ).disable();
    this.el.mask( 'Uploading...' );
    var tmpForm = Ext.getBody().createChild( {
      tag: 'form',
      cls: 'x-hidden',
      id: Ext.id()
    } );
    this.fileInput.appendTo( tmpForm );

    Ext.Ajax.request( {
      url: Sonatype.config.servicePath + '/migration/artifactory/upload',
      form: tmpForm,
      isUpload : true,
      callback: function( options, success, response ) {
        this.el.unmask();
        tmpForm.remove();

        //This is a hack to get around the fact that upload submit always returns
        //success = true
        if ( response.responseXML.title == '' ) {
          var r = Ext.decode( response.responseText );
          if ( r.data ) {
            this.loadImportData( r.data );
            return;
          }
        }

        var s = 'Artifact upload failed.<br />';
        var r = response.responseText;
        var n1 = r.toLowerCase().indexOf( '<h3>' ) + 4;
        var n2 = r.toLowerCase().indexOf( '</h3>' );
        if ( n2 > n1 ) {
          s += r.substring( n1, n2 );
        }
        else {
          s += 'Check Nexus logs for more information.';
        }
        Sonatype.MessageBox.show( {
          title: 'Upload Failed',
          msg: s,
          buttons: Sonatype.MessageBox.OK,
          icon: Sonatype.MessageBox.ERROR
        } );
      },
      scope : this
    } );
  }
} );

Sonatype.Events.addListener( 'nexusNavigationInit', function( nexusPanel ) {
  nexusPanel.add( {
    enabled: Sonatype.lib.Permissions.checkPermission( 'nexus:artifactorymigrate', Sonatype.lib.Permissions.CREATE ) &&
      Sonatype.lib.Permissions.checkPermission( 'nexus:artifactoryupload', Sonatype.lib.Permissions.CREATE ),
    sectionId: 'st-nexus-config',
    title: 'Artifactory Import',
    tabId: 'migration-artifactory',
    tabCode: Sonatype.repoServer.ArtifactoryMigrationPanel
  } );
} );
