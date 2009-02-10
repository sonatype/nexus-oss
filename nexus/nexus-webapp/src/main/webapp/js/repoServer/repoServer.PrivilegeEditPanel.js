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
 * Privilege Edit/Create panel layout and controller
 */
  
Sonatype.repoServer.PrivilegeEditPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Privileges'
  };
  Ext.apply( this, config, defaultConfig );

  this.sp = Sonatype.lib.Permissions;
  
  this.propertyTypeStore = new Ext.data.SimpleStore( {
    fields: [{name: 'type'},{name: 'converter'}],
    data: [['repository',this.convertRepositoryProperty.createDelegate( this )],
           ['repogroup',this.convertRepoGroupProperty.createDelegate( this )],
           ['repotarget',this.convertRepoTargetProperty.createDelegate( this )],
           ['string',this.convertStringProperty.createDelegate( this )]] 
  });
  
  this.privilegeTypeStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    fields: [
      { name: 'id' },
      { name: 'name' },
      { name: 'properties' }
    ],
    url: Sonatype.config.repos.urls.privilegeTypes
  } );

  this.groupStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    fields: [
      { name: 'id' },
      { name: 'format' },
      { name: 'name', sortType: Ext.data.SortTypes.asUCString }
    ],
    sortInfo: { field: 'name', direction: 'asc' },
    url: Sonatype.config.repos.urls.groups
  } );

  this.repoStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    fields: [
      { name: 'id' },
      { name: 'format' },
      { name: 'repoType' },
      { name: 'name', sortType: Ext.data.SortTypes.asUCString }
    ],
    sortInfo: { field: 'name', direction: 'asc' },
    url: Sonatype.config.repos.urls.repositories
  } );
  
  this.targetStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    fields: [
      { name: 'id' },
      { name: 'contentClass' },
      { name: 'name', sortType: Ext.data.SortTypes.asUCString }
    ],
    sortInfo: { field: 'name', direction: 'asc' },
    url: Sonatype.config.repos.urls.repoTargets
  } );

  Sonatype.Events.on( 'privilegeAddMenuInit', this.onAddMenuInit, this );
  Sonatype.Events.on( 'privilegeViewInit', this.onViewInit, this );
  
  Sonatype.repoServer.PrivilegeEditPanel.superclass.constructor.call( this, {
    addMenuInitEvent: 'privilegeAddMenuInit',
    rowClickEvent: 'privilegeViewInit',
    deleteButton: this.sp.checkPermission( 'nexus:privileges', this.sp.DELETE ),
    url: Sonatype.config.repos.urls.privileges,
    dataStores: [this.privilegeTypeStore, this.groupStore, this.repoStore, this.targetStore],
    columns: [
      { name: 'resourceURI' },
      { name: 'id' },
      { 
        name: 'name', 
        sortType: Ext.data.SortTypes.asUCString,
        header: 'Name',
        autoExpand: true
      },
      { 
        name: 'userManaged',
        header: 'User Managed',
        width: 80
      },
      { name: 'type' },
      {
        name: 'sType',
        mapping: 'type',
        convert: this.convertType.createDelegate( this ),
        header: 'Type',
        width: 125
      },
      { name: 'description' },
      { name: 'properties' },
      { 
        name: 'sTarget', 
        mapping: 'properties', 
        convert: this.convertTarget.createDelegate( this ),
        header: 'Target',
        width: 120
      },
      { 
        name: 'sRepository', 
        mapping: 'properties', 
        convert: this.convertRepository.createDelegate( this ),
        header: 'Repository',
        width: 150
      },
      { 
        name: 'sMethod', 
        mapping: 'properties', 
        convert: this.convertMethod.createDelegate( this ),
        header: 'Method',
        width: 150
      }
    ],
    listeners: {
      beforedestroy: {
        fn: function(){
          Sonatype.Events.un( 'privilegeAddMenuInit', this.onAddMenuInit, this );
          Sonatype.Events.un( 'privilegeViewInit', this.onViewInit, this );
        },
        scope: this
      }
    }
  } );  
  
  Sonatype.Events.fireEvent( 'privilegePanelInit', this );
};

