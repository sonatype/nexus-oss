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
/*
 * Repository Edit/Create panel layout and controller
 */

var REPO_REMOTE_STORAGE_REGEXP = /^(?:http|https|ftp):\/\//i;

Sonatype.repoServer.RepoEditPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  var tfStore = new Ext.data.SimpleStore({fields:['value'], data:[['True'],['False']]});
  var policyStore = new Ext.data.SimpleStore({fields:['value'], data:[['Release'], ['Snapshot']]});
  var checksumPolicyStore = new Ext.data.SimpleStore({fields:['value'], data:[['Ignore'], ['Warn'], ['StrictIfExists'], ['Strict']]});

  var ht = Sonatype.repoServer.resources.help.repos;

  this.restToContentUrl = function(r) {
    if (r.indexOf(Sonatype.config.host) > -1) {
      return r.replace(Sonatype.config.repos.urls.repositories, Sonatype.config.content.repositories);
    }
    else {
      return Sonatype.config.host + r.replace(Sonatype.config.repos.urls.repositories, Sonatype.config.content.repositories);
    }
  };
  
  this.convertId = function( value, parent ) {
    if ( ! value ) {
      value = parent.resourceURI.substring(parent.resourceURI.lastIndexOf('/')+1);
    }
    return value;
  };

  // START: Repo list ******************************************************
  this.repoRecordConstructor = Ext.data.Record.create([
    {name:'id', convert: this.convertId },
    {name:'repoType'},
    {name:'resourceURI'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
    {name:'repoPolicy'},
//  {name:'effectiveLocalStorageUrl'},
    {name:'contentUri', mapping:'resourceURI', convert: this.restToContentUrl }
  ]);

  this.reposReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.repoRecordConstructor );

  //@ext: must use data.Store (not JsonStore) to pass in reader instead of using fields config array
  this.reposDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.repositories,
    reader: this.reposReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true
  });

  this.repoTypeRecordConstructor = Ext.data.Record.create([
    {name:'description'},
    {name:'roleHint', sortType:Ext.data.SortTypes.asUCString}
  ]);
  this.repoTypeReader = new Ext.data.JsonReader({root: 'data', id: 'roleHint'}, this.repoTypeRecordConstructor );  
  this.repoTypeDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.repoTypes,
    reader: this.repoTypeReader,
    sortInfo: { field: 'roleHint', direction: 'ASC' },
    autoLoad: true
  });

  this.shadowRepoTypeRecordConstructor = Ext.data.Record.create([
    {name:'description'},
    {name:'roleHint', sortType:Ext.data.SortTypes.asUCString}
  ]);
  this.shadowRepoTypeReader = new Ext.data.JsonReader({root: 'data', id: 'roleHint'}, this.shadowRepoTypeRecordConstructor );  
  this.shadowRepoTypeDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.shadowRepoTypes,
    reader: this.shadowRepoTypeReader,
    sortInfo: { field: 'description', direction: 'ASC' },
    autoLoad: true
  });
  
  this.defaultTimeoutVals = {
    proxy_release : null,
    proxy_snapshot : null,
    hosted_release : null,
    hosted_snapshot : null
  };

  this.loadDataModFuncs = {
    virtual : {
      syncAtStartup : Sonatype.utils.capitalize
    },
    hosted : {
      repoPolicy : Sonatype.utils.capitalize,
      allowWrite : Sonatype.utils.capitalize,
      browseable : Sonatype.utils.capitalize,
      indexable : Sonatype.utils.capitalize
    },
    proxy : {
      repoPolicy : Sonatype.utils.capitalize,
      allowWrite : Sonatype.utils.capitalize,
      browseable : Sonatype.utils.capitalize,
      indexable : Sonatype.utils.capitalize,
      downloadRemoteIndexes : Sonatype.utils.capitalize,
      checksumPolicy : Sonatype.utils.capitalize
    }
  };
  
  this.templateLoadDataModFuncs = {
    virtual : Ext.apply({}, {
        id : Sonatype.utils.returnEmptyStr,
      name : Sonatype.utils.returnEmptyStr
      },
      this.loadDataModFuncs.virtual),
    hosted : Ext.apply({}, {
        id : Sonatype.utils.returnEmptyStr,
      name : Sonatype.utils.returnEmptyStr
      },
      this.loadDataModFuncs.hosted),
    proxy : Ext.apply({}, {
        id : Sonatype.utils.returnEmptyStr,
      name : Sonatype.utils.returnEmptyStr
      },
      this.loadDataModFuncs.proxy)
  };
  
  this.submitDataModFuncs = {
    virtual : {
      syncAtStartup : Sonatype.utils.convert.stringContextToBool
    },
    hosted : {
      repoPolicy : Sonatype.utils.lowercase,
      allowWrite : Sonatype.utils.convert.stringContextToBool,
      browseable : Sonatype.utils.convert.stringContextToBool,
      indexable : Sonatype.utils.convert.stringContextToBool,
      downloadRemoteIndexes : function() {return false;},
      checksumPolicy : function() {return 'ignore';}
    },
    proxy : {
      repoPolicy : Sonatype.utils.lowercase,
      allowWrite : Sonatype.utils.convert.stringContextToBool,
      browseable : Sonatype.utils.convert.stringContextToBool,
      indexable : Sonatype.utils.convert.stringContextToBool,
      downloadRemoteIndexes : Sonatype.utils.convert.stringContextToBool,
      checksumPolicy : Sonatype.utils.lowercaseFirstChar
    }
  };
  
  this.formConfig = {};
  this.formConfig.hosted = {
    // note: id should be added before using this config to instantiate a FormPanel
    region: 'center',
    width: '100%',
    height: '100%',
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
        xtype: 'textfield',
        fieldLabel: 'Repository ID',
        itemCls: 'required-field',
        helpText: ht.id,
        name: 'id',
        width: 200,
        allowBlank:false,
        disabled:true,
        validator: Sonatype.utils.validateId
      },{
        xtype: 'textfield',
        fieldLabel: 'Repository Name',
        itemCls: 'required-field',
        helpText: ht.name,
        name: 'name',
        width: 200,
        allowBlank:false
      },{
        xtype: 'textfield',
        fieldLabel: 'Repository Type',
        itemCls: 'required-field',
        helpText: ht.repoType,
        name: 'repoType',
        width: 100,
        disabled: true,
        allowBlank:false
      },
