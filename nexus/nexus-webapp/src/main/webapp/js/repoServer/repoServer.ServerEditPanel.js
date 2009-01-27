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
//Instance of Ext.FormPanel
Sonatype.repoServer.ServerEditPanel = function(config){

  var config = config || {};
  var defaultConfig = {autoScroll:true};
  Ext.apply(this, config, defaultConfig);
  
  var tfStore = new Ext.data.SimpleStore({fields:['value'], data:[['True'],['False']]});
  var securityConfigStore = new Ext.data.SimpleStore({fields:['value','display'], data:[[false,'Off'],[true,'On']]});
  
  //Simply a record to hold details of each service type 
  this.realmTypeRecordConstructor = Ext.data.Record.create([
    {name:'roleHint', sortType:Ext.data.SortTypes.asUCString},
    {name:'description'}
  ]);
  
  this.realmTypeReader = new Ext.data.JsonReader({root: 'data', id: 'roleHint'}, this.realmTypeRecordConstructor );
  
  this.realmTypeDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.realmComponents,
    reader: this.realmTypeReader,
    listeners: {
      load: {
        fn: this.loadServerConfig,
        scope: this
      }
    }
  });
  
  // help text alias
  var ht = Sonatype.repoServer.resources.help.server;

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
        title: 'SMTP Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },

        items: [
          {
            xtype: 'textfield',
            fieldLabel: 'Hostname',
            itemCls: 'required-field',
            helpText: ht.smtphost,
            name: 'smtpSettings.host',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:false,
            itemCls: 'required-field'
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Port',
            helpText: ht.smtpport,
            name: 'smtpSettings.port',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:false,
            itemCls: 'required-field'
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Username',
            helpText: ht.smtpuser,
            name: 'smtpSettings.username',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:true
          },
          {
            xtype: 'textfield',
            inputType:'password',
            fieldLabel: 'Password',
            helpText: ht.smtppass,
            name: 'smtpSettings.password',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:true
          },
          {
            xtype: 'checkbox',
            fieldLabel: 'SSL Enabled',
            helpText: ht.smtpssl,
            name: 'smtpSettings.sslEnabled'
          },
          {
            xtype: 'checkbox',
            fieldLabel: 'TLS Enabled',
            helpText: ht.smtptls,
            name: 'smtpSettings.tlsEnabled'
          },
          {
            xtype: 'textfield',
            fieldLabel: 'System Email',
            helpText: ht.smtpsysemail,
            name: 'smtpSettings.systemEmailAddress',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:false,
            itemCls: 'required-field'
          }
        ]
      },
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'HTTP Request Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },

        items: [
          {
            xtype: 'textfield',
            fieldLabel: 'User Agent Customization',
            helpText: ht.userAgentString,
            name: 'globalConnectionSettings.userAgentString',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank: true
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Additional URL Parameters',
            helpText: ht.queryString,
            name: 'globalConnectionSettings.queryString',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:true
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Request Timeout',
            helpText: ht.connectionTimeout,
            afterText: 'seconds',
            name: 'globalConnectionSettings.connectionTimeout',
            width: 50,
            allowBlank: false,
            itemCls: 'required-field',
            allowDecimals: false,
            allowNegative: false,
            maxValue: 36000
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Request Retry Attempts',
            helpText: ht.retrievalRetryCount,
            name: 'globalConnectionSettings.retrievalRetryCount',
            width: 50,
            allowBlank: false,
            itemCls: 'required-field',
            allowDecimals: false,
            allowNegative: false,
            maxValue: 10
          }
        ]
      }, //end http conn
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        id: formId + '_' + 'securitySettings',
        title: 'Security Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        items: [
          {
            xtype: 'combo',
            fieldLabel: 'Security',
            itemCls: 'required-field',
            helpText: ht.security,
            name: 'securityEnabled',
            width: 150,
            store: securityConfigStore,
            valueField:'value',
            displayField:'display',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText:'Select...',
            selectOnFocus:true,
            allowBlank: false
          },
          {
            xtype: 'twinpanelchooser',
            titleLeft: 'Selected Realms',
            titleRight: 'Available Realms',
            name: 'securityRealms',
            valueField: 'roleHint',
            displayField: 'roleHint',
            store: this.realmTypeDataStore,
            required: true,
            halfSize: true
          },
          {
            xtype: 'fieldset',
            checkboxToggle:true,
            collapsed: true,
            id: formId + '_' + 'anonymousAccessSettings',
            title: 'Anonymous Access',
            anchor: Sonatype.view.FIELDSET_OFFSET,
            autoHeight:true,
            layoutConfig: {
              labelSeparator: ''
            },
            listeners: {
              'expand' : {
                fn: function(panel) {
                  panel.find('name', 'securityAnonymousAccessEnabled')[0].setValue('true');
                  this.optionalFieldsetExpandHandler(panel);
                },
                scope: this
              },
              'collapse' : {
                fn: function(panel) {
                  panel.find('name', 'securityAnonymousAccessEnabled')[0].setValue('false');
                  panel.find('name', 'securityAnonymousPassword')[0].setValue('');
                  this.optionalFieldsetCollapseHandler(panel);
                },
                scope: this,
                delay: 100
              }
            },

            items: [
              {
                xtype: 'hidden',
                name: 'securityAnonymousAccessEnabled'
              },
              {
                xtype: 'panel',
                layout: 'fit',
                html: '<div style="padding-bottom:10px">' + ht.anonymousAccess + '</div>'
              },
              { 
                xtype: 'textfield',
                fieldLabel:'Anonymous Username', 
                name:'securityAnonymousUsername',
                itemCls: 'required-field',
                helpText: ht.anonUsername,
                width: 100,
                allowBlank:true,
                anchor: Sonatype.view.FIELD_OFFSET
              },
              { 
                xtype: 'textfield',
                fieldLabel:'Anonymous Password', 
                name:'securityAnonymousPassword', 
                itemCls: 'required-field',
                inputType:'password',
                helpText: ht.anonPassword,
                width: 100,
                allowBlank:true,
                minLength: 4,
                minLengthText : "Password must be 4 characters or more",
                maxLength: 25,
                maxLengthText : "Password must be 25 characters or less",
                anchor: Sonatype.view.FIELD_OFFSET 
              }
            ]
          }
        ]
      },
      {
        xtype: 'fieldset',
        checkboxToggle:true,
        collapsed: true,
        id: formId + '_' + 'applicationServerSettings',
        title: 'Application Server Settings (optional)',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        listeners: {
          'expand' : {
            fn: this.optionalFieldsetExpandHandler,
            scope: this
          },
          'collapse' : {
            fn: this.optionalFieldsetCollapseHandler,
            scope: this,
            delay: 100
          }
        },

        items: [
          {
            xtype: 'textfield',
            itemCls: 'required-field',
            fieldLabel: 'Base URL',
            helpText: ht.baseUrl,
            name: 'baseUrl',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank: true
          },
          {
            xtype: 'checkbox',
            fieldLabel: 'Force Base URL',
            helpText: ht.forceBaseUrl,
            name: 'forceBaseUrl',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank: true
          }
        ]
      },
      {
        xtype: 'fieldset',
        checkboxToggle:true,
        collapsed: true,
        id: formId + '_' + 'globalHttpProxySettings',
        title: 'Default HTTP Proxy Settings (optional)',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        listeners: {
          'expand' : {
            fn: this.optionalFieldsetExpandHandler,
            scope: this
          },
          'collapse' : {
            fn: this.optionalFieldsetCollapseHandler,
            scope: this,
            delay: 100
          }
        },

        items: [
          {
            xtype: 'textfield',
            fieldLabel: 'Proxy Host',
            helpText: ht.proxyHostname,
            name: 'globalHttpProxySettings.proxyHostname',
            anchor: Sonatype.view.FIELD_OFFSET,
            itemCls: 'required-field',
            allowBlank:true,
            validator: function(v){
              if (v.search(/:\//) == -1) { return true; }
              else {return 'Specify hostname without the protocol, example "my.host.com"';}
            }
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Proxy Port',
            helpText: ht.proxyPort,
            name: 'globalHttpProxySettings.proxyPort',
            width: 50,
            itemCls: 'required-field',
            allowBlank: true,
            allowDecimals: false,
            allowNegative: false,
            maxValue: 65535
          },
          {
            xtype: 'fieldset',
            checkboxToggle:true,
            collapsed: true,
            id: formId + '_' + 'globalHttpProxySettings.authentication',
            title: 'Authentication (optional)',
            //collapsible: false,
            autoHeight:true,
            layoutConfig: {
              labelSeparator: ''
            },

            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'Username',
                helpText: ht.username,
                name: 'globalHttpProxySettings.authentication.username',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Password',
                helpText: ht.password,
                inputType:'password',
                name: 'globalHttpProxySettings.authentication.password',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Private Key',
                helpText: ht.privateKey,
                name: 'globalHttpProxySettings.authentication.privateKey',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Key Passphrase',
                helpText: ht.passphrase,
                inputType:'password',
                name: 'globalHttpProxySettings.authentication.passphrase',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Host',
                helpText: ht.ntlmHost,
                name: 'globalHttpProxySettings.authentication.ntlmHost',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Manager Domain',
                helpText: ht.ntlmDomain,
                name: 'globalHttpProxySettings.authentication.ntlmDomain',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              }
            ]
          } //end auth fieldset
        ]
      } // end proxy settings
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
  
  this.formPanel.buttons[0].scope = this.formPanel; 
  this.formPanel.save = this.save;

  Sonatype.repoServer.ServerEditPanel.superclass.constructor.call(this, {
    autoScroll: false,
    layout: 'border',
/*
    tbar: [
      {
        text: 'Restart Nexus',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.reloadAll
        disabled: true
      }
    ],
*/
    items: [
      this.formPanel
    ]
  });
  
