/*
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
Sonatype.repoServer.FileUploadPanel = function(config){
  var config = config || {};
  var defaultConfig = {
    autoScroll:true
  };
  Ext.apply(this, config, defaultConfig);

  var ht = Sonatype.repoServer.resources.help.artifact;

  var packagingStore = new Ext.data.SimpleStore( {
    fields: ['value'], 
    data: [['pom'], ['jar'], ['ejb'], ['war'], ['ear'], ['rar'], ['par'], ['maven-archetype'], ['maven-plugin']]
  } );

  this.pomMode = true;

  this.filenameField = new Ext.form.TextField({
    xtype: 'textfield',
    name: 'filenameField',
    readOnly: true,
    columnWidth: .9
  });
  this.fileInput = null;

  this.pomnameField = new Ext.form.TextField({
    xtype: 'textfield',
    name: 'pomnameField',
    readOnly: true,
    columnWidth: .8
  });
  this.pomInput = null;

  this.pomCard = 'pomCard';
  this.attributeCard = 'attributeCard';
  this.cardPanel = new Ext.Panel( {
    xtype: 'panel',
    columnWidth: .8,
    layout: 'card',
    activeItem: this.pomCard, 
    items: [
      {
        xtype: 'fieldset',
        id: this.pomCard,
        labelWidth: 80,
        checkboxToggle:false,
        collapsed: false,
        collapsible: false,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        items: [
          {
            xtype: 'panel',
            layout: 'column',
            style: 'padding-bottom:4px',
            hideLabel: true,
            items: [
              {
                xtype: 'label',
                text: 'POM',
                width: 84
              },
              this.pomnameField,
              {
                xtype: 'browsebutton',
                text: 'Browse...',
                columnWidth: .2,
                uploadPanel: this,
                handler: function( b ) {
                  b.uploadPanel.pomInput = b.detachInputFile(); 
                  var filename = b.uploadPanel.pomInput.getValue();
                  b.uploadPanel.pomnameField.setRawValue( filename );
                  b.uploadPanel.updateUploadButton( b.uploadPanel );
                }
              }
            ]
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Classifier',
            helpText: ht.classifier,
            anchor: Sonatype.view.FIELD_OFFSET,
            name: 'pomc',
            allowBlank:true
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Extension',
            helpText: ht.extension,
            anchor: Sonatype.view.FIELD_OFFSET,
            name: 'pome',
            allowBlank:true
          }
        ] 
      },
      {
        xtype: 'fieldset',
        labelWidth: 80,
        checkboxToggle:false,
        collapsed: false,
        collapsible: false,
        autoHeight:true,
        id: this.attributeCard,
        layoutConfig: {
          labelSeparator: ''
        },
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
            }
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Group',
            itemCls: 'required-field',
            helpText: ht.groupId,
            anchor: Sonatype.view.FIELD_OFFSET,
            name: 'g',
            allowBlank: false
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Artifact',
            itemCls: 'required-field',
            helpText: ht.artifactId,
            anchor: Sonatype.view.FIELD_OFFSET,
            name: 'a',
            allowBlank:false
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
            }
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
            listWidth: 150
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Classifier',
            helpText: ht.classifier,
            anchor: Sonatype.view.FIELD_OFFSET,
            name: 'c',
            allowBlank:true
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Extension',
            helpText: ht.extension,
            anchor: Sonatype.view.FIELD_OFFSET,
            name: 'e',
            allowBlank:true
          }
        ]
      }
    ]
  } );

  Sonatype.repoServer.FileUploadPanel.superclass.constructor.call(this, {
    autoScroll: true,
    border: true,
    bodyBorder: true,
    frame: true,
    collapsible: false,
    collapsed: false,
    fileUpload: true,
    width: '100%',
    height: '100%',
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
        title: 'Select a File For Upload',
        collapsible: false,
        autoHeight:true,
        items: [
          {
            xtype: 'panel',
            layout: 'column',
            hideLabel: true,
            items: [
              this.filenameField,
              {
                xtype: 'browsebutton',
                text: 'Browse...',
                columnWidth: .1,
                uploadPanel: this,
                handler: function( b ) {
                  b.uploadPanel.fileInput = b.detachInputFile(); 
                  var filename = b.uploadPanel.fileInput.getValue();
                  b.uploadPanel.updateFilename( b.uploadPanel, filename );
                }
              }
            ] 
          }
        ]
      },
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'Specify Artifact Information',
        collapsible: false,
        autoHeight:true,
        layout: 'column',
        items: [
          {
            xtype: 'panel',
            columnWidth: .15,
            items: [
              {xtype: 'panel',
               items: [
              {
                xtype: 'radio',
                boxLabel: 'POM File',
                name: 'hasPom',
                value: 'true',
                checked: true,
                listeners: {
                  'check': {
                    fn: function( checkbox, checked ) {
                      if ( checked ) {
                        this.cardPanel.layout.setActiveItem( this.pomCard );
                        this.pomMode = true;
                      }
                      this.updateUploadButton( this );
                    },
                    scope: this
                  }
                }
              },
              {
                xtype: 'radio',
                boxLabel: 'Attributes',
                name: 'hasPom',
                value: 'false',
                listeners: {
                  'check': {
                    fn: function( checkbox, checked ) {
                      if ( checked ) {
                        this.cardPanel.layout.setActiveItem( this.attributeCard );
//                        this.cardPanel.findById( this.attributeCard ).find( 'name', 'p' )[0].setWidth( 150 );
                        this.pomMode = false;
                      }
                      this.updateUploadButton( this );
                    },
                    scope: this
                  }
                }
              }
              ]}
            ]
          },
          this.cardPanel
        ]
      }
    ],

    buttons: [
      {
        text: 'Upload',
        id: 'upload-button',
        handler: function() {
          if ( this.pomMode || this.form.isValid() ) {
            this.doUpload();
          }
        },
        disabled: true,
        scope: this
      },
      {
        text: 'Reset',
        handler: function() {
          this.form.reset();
          this.pomMode = true;
        },
        scope: this
      }
    ]
  });

  if ( Ext.isIE ) this.filenameField.on(
    'render',
    function( c ) {
      this.filenameField.ownerCt.doLayout();
    },
    this
  );
};

Ext.extend(Sonatype.repoServer.FileUploadPanel, Ext.FormPanel, {

  updateUploadButton: function( p ) {
    var filesSelected = p.filenameField.getValue().length > 0 &&
      ( ! p.pomMode || p.pomnameField.getValue().length > 0 );
    p.buttons[0].setDisabled( ! filesSelected );
  },
  
  doUpload: function() {
    Sonatype.MessageBox.wait( 'Uploading...' );
    this.createUploadForm();
  },

  createUploadForm: function() {
    var repoId = this.payload.id;
    repoId = repoId.substring( repoId.lastIndexOf( '/' ) + 1 );

    var repoTag = {
      tag: 'input',
      type: 'hidden',
      name: 'r',
      value: repoId
    }

    var tmpForm = Ext.getBody().createChild({
      tag: 'form',
      cls: 'x-hidden',
      id: Ext.id(),
      children:
        this.pomMode ?
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
                value: this.form.findField( 'pomc' ).getValue()
              },
              {
                tag: 'input',
                type: 'hidden',
                name: 'e',
                value: this.form.findField( 'pome' ).getValue()
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
              value: this.form.findField( 'c' ).getValue()
            },
            {
              tag: 'input',
              type: 'hidden',
              name: 'e',
              value: this.form.findField( 'e' ).getValue()
            }
          ]
    });

    if ( this.pomMode ) {
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
  },
  
  updateFilename: function( uploadPanel, filename ) {
    if ( filename ) {
      uploadPanel.filenameField.setValue( filename );
    }
    else {
      if ( ! ( filename = uploadPanel.filenameField.getValue() ) ) {
        return;
      }
    }

    var g = '';
    var a = '';
    var v = '';
    var c = '';
    var p = '';
    var e = '';

    if ( uploadPanel.cardPanel.find( 'name', 'autoguess' )[0].checked ) {
    
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

    uploadPanel.cardPanel.find( 'name', 'g' )[0].setRawValue( g );
    uploadPanel.cardPanel.find( 'name', 'a' )[0].setRawValue( a );
    uploadPanel.cardPanel.find( 'name', 'v' )[0].setRawValue( v );
    uploadPanel.cardPanel.find( 'name', 'c' )[0].setRawValue( c );
    uploadPanel.cardPanel.find( 'name', 'p' )[0].setRawValue( p );
    uploadPanel.cardPanel.find( 'name', 'e' )[0].setRawValue( e );
    if ( ! a ) uploadPanel.form.clearInvalid();
    uploadPanel.updateUploadButton( uploadPanel );
  }
});

Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec ) {
  var sp = Sonatype.lib.Permissions;
  
  if ( rec.data.resourceURI && 
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
              var uploadPanel = cardPanel.add( {
                xtype: 'panel',
                layout: 'fit',
                tabTitle: 'Upload',
                items: [ new Sonatype.repoServer.FileUploadPanel( { payload: rec } ) ]
              } );
              
              uploadPanel.on( 'show', function( p ) {
                p.doLayout();
                if ( ! p.browseButtonsUpdated ) {
                  var b = p.find( 'xtype', 'browsebutton' );
                  for ( var i = 0; i < b.length; i++ ) {
                    b[i].setClipSize();
                  }
                  p.browseButtonsUpdated = true;
                }
              } );
            }
            else {
              cardPanel.add( {
                xtype: 'panel',
                tabTitle: 'Upload',
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