//      {
//        xtype: 'textfield',
//        fieldLabel: 'Security Realm',
//        name: 'realmId',
//        width: 100,
//        allowBlank:false
//      },
      {
        xtype: 'combo',
        fieldLabel: 'Format',
        itemCls: 'required-field',
        helpText: ht.format,
        name: 'format',
        //hiddenName: 'connectionTimeout',
        width: 150,
        store: this.repoTypeDataStore,
        displayField:'description',
        valueField:'roleHint',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false,
        disabled: true          
      },
      {
        xtype: 'combo',
        fieldLabel: 'Repository Policy',
        itemCls: 'required-field',
        helpText: ht.repoPolicy,
        name: 'repoPolicy',
        //hiddenName: 'connectionTimeout',
        width: 80,
        store: policyStore,
        displayField:'value',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false          
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Default Local Storage Location',
        helpText: ht.defaultLocalStorageUrl,
        name: 'defaultLocalStorageUrl',
        anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
        allowBlank:true,
        disabled: true
      },      
      {
        xtype: 'textfield',
        fieldLabel: 'Override Local Storage Location',
        helpText: ht.overrideLocalStorageUrl,
        name: 'overrideLocalStorageUrl',
        anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
        allowBlank:true
      },
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'Access Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        defaults: {
          xtype: 'combo',
          fieldLabel: 'default',
          itemCls: 'required-field',
          name: 'default',
          //hiddenName: 'connectionTimeout',
          width: 75,
          store: tfStore,
          displayField:'value',
          editable: false,
          forceSelection: true,
          mode: 'local',
          triggerAction: 'all',
          emptyText:'Select...',
          selectOnFocus:true,
          allowBlank: false          
        },

        items: [
          {
            fieldLabel: 'Allow Deployment',
            helpText: ht.allowWrite,
            name: 'allowWrite'
          },
          {
            fieldLabel: 'Allow File Browsing',
            helpText: ht.browseable,
            name: 'browseable'
          },
          {
            fieldLabel: 'Include in Search',
            helpText: ht.indexable,
            name: 'indexable'
          }
        ]
      },
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'Expiration Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        defaults: {
          xtype: 'numberfield',
          fieldLabel: 'default',
          itemCls: 'required-field',
          afterText: 'minutes',
          name: 'default',
          width: 50,
          allowBlank: false,
          allowDecimals: false,
          allowNegative: true,
          minValue: -1,
          maxValue: 511000      
        },

        items: [
          {
            fieldLabel: 'Not Found Cache TTL',
            helpText: ht.notFoundCacheTTL,
            name: 'notFoundCacheTTL'
          }
        ]
      }
    ],
    buttons: [
      {
        id: 'savebutton',
        text: 'Save',
        disabled: true
      },
      {
        id: 'cancelbutton',
        text: 'Cancel'
      }
    ]
  };
  
  this.formConfig.proxy = {
    region: 'center',
    width: '100%',
    height: '100%',
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
        xtype: 'textfield',
        fieldLabel: 'Repository ID',
        itemCls: 'required-field',
        helpText: ht.id,
        name: 'id',
        width: 200,
        allowBlank:false,
        disabled:true,
        validator: function(v){
          if(v.search(' ') == -1){ return true; }
          else{ return 'No spaces allowed in ID'; }
        }
      },{
        xtype: 'textfield',
        fieldLabel: 'Repository Name',
        itemCls: 'required-field',
        helpText: ht.name,
        name: 'name',
        width: 200,
        allowBlank:false
      },{
        xtype: 'textfield',
        fieldLabel: 'Repository Type',
        itemCls: 'required-field',
        helpText: ht.repoType,
        name: 'repoType',
        width: 100,
        disabled: true,
        allowBlank:false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Format',
        itemCls: 'required-field',
        helpText: ht.format,
        name: 'format',
        //hiddenName: 'connectionTimeout',
        width: 150,
        store: this.repoTypeDataStore,
        displayField:'description',
        valueField:'roleHint',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false,
        disabled: true          
      },
      {
        xtype: 'combo',
        fieldLabel: 'Repository Policy',
        itemCls: 'required-field',
        helpText: ht.repoPolicy,
        name: 'repoPolicy',
        //hiddenName: 'connectionTimeout',
        width: 80,
        store: policyStore,
        displayField:'value',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false          
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Default Local Storage Location',
        helpText: ht.defaultLocalStorageUrl,
        name: 'defaultLocalStorageUrl',
        anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
        allowBlank:true,
        disabled: true
      },      
      {
        xtype: 'textfield',
        fieldLabel: 'Override Local Storage Location',
        helpText: ht.overrideLocalStorageUrl,
        name: 'overrideLocalStorageUrl',
        anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
        allowBlank:true
      },
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'Remote Repository Access',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
  
        items: [
          {
            xtype: 'textfield',
            fieldLabel: 'Remote Storage Location',
            itemCls: 'required-field',
            helpText: ht.remoteStorageUrl,
            name: 'remoteStorage.remoteStorageUrl',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:false,
            validator: function(v){
              if(v.match(REPO_REMOTE_STORAGE_REGEXP)){ return true; }
              else{ return 'Protocol must be http://, https:// or ftp://'; }
            }
          },
          {
            xtype: 'combo',
            fieldLabel: 'Download Remote Indexes',
            helpText: ht.downloadRemoteIndexes,
            name: 'downloadRemoteIndexes',
            itemCls: 'required-field',
            width: 75,
            store: tfStore,
            displayField:'value',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText:'Select...',
            selectOnFocus:true,
            allowBlank: false          
          },
          {
            xtype: 'combo',
            fieldLabel: 'Checksum Policy',
            itemCls: 'required-field',
            helpText: ht.checksumPolicy,
            name: 'checksumPolicy',
            width: 95,
            store: checksumPolicyStore,
            displayField:'value',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText:'Select...',
            selectOnFocus:true,
            allowBlank: false          
          },
          {
            xtype: 'fieldset',
            checkboxToggle:true,
            title: 'Authentication (optional)',
            id: '_remoteStorage.authentication', //needs late prepend of specific form id
            collapsed: true,
            autoHeight:true,
            layoutConfig: {
              labelSeparator: ''
            },
  
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'Username',
                helpText: ht.remoteUsername,
                name: 'remoteStorage.authentication.username',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Password',
                helpText: ht.remotePassword,
                inputType:'password',
                name: 'remoteStorage.authentication.password',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Private Key',
                helpText: ht.remotePrivateKey,
                name: 'remoteStorage.authentication.privateKey',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Key Passphrase',
                helpText: ht.remotePassphrase,
                name: 'remoteStorage.authentication.passphrase',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Host',
                helpText: ht.remoteNtlmHost,
                name: 'remoteStorage.authentication.ntlmHost',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Manager Domain',
                helpText: ht.remoteNtlmDomain,
                name: 'remoteStorage.authentication.ntlmDomain',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              }
            ]
          }
        ]
      }, //end remote storage)
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'Access Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        defaults: {
          xtype: 'combo',
          fieldLabel: 'default',
          itemCls: 'required-field',
          name: 'default',
          //hiddenName: 'connectionTimeout',
          width: 75,
          store: tfStore,
          displayField:'value',
          editable: false,
          forceSelection: true,
          mode: 'local',
          triggerAction: 'all',
          emptyText:'Select...',
          selectOnFocus:true,
          allowBlank: false          
        },

        items: [
          {
            fieldLabel: 'Allow File Browsing',
            helpText: ht.browseable,
            name: 'browseable'
          },
          {
            fieldLabel: 'Include in Search',
            helpText: ht.indexable,
            name: 'indexable'
          }
        ]
      },
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'Expiration Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        defaults: {
          xtype: 'numberfield',
          fieldLabel: 'default',
          itemCls: 'required-field',
          afterText: 'minutes',
          name: 'default',
          width: 50,
          allowBlank: false,
          allowDecimals: false,
          allowNegative: true,
          minValue: -1,
          maxValue: 511000      
        },

        items: [
          {
            fieldLabel: 'Not Found Cache TTL',
            helpText: ht.notFoundCacheTTL,
            name: 'notFoundCacheTTL'
          },{
            fieldLabel: 'Artifact Max Age',
            helpText: ht.artifactMaxAge,
            name: 'artifactMaxAge'
          },
          {
            fieldLabel: 'Metadata Max Age',
            helpText: ht.metadataMaxAge,
            name: 'metadataMaxAge'
          }
        ]
      },

      {
        xtype: 'fieldset',
        checkboxToggle:true,
        title: 'HTTP Request Settings (optional)',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        id: '_remoteStorage.connectionSettings', //needs late prepend of specific form id
        collapsed: true,
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
            fieldLabel: 'User Agent',
            itemCls: 'required-field',
            helpText: ht.userAgentString,
            name: 'remoteStorage.connectionSettings.userAgentString',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:true
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Additional URL Parameters',
            helpText: ht.queryString,
            name: 'remoteStorage.connectionSettings.queryString',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:true
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Request Timeout',
            itemCls: 'required-field',
            helpText: ht.connectionTimeout,
            afterText: 'seconds',
            name: 'remoteStorage.connectionSettings.connectionTimeout',
            width: 75,
            allowBlank: true,
            allowDecimals: false,
            allowNegative: false,
            maxValue: 36000
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Request Retry Attempts',
            itemCls: 'required-field',
            helpText: ht.retrievalRetryCount,
            name: 'remoteStorage.connectionSettings.retrievalRetryCount',
            width: 50,
            allowBlank: true,
            allowDecimals: false,
            allowNegative: false,
            maxValue: 10
          }
        ]
      }, //end http conn
      {
        xtype: 'fieldset',
        checkboxToggle:true,
        title: 'Override HTTP Proxy Settings (optional)',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        id: '_remoteStorage.httpProxySettings', //needs late prepend of specific form id
        collapsed: true,
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
            itemCls: 'required-field',
            helpText: ht.proxyHostname,
            name: 'remoteStorage.httpProxySettings.proxyHostname',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank:true,
            validator: function(v){
              if (v.search(/:\//) == -1) { return true; }
              else {return 'Specify hostname without the protocol, example "my.host.com"';}
            }
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Proxy Port',
            itemCls: 'required-field',
            helpText: ht.proxyPort,
            name: 'remoteStorage.httpProxySettings.proxyPort',
            width: 50,
            allowBlank: true,
            allowDecimals: false,
            allowNegative: false,
            maxValue: 65535
          },
          {
            xtype: 'fieldset',
            checkboxToggle:true,
            title: 'Authentication (optional)',
            id: '_remoteStorage.httpProxySettings.authentication', //needs late prepend of specific form id
            collapsed: true,
            autoHeight:true,
            layoutConfig: {
              labelSeparator: ''
            },
  
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'Username',
                helpText: ht.username,
                name: 'remoteStorage.httpProxySettings.authentication.username',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Password',
                helpText: ht.password,
                inputType:'password',
                name: 'remoteStorage.httpProxySettings.authentication.password',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Private Key',
                helpText: ht.privateKey,
                name: 'remoteStorage.httpProxySettings.authentication.privateKey',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Key Passphrase',
                helpText: ht.passphrase,
                name: 'remoteStorage.httpProxySettings.authentication.passphrase',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Host',
                helpText: ht.ntlmHost,
                name: 'remoteStorage.httpProxySettings.authentication.ntlmHost',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Manager Domain',
                helpText: ht.ntlmDomain,
                name: 'remoteStorage.httpProxySettings.authentication.ntlmDomain',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              }
            ]
          }
        ]
      } // end proxy settings
    ],
    buttons: [
      {
        text: 'Save',
        disabled: true
      },
      {
        text: 'Cancel'
      }
    ]
  };
  
  this.formConfig.virtual = Ext.apply({}, this.formConfig.hosted);
  this.formConfig.virtual.items = [
    {
      xtype: 'textfield',
      fieldLabel: 'Repository ID',
      itemCls: 'required-field',
      helpText: ht.id,
      name: 'id',
      width: 200,
      allowBlank:false,
      disabled:true,
      validator: function(v){
        if(v.search(' ') == -1){ return true; }
        else{ return 'No spaces allowed in ID'; }
      }
    },{
      xtype: 'textfield',
      fieldLabel: 'Repository Name',
      itemCls: 'required-field',
      helpText: ht.name,
      name: 'name',
      width: 200,
      allowBlank:false
    },{
      xtype: 'textfield',
      fieldLabel: 'Repository Type',
      itemCls: 'required-field',
      helpText: ht.repoType,
      name: 'repoType',
      width: 100,
      disabled: true,
      allowBlank:false
    },
//    {
//      xtype: 'textfield',
//      fieldLabel: 'Security Realm',
//      name: 'realmId',
//      width: 100,
//      allowBlank:false
//    },
    {
      xtype: 'combo',
      fieldLabel: 'Format',
      itemCls: 'required-field',
      helpText: ht.format,
      name: 'format',
      //hiddenName: 'connectionTimeout',
      width: 200,
      midWidth: 200,
      store: this.shadowRepoTypeDataStore,
      displayField:'description',
      valueField:'roleHint',
      editable: false,
      forceSelection: true,
      mode: 'local',
      triggerAction: 'all',
      emptyText:'Select...',
      selectOnFocus:true,
      allowBlank: false,
      disabled: true          
    },
    {
      xtype: 'combo',
      fieldLabel: 'Source Nexus Repository ID',
      itemCls: 'required-field',
      helpText: ht.shadowOf,
      name: 'shadowOf',
      width: 200,
      midWidth: 200,
      store: this.reposDataStore,
      displayField:'name',
      valueField: 'id',
      editable: false,
      forceSelection: true,
      mode: 'local',
      triggerAction: 'all',
      emptyText:'Select...',
      selectOnFocus:true,
      allowBlank: false,
      validator: function( v ) {
        var rec = this.store.getAt( this.selectedIndex );
        if ( rec && rec.id.substring( 0, 8 ) == 'new_repo' ) {
          return 'Cannot source from a non-existent repository';
        }
        return true;
      }
    },
    {
      xtype: 'combo',
      fieldLabel: 'Synchronize on Startup',
      itemCls: 'required-field',
      helpText: ht.syncAtStartup,
      name: 'syncAtStartup',
      //hiddenName: 'connectionTimeout',
      width: 75,
      store: tfStore,
      displayField:'value',
      editable: false,
      forceSelection: true,
      mode: 'local',
      triggerAction: 'all',
      emptyText:'Select...',
      selectOnFocus:true,
      allowBlank: false          
    }
  ];
  
  this.sp = Sonatype.lib.Permissions;

  this.reposGridPanel = new Ext.grid.GridPanel({
    title: 'Repositories',
    id: 'st-repos-grid',
    selModel: new Ext.grid.RowSelectionModel({
      singleSelect: true
    }),
    region: 'north',
    layout:'fit',
    collapsible: true,
    split:true,
    height: 200,
    minHeight: 150,
    maxHeight: 400,
    frame: false,
    autoScroll: true,
    tbar: [
      {
        id: 'repo-refresh-btn',
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.reloadAll
      },
      {
        id: 'repo-add-btn',
        text:'Add...',
        icon: Sonatype.config.resourcePath + '/images/icons/add.png',
        cls: 'x-btn-text-icon',
        tooltip: {title:'Add Repository',text:'Select the type of Repository to create'},
        menu: {
          width:75,
          items: [
            {
              text: 'Hosted',
              handler: this.addRepoHandler.createDelegate(this, ['hosted'])
            },
            {
              text: 'Proxy',
              handler: this.addRepoHandler.createDelegate(this, ['proxy'])
            },
            {
              text: 'Virtual',
              handler: this.addRepoHandler.createDelegate(this, ['virtual'])
            }
          ]
        },
        disabled: !this.sp.checkPermission('nexus:repositories', this.sp.CREATE)
      },
      {
        id: 'repo-delete-btn',
        text: 'Delete',
        icon: Sonatype.config.resourcePath + '/images/icons/delete.png',
        cls: 'x-btn-text-icon',
        scope:this,
        handler: this.deleteRepoHandler,
        disabled: !this.sp.checkPermission('nexus:repositories', this.sp.DELETE)
      },
      {
        id: 'repo-trash-btn',
        text:'Trash...',
        icon: Sonatype.config.resourcePath + '/images/icons/user-trash.png',
        cls: 'x-btn-text-icon',
        tooltip: {title:'Trash',text:'Manage the Trash contents'},
        menu: {
          width:125,
          items: [
            {
              text: 'Empty Trash',
              handler: this.deleteTrashHandler.createDelegate(this)
            }
          ]
        },
        disabled: !this.sp.checkPermission('nexus:wastebasket', this.sp.DELETE)
      }
    ],

    //grid view options
    ds: this.reposDataStore,
    sortInfo:{field: 'name', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      {header: 'Repository', dataIndex: 'name', width:175},
      {header: 'Type', dataIndex: 'repoType', width:50},
      {header: 'Policy', dataIndex: 'repoPolicy', width:60},
      {header: 'Repository Path', dataIndex: 'contentUri', id: 'repo-config-url-col', width:300,renderer: function(s){return '<a href="' + s + ((s != null && (s.charAt(s.length)) == '/') ? '' : '/') +'" target="_blank">' + s + '</a>';},menuDisabled:true}
    ],
    autoExpandColumn: 'repo-config-url-col',
    disableSelection: false,
    viewConfig: {
      emptyText: 'Click "Add..." to create a repository'
    }
  });
  this.reposGridPanel.on('rowclick', this.repoRowClick, this);
  this.reposGridPanel.on('rowcontextmenu', this.onContextClickHandler, this);
  // END: Repo List ******************************************************
  // *********************************************************************

  Sonatype.repoServer.RepoEditPanel.superclass.constructor.call(this, {
    layout: 'border',
    autoScroll: false,
    width: '100%',
    height: '100%',
    items: [
      this.reposGridPanel,
      {
        xtype: 'panel',
        id: 'repo-config-forms',
        title: 'Repository Configuration',
        layout: 'card',
        region: 'center',
        activeItem: 0,
        deferredRender: false,
        autoScroll: false,
        frame: false,
        items: [
          {
            xtype: 'panel',
            layout: 'fit',
            html: '<div class="little-padding">Select a repository to edit it, or click "Add..." to create a new one.</div>'
          }
        ]
      }
    ]
  });

  this.formCards = this.findById('repo-config-forms');
};


