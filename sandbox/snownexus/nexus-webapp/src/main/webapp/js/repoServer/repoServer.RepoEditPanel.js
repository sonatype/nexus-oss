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
        scope: this,
        success: this.templateLoadSuccess.createDelegate(this)
      } );
    }
    else {
      Sonatype.repoServer.AbstractRepositoryEditor.superclass.loadData.call( this );
    }
  },
  
  providerSelectHandler: function( combo, rec, index ) {
    this.form.findField( 'format' ).setValue( rec.data.format );
    this.form.findField( 'providerRole' ).setValue( rec.data.providerRole );
    this.afterProviderSelectHandler( combo, rec, index);
  },
  
  templateLoadSuccess: function( form, action ) {},
  
  afterProviderSelectHandler: function( combo, rec, index ) {},

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
      
      // filter the Deploy Policy combo
      this.updateWritePolicy();
      
      this.lastPolicy = repoPolicy;
    }
  },
  
  repoPolicySubmitDataModifier: function() {
  	
  },

  submitHandler: function( form, action, receivedData ) {
    if ( this.isNew ) {
      if ( ! receivedData.resourceURI ) {
        receivedData.resourceURI =
          Sonatype.config.host + Sonatype.config.repos.urls.repositories + '/' + receivedData.id;
        if ( receivedData.exposed == null ) {
          receivedData.exposed = true;
        }
        if ( receivedData.userManaged == null ) {
          receivedData.userManaged = true;
        }
      }
      
      var repoPanel = Ext.getCmp('view-repositories');
      repoPanel.statusStart();
      
      // convert case
      receivedData.repoPolicy = Sonatype.utils.upperFirstCharLowerRest( receivedData.repoPolicy );
      
      return;
    }
    
    var rec = this.payload;
    rec.beginEdit();
    rec.set( 'name', receivedData.name );
    rec.set( 'repoType', receivedData.repoType );
    rec.set( 'format', receivedData.format );
    rec.set( 'repoPolicy', Sonatype.utils.upperFirstCharLowerRest( receivedData.repoPolicy ) );	
    rec.commit();
    rec.endEdit();
    
  },
  
    //@override
  addSorted : function(store, rec) {
  // make sure listing group first, then the repositories sorted
  var insertIndex = store.getCount();
  for (var i=0 ; i < store.getCount() ; i++) {
    var tempRec = store.getAt(i);
    if (tempRec.get('repoType') == 'group') {
      continue;
    }
    if (tempRec.get('name').toLowerCase() > rec.get('name').toLowerCase() ) {
      insertIndex = i;
      break;
    }
  }
  // hack the policy
  if ( rec.get('repoPolicy') && rec.get('repoPolicy') == 'MIXED'  ) {
  	rec.beginEdit();
  	rec.set('repoPolicy', '');
  	rec.commit();
  	rec.endEdit();
  }
  
  store.insert( insertIndex, [rec] );
  }
  
} );