Ext.extend( Sonatype.repoServer.PrivilegeEditPanel, Sonatype.panels.GridViewer, {
  convertRepository: function( value, parent ) {
    var targetPriv = false;
    for ( var i = 0; i < parent.properties.length; i++){
      if ( parent.properties[i].key == 'repositoryId'
        && !Ext.isEmpty(parent.properties[i].value) ){
        if ( '*' == parent.properties[i].value){
          return 'All Repositories';
        }
        else {
          return this.convertDataValue( parent.properties[i].value, this.repoStore, 'id', 'name' );
        }
      }
      else if ( parent.properties[i].key == 'repositoryGroupId'
        && !Ext.isEmpty(parent.properties[i].value) ){
        if ( '*' == parent.properties[i].value){
          return 'All Repository Groups';
        }
        else {
          return this.convertDataValue( parent.properties[i].value, this.groupStore, 'id', 'name' );
        }
      }
      else if ( parent.properties[i].key == 'repositoryTargetId' ){
        targetPriv = true;
      }
    }
    
    if ( targetPriv ){
      return 'All Repositories';
    }
    else {
      return '';
    }
  },

  convertTarget: function( value, parent ) {
    for ( var i = 0; i < parent.properties.length; i++){
      if ( parent.properties[i].key == 'repositoryTargetId'
        && !Ext.isEmpty(parent.properties[i].value) ){
        return this.convertDataValue( parent.properties[i].value, this.targetStore, 'id', 'name' );
      }
    }
    return '';
  },

  convertType: function( value, parent ) {
    return this.convertDataValue( value, this.privilegeTypeStore, 'id', 'name' );
  },
  
  convertMethod: function( value, parent ) {
    for ( var i = 0; i < parent.properties.length; i++){
      if ( parent.properties[i].key == 'method'
        && !Ext.isEmpty(parent.properties[i].value) ){
        return parent.properties[i].value;
      }
    }
    return '';
  },
  
  convertStringProperty: function( value, parent ) {
    return value;
  },
  
  convertRepositoryProperty: function( value, parent ) {
    if ( Ext.isEmpty( value ) ){
      for ( var i = 0; i < parent.length; i++ ){
        if ( parent[i].key == 'repositoryGroupId'
          && !Ext.isEmpty(parent[i].value) ){
          return '';
        }
      }
      return 'All Repositories';
    }
    else if ( '*' == value ){
      return 'All Repositories';
    }
    return this.convertDataValue( value, this.repoStore, 'id', 'name' );
  },
  
  convertRepoGroupProperty: function( value, parent ) {
    if ( !Ext.isEmpty( value ) 
        && '*' == value ) {
      return 'All Repository Groups';
    }
    else if ( !Ext.isEmpty( value ) ){
      return this.convertDataValue( value, this.groupStore, 'id', 'name' );
    }
    return '';
  },
  
  convertRepoTargetProperty: function( value, parent ) {
    return this.convertDataValue( value, this.targetStore, 'id', 'name' );
  },
  
  onAddMenuInit: function( menu ) {
    if ( this.sp.checkPermission( 'nexus:privileges', this.sp.CREATE ) ) {
      menu.add( [
        '-',
        {
          text: 'Repository Target Privilege',
          autoCreateNewRecord: true,
          handler: function( container, rec, item, e ) {
            rec.beginEdit();
            rec.set( 'type', 'target' );
            rec.set( 'sType', this.convertType( 'target' ) );
            rec.commit();
            rec.endEdit();
          },
          scope: this
        }
      ] );
    }
  },

  onViewInit: function( cardPanel, rec ) {
    var editor = new Sonatype.repoServer.PrivilegeEditor( {
      payload: rec,
      privilegeTypeStore: this.privilegeTypeStore,
      repoStore: this.repoStore,
      targetStore: this.targetStore,
      groupStore: this.groupStore,
      propertyTypeStore: this.propertyTypeStore
    } );
    editor.on( 'submit', this.submitHandler, this );

    Sonatype.Events.fireEvent( 'privilegeEditorInit', editor );
    
    cardPanel.add( editor );
  },
  
  submitHandler: function( form, action, receivedData ) {
    if ( Ext.isArray( receivedData ) ) {
      for ( var i = 0; i < receivedData.length; i++ ) {
        var r = receivedData[i];
        r.sTarget = this.convertTarget( r.repositoryTargetId, r );
        r.sType = this.convertType( r.type, r );
        r.sRepository = this.convertRepository( r.repositoryId, r );
      }
    }
  }
} );