Ext.extend(Sonatype.repoServer.RepoEditPanel, Sonatype.repoServer.AbstractRepoPanel, {
  reloadAll : function(){
    this.reposDataStore.reload();
    this.formCards.items.each(function(item, i, len){
      if(i>0){this.remove(item, true);}
    }, this.formCards);
    
    this.formCards.getLayout().setActiveItem(0);
  },
  
  optionalFieldsetExpandHandler : function(panel){
    panel.items.each(function(item, i, len){
      if (item.getEl().up('div.required-field', 3)) {
        item.allowBlank = false;
      }
      else if (item.isXType('fieldset', true)){
        this.optionalFieldsetExpandHandler(item);
      }
    }, this); // "this" is RepoEditPanel
  },
  
  optionalFieldsetCollapseHandler : function(panel){
    panel.items.each(function(item, i, len){
      if (item.getEl().up('div.required-field', 3)) {
        item.allowBlank = true;
      }
      else if (item.isXType('fieldset', true)){
        this.optionalFieldsetCollapseHandler(item);
      }
    }, this); // "this" is RepoEditPanel
  },
  
//contentUriColRender: function(value, p, record, rowIndex, colIndex, store) {
//  return String.format('<a target="_blank" href="{0}">{0}</a>', value);
//},


  onContextClickHandler : function(grid, index, e){
    this.onContextHideHandler();
    
    var reindexPriv = this.sp.checkPermission('nexus:index', this.sp.DELETE);
    var attributesPriv = this.sp.checkPermission('nexus:attributes', this.sp.DELETE);
    var uploadPriv = this.sp.checkPermission('nexus:artifact', this.sp.CREATE);
    
    if ( e.target.nodeName == 'A' ) return; // no menu on links
    
    this.ctxRow = this.reposGridPanel.view.getRow(index);
    this.ctxRecord = this.reposGridPanel.store.getAt(index);
    Ext.fly(this.ctxRow).addClass('x-node-ctx');

    //@todo: would be faster to pre-render the six variations of the menu for whole instance
    var menu = new Sonatype.menu.Menu({
      id:'repo-maint-grid-ctx',
      payload: this.ctxRecord,
      scope: this,
      items: []
    });
    
    Sonatype.Events.fireEvent( 'repositoryMenuInit', menu, this.ctxRecord );

    if ( ! menu.items.first() ) return;

    menu.on('hide', this.onContextHideHandler, this);
    e.stopEvent();
    menu.showAt(e.getXY());
  },

  onContextHideHandler : function(){
    if(this.ctxRow){
      Ext.fly(this.ctxRow).removeClass('x-node-ctx');
      this.ctxRow = null;
      this.ctxRecord = null;
    }
  },
  
  // formInfoObj : {formPanel, isNew, repoType, [resourceURI]}
  saveHandler : function(formInfoObj){
    if (formInfoObj.formPanel.form.isValid()) {
      var isNew = formInfoObj.isNew;
      var repoType = formInfoObj.repoType;
      var createUri = Sonatype.config.repos.urls.repositories;
      var updateUri = (formInfoObj.resourceURI) ? formInfoObj.resourceURI : '';
      var form = formInfoObj.formPanel.form;
    
      form.doAction('sonatypeSubmit', {
        method: (isNew) ? 'POST' : 'PUT',
        url: isNew ? createUri : updateUri,
        waitMsg: isNew ? 'Creating repository...' : 'Updating repository configuration...',
        fpanel: formInfoObj.formPanel,
        dataModifiers: this.submitDataModFuncs[repoType],
        serviceDataObj : Sonatype.repoServer.referenceData.repositoryState[repoType],
        isNew : isNew //extra option to send to callback, instead of conditioning on method
      });
    }
  },
  
  // formInfoObj : {formPanel, isNew, repoType, [resourceURI]}
  cancelHandler : function(formInfoObj) {
    var formLayout = this.formCards.getLayout();
    var gridSelectModel = this.reposGridPanel.getSelectionModel();
    var store = this.reposGridPanel.getStore();

    this.formCards.remove(formInfoObj.formPanel.id, true);
    //select previously selected form, or the default view (index == 0)
    var newIndex = this.formCards.items.length - 1;
    newIndex = (newIndex >= 0) ? newIndex : 0;
    formLayout.setActiveItem(newIndex);

    //delete row from grid if canceling a new repo form
    if(formInfoObj.isNew){
      store.remove( store.getById(formInfoObj.formPanel.id) );
    }
    
    //select the coordinating row in the grid, or none if back to default
    var i = store.indexOfId(formLayout.activeItem.id);
    if (i >= 0){
      gridSelectModel.selectRow(i);
    }
    else{
      gridSelectModel.clearSelections();
    }
  },
  
  deleteTrashHandler : function() {
    //@note: this handler selects the "No" button as the default
    //@todo: could extend Sonatype.MessageBox to take the button to select as a param
    Sonatype.MessageBox.getDialog().on('show', function(){
      this.focusEl = this.buttons[2]; //ack! we're offset dependent here
      this.focus();
    },
    Sonatype.MessageBox.getDialog(),
    {single:true});
    
    Sonatype.MessageBox.show({
      animEl: this.reposGridPanel.getEl(),
      title : 'Empty Trash?',
      msg : 'Delete the entire contents of the Trash?<br><br>This operation cannot be undone!',
      buttons: Sonatype.MessageBox.YESNO,
      scope: this,
      icon: Sonatype.MessageBox.QUESTION,
      fn: function(btnName){
        if (btnName == 'yes' || btnName == 'ok') {
          Ext.Ajax.request({
            callback: this.deleteTrashCallback,
            scope: this,
            method: 'DELETE',
            url:Sonatype.config.repos.urls.trash
          });
        }
      }
    });
  },
  
  addRepoHandler : function(repoType) {
    var id = 'new_repo_' + new Date().getTime();

    var config = Ext.apply({}, this.formConfig[repoType], {id:id});
    config = (repoType == 'proxy') ? this.proxyConfigGen(id, config) : config;
    var formPanel = new Ext.FormPanel(config);
    
    formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
    formPanel.form.on('actionfailed', this.actionFailedHandler, this);
    formPanel.on('beforerender', this.beforeFormRenderHandler, this);
    formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
    formPanel.on('render', function(fpanel, sType){
      // @Ext: findField by id or name does not seem to work
      fpanel.form.findField(2).setValue(sType);
    }.createDelegate(this, [repoType], true));
    
    var buttonInfoObj = {
        formPanel : formPanel,
        isNew : true,
        repoType : repoType
      };
    
    //save button event handler
    formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
    //cancel button event handler
    formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
    
    //add place holder to grid
    var newRec = new this.repoRecordConstructor({
        repoType : repoType,
        resourceURI : 'new',
        name : 'New Repository'
//      effectiveLocalStorageUrl : '-'
      },
      id); //use "new_repo_" id instead of resourceURI like the reader does
    this.reposDataStore.insert(0, [newRec]);
    this.reposGridPanel.getSelectionModel().selectRow(0);
    
    //add new form
    this.formCards.add(formPanel);
    //register a one time handler for loading this form to clear invalids, since it's a new form
    var basicForm = formPanel.getForm();
    basicForm.on('actioncomplete', function(form, action){form.clearInvalid();}, {single:true});
    //load default data
    this.formDataLoader(formPanel, Sonatype.config.repos.urls.repoTemplate[repoType], this.templateLoadDataModFuncs[repoType]);
    
    //always set active and re-layout
    this.formCards.getLayout().setActiveItem(formPanel);
    formPanel.doLayout();
  },
  
  afterLayoutFormHandler : function(formPanel, fLayout){
    // register required field quicktip, but have to wait for elements to show up in DOM
    var temp = function(){
      var els = Ext.select('.required-field .x-form-item-label, .required-field .x-panel-header-text', this.getEl());
      els.each(function(el, els, i){
        Ext.QuickTips.register({
          target: el,
          cls: 'required-field',
          title: '',
          text: 'Required Field',
          enabled: true
        });
      });
    }.defer(300, formPanel);
    
  },
  
  deleteRepoHandler : function(){
    if (this.reposGridPanel.getSelectionModel().hasSelection()){
      var rec = this.reposGridPanel.getSelectionModel().getSelected();
      
      if(rec.data.resourceURI == 'new'){
        this.cancelHandler({
          formPanel : Ext.getCmp(rec.id),
          isNew : true,
          repoType : rec.data.repotype
        });
      }
      else {
        //@note: this handler selects the "No" button as the default
        //@todo: could extend Sonatype.MessageBox to take the button to select as a param
        Sonatype.MessageBox.getDialog().on('show', function(){
          this.focusEl = this.buttons[2]; //ack! we're offset dependent here
          this.focus();
        },
        Sonatype.MessageBox.getDialog(),
        {single:true});
        
        Sonatype.MessageBox.show({
          animEl: this.reposGridPanel.getEl(),
          title : 'Delete Repository?',
          msg : 'Delete the ' + rec.get('name') + ' repository?',
          buttons: Sonatype.MessageBox.YESNO,
          scope: this,
          icon: Sonatype.MessageBox.QUESTION,
          fn: function(btnName){
            if (btnName == 'yes' || btnName == 'ok') {
              Ext.Ajax.request({
                callback: this.deleteCallback,
                cbPassThru: {
                  resourceId: rec.id
                },
                scope: this,
                method: 'DELETE',
                url:rec.data.resourceURI
              });
            }
          }
        });
      }
    }
  },
  
  deleteTrashCallback : function(options, isSuccess, response){
    if(!isSuccess){
      Sonatype.MessageBox.alert('The server did not empty the trash.');
    }
  },
  
  deleteCallback : function(options, isSuccess, response){
    if(isSuccess){
      var resourceId = options.cbPassThru.resourceId;
      var formLayout = this.formCards.getLayout();
      var gridSelectModel = this.reposGridPanel.getSelectionModel();
      var store = this.reposGridPanel.getStore();
      
      if(formLayout.activeItem.id == resourceId) {
        this.formCards.remove(resourceId, true);
        //select previously selected form, or the default view (index == 0)
        var newIndex = this.formCards.items.length - 1;
        newIndex = (newIndex >= 0) ? newIndex : 0;
        formLayout.setActiveItem(newIndex);
      }
      else {
        this.formCards.remove(resourceId, true);
      }
      
      store.remove( store.getById(resourceId) );
      
      //select the coordinating row in the grid, or none if back to default
      var i = store.indexOfId(formLayout.activeItem.id);
      if (i >= 0){
        gridSelectModel.selectRow(i);
      }
      else{
        gridSelectModel.clearSelections();
      }

      Sonatype.Events.fireEvent( 'repositoryChanged' );
    }
    else {
      Sonatype.MessageBox.alert('The server did not delete the repository.');
    }
  },
  
  //(Ext.form.BasicForm, Ext.form.Action)
  actionCompleteHandler : function(form, action) {
    //@todo: handle server error response here!!

    if (action.type == 'sonatypeSubmit'){
      var isNew = action.options.isNew;
      
      if (isNew) {
        //@todo: Replace sentData with action.result.data after service sends back data response for creates
      
        //successful create
        var sentData = action.output.data;
        //repo state data doesn't have resourceURI in it like the list data
        sentData.resourceURI = action.getUrl() + '/' + sentData.id; //add this to match the list data field to create the record
        sentData.contentUri = this.restToContentUrl(sentData.resourceURI);

        var newRec = new this.repoRecordConstructor(sentData, action.options.fpanel.id); //form and grid data id match, keep the new id

        this.reposDataStore.remove(this.reposDataStore.getById(action.options.fpanel.id)); //remove old one
        this.reposDataStore.addSorted(newRec);
        this.reposGridPanel.getSelectionModel().selectRecords([newRec], false);
      
        var idTextField = action.options.fpanel.find('name', 'id')[0];
        idTextField.disable();
        
        var formatField = action.options.fpanel.find('name', 'format')[0];
        formatField.disable();
        
        //remove button click listeners
        action.options.fpanel.buttons[0].purgeListeners();
        action.options.fpanel.buttons[1].purgeListeners();
      
        var buttonInfoObj = {
            formPanel : action.options.fpanel,
            isNew : false,
            repoType : sentData.repoType,
            resourceURI : sentData.resourceURI
          };
      
        //save button event handler
        action.options.fpanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
        //cancel button event handler
        action.options.fpanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
      }
      else {
        var sentData = action.output.data;
      
        var i = this.reposDataStore.indexOfId(action.options.fpanel.id);
        var rec = this.reposDataStore.getAt(i);
//      var locUrl = (sentData.overrideLocalStorageUrl) ? sentData.overrideLocalStorageUrl : sentData.defaultLocalStorageUrl;
        rec.beginEdit();
        rec.set('name', sentData.name);
//      if(locUrl) {
//        rec.set('effectiveLocalStorageUrl', locUrl);
//      }
        rec.commit();
        rec.endEdit();
        
        var sortState = this.reposDataStore.getSortState();
        this.reposDataStore.sort(sortState.field, sortState.direction);
      }

      Sonatype.Events.fireEvent( 'repositoryChanged' );
    }
    
    if (action.type == 'sonatypeLoad'){
      var repoType = action.result.data.repoType;
      var repoPolicy = action.result.data.repoPolicy;
      
      if ( repoType == 'proxy' &&! action.result.data.remoteStorage.remoteStorageUrl
          .match( REPO_REMOTE_STORAGE_REGEXP ) ) {
        var rsUrl = form.findField( 'remoteStorage.remoteStorageUrl' );
        rsUrl.disable();
        rsUrl.clearInvalid();
        
        // Disable the editor - this is a temporary measure,
        // until we find a better solution for procurement repos
        action.options.fpanel.buttons[0].disable();
      }
      
      if (repoType == 'hosted' || repoType == 'proxy') {
        
        //only if data from a template
        if (action.options.url.search(Sonatype.config.repos.urls.repoTemplates) !== -1) {
          if (!this.defaultTimeoutVals[repoType + '_' + repoPolicy]) {
            //set data for later use w/o request
            if (repoType == 'proxy') {
              this.defaultTimeoutVals[repoType + '_' + repoPolicy] = {
                notFoundCacheTTL : action.result.data.notFoundCacheTTL,
                artifactMaxAge : action.result.data.artifactMaxAge,                
                metadataMaxAge : action.result.data.metadataMaxAge
              };
            }
            else {
              this.defaultTimeoutVals[repoType + '_' + repoPolicy] = {
                notFoundCacheTTL : action.result.data.notFoundCacheTTL
              };
            }
          }
        }
        
        form.setBackExpValsFunc = null;
        
        var repoField = form.findField('repoPolicy');
        
        form.defaultTimeoutVals = this.defaultTimeoutVals; //put a reference on the form, so it's in scope
        
        //@todo: should not need to store this myself.  Maybe extend select event to provide a change bit
        form.lastPolicy = repoField.getValue();
        
        repoField.on('select', function(field, rec, index){  
            //note: scoped to form
            
            if (this.lastPolicy != field.getValue()){
              if (this.setBackExpValsFunc) {
                this.setBackExpValsFunc();
              }
              else {
                if (repoType == 'proxy') {
                  var notFoundCacheTTL = this.findField('notFoundCacheTTL').getValue();
                  var artifactMaxAge = this.findField('artifactMaxAge').getValue();
                  var metadataMaxAge = this.findField('metadataMaxAge').getValue();
          
                  this.setBackExpValsFunc = function(){
                    this.setValues({
                      notFoundCacheTTL : notFoundCacheTTL,
                      artifactMaxAge : artifactMaxAge,                
                      metadataMaxAge : metadataMaxAge
                    });
            
                    this.setBackExpValsFunc = null; //terminate self
                  }.createDelegate(this);
                }
                else {
                  var notFoundCacheTTL = this.findField('notFoundCacheTTL').getValue();
                  
                  this.setBackExpValsFunc = function(){
                    this.setValues({
                      notFoundCacheTTL : notFoundCacheTTL
                    });
            
                    this.setBackExpValsFunc = null; //terminate self
                  }.createDelegate(this);
                }
            
                var repoType = this.findField('repoType').getValue();
                var repoPolicy = field.getValue().toLowerCase();
            
                if (this.defaultTimeoutVals[repoType + '_' + repoPolicy]) {
                  this.setValues(this.defaultTimeoutVals[repoType + '_' + repoPolicy]);
                }
                else {
                  Ext.Ajax.request({
                    scope: this, //this is the basic form
                    method: 'GET',
                    url: Sonatype.config.repos.urls.repoTemplate[repoType + '_' + repoPolicy],
                    callback: function(options, success, response){
                      if (success) {
                        var templateData = Ext.decode(response.responseText);
                        if (repoType == 'proxy') {
                          this.defaultTimeoutVals[repoType + '_' + repoPolicy] = {
                            notFoundCacheTTL : templateData.data.notFoundCacheTTL,
                            artifactMaxAge : templateData.data.artifactMaxAge,
                            metadataMaxAge : templateData.data.metadataMaxAge
                          };
                        }
                        else {
                          this.defaultTimeoutVals[repoType + '_' + repoPolicy] = {
                            notFoundCacheTTL : templateData.data.notFoundCacheTTL
                          };
                        }
                        
                        this.setValues(this.defaultTimeoutVals[repoType + '_' + repoPolicy]);
                      }  
                    }
                  });
                }
            
              }
              this.lastPolicy = field.getValue();
            }
          },
          form
        );
      }
    }
    
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
  
  beforeFormRenderHandler : function(component){
    if(component.id.search(/new_/) === 0){
      component.items.items[0].disabled = false; // endable ID field for new repo forms only
      component.items.items[3].disabled = false; // enable format field for new repo forms only
    }
    
    var sp = Sonatype.lib.Permissions;
    if(sp.checkPermission('nexus:repositories', sp.EDIT)){
      component.buttons[0].disabled = false;
    }
  },
  
  formDataLoader : function(formPanel, resourceURI, modFuncs){
    formPanel.getForm().doAction('sonatypeLoad', {url:resourceURI, method:'GET', fpanel:formPanel, dataModifiers: modFuncs, scope: this});
  },
  
  repoRowClick : function(grid, rowIndex, e){
    var rec = grid.store.getAt(rowIndex);
    this.viewRepo( rec );
  },
    
  viewRepo: function( rec ) {
    var id = rec.id; //note: rec.id is unique for new repos and equal to resourceURI for existing ones
    var formPanel = this.formCards.findById(id);
    
    //assumption: new repo forms always exist in formCards, so they won't get into this case
    if(!formPanel){ //create form and populate current data
      var config = Ext.apply({}, this.formConfig[rec.data.repoType], {id:id});
      config = (rec.data.repoType == 'proxy') ? this.proxyConfigGen(id, config) : config;
      formPanel = new Ext.FormPanel(config);
      formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
      formPanel.form.on('actionfailed', this.actionFailedHandler, this);
      formPanel.on('beforerender', this.beforeFormRenderHandler, this);
      formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
      
      var buttonInfoObj = {
          formPanel : formPanel,
          isNew : false, //not a new repo form, see assumption
          repoType : rec.data.repoType,
          resourceURI : rec.data.resourceURI
        };
      
      //save button event handler
      formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
      //cancel button event handler
      formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
      
      this.formDataLoader(formPanel, rec.data.resourceURI, this.loadDataModFuncs[rec.data.repoType]);
      this.formCards.add(formPanel);
    }
    
    //always set active and re-layout
    this.formCards.getLayout().setActiveItem(formPanel);
    formPanel.doLayout();
  },
  
  //creates a unique proxy config object with specific IDs on optional fieldsets
  proxyConfigGen : function(id, config){
    //@note: there has to be a better way to do this.  Depending on offsets is very error prone
    
    var newConfig = config; //Sonatype.utils.cloneObj(config);
    
    var fieldsets = [
      {obj : newConfig.items[7].items[3], postpend : '_remoteStorage.authentication'},
      {obj : newConfig.items[10], postpend : '_remoteStorage.connectionSettings'},
      {obj : newConfig.items[11], postpend : '_remoteStorage.httpProxySettings'},
      {obj : newConfig.items[11].items[2], postpend : '_remoteStorage.httpProxySettings.authentication'}
    ];
    
    for (var i = 0; i<fieldsets.length; i++) {
      fieldsets[i].obj.id = id + fieldsets[i].postpend;
    }
    
    return newConfig;
  }
  
});


Sonatype.repoServer.AbstractRepositoryEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    uri: Sonatype.config.repos.urls.repositories,
    resetButton: true,
    defaultTimeoutValues: {}
  };
  Ext.apply( this, config, defaultConfig );
  
  Sonatype.repoServer.AbstractRepositoryEditor.superclass.constructor.call( this, {
    listeners: {
      submit: {
        fn: this.submitHandler,
        scope: this
      }
    }
  } );
};

