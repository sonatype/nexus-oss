/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global NX, Nexus, Sonatype, Ext*/

NX.define('Sonatype.repoServer.LogEditPanel', {
  extend : 'Ext.Panel',
  requirejs : ['Sonatype/all'],
  constructor : function(cfg) {
    var
          config = cfg || {},
          defaultConfig = {},
          formId = Ext.id(),
          rootLoggerLevelStore = new Ext.data.SimpleStore({
            fields : ['value', 'display'],
            data : [
              ['DEBUG', 'DEBUG'],
              ['INFO', 'INFO'],
              ['ERROR', 'ERROR']
            ]
          }),
    // help text alias
          ht = Sonatype.repoServer.resources.help.log;

    Ext.apply(this, config, defaultConfig);

    this.formPanel = new Ext.FormPanel({
      region : 'center',
      id : formId,
      trackResetOnLoad : true,
      autoScroll : true,
      border : false,
      frame : true,
      collapsible : false,
      collapsed : false,
      labelWidth : 175,
      layoutConfig : {
        labelSeparator : ''
      },

      items : [
        {
          xtype : 'combo',
          fieldLabel : 'Root Logger Level',
          itemCls : 'required-field',
          allowBlank : false,
          width : 100,
          helpText : ht.rootLoggerLevel,
          name : 'rootLoggerLevel',
          store : rootLoggerLevelStore,
          valueField : 'value',
          displayField : 'display',
          mode : 'local',
          triggerAction : 'all',
          forceSelection : true,
          selectOnFocus : true
        }
      ],

      buttons : [
        {
          text : 'Save',
          handler : this.saveBtnHandler,
          disabled : true,
          scope : this
        },
        {
          text : 'Cancel',
          handler : this.cancelBtnHandler,
          scope : this
        }
      ]

    });

    Sonatype.repoServer.LogEditPanel.superclass.constructor.call(this, {
      autoScroll : false,
      layout : 'border',
      items : [this.formPanel]
    });

    this.formPanel.on('beforerender', this.beforeRenderHandler, this.formPanel);
    this.formPanel.on('afterlayout', this.afterLayoutHandler, this, {
      single : true
    });
  },

  loadLogConfig : function() {
    var fpanel = this.formPanel;
    this.formPanel.getForm().doAction('sonatypeLoad', {
      url : Sonatype.config.repos.urls.logConfig,
      method : 'GET',
      fpanel : fpanel
    });
  },

  beforeRenderHandler : function() {
    var sp = Sonatype.lib.Permissions;
    if (sp.checkPermission('nexus:logconfig', sp.EDIT)) {
      this.buttons[0].disabled = false;
    }
  },

  afterLayoutHandler : function() {
    this.loadLogConfig();
  },

  cancelBtnHandler : function() {
    Sonatype.view.mainTabPanel.remove(this.id, true);
  },

  saveBtnHandler : function() {
    var allValid = this.formPanel.getForm().isValid();
    if (allValid) {
      this.save();
    }
  },

  save : function() {
    var form = this.formPanel.getForm();
    form.doAction('sonatypeSubmit', {
      url : Sonatype.config.repos.urls.logConfig,
      method : 'PUT',
      waitMsg : 'Updating log configuration...',
      fpanel : this.formPanel,
      serviceDataObj : Sonatype.repoServer.referenceData.logConfig,
      success : function(form, action) {
        form.findField('rootLoggerLevel').setValue(action.result.data.rootLoggerLevel);
      }
    });
  }

});

