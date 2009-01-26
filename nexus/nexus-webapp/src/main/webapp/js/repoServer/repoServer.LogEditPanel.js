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
Sonatype.repoServer.LogEditPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  // help text alias
  var ht = Sonatype.repoServer.resources.help.log;
  var formId = Ext.id();

  this.formPanel = new Ext.FormPanel({  
    region: 'center',
    id: formId,
    trackResetOnLoad: true,
    autoScroll: true,
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
    labelWidth: 175,
    layoutConfig: {
      labelSeparator: ''
    },
        
    items: [
      {
      xtype: 'fieldset',
      checkboxToggle:false,
      collapsible: true,
      collapsed: false,
      id: formId + '_' + 'logConfig',
      title: 'Log4j Configuration',
      anchor: Sonatype.view.FIELDSET_OFFSET,
      autoHeight:true,
      layoutConfig: {
        labelSeparator: ''
      },
      items: [
        {
          xtype: 'textfield',
          fieldLabel: 'Root Logger',
          itemCls: 'required-field',
          allowBlank: false,
          helpText: ht.rootLogger,
          name: 'rootLogger',
          anchor: Sonatype.view.FIELD_OFFSET
        },
        {
          xtype: 'textfield',
          fieldLabel: 'File Appender Location',
          itemCls: 'required-field',
          allowBlank: false,
          name: 'fileAppenderLocation',
          helpText: ht.fileAppenderLocation,
          anchor: Sonatype.view.FIELD_OFFSET
        },
        {
          xtype: 'textfield',
          fieldLabel: 'File Appender Pattern',
          itemCls: 'required-field',
          allowBlank: false,
          helpText: ht.fileAppenderPattern,
          name: 'fileAppenderPattern',
          anchor: Sonatype.view.FIELD_OFFSET
        }
      ]
      },

      {
        xtype: 'fieldset',
        checkboxToggle:true,
        collapsed: true,
        id: formId + '_' + 'logCustomize',
        title: 'Log4j Customization (not yet implemented)',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        listeners: {
          'expand' : {
            fn: this.logCustomizeExpandHandler,
            scope: this
          },
          'collapse' : {
            fn: this.logCustomizeCollapseHandler,
            scope: this,
            delay: 100
          }
        }
      }
    ],

    buttons: [
      {
        id: 'savebutton',
        text: 'Save',
        handler: this.saveBtnHandler,
        disabled: true,
        scope: this
      },
      {
        id: 'cancelbutton',
        text: 'Cancel',
        handler: this.cancelBtnHandler,
        scope: this
      }
    ]

  });

  Sonatype.repoServer.LogEditPanel.superclass.constructor.call(this, {
    autoScroll: false,
    layout: 'border',
    items: [
      this.formPanel
    ]
  });

  this.formPanel.on('beforerender', this.beforeRenderHandler, this.formPanel);
  this.formPanel.on('afterlayout', this.afterLayoutHandler, this, {single:true});
};


Ext.extend(Sonatype.repoServer.LogEditPanel, Ext.Panel, {
  
  loadLogConfig: function(){
    var fpanel = this.formPanel;
    this.formPanel.getForm().doAction('sonatypeLoad',{
      url: Sonatype.config.repos.urls.logConfig,
      method: 'GET',
      fpanel: fpanel
    }    
    );
  },

  beforeRenderHandler : function(){
    var sp = Sonatype.lib.Permissions;
    if(sp.checkPermission('nexus:logconfig', sp.EDIT)){
      this.buttons[0].disabled = false;
    }
  },

  afterLayoutHandler: function(){
    this.loadLogConfig();
  },

  cancelBtnHandler: function(){
    Sonatype.view.mainTabPanel.remove(this.id, true);
  },

  saveBtnHandler: function(){
    var allValid = this.formPanel.getForm().isValid();
    if (allValid){
      this.save();
    }
  },

  save: function(){
    var form = this.formPanel.getForm();
    form.doAction( 'sonatypeSubmit', {
      url: Sonatype.config.repos.urls.logConfig,
      method: 'PUT',
      waitMsg: 'Updating log configuration...',
      fpanel: this.formPanel,
      serviceDataObj : Sonatype.repoServer.referenceData.logConfig
      }
    );
  },
  
  logCustomizeExpandHandler: function(){
    var logConfigPanel = this.formPanel.findById(this.formPanel.getId() + '_logConfig' );
    logConfigPanel.collapse();
    logConfigPanel.disable();

  },

  logCustomizeCollapseHandler: function(){
    var logConfigPanel = this.formPanel.findById(this.formPanel.getId() + '_logConfig' );
    logConfigPanel.expand();
    logConfigPanel.enable();
  }

});