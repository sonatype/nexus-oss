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

  var mixResolutionStore = new Ext.data.SimpleStore( {
    fields: ['value'], 
    data: [['releases only'], ['snapshots only'], ['both']]
  } );

  var typeResolutionStore = new Ext.data.SimpleStore( {
    fields: ['value'], 
    data: [['maven 1 only'], ['maven 2 only'], ['virtual both']]
  } );

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
        mapping: 'isMixed',
        convert: function( v, rec ) { return v ? rec.repositoryTypeResolution.toLowerCase().replace( /_/g, ' ' ) : ''; }
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
        convert: function( v, rec ) { return v ? rec.mixResolution.toLowerCase().replace( /_/g, ' ' ) : ''; }
      },
      { name: 'import', type: 'bool', defaultValue: true }
    ]
  } );

  this.userStore = new Ext.data.JsonStore( {
    id: 'userId',
    autoLoad: false,
    sortInfo: { field: 'userId', direction: 'ASC' },
    data: [],
    fields: [
      { name: 'userId', sortType:Ext.data.SortTypes.asUCString },
      { name: 'email' },
      { name: 'isAdmin', type: 'bool' },
      { name: 'password' },
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
          'In order to proceed, you first need to upload a .zip file with a configuration backup to ' +
          'the server where Nexus is running. Once the file upload is complete you can specify its ' +
          'location in the form below, and then you will be presented with a list of available ' +
          'repositories and import options.'
      },
      { 
        xtype: 'panel',
        layout: 'fit',
        items: [
          {
            xtype: 'fieldset',
            checkboxToggle: false,
            title: '1. Load Artifactory Configuration',
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
                html: 'Enter the path to your .zip file with Artifactory configuration backup and click "Load":'
              },
              {
                xtype: 'panel',
                layout: 'column',
                hideLabel: true,
                items: [
                  {
                    xtype: 'textfield',
                    name: 'filenameField',
                    columnWidth: .85,
                    validator: function( v ) {
                      Ext.getCmp( 'artifactory-import-browse-button' ).setDisabled( v == '' );
                      return true;
                    }
                  },
                  {
                    xtype: 'button',
                    text: 'Load',
                    id: 'artifactory-import-browse-button',
                    columnWidth: .1,
                    handler: this.loadBackup,
                    scope: this,
                    disabled: true,
                    setSize: function() {}
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
                    xtype: 'editorgrid',
                    title: 'Groups',
                    region: 'center',
                    frame: true,
                    ds: this.groupStore,
                    sortInfo: { field: 'groupId', direction: 'asc' },
                    loadMask: true,
                    deferredRender: true,
                    clicksToEdit: 1,
                    plugins: groupImportColumn,
                    columns: [
                      groupImportColumn,
                      { header: 'Group ID', dataIndex: 'groupId', width: 200 },
                      { 
                        header: 'Content Type Resolution', 
                        dataIndex: 'displayType', 
                        width: 200, 
                        editor: new Ext.form.ComboBox( {
                          typeAhead: true,
                          forceSelection: true,
                          selectOnFocus: true,
                          triggerAction: 'all',
                          store: typeResolutionStore,
                          mode: 'local',
                          displayField: 'value',
                          valueField: 'value',
                          lazyRender: true,
                          listClass: 'x-combo-list-small'
                        } )
                      }
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
                    xtype: 'editorgrid',
                    title: 'Repositories',
                    region: 'center',
                    frame: true,
                    ds: this.repoStore,
                    sortInfo: { field: 'repositoryId', direction: 'asc' },
                    loadMask: true,
                    deferredRender: true,
                    clicksToEdit: 1,
                    plugins: [ repoImportColumn, mapUrlsColumn, copyCachedArtifactsColumn, mergeWithColumn ],
                    columns: [
                      repoImportColumn,
                      { header: 'Repository ID', dataIndex: 'repositoryId', width: 200 },
                      { header: 'Type', dataIndex: 'displayType', width: 55 },
                      mapUrlsColumn,
                      copyCachedArtifactsColumn,
                      { 
                        header: 'Releases/Snapshots', 
                        dataIndex: 'displayMixedResolution', 
                        width: 120, 
                        editor: new Ext.form.ComboBox( {
                          typeAhead: true,
                          forceSelection: true,
                          selectOnFocus: true,
                          triggerAction: 'all',
                          store: mixResolutionStore,
                          mode: 'local',
                          displayField: 'value',
                          valueField: 'value',
                          lazyRender: true,
                          listClass: 'x-combo-list-small'
                        } )
                      },
                      mergeWithColumn
                    ],
                    listeners: {
                      beforeedit: function( e ) {
                        return e.value != '';
                      },
                      scope: this
                    }
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
                    xtype: 'editorgrid',
                    title: 'Users',
                    region: 'center',
                    frame: true,
                    ds: this.userStore,
                    sortInfo: { field: 'userId', direction: 'asc' },
                    loadMask: true,
                    deferredRender: true,
                    plugins: [ userImportColumn, adminColumn ],
                    columns: [
                      userImportColumn,
                      { header: 'User ID', dataIndex: 'userId', width: 200 },
                      { header: 'Email', dataIndex: 'email', width: 200 },
                      adminColumn
                    ]
                  }
                ]
              },
              {
                xtype: 'checkbox',
                boxLabel: 'Import Permissions',
                hideLabel: true,
                name: 'resolvePermission'
              }
            ]
          }
        ]
      }
    ],
    buttons: [
      {
        text: 'Start Import',
        disabled: true,
        handler: this.startImport,
        scope: this
      },
      {
        xtype: 'button',
        text: 'Show Log',
        id: 'artifactory-view-log-button',
        columnWidth: .1,
        handler: this.showMigrationLog,
        scope: this,
        disabled: false,
        setSize: function() {}
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

  loadBackup: function() {
    this.el.mask( 'Loading...' );

    var filenameField = this.formPanel.form.findField( 'filenameField' );
    var filename = filenameField.getValue( filename );
    filenameField.disable();
    this.findById( 'artifactory-import-browse-button' ).disable();

    Ext.Ajax.request( {
      method: 'POST',
      url: Sonatype.config.servicePath + '/migration/artifactory/filelocation',
      jsonData: { data: { fileLocation: filename } },
      callback: function( options, success, response ) {
        this.el.unmask();

        if ( success ) {
          var r = Ext.decode( response.responseText );
          this.loadImportData( r.data );
          return;
        }

        this.formPanel.form.findField( 'filenameField' ).enable();
      },
      scope : this
    } );
  },

  showMigrationLog: function() {

//	  FIXME, tabe title is duplicated here
	  var panel = Sonatype.view.mainTabPanel.addOrShowTab(
              'st-nexus-search-panel', Sonatype.repoServer.LogsViewPanel, { title: 'Logs and Config Files' } );
	  
	  // FIXME: Add check to see if log exists
	  panel.showLog('migration.log');

  },
  

  loadImportData: function( data ) {
    this.importData = data;
    this.groupStore.loadData( data.groupsResolution );
    this.repoStore.loadData( data.repositoriesResolution );
    this.userStore.loadData( data.usersResolution );
    this.formPanel.form.setValues( { resolvePermission: data.resolvePermission } );

    var fieldset1 = this.findById( 'artifactory-import-step1-fieldset' );
    var fieldset2 = this.findById( 'artifactory-import-step2-fieldset' );
    fieldset2.expand();
    fieldset1.setWidth( fieldset2.getSize().width ); // weird layout issue, fieldset1 wouldn't resize without this
    this.formPanel.buttons[0].enable();
  },
  
  startImport: function() {
    this.el.mask( 'Importing...' );
    this.formPanel.buttons[0].disable();

    var data = {
      backupLocation: this.importData.backupLocation,
      resolvePermission: this.formPanel.form.findField( 'resolvePermission' ).checked,
      groupsResolution: [],
      repositoriesResolution: [],
      usersResolution: []
    };
    
    this.groupStore.each( function( rec ) {
      if ( rec.data.import ) {
        data.groupsResolution.push( {
          groupId: rec.data.groupId,
          isMixed: rec.data.isMixed,
          repositoryTypeResolution: rec.data.displayType.toUpperCase().replace( /\ /g, '_' )
        } );
      }
    }, this );
    
    this.repoStore.each( function( rec ) {
      if ( rec.data.import ) {
        data.repositoriesResolution.push( {
          repositoryId: rec.data.repositoryId,
          type: rec.data.type,
          mapUrls: rec.data.mapUrls,
          copyCachedArtifacts: rec.data.copyCachedArtifacts,
          isMixed: rec.data.isMixed,
          mixResolution: rec.data.displayMixedResolution.toUpperCase().replace( /\ /g, '_' ),
          similarRepositoryId: rec.data.similarRepositoryId, 
          mergeSimilarRepository: rec.data.mergeSimilarRepository
        } );
      }
    }, this );

    this.userStore.each( function( rec ) {
      if ( rec.data.import ) {
        data.usersResolution.push( {
          userId: rec.data.userId,
          email: rec.data.email,
          password: rec.data.password,
          isAdmin: rec.data.isAdmin
        } );
      }
    }, this );

    Ext.Ajax.request( {
      method: 'POST',
      url: Sonatype.config.servicePath + '/migration/artifactory/content',
      jsonData: { data: data },
      callback: function( options, success, response ) {
        this.el.unmask();

        if ( success ) {
          this.formPanel.buttons[1].setText( 'Close' );
          Sonatype.MessageBox.show( {
            title: 'Import Successful',
            msg: 'Artifactory backup import completed successfully',
            buttons: Sonatype.MessageBox.OK,
            icon: Sonatype.MessageBox.INFO
          } );
        }
        else {
          this.formPanel.buttons[0].enable();
          Sonatype.utils.connectionError( response, 'Artifactory import failed!' );
        }
      },
      scope : this
    } );
  }
} );

Sonatype.Events.addListener( 'nexusNavigationInit', function( nexusPanel ) {
  nexusPanel.add( {
    enabled: Sonatype.lib.Permissions.checkPermission( 'nexus:artifactorymigrate', Sonatype.lib.Permissions.CREATE ) &&
      Sonatype.lib.Permissions.checkPermission( 'nexus:artifactoryfilelocation', Sonatype.lib.Permissions.CREATE ),
    sectionId: 'st-nexus-config',
    title: 'Artifactory Import',
    tabId: 'migration-artifactory',
    tabCode: Sonatype.repoServer.ArtifactoryMigrationPanel
  } );
} );