Ext.extend( Sonatype.repoServer.AbstractRepositoryEditor, Sonatype.ext.FormPanel, {
  loadData: function() {
    if ( this.isNew ) {
      var templateModifiers = Ext.apply( {}, 
        {
          id : Sonatype.utils.returnEmptyStr,
          name : Sonatype.utils.returnEmptyStr
        },
        this.dataModifiers.load );

      this.form.on( 'actioncomplete',
        function( form, action ) { 
          form.clearInvalid();
        }, 
        { single:true } 
      );
      this.form.doAction( 'sonatypeLoad', {
        url: Sonatype.config.repos.urls.repoTemplate[this.payload.data.repoType],
        method: 'GET',
        fpanel: this,
        dataModifiers: templateModifiers,
        scope: this
      } );
    }
    else {
      Sonatype.repoServer.AbstractRepositoryEditor.superclass.loadData.call( this );
    }
  },
  
  providerSelectHandler: function( combo, rec, index ) {
    this.form.findField( 'format' ).setValue( rec.data.format );
  },

  repoPolicySelectHandler: function( combo, rec, index ) {
    var repoPolicy = rec.data.value.toLowerCase();
    var fields = ['notFoundCacheTTL', 'artifactMaxAge', 'metadataMaxAge'];
  
    if ( this.lastPolicy != repoPolicy ) {
      if ( this.setBackExpValsFunc ) {
        this.setBackExpValsFunc();
      }
      else {
        var oldValues = {};
        for ( var i = fields.length - 1; i >= 0; i-- ) {
          var formField = this.form.findField( fields[i] );
          if ( formField ) {
            oldValues[fields[i]] = formField.getValue();
          }
        }
  
        this.setBackExpValsFunc = function() {
          this.form.setValues( oldValues );
          this.setBackExpValsFunc = null;
        }.createDelegate( this );
  
        var repoType = this.form.findField( 'repoType' ).getValue();
  
        if ( this.defaultTimeoutValues[repoPolicy]) {
          this.form.setValues( this.defaultTimeoutValues[repoPolicy] );
        }
        else {
          Ext.Ajax.request( {
            scope: this,
            url: Sonatype.config.repos.urls.repoTemplate[repoType + '_' + repoPolicy],
            callback: function( options, success, response ) {
              if ( success ) {
                var templateData = Ext.decode( response.responseText );
                this.defaultTimeoutValues[repoPolicy] = {};
                for ( var i = fields.length - 1; i >= 0; i-- ) {
                  var formField = this.form.findField( fields[i] );
                  if ( formField ) {
                    this.defaultTimeoutValues[repoPolicy][fields[i]] = templateData.data[fields[i]];
                  }
                }
                
                this.form.setValues( this.defaultTimeoutValues[repoPolicy] );
              }  
            }
          } );
        }
    
      }
  
      this.lastPolicy = repoPolicy;
    }
  },

  submitHandler: function( form, action, receivedData ) {
    if ( this.isNew ) {
      if ( ! receivedData.resourceURI ) {
        var url = action.options.url + '/' + receivedData.id;
        receivedData.resourceURI = url;
        receivedData.displayURI = url.replace(
          Sonatype.config.repos.urls.repositories, Sonatype.config.content.repositories );
      }
      return;
    }
    
    var rec = this.payload;
    rec.beginEdit();
    rec.set( 'name', receivedData.name );
    rec.set( 'repoType', receivedData.repoType );
    rec.set( 'repoPolicy', receivedData.repoPolicy );
    rec.commit();
    rec.endEdit();
  }
} );