Sonatype.repoServer.HostedRepositoryEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    dataModifiers: {
      load: {
        repoPolicy: Sonatype.utils.upperFirstCharLowerRest,
        browseable: Sonatype.utils.capitalize,
        indexable: Sonatype.utils.capitalize,
        exposed: Sonatype.utils.capitalize
      },
      submit: { 
        repoPolicy: Sonatype.utils.uppercase,
        browseable: Sonatype.utils.convert.stringContextToBool,
        indexable: Sonatype.utils.convert.stringContextToBool,
        exposed: Sonatype.utils.convert.stringContextToBool,
        downloadRemoteIndexes: function() { return false; },
        checksumPolicy: function() { return 'IGNORE'; }
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
  
  this.writePolicyStore = new Ext.data.SimpleStore( {
    fields:['value', 'display'], 
    data:[
      ['ALLOW_WRITE', 'Allow Redeploy'],
      ['ALLOW_WRITE_ONCE', 'Disable Redeploy'],
      ['READ_ONLY', 'Read Only']
    ]
  } );
  
  this.providerStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'provider',
    fields: [
      { name: 'description', sortType:Ext.data.SortTypes.asUCString },
      { name: 'format' },
      { name: 'providerRole' },
      { name: 'provider' }
    ],
    sortInfo: { field: 'description', direction: 'asc' },
    url: Sonatype.config.repos.urls.repoTypes + '?repoType=hosted',
    autoLoad: true
  } );

  this.checkPayload();

  Sonatype.repoServer.HostedRepositoryEditor.superclass.constructor.call( this, {
    dataStores: [
      this.providerStore
    ],
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
        xtype: 'hidden',
        name: 'providerRole'
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
            fieldLabel: 'Deployment Policy',
            helpText: ht.writePolicy,
            name: 'writePolicy',
            store: this.writePolicyStore,
            displayField: 'display',
            valueField: 'value',
            width: 120,
            listWidth: 120,
            lastQuery: ''
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
          },
          {
              fieldLabel: 'Publish URL',
              helpText: ht.exposed,
              name: 'exposed'
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
  
  this.on( 'show', this.showHandler, this );
};

Ext.extend( Sonatype.repoServer.HostedRepositoryEditor, Sonatype.repoServer.AbstractRepositoryEditor, {
  afterProviderSelectHandler: function( combo, rec, index ) {
    this.updateIndexableCombo(rec.data.format);
  },
  
  showHandler: function ( panel ) {
    var formatField = this.form.findField('format');
    if ( formatField ){
      this.updateIndexableCombo( formatField.getValue() );
    }
    
    this.updateWritePolicy();
  },
  
  updateIndexableCombo: function( repoFormat ){
    var indexableCombo = this.form.findField('indexable');
    if ( repoFormat == 'maven2'){
      indexableCombo.enable();      
    }
    else{
      indexableCombo.setValue('False');
      indexableCombo.disable();       
    }
  },
  updateWritePolicy: function(){
  
      var repoPolicyField = this.find( 'name', 'repoPolicy' )[0];
      var repoPolicy = repoPolicyField.getValue();
  
      var writePolicyField = this.find( 'name', 'writePolicy' )[0];
      
      // filter out the redeploy option for SNAPSHOT repos
      if( ("" + repoPolicy).toLowerCase() == 'snapshot' )
      {
        // first change the value if it is ALLOW_WRITE_ONCE
        if( writePolicyField.getValue() != 'READ_ONLY' )
        {
        	writePolicyField.setValue('ALLOW_WRITE');
        }
        
      	this.writePolicyStore.filterBy(function(rec, id)
      	{
      	  return rec.data.value != 'ALLOW_WRITE_ONCE';
      	});
      }
      else
      {
        writePolicyField.store.clearFilter();
      }
  }
} );

Sonatype.repoServer.ProxyRepositoryEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    dataModifiers: {
      load: {
        repoPolicy: Sonatype.utils.upperFirstCharLowerRest,
        browseable: Sonatype.utils.capitalize,
        indexable: Sonatype.utils.capitalize,
        exposed: Sonatype.utils.capitalize,
        downloadRemoteIndexes: Sonatype.utils.capitalize,
        checksumPolicy: Sonatype.utils.upperFirstCharLowerRest
      },
      submit: { 
        repoPolicy: Sonatype.utils.uppercase,
        browseable: Sonatype.utils.convert.stringContextToBool,
        indexable: Sonatype.utils.convert.stringContextToBool,
        exposed: Sonatype.utils.convert.stringContextToBool,
        downloadRemoteIndexes: Sonatype.utils.convert.stringContextToBool,
        checksumPolicy: Sonatype.utils.uppercase
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
    fields: ['label', 'value'], 
    data: [['Ignore', 'IGNORE'], ['Warn', 'WARN'], ['StrictIfExists', 'STRICT_IF_EXISTS'], ['Strict', 'STRICT']]
  } );

  this.providerStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'provider',
    fields: [
      { name: 'description', sortType:Ext.data.SortTypes.asUCString },
      { name: 'format' },
      { name: 'providerRole' },
      { name: 'provider' }
    ],
    sortInfo: { field: 'description', direction: 'asc' },
    url: Sonatype.config.repos.urls.repoTypes + '?repoType=proxy',
    autoLoad: true
  } );

  this.checkPayload();

  Sonatype.repoServer.ProxyRepositoryEditor.superclass.constructor.call( this, {
    dataStores: [
      this.providerStore
    ],
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
        xtype: 'hidden',
        name: 'providerRole'
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
            emptyText: 'http://some-remote-repository/repo-root',
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
            displayField: 'label',
            valueField: 'value',
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
          },
          {
              fieldLabel: 'Publish URL',
              helpText: ht.exposed,
              name: 'exposed'
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
  
  this.on( 'show', this.showHandler, this );
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
  },
  
  afterProviderSelectHandler: function( combo, rec, index ) {
    this.updateDownloadRemoteIndexCombo(rec.data.format);
    this.updateIndexableCombo(rec.data.format);
  },
  
  showHandler: function ( panel ) {
    var formatField = this.form.findField('format');
    if ( formatField ){
      this.updateDownloadRemoteIndexCombo( formatField.getValue() );
      this.updateIndexableCombo( formatField.getValue() );
    }
  },
  
  updateDownloadRemoteIndexCombo: function( repoFormat ){
    var downloadRemoteIndexCombo = this.form.findField('downloadRemoteIndexes');
    if ( repoFormat == 'maven2'){
      downloadRemoteIndexCombo.enable();      
    }
    else{
      downloadRemoteIndexCombo.setValue('False');
      downloadRemoteIndexCombo.disable();       
    }
  },
  
  updateIndexableCombo: function( repoFormat ){
    var indexableCombo = this.form.findField('indexable');
    if ( repoFormat == 'maven2'){
      indexableCombo.enable();      
    }
    else{
      indexableCombo.setValue('False');
      indexableCombo.disable();       
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
        syncAtStartup: Sonatype.utils.convert.stringContextToBool,
        exposed: function() { return true; }
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
      { name: 'name', sortType: Ext.data.SortTypes.asUCString },
      { name: 'format'},
      { name: 'repoType'}
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
      { name: 'providerRole' },
      { name: 'provider' }
    ],
    sortInfo: { field: 'description', direction: 'asc' },
    url: Sonatype.config.repos.urls.repoTypes + '?repoType=shadow',
    autoLoad: true
  } );

  this.checkPayload();

  Sonatype.repoServer.VirtualRepositoryEditor.superclass.constructor.call( this, {
    dataStores: [
      this.providerStore,
      this.repoStore
    ],
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
        xtype: 'hidden',
        name: 'providerRole'
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
        allowBlank: false,
        // this config can solve the problem of 'filter is not applied the first time'
        lastQuery: ''
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
  
  templateLoadSuccess: function( form, action ) {
    var rec = {
      data: {
        provider: this.find( 'name', 'provider' )[0].getValue() 
      }
    };
    
    this.afterProviderSelectHandler( null, rec, null );
  },
  afterProviderSelectHandler: function( combo, rec, index ) {
    var provider = rec.data.provider;
    var sourceRepoCombo = this.form.findField('shadowOf');
    sourceRepoCombo.clearValue();
    sourceRepoCombo.focus();
    if ( provider == 'm1-m2-shadow'){
      sourceRepoCombo.store.filterBy( 
        function fn(rec, id){
          if ( rec.data.repoType != 'virtual' && rec.data.format == 'maven1'){
            return true;
          }
          return false;
        }
      );
    }
    else if ( provider == 'm2-m1-shadow'){
      sourceRepoCombo.store.filterBy( 
        function fn(rec, id){
          if ( rec.data.repoType != 'virtual' && rec.data.format == 'maven2'){
            return true;
          }
          return false;
        }
      );
    }
  }
} );

Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec ) {
  var sp = Sonatype.lib.Permissions;
  
  var repoEditors = {
    hosted: Sonatype.repoServer.HostedRepositoryEditor,
    proxy: Sonatype.repoServer.ProxyRepositoryEditor,
    virtual: Sonatype.repoServer.VirtualRepositoryEditor
  };

  var editor = repoEditors[rec.data.repoType];
  
  if ( editor && rec.data.userManaged && sp.checkPermission( 'nexus:repositories', sp.READ ) &&
      ( sp.checkPermission( 'nexus:repositories', sp.CREATE ) ||
        sp.checkPermission( 'nexus:repositories', sp.DELETE ) ||
        sp.checkPermission( 'nexus:repositories', sp.EDIT ) ) ) {
    cardPanel.add( new editor( {
      tabTitle: 'Configuration',
      name: 'configuration',
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
      rec.set( 'exposed', true );
      rec.set( 'userManaged', true );
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
