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
  
  this.filenameField = new Ext.form.TextField({
    xtype: 'textfield',
    name: 'filenameField',
    readOnly: true,
    columnWidth: .85
  });

  this.pomnameField = new Ext.form.TextField({
    xtype: 'textfield',
    name: 'pomnameField',
    readOnly: true,
    columnWidth: .8
  });

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
          xtype: 'panel',
          layout: 'column',
          hideLabel: true,
          id: this.pomCard,
          items: [
            this.pomnameField,
            {
              xtype: 'browsebutton',
              text: 'Browse...',
              columnWidth: .2,
              setSize: function() {}, // column layout requires setSize()
              uploadPanel: this,
              handler: function( b ) {
                var filename = b.detachInputFile().getValue();
                b.uploadPanel.pomnameField.setRawValue( filename );
              }
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
              fieldLabel: 'Artifact',
              itemCls: 'required-field',
//          helpText: ht.workingDirectory,
              anchor: Sonatype.view.FIELD_OFFSET,
              name: 'artifactId',
              allowBlank:false
            },
            {
              xtype: 'textfield',
              fieldLabel: 'Group',
              itemCls: 'required-field',
//          helpText: ht.workingDirectory,
              anchor: Sonatype.view.FIELD_OFFSET,
              name: 'groupId',
              allowBlank: false
            },
            {
              xtype: 'textfield',
              fieldLabel: 'Version',
              itemCls: 'required-field',
//          helpText: ht.workingDirectory,
              anchor: Sonatype.view.FIELD_OFFSET,
              name: 'version',
              allowBlank:false
            }
          ]
        }
      ]
    }
  );

  Sonatype.repoServer.FileUploadPanel.superclass.constructor.call(this, {
    trackResetOnLoad: true,
    autoScroll: true,
    border: false,
    frame: true,
    autoHeight: true,
    collapsible: false,
    collapsed: false,
    labelWidth: 120,
    layoutConfig: {
      labelSeparator: ''
    },
        
    items: [
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
                columnWidth: .2,
                setSize: function() {}, // column layout requires setSize()
                uploadPanel: this,
                handler: function( b ) {
                  var filename = b.detachInputFile().getValue();
                  b.uploadPanel.filenameField.setRawValue( filename );
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
              {
                xtype: 'radio',
                boxLabel: 'POM File',
                name: 'infoSwitch',
                value: 'pom',
                checked: true,
                listeners: {
                  'check': {
                    fn: function( checkbox, checked ) {
                      if ( checked ) {
                        this.cardPanel.layout.setActiveItem( this.pomCard );
                      }
                    },
                    scope: this
                  }
                }
              },
              {
                xtype: 'radio',
                boxLabel: 'Attributes',
                name: 'infoSwitch',
                value: 'attributes',
                listeners: {
                  'check': {
                    fn: function( checkbox, checked ) {
                      if ( checked ) {
                        this.cardPanel.layout.setActiveItem( this.attributeCard );
                      }
                    },
                    scope: this
                  }
                }
              }
            ]
          },
          this.cardPanel
        ]
      }
    ],

    buttons: [
      {
        text: 'Upload',
//        handler: this.saveBtnHandler,
        disabled: true,
        scope: this
      },
      {
        text: 'Cancel',
//        handler: this.cancelBtnHandler,
        scope: this
      }
    ]
  });
};

Ext.extend(Sonatype.repoServer.FileUploadPanel, Ext.FormPanel, {
});
