/*
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
Sonatype.repoServer.ArtifactUploadPanel = function(config){
  var config = config || {};
  var defaultConfig = {
  };
  Ext.apply(this, config, defaultConfig);

  var ht = Sonatype.repoServer.resources.help.artifact;
  
  this.fileInput = null;
  this.pomInput = null;
  
  this.gavDefinitionStore = new Ext.data.SimpleStore({fields:['value','display'], data:[['pom','From POM'],['gav','GAV Parameters']]});
  
  var packagingStore = new Ext.data.SimpleStore( {
    fields: ['value'], 
    data: [['pom'], ['jar'], ['ejb'], ['war'], ['ear'], ['rar'], ['par'], ['maven-archetype'], ['maven-plugin']]
  } );

  Sonatype.repoServer.ArtifactUploadPanel.superclass.constructor.call(this, {
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
    fileUpload: true,
    width: '100%',
    height: '100%',
    autoScroll: true,
    layoutConfig: {
      labelSeparator: ''
    },
    items: [
      {
        xtype: 'hidden',
        name: 'r',
        value: this.payload.id
      },
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'Select Artifact(s) for Upload',
        collapsible: false,
        autoHeight:true,
        items: [
          {
            hideLabel: true,
            xtype: 'browsebutton',
            text: 'Select Artifact to Upload...',
            style :'margin-bottom: 5px;',
            uploadPanel: this,
            handler: function( b ) {
              b.uploadPanel.fileInput = b.detachInputFile(); 
              var filename = b.uploadPanel.fileInput.getValue();
              b.uploadPanel.updateFilename( b.uploadPanel, filename );
            }
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Filename',
            name: 'filenameField',
            anchor: Sonatype.view.FIELD_OFFSET,
            readOnly: true,
            allowBlank:true
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Classifier',
            helpText: ht.classifier,
            anchor: Sonatype.view.FIELD_OFFSET,
            name: 'classifier',
            allowBlank:true
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Extension',
            helpText: ht.extension,
            anchor: Sonatype.view.FIELD_OFFSET,
            name: 'extension',
            allowBlank:true
          },
          {
            xtype: 'combo',
            lazyInit: false,
            fieldLabel: 'GAV Definition',
            itemCls: 'required-field',
            helpText: ht.gavDefinition,
            name: 'gavDefinition',
            store: this.gavDefinitionStore,
            valueField:'value',
            displayField:'display',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            emptyText: 'Select...',
            allowBlank: false,
            value: 'pom',
            listeners: {
              select: {
                fn: this.gavDefinitionSelectHandler,
                scope: this
              }
            }
          },
          {
            xtype: 'panel',
            id: 'gav-definition-card-panel',
            header: false,
            layout: 'card',
            region: 'center',
            activeItem: 0,
            bodyStyle: 'padding:15px',
            deferredRender: false,
            autoScroll: false,
            frame: false,
            items: [
              {
                  xtype: 'fieldset',
                  checkboxToggle:false,
                  title: 'GAV Details',
                  anchor: Sonatype.view.FIELDSET_OFFSET,
                  collapsible: false,
                  autoHeight:true,
                  layoutConfig: {
                    labelSeparator: ''
                  },
                  items: [
                  {
                    xtype: 'label',
                    text: 'Select a GAV Definition to enter details.'
                  }
                ]
              },
              {
                xtype: 'fieldset',
                checkboxToggle:false,
                title: 'GAV details',
                collapsible: false,
                autoHeight:true,
                items: [
                  {
                    hideLabel: true,
                    xtype: 'browsebutton',
                    text: 'Select POM to Upload...',
                    style :'margin-bottom: 5px;',
                    uploadPanel: this,
                    handler: function( b ) {
                      b.uploadPanel.pomInput = b.detachInputFile(); 
                      var filename = b.uploadPanel.pomInput.getValue();
                      b.uploadPanel.updatePomFilename( b.uploadPanel, filename );
                    }
                  },
                  {
                    xtype: 'textfield',
                    fieldLabel: 'POM Filename',
                    name: 'pomnameField',
                    anchor: Sonatype.view.FIELD_OFFSET,
                    readOnly: true,
                    allowBlank:false,
                    itemCls: 'required-field',
                    disabled: true
                  }
                ]
              },
              {
                xtype: 'fieldset',
                checkboxToggle:false,
                title: 'GAV details',
                collapsible: false,
                autoHeight:true,
                items: [
                  {
                    xtype: 'checkbox',
                    fieldLabel: 'Auto Guess',
                    checked: true,
                    name: 'autoguess',
                    helpText: ht.autoguess,
                    listeners: {
                      'check': {
                        fn: function( checkbox, value ) {
                          this.updateFilename( this );
                        },
                        scope: this
                      }
                    },
                    disabled: true
                  },
                  {
                    xtype: 'textfield',
                    fieldLabel: 'Group',
                    itemCls: 'required-field',
                    helpText: ht.groupId,
                    anchor: Sonatype.view.FIELD_OFFSET,
                    name: 'g',
                    allowBlank: false,
                    disabled: true
                  },
                  {
                    xtype: 'textfield',
                    fieldLabel: 'Artifact',
                    itemCls: 'required-field',
                    helpText: ht.artifactId,
                    anchor: Sonatype.view.FIELD_OFFSET,
                    name: 'a',
                    allowBlank:false,
                    disabled: true
                  },
                  {
                    xtype: 'textfield',
                    fieldLabel: 'Version',
                    itemCls: 'required-field',
                    helpText: ht.version,
                    anchor: Sonatype.view.FIELD_OFFSET,
                    name: 'v',
                    allowBlank: false,
                    uploadPanel: this,
                    validator: function( v ){
                      var isSnapshotVersion = /-SNAPSHOT$/.test( v ) || /LATEST$/.test( v ) || /^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$/.test( v );
                      var isSnapshotRepo = this.uploadPanel.payload.data.repoPolicy == 'snapshot';
                      if ( isSnapshotRepo ) {
                        if ( ! isSnapshotVersion ) {
                          return 'You cannot upload a release version into a snapshot repository';
                        }
                      }
                      else {
                        if ( isSnapshotVersion ) {
                          return 'You cannot upload a snapshot version into a release repository';
                        }
                      }
                      return true;
                    },
                    disabled: true
                  },
                  {
                    xtype: 'combo',
                    fieldLabel: 'Packaging',
                    itemCls: 'required-field',
                    helpText: ht.packaging,
                    store: packagingStore,
                    displayField: 'value',
                    editable: true,
                    forceSelection: false,
                    mode: 'local',
                    triggerAction: 'all',
                    emptyText: 'Select...',
                    selectOnFocus: true,
                    allowBlank: false,
                    name: 'p',
                    width: 150,
                    listWidth: 150,
                    disabled: true
                  }
                ]
              }
            ]
          },
          {
            xtype: 'button',
            id: 'upload-button',
            text: 'Upload Artifact',
            handler: this.uploadArtifacts,
            scope: this
          }
        ]
      }
    ]
  });
};

Ext.extend(Sonatype.repoServer.ArtifactUploadPanel, Ext.FormPanel, {
  updateFilename: function( uploadPanel, filename ) {
    var filenameField = uploadPanel.find('name', 'filenameField')[0];
    if ( filename ) {
      filenameField.setValue( filename );
    }
    else {
      if ( ! ( filename = filenameField.getValue() ) ) {
        return;
      }
    }
    var g = '';
    var a = '';
    var v = '';
    var c = '';
    var p = '';
    var e = '';
    
    var cardPanel = uploadPanel.find( 'id', 'gav-definition-card-panel')[0];

    if ( cardPanel.find( 'name', 'autoguess' )[0].checked ) {
    
      // match extension to guess the packaging
      var extensionIndex = filename.lastIndexOf( '.' );
      if ( extensionIndex > 0 ) {
        p = filename.substring( extensionIndex + 1 );
        filename = filename.substring( 0, extensionIndex );
      }
  
      // match the path to guess the group
      if ( filename.indexOf( '\\' ) >= 0 ) {
        filename = filename.replace( /\\/g, '\/' );
      }
      var slashIndex = filename.lastIndexOf( '/' );
      if ( slashIndex ) {
        var g = filename.substring( 0, slashIndex );
  
        filename = filename.substring( slashIndex + 1 );
      }
  
      // separate the artifact name and version
      var versionIndex = filename.search( /\-[\d]/ );
      if ( versionIndex == -1 ) {
        versionIndex = filename.search( /-LATEST-/i );
        if ( versionIndex == -1 ) {
          versionIndex = filename.search( /-CURRENT-/i );
        }
      }
      if ( versionIndex >= 0 ) {
        a = filename.substring( 0, versionIndex ).toLowerCase();
  
        // guess the version
        filename = filename.substring( versionIndex + 1 );
        var classifierIndex = filename.lastIndexOf( '-' );
        if ( classifierIndex >= 0 ) {
          var classifier = filename.substring( classifierIndex + 1 );
          if ( classifier && ! ( /^SNAPSHOT$/i.test( classifier ) || /^\d/.test( classifier ) || /^LATEST$/i.test( classifier ) || /^CURRENT$/i.test( classifier ) ) ) {
            c = classifier;
            filename = filename.substring( 0, classifierIndex );
          }
        }
        v = filename;
  
        if ( g ) {
          // if group ends with version and artifact name, strip those parts
          // (useful if uploading from a local maven repo)
          var i = g.search( new RegExp( '\/' + v + '$' ) );
          if ( i > -1 ) {
            g = g.substring( 0, i );
          }
          i = g.search( new RegExp( '\/' + a + '$' ) );
          if ( i > -1 ) {
            g = g.substring( 0, i );
          }
          
          // strip extra path parts, leave only com.* or org.* or net.* or the last element
          var i = g.lastIndexOf( '/com/' );
          if ( i == -1 ) {
            i = g.lastIndexOf( '/org/' );
            if ( i == -1 ) {
              i = g.lastIndexOf( '/net/' );
              if ( i == -1 ) {
                i = g.lastIndexOf( '/' );
              }
            }
          }
          g = g.substring( i + 1 ).replace( /\//g, '.' ).toLowerCase();
        }
      }
      else {
        g = '';
      }
    }

    cardPanel.find( 'name', 'g' )[0].setRawValue( g );
    cardPanel.find( 'name', 'a' )[0].setRawValue( a );
    cardPanel.find( 'name', 'v' )[0].setRawValue( v );
    cardPanel.find( 'name', 'p' )[0].setRawValue( p );
    if ( ! a ) uploadPanel.form.clearInvalid();
  },
  updatePomFilename: function( uploadPanel, filename ) {
    var filenameField = uploadPanel.find('name', 'pomnameField')[0];
    if ( filename ) {
      filenameField.setValue( filename );
    }
    else {
      if ( ! ( filename = filenameField.getValue() ) ) {
        return;
      }
    }
  },
  gavDefinitionSelectHandler : function(combo, record, index){
    var gavDefinitionPanel = this.findById('gav-definition-card-panel');
    //First disable all the items currently on screen, so they wont be validated/submitted etc
    gavDefinitionPanel.getLayout().activeItem.items.each(function(item){
      if ( item.xtype != 'browsebutton' ){
        item.disable();
      }
    });
    //Then find the proper card to activate (based upon the selected schedule type)
    if (record.data.value == 'pom'){
      gavDefinitionPanel.getLayout().setActiveItem(gavDefinitionPanel.items.itemAt(1));
    }
    else if (record.data.value == 'gav'){
      gavDefinitionPanel.getLayout().setActiveItem(gavDefinitionPanel.items.itemAt(2));
    }
    else {
      gavDefinitionPanel.getLayout().setActiveItem(gavDefinitionPanel.items.itemAt(0));
    }
    //Then enable the fields in that card
    gavDefinitionPanel.getLayout().activeItem.items.each(function(item){
      if ( item.xtype != 'browsebutton' ){
        item.enable();
        if ( item.readOnly ){
          item.getEl().dom.readOnly = true;
        }
      }
      else {
        item.setClipSize();
      }
    });
    this.doLayout();
  },
  uploadArtifacts : function(){
    if ( this.form.isValid() ) {
      this.doUpload();
    }
  },
  doUpload: function() {
    Sonatype.MessageBox.wait( 'Uploading...' );
    this.createUploadForm();
  },
  createUploadForm: function() {
    var repoId = this.payload.id;
    repoId = repoId.substring( repoId.lastIndexOf( '/' ) + 1 );
    var pomMode = this.find( 'name', 'gavDefinition' )[0].getValue() == 'pom';
    
    var repoTag = {
      tag: 'input',
      type: 'hidden',
      name: 'r',
      value: repoId
    };

    var tmpForm = Ext.getBody().createChild({
      tag: 'form',
      cls: 'x-hidden',
      id: Ext.id(),
      children:
        pomMode ?
          [
            repoTag,
            {
              tag: 'input',
              type: 'hidden',
              name: 'hasPom',
              value: 'true'
            },
            {
                tag: 'input',
                type: 'hidden',
                name: 'c',
                value: this.form.findField( 'classifier' ).getValue()
              },
              {
                tag: 'input',
                type: 'hidden',
                name: 'e',
                value: this.form.findField( 'extension' ).getValue()
              }
          ]
        :
          [
            repoTag,
            {
              tag: 'input',
              type: 'hidden',
              name: 'g',
              value: this.form.findField( 'g' ).getValue()
            },
            {
              tag: 'input',
              type: 'hidden',
              name: 'a',
              value: this.form.findField( 'a' ).getValue()
            },
            {
              tag: 'input',
              type: 'hidden',
              name: 'v',
              value: this.form.findField( 'v' ).getValue()
            },
            {
              tag: 'input',
              type: 'hidden',
              name: 'p',
              value: this.form.findField( 'p' ).getValue()
            },
            {
              tag: 'input',
              type: 'hidden',
              name: 'c',
              value: this.form.findField( 'classifier' ).getValue()
            },
            {
              tag: 'input',
              type: 'hidden',
              name: 'e',
              value: this.form.findField( 'extension' ).getValue()
            }
          ]
    });

    if ( pomMode ) {
      this.pomInput.appendTo( tmpForm );
    }
    this.fileInput.appendTo( tmpForm );
    
    Ext.Ajax.request({
      url: Sonatype.config.repos.urls.upload,
      form : tmpForm,
      isUpload : true,
      callback: function( options, success, response ) {
        tmpForm.remove();

        //This is a hack to get around the fact that upload submit always returns
        //success = true
        if ( response.responseXML.title == '' ) {
          Sonatype.MessageBox.show({
            title: 'Upload Complete',
            msg: 'Artifact upload finished successfully',
            buttons: Sonatype.MessageBox.OK,
            icon: Sonatype.MessageBox.INFO
          });
        }
        else {
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
          Sonatype.MessageBox.show({
            title: 'Upload Failed',
            msg: s,
            buttons: Sonatype.MessageBox.OK,
            icon: Sonatype.MessageBox.ERROR
          });
        }
      },
      scope : this
    });
  }
});

Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec ) {
  var sp = Sonatype.lib.Permissions;
  
  if ( rec.data.exposed && rec.data.resourceURI && rec.data.userManaged &&
      sp.checkPermission( 'nexus:artifact', sp.CREATE ) &&
      rec.data.repoType == 'hosted' && rec.data.repoPolicy == 'release' ) {
    
    Ext.Ajax.request({
      url: rec.data.resourceURI,
      scope: this,
      callback: function( options, success, response ) {
        if ( success ) {
          var statusResp = Ext.decode( response.responseText );
          if ( statusResp.data ) {
            if ( statusResp.data.allowWrite ) {
              var uploadPanel = new Sonatype.repoServer.ArtifactUploadPanel( { payload: rec } ); 
              var card = cardPanel.add( {
                xtype: 'panel',
                layout: 'fit',
                tabTitle: 'Artifact Upload',
                items: [ uploadPanel ]
              } );
              
              card.on( 'show', function( p ) {
                // This is a hack to fix the width of the edit box in IE
                p.doLayout();
                p.find('name', 'filenameField')[0].setValue('.');
                p.find('name', 'filenameField')[0].setValue('');
                
                // another hack to make the whole browse button clickable
                if ( ! p.browseButtonsUpdated ) {
                  var b = p.find( 'xtype', 'browsebutton' );
                  for ( var i = 0; i < b.length; i++ ) {
                    b[i].setClipSize();
                  }
                  p.browseButtonsUpdated = true;
                }
                
                var gavDefComboField = p.find( 'name', 'gavDefinition' )[0];
                //another hack to fix the combo box lists
                gavDefComboField.syncSize();
              } );
            }
            else {
              cardPanel.add( {
                xtype: 'panel',
                tabTitle: 'Artifact Upload',
                items: [
                  {
                    border: false,
                    html: '<div class="little-padding">' +
                      'Artifact deployment is disabled for ' + rec.data.name + '.<br /><br />' +
                      'You can enable it in the "Access Settings" section of the ' +
                      'repository configuration.</div>'
                  }
                ]
              } );
            }
            
            cardPanel.doLayout();
            
            return;
          }
        }
        Sonatype.utils.connectionError( response,
          'There was a problem obtaining repository status.' );
      }
    } );
  }
} );