Sonatype.repoServer.PrivilegeEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    uri: Sonatype.config.repos.urls.privileges,
    dataModifiers: {
      load: {
        properties: function( value, parent, fpanel ) {
          for ( var i = 0; i < value.length; i++ ){
            var field = fpanel.form.findField( value[i].key );
            field.setValue( fpanel.propertyTypeStore.getAt( fpanel.propertyTypeStore.find('type', field.fieldConverterType) ).data.converter(value[i].value, value) );
          }
        },
        type: function( value, parent, fpanel ) {
          return fpanel.convertDataValue( value, fpanel.privilegeTypeStore, 'id', 'name' );
        }
      },
      submit: { 
        method: function( val, fpanel ) {
          return ['create', 'read', 'update', 'delete'];
        },
        repositoryId: function( val, fpanel ) {
          var v = fpanel.form.findField( 'repositoryOrGroup' ).getValue();
          return v.indexOf( 'repo_' ) == 0 ? v.substring( 'repo_'.length ) : '';
        },
        repositoryGroupId: function( val, fpanel ) {
          var v = fpanel.form.findField( 'repositoryOrGroup' ).getValue();
          return v.indexOf( 'group_' ) == 0 ? v.substring( 'group_'.length ) : '';
        }
      }
    },
    validationModifiers: {
      repositoryId: "repositoryOrGroup",
      repositoryGroupId: "repositoryOrGroup"
    },
    referenceData: Sonatype.repoServer.referenceData.privileges.target
  };
  Ext.apply( this, config, defaultConfig );

  var ht = Sonatype.repoServer.resources.help.privileges;
  this.COMBO_WIDTH = 300;
  this.sp = Sonatype.lib.Permissions;
  
  this.combinedStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    fields: [
      { name: 'id' },
      { name: 'format' },
      { name: 'name', sortType: Ext.data.SortTypes.asUCString }
    ],
    url: Sonatype.config.repos.urls.repositories
  } );
  this.initCombinedStore();

  this.checkPayload();
  if ( ! ( this.sp.checkPermission( 'nexus:privileges', this.sp.UPDATE ) || this.isNew ) ) {
    this.readOnly = true;
  }
  
  var items = [
    {
      xtype: 'hidden',
      name: 'id'
    },
    {
      xtype: 'textfield',
      fieldLabel: 'Name',
      itemCls: this.readOnly ? '' : 'required-field',
      helpText: ht.name,
      name: 'name',
      allowBlank: false,
      width: this.COMBO_WIDTH,
      disabled: this.readOnly
    },
    {
      xtype: 'textfield',
      fieldLabel: 'Description',
      itemCls: this.readOnly ? '' : 'required-field',
      helpText: ht.description,
      name: 'description',
      allowBlank: false,
      width: this.COMBO_WIDTH,
      disabled: this.readOnly
    }
  ];
  
  if ( this.isNew ) {
    items.push(
      {
        xtype: 'combo',
        fieldLabel: 'Type',
        itemCls: 'required-field',
        helpText: ht.type,
        name: 'type',
        store: this.privilegeTypeStore,
        displayField: 'name',
        valueField: 'id',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select...',
        selectOnFocus: true,
        allowBlank: false,
        width: this.COMBO_WIDTH,
        value: 'target',
        lazyInit: false,
        disabled: true
      }
    );
    
    // clone the target store
    var targetStore2 = new Ext.data.JsonStore( {
      root: 'data',
      id: 'id',
      fields: [
        { name: 'id' },
        { name: 'contentClass' },
        { name: 'name', sortType: Ext.data.SortTypes.asUCString }
      ],
      url: Sonatype.config.repos.urls.repoTargets
    } );
    targetStore2.add( this.targetStore.getRange() );
    this.targetStore = targetStore2;

    items.push( {
      xtype: 'combo',
      fieldLabel: 'Repository',
      itemCls: 'required-field',
      helpText: ht.repositoryOrGroup,
      name: 'repositoryOrGroup',
      store: this.combinedStore,
      displayField:'name',
      valueField:'id',
      editable: false,
      forceSelection: true,
      mode: 'local',
      triggerAction: 'all',
      emptyText:'Select...',
      selectOnFocus:true,
      allowBlank: false,
      width: this.COMBO_WIDTH,
      minListWidth: this.COMBO_WIDTH,
      value: "all_repo",
      listeners: {
        select: {
          fn: this.repositorySelectHandler,
          scope: this
        }
      }
    } );
    items.push( {
      xtype: 'combo',
      fieldLabel: 'Repository Target',
      itemCls: 'required-field',
      helpText: ht.repositoryTarget,
      name: 'repositoryTargetId',
      store: this.targetStore,
      displayField:'name',
      valueField:'id',
      editable: false,
      forceSelection: true,
      mode: 'local',
      triggerAction: 'all',
      emptyText:'Select...',
      selectOnFocus:true,
      allowBlank: false,
      width: this.COMBO_WIDTH
    } );
  }
  else {
    items.push( {
      xtype: 'textfield',
      fieldLabel: 'Type',
      helpText: ht.type,
      name: 'type',
      width: this.COMBO_WIDTH,
      disabled: true
    } );
    
    var typeRec = this.privilegeTypeStore.getById(this.payload.data.type);
    
    if ( !Ext.isEmpty(typeRec) ) {
      for ( var i = 0; i < typeRec.data.properties.length; i++){
        items.push( {
          xtype: 'textfield',
          fieldConverterType: typeRec.data.properties[i].type,
          fieldLabel: typeRec.data.properties[i].name,
          helpText: typeRec.data.properties[i].helpText,
          name: typeRec.data.properties[i].id,
          width: this.COMBO_WIDTH,
          disabled: true
        } );
      }
    }
  }
  
  Sonatype.repoServer.PrivilegeEditor.superclass.constructor.call( this, {
	labelWidth: 120,
    items: items
  } );
};

Ext.extend( Sonatype.repoServer.PrivilegeEditor, Sonatype.ext.FormPanel, {
  initCombinedRecord: function( rec ) {
    var isGroup = rec.data.repoType == null;
    return {
      id: ( isGroup ? 'group_' : 'repo_' ) + rec.data.id,
      name: rec.data.name + ( isGroup ? ' (Group)' : ' (Repo)' ),
      format: rec.data.format
    };
  },
  
  initCombinedStore: function() {
    var data = [
      {
        id: 'all_repo',
        name: 'All Repositories'
      }
    ];

    this.repoStore.each( function( rec ) { 
      data.push( this.initCombinedRecord( rec ) );
    }, this );
    this.groupStore.each( function( rec ) { 
      data.push( this.initCombinedRecord( rec ) );
    }, this );

    this.combinedStore.loadData( { data: data } );
  },
  
  repositorySelectHandler: function( combo, rec, index ) {
    var targetCombo = this.form.findField( 'repositoryTargetId' );
    targetCombo.setValue( null );
    targetCombo.store.clearFilter();

    var filterValue = rec.data.format;
    if ( filterValue ) {
      targetCombo.store.filter( 'contentClass', filterValue );
    }
  }
} );
