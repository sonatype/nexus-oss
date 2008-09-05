/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
Sonatype.repoServer.FileUploadPanel = function(config){
  var config = config || {};
  var defaultConfig = {autoScroll:true};
  Ext.apply(this, config, defaultConfig);

  var ht = Sonatype.repoServer.resources.help.artifact;

  var packagingStore = new Ext.data.SimpleStore({fields:['value'], data:[['pom'], ['jar'], ['ejb'], ['war'], ['ear'], ['rar'], ['par'], ['maven-archetype'], ['maven-plugin']]});

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
  this.cardPanel = new Ext.Panel(
    {
      xtype: 'panel',
      columnWidth: .8,
      layout: 'card',
      activeItem: this.pomCard, 
      items: [
           {
               xtype: 'fieldset',
               id: this.pomCard,
               labelWidth: 70,
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
                            width: Ext.isIE ? 79 : 75
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
          labelWidth: 70,
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
                var isSnapshotRepo = this.uploadPanel.repoRecord.get( 'repoPolicy' ) == 'snapshot';
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
              forceSelection: true,
              mode: 'local',
              triggerAction: 'all',
              emptyText: 'Select...',
              selectOnFocus: true,
              allowBlank: false,
              name: 'p'
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
    }
  );

  Sonatype.repoServer.FileUploadPanel.superclass.constructor.call(this, {
    autoScroll: true,
    border: true,
    bodyBorder: true,
    frame: true,
    collapsible: false,
    collapsed: false,
    fileUpload: true,
    layoutConfig: {
      labelSeparator: ''
    },
        
    items: [
      {
        xtype: 'hidden',
        name: 'r',
        value: this.repoRecord.id
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
                  b.uploadPanel.filenameField.setValue( filename );
                  var extensionIndex = filename.lastIndexOf( '.' );
                  if ( extensionIndex > 0 ) {
                    b.uploadPanel.cardPanel.find( 'name', 'p' )[0].setValue( filename.substring( extensionIndex + 1 ) );
                  }
                  b.uploadPanel.updateUploadButton( b.uploadPanel );
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
        text: 'Cancel',
        handler: function() {
          if ( this.repoPanel ) {
            this.repoPanel.viewRepo( this.repoRecord );
          }
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
    var repoId = this.repoRecord.id;
    repoId = repoId.substring( repoId.lastIndexOf( '/' ) + 1 );

//    var authTokenTag = {
//      tag: 'input',
//      type: 'hidden',
//      name: 'NexusAuthToken',
//      value: Sonatype.user.curr.authToken
//    };
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
//            authTokenTag,
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
//            authTokenTag,
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
    
  }
});
