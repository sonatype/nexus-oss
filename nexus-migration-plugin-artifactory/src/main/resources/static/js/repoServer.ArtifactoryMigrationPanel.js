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
      { name: 'isImport', type: 'bool', defaultValue: true },
      { name: 'alreadyExists' }
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
      { name: 'isImport', type: 'bool', defaultValue: true },
      { name: 'alreadyExists' }
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
      { name: 'isImport', type: 'bool', defaultValue: true }
    ]
  } );

  var groupImportColumn = new Ext.grid.CheckColumn( {
    header: 'Import',
    dataIndex: 'isImport',
    width: 45
  } );

  var repoImportColumn = new Ext.grid.CheckColumn( {
    header: 'Import',
    dataIndex: 'isImport',
    width: 45,
    onMouseDown : function(e, t)
    {
      if (t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1)
      {
        e.stopEvent();
    
        var index = this.grid.getView().findRowIndex(t);
        var record = this.grid.store.getAt(index);
    
        var alreadyExists = record.data.alreadyExists;
    
        if ( alreadyExists ) {
          var repoId  = record.data.repositoryId;
          
          Sonatype.MessageBox.show( {
            title: 'Unable to import "' + repoId + '"',
            msg: 'A repository with the same ID already exists!',
            buttons: Sonatype.MessageBox.OK,
            icon: Sonatype.MessageBox.WARNING
          } );
        } else {
          record.set(this.dataIndex, !record.data[this.dataIndex]);        
        }
      }
    } 
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
    dataIndex: 'isImport',
    width: 45
  } );

  var adminColumn = new Ext.grid.CheckColumn( {
    header: 'Admin',
    dataIndex: 'isAdmin',
    width: 45
  } );
  
  this.repositoryGrid = new Ext.grid.EditorGridPanel( {
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
          var rec = e.record;
          var alreadyExists = rec.data.alreadyExists;
          if( alreadyExists ) {
              return false;
          }

        return e.value != '';
      },
      scope: this
    },
    tools: [
      { id: 'help', qtip: 'List of repositories to be imported.  Uncheck "Import" flag to prevent the repository from being imported.', handler: function(){ } }
    ]
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
        html: 'The Artifactory Import is used to import an existing Artifactory System Export into Nexus.  It is tested against Artifactory 1.2.5 and 2.0.6, but it should be compatible with other versions.<br/><br/>' +
          '1 - Create an Artifactory "Entire System Export", the same used to migrate between Artifactory versions.  It can be zipped or not, both are supported.  The options "Include metadata" and "Create a .m2 compatible export" aren\'t relevant<br>' +
          '2 - Place this System Export on the server where Nexus is running.<br>' +
          '3 - Specify its location in Step 1. Load Artifactory Configuration, and then you will be presented with a list of available repositories and import options, which is customizable in Step 2. Customize Import on Artifactory Import.'
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
                html: 'Enter the path on the server to the location of your Artifactory System Export and select "Load".'
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
                  },
                  {
                    xtype:'box',
                    anchor:'',
                    width: 16,
                    height: 16,
                    autoEl:{
                      tag:'img',
                      qtip:'Specify the path to the Artifactory configuration zip file which has been stored on the server where Nexus is currently running.',
                      cls: 'migration-help-icon',
                      src: Sonatype.config.resourcePath + '/images/icons/help.png'
                    }
                  }
                ] 
              }
            ]
          },
          {
            xtype: 'fieldset',
            checkboxToggle: false,
            title: '2. Customize Import on Artifactory Import',
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
                    ],
                   listeners: {
                      beforeedit: function( e ) {
                          var rec = e.record;
                          var alreadyExists = rec.data.alreadyExists;
                        return !alreadyExists;
                      },
                      scope: this
                    },
                    tools: [
                      { id: 'help', qtip: 'List of groups to be imported.  Uncheck "Import" flag to prevent the group from being imported.', handler: function(){ } }
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
                  this.repositoryGrid
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
                    ],
                    tools: [
                      { id: 'help', qtip: 'List of users to be imported.  Uncheck "Import" flag to prevent the user from being imported.', handler: function(){ } }
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
        disabled: true,
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

//    FIXME, tabe title is duplicated here
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
    Ext.MessageBox.confirm('Warning', 'Loading the Artifactory Configuration could take a long period of time depending on the amount of information you are attempting to import. Your Nexus instance will be unusable during this time. Continue?' , this.proceedImport, this);  
  },

  proceedImport: function(action) {
    if(action == 'yes') {
      this.el.mask( 'Importing...' );
      this.formPanel.buttons[0].disable();
  
      var data = {
        backupLocation: this.importData.backupLocation,
        resolvePermission: this.formPanel.form.findField( 'resolvePermission' ).checked,
        groupsResolution: [],
        repositoriesResolution: [],
        usersResolution: []
      };
  
      this.groupStore.each( function( item ) {
        if ( item.data.isImport ) {
          data.groupsResolution.push( {
            groupId: item.data.groupId,
            isMixed: item.data.isMixed,
            repositoryTypeResolution: item.data.displayType.toUpperCase().replace( /\ /g, '_' )
          } );
        }
      }, this );
      
      this.repoStore.each( function( item ) {
        if ( item.data.isImport ) {
          data.repositoriesResolution.push( {
            repositoryId: item.data.repositoryId,
            type: item.data.type,
            mapUrls: item.data.mapUrls,
            copyCachedArtifacts: item.data.copyCachedArtifacts,
            isMixed: item.data.isMixed,
            mixResolution: item.data.displayMixedResolution.toUpperCase().replace( /\ /g, '_' ),
            similarRepositoryId: item.data.similarRepositoryId, 
            mergeSimilarRepository: item.data.mergeSimilarRepository
          } );
        }
      }, this );
      
      this.userStore.each( function( item ) {
        if ( item.data.isImport ) {
          data.usersResolution.push( {
            userId: item.data.userId,
            email: item.data.email,
            password: item.data.password,
            isAdmin: item.data.isAdmin
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
            this.formPanel.buttons[2].setText( 'Close' );
            this.formPanel.buttons[1].setDisabled( false );
            Sonatype.MessageBox.show( {
              title: 'Import Scheduled',
              msg: 'Artifactory importation started.  Click on "Show Log" for details.',
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