Sonatype.repoServer.HostedRepositoryEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    dataModifiers: {
      load: {
        repoPolicy: Sonatype.utils.capitalize,
        allowWrite: Sonatype.utils.capitalize,
        browseable: Sonatype.utils.capitalize,
        indexable: Sonatype.utils.capitalize
      },
      submit: { 
        repoPolicy: Sonatype.utils.lowercase,
        allowWrite: Sonatype.utils.convert.stringContextToBool,
        browseable: Sonatype.utils.convert.stringContextToBool,
        indexable: Sonatype.utils.convert.stringContextToBool,
        downloadRemoteIndexes: function() { return false; },
        checksumPolicy: function() { return 'ignore'; }
      }
    },
    referenceData: Sonatype.repoServer.referenceData.repositoryState.hosted
  };
  Ext.apply( this, config, defaultConfig );

  var ht = Sonatype.repoServer.resources.help.repos;

  this.tfStore = new Ext.data.SimpleStore( {
    fields: ['value'],
    data: [['True'], ['False']] 
  } );

  this.policyStore = new Ext.data.SimpleStore( {
    fields: ['value'], 
    data: [['Release'], ['Snapshot']]
  } );
  
  this.providerStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'provider',
    fields: [
      { name: 'description', sortType:Ext.data.SortTypes.asUCString },
      { name: 'format' },
      { name: 'provider' }
    ],
    sortInfo: { field: 'description', direction: 'asc' },
    url: Sonatype.config.repos.urls.repoTypes,
    autoLoad: true
  } );

  this.checkPayload();

  Sonatype.repoServer.HostedRepositoryEditor.superclass.constructor.call( this, {
    items: [
      {
        xtype: 'textfield',
        fieldLabel: 'Repository ID',
        itemCls: 'required-field',
        helpText: ht.id,
        name: 'id',
        width: 200,
        allowBlank: false,
        disabled: ! this.isNew,
        validator: Sonatype.utils.validateId
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Repository Name',
        itemCls: 'required-field',
        helpText: ht.name,
        name: 'name',
        width: 200,
        allowBlank: false
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Repository Type',
        itemCls: 'required-field',
        helpText: ht.repoType,
        name: 'repoType',
        width: 100,
        disabled: true,
        allowBlank: false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Provider',
        itemCls: 'required-field',
        helpText: ht.provider,
        name: 'provider',
        width: 150,
        store: this.providerStore,
        displayField: 'description',
        valueField: 'provider',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select...',
        selectOnFocus: true,
        allowBlank: false,
        disabled: ! this.isNew,
        listeners: {
          select: this.providerSelectHandler,
          scope: this
        }
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Format',
        itemCls: 'required-field',
        helpText: ht.format,
        name: 'format',
        width: 100,
        disabled: true,
        allowBlank: false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Repository Policy',
        itemCls: 'required-field',
        helpText: ht.repoPolicy,
        name: 'repoPolicy',
        width: 80,
        store: this.policyStore,
        displayField: 'value',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select...',
        selectOnFocus: true,
        allowBlank: false,
        listeners: {
          select: this.repoPolicySelectHandler,
          scope: this
        }
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Default Local Storage Location',
        helpText: ht.defaultLocalStorageUrl,
        name: 'defaultLocalStorageUrl',
        anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
        allowBlank: true,
        disabled: true
      },      
      {
        xtype: 'textfield',
        fieldLabel: 'Override Local Storage Location',
        helpText: ht.overrideLocalStorageUrl,
        name: 'overrideLocalStorageUrl',
        anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
        allowBlank: true
      },
      {
        xtype: 'fieldset',
        checkboxToggle: false,
        title: 'Access Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight: true,
        layoutConfig: {
          labelSeparator: ''
        },
        defaults: {
          xtype: 'combo',
          fieldLabel: 'default',
          itemCls: 'required-field',
          name: 'default',
          width: 75,
          store: this.tfStore,
          displayField: 'value',
          editable: false,
          forceSelection: true,
          mode: 'local',
          triggerAction: 'all',
          emptyText: 'Select...',
          selectOnFocus: true,
          allowBlank: false          
        },
        items: [
          {
            fieldLabel: 'Allow Deployment',
            helpText: ht.allowWrite,
            name: 'allowWrite'
          },
          {
            fieldLabel: 'Allow File Browsing',
            helpText: ht.browseable,
            name: 'browseable'
          },
          {
            fieldLabel: 'Include in Search',
            helpText: ht.indexable,
            name: 'indexable'
          }
        ]
      },
      {
        xtype: 'fieldset',
        checkboxToggle: false,
        title: 'Expiration Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: true,
        autoHeight: true,
        layoutConfig: {
          labelSeparator: ''
        },
        defaults: {
          xtype: 'numberfield',
          fieldLabel: 'default',
          itemCls: 'required-field',
          afterText: 'minutes',
          name: 'default',
          width: 50,
          allowBlank: false,
          allowDecimals: false,
          allowNegative: true,
          minValue: -1,
          maxValue: 511000      
        },
        items: [
          {
            fieldLabel: 'Not Found Cache TTL',
            helpText: ht.notFoundCacheTTL,
            name: 'notFoundCacheTTL'
          }
        ]
      }
    ]
  } );
};

