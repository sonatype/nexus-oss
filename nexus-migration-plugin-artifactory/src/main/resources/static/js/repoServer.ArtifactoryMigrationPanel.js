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

  this.formPanel = new Ext.form.FormPanel( {
    region: 'center',
    trackResetOnLoad: true,
    autoScroll: true,
    border: false,
    frame: true,
    width: '100%',
//    autoWidth: true,
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
  
  cleanupImportForm: function() {
    var fieldset = this.findById( 'artifactory-import-step2-fieldset' );
    if ( this.importPanel ) {
      fieldset.remove( this.importPanel );
      this.importPanel = null;
    }
    fieldset.collapse();

    this.findById( 'artifactory-import-browse-button' ).enable();
  },

  createImportForm: function( data ) {
    var fieldset1 = this.findById( 'artifactory-import-step1-fieldset' );
    var fieldset2 = this.findById( 'artifactory-import-step2-fieldset' );
    fieldset2.expand();
    
    this.importData = data;
    this.importPanel = new Ext.Panel( {
      items: [
        {
          xtype: 'hidden',
          name: 'backupLocation',
          value: data.backupLocation
        },
        {
          xtype: 'hidden',
          name: 'resolvePermission',
          value: data.resolvePermission
        }
      ]
    } );
    this.createGroupForms( data.groupsResolution );
    this.createRepositoryForms( data.repositoriesResolution );
    this.createUserForms( data.userResolution );
    fieldset2.add( this.importPanel );
    this.doLayout();
    fieldset1.setWidth( fieldset2.getSize().width ); // weird layout issue, fieldset1 wouldn't resize without this
  },

  createGroupForms: function( groups ) {
    if ( groups && groups.length > 0 ) {
      var items = [];
      for ( var i = 0; i < groups.length; i++ ) {
        var group = groups[i];
        var prefix = 'groupsResolution_' + i;
        items.push( {
          xtype: 'checkbox',
          hideLabel: true,
          boxLabel: group.groupId + ' (' + group.repositoryTypeResolution.toLowerCase().replace( /_/g, ' ' ) + ')',
          name: prefix,
          checked: true
        } );
      }

      this.importPanel.add( {
        xtype: 'fieldset',
        checkboxToggle: true,
        checked: true,
        name: 'groupsResolution',
        title: 'Groups',
        anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        autoHeight: true,
        items: items
      } );
    }
  },

  createRepositoryForms: function( repositories ) {
    if ( repositories && repositories.length > 0 ) {
      var items = [];
      for ( var i = 0; i < repositories.length; i++ ) {
        var repo = repositories[i];
        var prefix = 'repositoriesResolution_' + i;
        items.push( {
          xtype: 'fieldset',
          checkboxToggle: true,
          checked: true,
          border: false,
          name: prefix,
          title: repo.repositoryId + ' (' + repo.type.toLowerCase() + ')',
          anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
          autoHeight: true,
          labelWidth: 40,
          layoutConfig: {
            labelSeparator: ''
          },
  
          items: [
            {
              style: 'padding-left: 30px',
              xtype: 'checkbox',
              boxLabel: 'Map URLs',
              name: prefix + '.mapUrls',
              checked: repo.mapUrls
            },
            {
              style: 'padding-left: 30px',
              xtype: 'checkbox',
              boxLabel: 'Copy Cached Artifacts',
              name: prefix + '.copyCachedArtifacts',
              checked: repo.copyCachedArtifacts
            }
          ]
        } );
      }

      this.importPanel.add( {
        xtype: 'fieldset',
        checkboxToggle: true,
        checked: true,
        name: 'repositoriesResolution',
        title: 'Repositories',
        anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        autoHeight: true,
        items: items
      } );
    }
  },

  createUserForms: function( users ) {
    if ( users && users.length > 0 ) {
      var items = [];
      for ( var i = 0; i < users.length; i++ ) {
        var user = users[i];
        var prefix = 'userResolution_' + i;
        var text = user.id;
        user.email = 'bebe@bebe.be';
        if ( user.email ) {
          text += ' <a href="mailto:' + user.email + '">&lt;' + user.email + '&gt;</a>';
        }
        if ( user.isAdmin ) {
          text += ' (admin)';
        }
        items.push( {
          xtype: 'checkbox',
          hideLabel: true,
          boxLabel: text,
          name: prefix,
          checked: true
        } );
      }

      this.importPanel.add( {
        xtype: 'fieldset',
        checkboxToggle: true,
        checked: true,
        name: 'userResolution',
        title: 'Users',
        anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        autoHeight: true,
        items: items
      } );
    }
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
            this.createImportForm( r.data );
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