//  this.form = this.formPanel.form;
//  this.buttons = this.formPanel.buttons;
  
  this.formPanel.on('beforerender', this.beforeRenderHandler, this.formPanel);
  this.formPanel.on('afterlayout', this.afterLayoutHandler, this, {single:true});
  this.formPanel.form.on('actioncomplete', this.actionCompleteHandler, this.formPanel);
  this.formPanel.form.on('actionfailed', this.actionFailedHandler, this.formPanel);
  
  var securityConfigField = this.formPanel.find('name', 'securityEnabled')[0];
  securityConfigField.on('select', this.securitySelectHandler, securityConfigField);
  
//  var anonymousField = this.formPanel.find('name', 'securityAnonymousAccessEnabled')[0];
//  anonymousField.on('check', this.anonymousCheckHandler, anonymousField);
};

Ext.extend(Sonatype.repoServer.ServerEditPanel, Ext.Panel, {

  optionalFieldsetExpandHandler : function(panel){
    panel.items.each(function(item, i, len){
      if (item.isXType('fieldset', true)){
        this.optionalFieldsetExpandHandler(item);
      }
      else if (item.getEl() != null && item.getEl().up('div.required-field', 3)){
        item.allowBlank = false;
      }
      else {
        item.allowBlank = true;
      }
    }, this); // "this" is RepoEditPanel
  },
  
  optionalFieldsetCollapseHandler : function(panel){
    panel.items.each(function(item, i, len){
      if (item.isXType('fieldset', true)){
        this.optionalFieldsetCollapseHandler(item);
      }
      else {
        item.allowBlank = true;
      }
    }, this); // "this" is RepoEditPanel
  },
  
  saveBtnHandler : function() {
    var allValid = this.form.isValid() &&
      this.find( 'name', 'securityRealms' )[0].validate();
    
    if (allValid) {
      this.save();
    }
  },
  
  //takes an optional config object
  // only defined value now is {restartRequired:bool}
  save : function(config) {
    var form = this.form;

    var appSettingsPanel = this.findById( this.id + '_applicationServerSettings' );
    if ( appSettingsPanel.collapsed ) {
      var baseUrlField = this.find( 'name', 'baseUrl' )[0];
      baseUrlField.setValue( '' );
    }

    form.doAction('sonatypeSubmit', {
      method: 'PUT',
      url: Sonatype.config.repos.urls.globalSettingsState,
      waitMsg: 'Updating server configuration...',
      fpanel:this,
      restartRequired: (config) ? config.restartRequired : false, 
      dataModifiers: {
        "routing.followLinks" : Sonatype.utils.convert.stringContextToBool,
        "routing.groups.stopItemSearchOnFirstFoundFile" : Sonatype.utils.convert.stringContextToBool,
        "routing.groups.mergeMetadata" : Sonatype.utils.convert.stringContextToBool,
        "securityRealms" : function(val, fpanel){
          return fpanel.find( 'name', 'securityRealms' )[0].getValue();
        },
        "baseUrl" : Sonatype.utils.returnValidStr
      },
      serviceDataObj : Sonatype.repoServer.referenceData.globalSettingsState
    });
  },
  
  cancelBtnHandler : function() {
    Sonatype.view.mainTabPanel.remove(this.id, true);
  },
  
  beforeRenderHandler : function(){
    var sp = Sonatype.lib.Permissions;
    if(sp.checkPermission('nexus:settings', sp.EDIT)){
      this.buttons[0].disabled = false;
    }
  },
  
  afterLayoutHandler : function(){

    this.realmTypeDataStore.load();

    var fpanel = this.formPanel;
    
    // register required field quicktip, but have to wait for elements to show up in DOM
    var temp = function(){
      var els = Ext.select('.required-field .x-form-item-label', fpanel.getEl());
      els.each(function(el, els, i){
        Ext.QuickTips.register({
          target: el,
          cls: 'required-field',
          title: '',
          text: 'Required Field',
          enabled: true
        });
      });
    }.defer(300, fpanel);
    
  },
  
  loadServerConfig: function() {
    
    var fpanel = this.formPanel;
    var appSettingsPanel = fpanel.findById( fpanel.id + '_applicationServerSettings' );

    this.formPanel.getForm().doAction('sonatypeLoad', {
      url:Sonatype.config.repos.urls.globalSettingsState,
      method:'GET',
      fpanel:fpanel,
      dataModifiers: {
        "routing.followLinks" : Sonatype.utils.capitalize,
        "routing.groups.stopItemSearchOnFirstFoundFile" : Sonatype.utils.capitalize,
        "routing.groups.mergeMetadata" : Sonatype.utils.capitalize,
        "securityRealms" : function(arr, srcObj, fpanel){
          fpanel.find( 'name', 'securityRealms' )[0].setValue( arr );
          return arr; //return arr, even if empty to comply with sonatypeLoad data modifier requirement
        },
        "baseUrl" : function(str) {
            if (!Ext.isEmpty(str)){
              appSettingsPanel.expand();
            }
            return str;
          }
      }
    });
  },
  
  //(Ext.form.BasicForm, Ext.form.Action)
  actionCompleteHandler : function(form, action) {
    if (action.type == 'sonatypeSubmit'){
      if (action.options.restartRequired) {
        Sonatype.MessageBox.show({
          title : 'Restart Required',
          msg : 'Nexus must now be restarted for the change to take effect',
          buttons: false,
          closable: false,
          icon: Sonatype.MessageBox.WARNING
        });
      }
    }
    
    if (action.type == 'sonatypeLoad'){
      //@note: this is a work around to get proper use of the isDirty() function of this field
      //@todo: could/should extend sonatypeLoad to set the originalValue on all fields to the value it loads
      //        default behavior sets the original value to whatever is specified in the config.
      if (action.options.fpanel.find('name', 'securityAnonymousAccessEnabled')[0].getValue() == "true") {
        action.options.fpanel.find('id', (action.options.fpanel.id + '_' + 'anonymousAccessSettings'))[0].expand();
      }
      if (!Ext.isEmpty(action.options.fpanel.find('name', 'baseUrl')[0].getValue())) {
        action.options.fpanel.find('id', (action.options.fpanel.id + '_' + 'applicationServerSettings'))[0].expand();
      }
    }
    
    //@todo: some completion message would be helpful.
  },
  
  //(Ext.form.BasicForm, Ext.form.Action)
  actionFailedHandler : function(form, action){
    if(action.failureType == Ext.form.Action.CLIENT_INVALID){
      Sonatype.MessageBox.alert('Missing or Invalid Fields', 'Please change the missing or invalid fields.').setIcon(Sonatype.MessageBox.WARNING);
    }
    //@note: server validation error are now handled just like client validation errors by marking the field invalid
//  else if(action.failureType == Ext.form.Action.SERVER_INVALID){
//    Sonatype.MessageBox.alert('Invalid Fields', 'The server identified invalid fields.').setIcon(Sonatype.MessageBox.ERROR);
//  }
    else if(action.failureType == Ext.form.Action.CONNECT_FAILURE){
      Sonatype.utils.connectionError( action.response, 'There is an error communicating with the server.' )
    }
    else if(action.failureType == Ext.form.Action.LOAD_FAILURE){
      Sonatype.MessageBox.alert('Load Failure', 'The data failed to load from the server.').setIcon(Sonatype.MessageBox.ERROR);
    }
    
    //@todo: need global alert mechanism for fatal errors.
  },
  
  securitySelectHandler : function(combo, record, index){
  },
  
  anonymousCheckHandler : function(checkbox, checked){
    this.ownerCt.find('name', 'securityAnonymousUsername')[0].setDisabled(!checked);
    this.ownerCt.find('name', 'securityAnonymousPassword')[0].setDisabled(!checked);
  }
  
});