Ext.extend( Sonatype.repoServer.HostedRepositoryEditor, Sonatype.repoServer.AbstractRepositoryEditor, {
} );

Sonatype.repoServer.ProxyRepositoryEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    dataModifiers: {
      load: {
        repoPolicy: Sonatype.utils.capitalize,
        allowWrite: Sonatype.utils.capitalize,
        browseable: Sonatype.utils.capitalize,
        indexable: Sonatype.utils.capitalize,
        downloadRemoteIndexes: Sonatype.utils.capitalize,
        checksumPolicy: Sonatype.utils.capitalize
      },
      submit: { 
        repoPolicy: Sonatype.utils.lowercase,
        allowWrite: Sonatype.utils.convert.stringContextToBool,
        browseable: Sonatype.utils.convert.stringContextToBool,
        indexable: Sonatype.utils.convert.stringContextToBool,
        downloadRemoteIndexes: Sonatype.utils.convert.stringContextToBool,
        checksumPolicy: Sonatype.utils.lowercaseFirstChar
      }
    },
    referenceData: Sonatype.repoServer.referenceData.repositoryState.proxy
  };
  Ext.apply( this, config, defaultConfig );

  var ht = Sonatype.repoServer.resources.help.repos;

  this.tfStore = new Ext.data.SimpleStore( {
    fields: ['value'],
    data: [['True'], ['False']] 
  } );

  this.policyStore = new Ext.data.SimpleStore( {
    fields: ['value'], 
    data: [['Release'], ['Snapshot']]
  } );

  this.checksumPolicyStore = new Ext.data.SimpleStore( {
    fields: ['value'], 
    data: [['Ignore'], ['Warn'], ['StrictIfExists'], ['Strict']]
  } );

  this.providerStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'provider',
    fields: [
      { name: 'description', sortType:Ext.data.SortTypes.asUCString },
      { name: 'format' },
      { name: 'provider' }
    ],
    sortInfo: { field: 'description', direction: 'asc' },
    url: Sonatype.config.repos.urls.repoTypes,
    autoLoad: true
  } );

  this.checkPayload();

  Sonatype.repoServer.ProxyRepositoryEditor.superclass.constructor.call( this, {
    items: [
      {
        xtype: 'textfield',
        fieldLabel: 'Repository ID',
        itemCls: 'required-field',
        helpText: ht.id,
        name: 'id',
        width: 200,
        allowBlank: false,
        disabled: ! this.isNew,
        validator: Sonatype.utils.validateId
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Repository Name',
        itemCls: 'required-field',
        helpText: ht.name,
        name: 'name',
        width: 200,
        allowBlank: false
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Repository Type',
        itemCls: 'required-field',
        helpText: ht.repoType,
        name: 'repoType',
        width: 100,
        disabled: true,
        allowBlank: false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Provider',
        itemCls: 'required-field',
        helpText: ht.provider,
        name: 'provider',
        width: 150,
        store: this.providerStore,
        displayField: 'description',
        valueField: 'provider',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select...',
        selectOnFocus: true,
        allowBlank: false,
        disabled: ! this.isNew,
        listeners: {
          select: this.providerSelectHandler,
          scope: this
        }
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Format',
        itemCls: 'required-field',
        helpText: ht.format,
        name: 'format',
        width: 100,
        disabled: true,
        allowBlank: false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Repository Policy',
        itemCls: 'required-field',
        helpText: ht.repoPolicy,
        name: 'repoPolicy',
        width: 80,
        store: this.policyStore,
        displayField: 'value',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select...',
        selectOnFocus: true,
        allowBlank: false,
        listeners: {
          select: this.repoPolicySelectHandler,
          scope: this
        }
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Default Local Storage Location',
        helpText: ht.defaultLocalStorageUrl,
        name: 'defaultLocalStorageUrl',
        anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
        allowBlank: true,
        disabled: true
      },      
      {
        xtype: 'textfield',
        fieldLabel: 'Override Local Storage Location',
        helpText: ht.overrideLocalStorageUrl,
        name: 'overrideLocalStorageUrl',
        anchor: Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
        allowBlank: true
      },
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'Remote Repository Access',
        anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        collapsible: true,
        autoHeight: true,
        layoutConfig: {
          labelSeparator: ''
        },
        items: [
          {
            xtype: 'textfield',
            fieldLabel: 'Remote Storage Location',
            itemCls: 'required-field',
            helpText: ht.remoteStorageUrl,
            name: 'remoteStorage.remoteStorageUrl',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank: false,
            validator: function(v){
              if ( v.match( REPO_REMOTE_STORAGE_REGEXP ) ) { 
                return true; 
              }
              else { 
                return 'Protocol must be http://, https:// or ftp://'; 
              }
            }
          },
          {
            xtype: 'combo',
            fieldLabel: 'Download Remote Indexes',
            helpText: ht.downloadRemoteIndexes,
            name: 'downloadRemoteIndexes',
            itemCls: 'required-field',
            width: 75,
            store: this.tfStore,
            displayField: 'value',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText: 'Select...',
            selectOnFocus: true,
            allowBlank: false          
          },
          {
            xtype: 'combo',
            fieldLabel: 'Checksum Policy',
            itemCls: 'required-field',
            helpText: ht.checksumPolicy,
            name: 'checksumPolicy',
            width: 95,
            store: this.checksumPolicyStore,
            displayField: 'value',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText: 'Select...',
            selectOnFocus: true,
            allowBlank: false          
          },
          {
            xtype: 'fieldset',
            checkboxToggle: true,
            title: 'Authentication (optional)',
            name: 'fieldset_remoteStorage.authentication',
            collapsed: true,
            autoHeight: true,
            layoutConfig: {
              labelSeparator: ''
            },
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'Username',
                helpText: ht.remoteUsername,
                name: 'remoteStorage.authentication.username',
                width: 100,
                allowBlank: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Password',
                helpText: ht.remotePassword,
                inputType:'password',
                name: 'remoteStorage.authentication.password',
                width: 100,
                allowBlank: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Private Key',
                helpText: ht.remotePrivateKey,
                name: 'remoteStorage.authentication.privateKey',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Key Passphrase',
                helpText: ht.remotePassphrase,
                name: 'remoteStorage.authentication.passphrase',
                width: 100,
                allowBlank:true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Host',
                helpText: ht.remoteNtlmHost,
                name: 'remoteStorage.authentication.ntlmHost',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Manager Domain',
                helpText: ht.remoteNtlmDomain,
                name: 'remoteStorage.authentication.ntlmDomain',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank: true
              }
            ]
          }
        ]
      }, //end remote storage)
      {
        xtype: 'fieldset',
        checkboxToggle: false,
        title: 'Access Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        collapsible: true,
        autoHeight: true,
        layoutConfig: {
          labelSeparator: ''
        },
        defaults: {
          xtype: 'combo',
          fieldLabel: 'default',
          itemCls: 'required-field',
          name: 'default',
          width: 75,
          store: this.tfStore,
          displayField:'value',
          editable: false,
          forceSelection: true,
          mode: 'local',
          triggerAction: 'all',
          emptyText: 'Select...',
          selectOnFocus: true,
          allowBlank: false          
        },

        items: [
          {
            fieldLabel: 'Allow File Browsing',
            helpText: ht.browseable,
            name: 'browseable'
          },
          {
            fieldLabel: 'Include in Search',
            helpText: ht.indexable,
            name: 'indexable'
          }
        ]
      },
      {
        xtype: 'fieldset',
        checkboxToggle:false,
        title: 'Expiration Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        collapsible: true,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        defaults: {
          xtype: 'numberfield',
          fieldLabel: 'default',
          itemCls: 'required-field',
          afterText: 'minutes',
          name: 'default',
          width: 50,
          allowBlank: false,
          allowDecimals: false,
          allowNegative: true,
          minValue: -1,
          maxValue: 511000      
        },

        items: [
          {
            fieldLabel: 'Not Found Cache TTL',
            helpText: ht.notFoundCacheTTL,
            name: 'notFoundCacheTTL'
          },{
            fieldLabel: 'Artifact Max Age',
            helpText: ht.artifactMaxAge,
            name: 'artifactMaxAge'
          },
          {
            fieldLabel: 'Metadata Max Age',
            helpText: ht.metadataMaxAge,
            name: 'metadataMaxAge'
          }
        ]
      },

      {
        xtype: 'fieldset',
        checkboxToggle:true,
        title: 'HTTP Request Settings (optional)',
        anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        name: 'fieldset_remoteStorage.connectionSettings',
        collapsed: true,
        autoHeight: true,
        layoutConfig: {
          labelSeparator: ''
        },
        listeners: {
          'expand': {
            fn: this.optionalFieldsetExpandHandler,
            scope: this
          },
          'collapse': {
            fn: this.optionalFieldsetCollapseHandler,
            scope: this,
            delay: 100
          }
        },
        items: [
          {
            xtype: 'textfield',
            fieldLabel: 'User Agent',
            itemCls: 'required-field',
            helpText: ht.userAgentString,
            name: 'remoteStorage.connectionSettings.userAgentString',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank: true
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Additional URL Parameters',
            helpText: ht.queryString,
            name: 'remoteStorage.connectionSettings.queryString',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank: true
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Request Timeout',
            itemCls: 'required-field',
            helpText: ht.connectionTimeout,
            afterText: 'seconds',
            name: 'remoteStorage.connectionSettings.connectionTimeout',
            width: 75,
            allowBlank: true,
            allowDecimals: false,
            allowNegative: false,
            maxValue: 36000
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Request Retry Attempts',
            itemCls: 'required-field',
            helpText: ht.retrievalRetryCount,
            name: 'remoteStorage.connectionSettings.retrievalRetryCount',
            width: 50,
            allowBlank: true,
            allowDecimals: false,
            allowNegative: false,
            maxValue: 10
          }
        ]
      }, //end http conn
      {
        xtype: 'fieldset',
        checkboxToggle:true,
        title: 'Override HTTP Proxy Settings (optional)',
        anchor: Sonatype.view.FIELDSET_OFFSET_WITH_SCROLL,
        name: 'fieldset_remoteStorage.httpProxySettings',
        collapsed: true,
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
            itemCls: 'required-field',
            helpText: ht.proxyHostname,
            name: 'remoteStorage.httpProxySettings.proxyHostname',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank: true,
            validator: function( v ){
              if ( v.search( /:\// ) == -1 ) { 
                return true; 
              }
              else { 
                return 'Specify hostname without the protocol, example "my.host.com"';
              }
            }
          },
          {
            xtype: 'numberfield',
            fieldLabel: 'Proxy Port',
            itemCls: 'required-field',
            helpText: ht.proxyPort,
            name: 'remoteStorage.httpProxySettings.proxyPort',
            width: 50,
            allowBlank: true,
            allowDecimals: false,
            allowNegative: false,
            maxValue: 65535
          },
          {
            xtype: 'fieldset',
            checkboxToggle: true,
            title: 'Authentication (optional)',
            name: 'fieldset_remoteStorage.httpProxySettings.authentication',
            collapsed: true,
            autoHeight: true,
            layoutConfig: {
              labelSeparator: ''
            },
  
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'Username',
                helpText: ht.username,
                name: 'remoteStorage.httpProxySettings.authentication.username',
                width: 100,
                allowBlank: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Password',
                helpText: ht.password,
                inputType:'password',
                name: 'remoteStorage.httpProxySettings.authentication.password',
                width: 100,
                allowBlank: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Private Key',
                helpText: ht.privateKey,
                name: 'remoteStorage.httpProxySettings.authentication.privateKey',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'Key Passphrase',
                helpText: ht.passphrase,
                name: 'remoteStorage.httpProxySettings.authentication.passphrase',
                width: 100,
                allowBlank: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Host',
                helpText: ht.ntlmHost,
                name: 'remoteStorage.httpProxySettings.authentication.ntlmHost',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank: true
              },
              {
                xtype: 'textfield',
                fieldLabel: 'NT LAN Manager Domain',
                helpText: ht.ntlmDomain,
                name: 'remoteStorage.httpProxySettings.authentication.ntlmDomain',
                anchor: Sonatype.view.FIELD_OFFSET,
                allowBlank: true
              }
            ]
          }
        ]
      } // end proxy settings
    ],
    listeners: {
      load: {
        fn: this.loadHandler,
        scope: this
      }
    }
  } );
};

Ext.extend( Sonatype.repoServer.ProxyRepositoryEditor, Sonatype.repoServer.AbstractRepositoryEditor, {
  loadHandler: function( form, action, receivedData ) {
    var repoType = receivedData.repoType;
    var repoPolicy = receivedData.repoPolicy;
    
    if ( repoType == 'proxy' &&! receivedData.remoteStorage.remoteStorageUrl
        .match( REPO_REMOTE_STORAGE_REGEXP ) ) {
      var rsUrl = this.form.findField( 'remoteStorage.remoteStorageUrl' );
      rsUrl.disable();
      rsUrl.clearInvalid();
      
      // Disable the editor - this is a temporary measure,
      // until we find a better solution for procurement repos
      this.buttons[0].disable();
    }
  }
} );

Sonatype.repoServer.VirtualRepositoryEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    dataModifiers: {
      load: {
        syncAtStartup: Sonatype.utils.capitalize
      },
      submit: { 
        syncAtStartup: Sonatype.utils.convert.stringContextToBool
      }
    },
    referenceData: Sonatype.repoServer.referenceData.repositoryState.virtual
  };
  Ext.apply( this, config, defaultConfig );

  var ht = Sonatype.repoServer.resources.help.repos;
  
  this.COMBO_WIDTH = 300;

  this.tfStore = new Ext.data.SimpleStore( {
    fields: ['value'],
    data: [['True'], ['False']] 
  } );

  this.repoStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    fields: [
      { name: 'id' },
      { name: 'name', sortType: Ext.data.SortTypes.asUCString }
    ],
    sortInfo: { field: 'name', direction: 'asc' },
    url: Sonatype.config.repos.urls.repositories,
    autoLoad: true
  } );

  this.providerStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'provider',
    fields: [
      { name: 'description', sortType:Ext.data.SortTypes.asUCString },
      { name: 'format' },
      { name: 'provider' }
    ],
    sortInfo: { field: 'description', direction: 'asc' },
    url: Sonatype.config.repos.urls.shadowRepoTypes,
    autoLoad: true
  } );

  this.checkPayload();

  Sonatype.repoServer.VirtualRepositoryEditor.superclass.constructor.call( this, {
    items: [
      {
        xtype: 'textfield',
        fieldLabel: 'Repository ID',
        itemCls: 'required-field',
        helpText: ht.id,
        name: 'id',
        width: 200,
        allowBlank: false,
        disabled: ! this.isNew,
        validator: Sonatype.utils.validateId
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Repository Name',
        itemCls: 'required-field',
        helpText: ht.name,
        name: 'name',
        width: 200,
        allowBlank: false
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Repository Type',
        itemCls: 'required-field',
        helpText: ht.repoType,
        name: 'repoType',
        width: 100,
        disabled: true,
        allowBlank: false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Provider',
        itemCls: 'required-field',
        helpText: ht.provider,
        name: 'provider',
        width: 150,
        store: this.providerStore,
        displayField: 'description',
        valueField: 'provider',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select...',
        selectOnFocus: true,
        allowBlank: false,
        disabled: ! this.isNew,
        listeners: {
          select: this.providerSelectHandler,
          scope: this
        }
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Format',
        itemCls: 'required-field',
        helpText: ht.format,
        name: 'format',
        width: 100,
        disabled: true,
        allowBlank: false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Source Nexus Repository ID',
        itemCls: 'required-field',
        helpText: ht.shadowOf,
        name: 'shadowOf',
        width: 200,
        midWidth: 200,
        store: this.repoStore,
        displayField: 'name',
        valueField: 'id',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select...',
        selectOnFocus: true,
        allowBlank: false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Synchronize on Startup',
        itemCls: 'required-field',
        helpText: ht.syncAtStartup,
        name: 'syncAtStartup',
        width: 75,
        store: this.tfStore,
        displayField: 'value',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select...',
        selectOnFocus: true,
        allowBlank: false          
      }
    ]
  } );
};

Ext.extend( Sonatype.repoServer.VirtualRepositoryEditor, Sonatype.repoServer.AbstractRepositoryEditor, {
} );

Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec ) {
  var sp = Sonatype.lib.Permissions;
  
  var repoEditors = {
    hosted: Sonatype.repoServer.HostedRepositoryEditor,
    proxy: Sonatype.repoServer.ProxyRepositoryEditor,
    virtual: Sonatype.repoServer.VirtualRepositoryEditor
  };

  var editor = repoEditors[rec.data.repoType];
  
  if ( editor && sp.checkPermission( 'nexus:repositories', sp.READ ) &&
      ( sp.checkPermission( 'nexus:repositories', sp.CREATE ) ||
        sp.checkPermission( 'nexus:repositories', sp.DELETE ) ||
        sp.checkPermission( 'nexus:repositories', sp.EDIT ) ) ) {
    cardPanel.add( new editor( {
      tabTitle: 'Configuration',
      payload: rec 
    } ) );
  }
} );

Sonatype.Events.addListener( 'repositoryAddMenuInit', function( menu ) {
  var sp = Sonatype.lib.Permissions;
  
  if ( sp.checkPermission( 'nexus:repositories', sp.CREATE ) ) {
    var createRepoFunc = function( container, rec, item, e ) {
      rec.beginEdit();
      rec.set( 'repoType', item.value );
      rec.commit();
      rec.endEdit();
    };

    menu.add( [
      '-',
      {
        text: 'Hosted Repository',
        value: 'hosted',
        autoCreateNewRecord: true,
        handler: createRepoFunc
      },
      {
        text: 'Proxy Repository',
        value: 'proxy',
        autoCreateNewRecord: true,
        handler: createRepoFunc
      },
      {
        text: 'Virtual Repository',
        value: 'virtual',
        autoCreateNewRecord: true,
        handler: createRepoFunc
      }
    ] );
  }
} );
