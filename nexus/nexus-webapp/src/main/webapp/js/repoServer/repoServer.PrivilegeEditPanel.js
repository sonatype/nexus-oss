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

  this.typeStore = new Ext.data.SimpleStore( {
    fields: ['value', 'display'], 
    data: [
      ['target', 'Repository Target'],
      ['method', 'Application']
    ]
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
    dataStores: [this.groupStore, this.repoStore, this.targetStore],
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
        width: 100
      },
      { name: 'description' },
      { name: 'repositoryTargetId' },
      { 
        name: 'sTarget', 
        mapping: 'repositoryTargetId', 
        convert: this.convertTarget.createDelegate( this ),
        header: 'Target',
        width: 120
      },
      { name: 'repositoryId' },
      { name: 'repositoryGroupId' },
      { 
        name: 'sRepository', 
        mapping: 'repositoryId', 
        convert: this.convertRepository.createDelegate( this ),
        header: 'Repository',
        width: 150
      },
      { 
        name: 'method',
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
};

Ext.extend( Sonatype.repoServer.PrivilegeEditPanel, Sonatype.panels.GridViewer, {
  convertRepository: function( value, parent ) {
    if ( parent.repositoryId ) {
      return this.convertDataValue( parent.repositoryId, this.repoStore, 'id', 'name' );
    }
    else if ( parent.repositoryGroupId ) {
      return this.convertDataValue( parent.repositoryGroupId, this.groupStore, 'id', 'name' );
    }
    return '';
  },

  convertTarget: function( value, parent ) {
    return this.convertDataValue( value, this.targetStore, 'id', 'name' );
  },

  convertType: function( value, parent ) {
    return this.convertDataValue( value, this.typeStore, 'value', 'display' );
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
      typeStore: this.typeStore,
      repoStore: this.repoStore,
      targetStore: this.targetStore,
      groupStore: this.groupStore
    } );
    editor.on( 'submit', this.submitHandler, this );

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
        repositoryId: function( value, parent, fpanel ) {
          if ( value ) {
            fpanel.form.findField( 'repositoryOrGroup' ).setValue( 'repo_' + value );
          }
        },
        repositoryGroupId: function( value, parent, fpanel ) {
          if ( value ) {
            fpanel.form.findField( 'repositoryOrGroup' ).setValue( 'group_' + value );
          }
        },
        id: function( value, parent, fpanel ) {
          if ( parent.type == 'target' &&
              ! ( parent.repositoryId || parent.repositoryGroupId ) ) {
            fpanel.form.findField( 'repositoryOrGroup' ).setValue( 'all_repo' );
          }
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
      itemCls: 'required-field',
      helpText: ht.name,
      name: 'name',
      allowBlank: false,
      width: this.COMBO_WIDTH,
      disabled: this.readOnly
    },
    {
      xtype: 'textfield',
      fieldLabel: 'Description',
      itemCls: 'required-field',
      helpText: ht.description,
      name: 'description',
      allowBlank: false,
      width: this.COMBO_WIDTH,
      disabled: this.readOnly
    },
    {
      xtype: 'combo',
      fieldLabel: 'Type',
      itemCls: 'required-field',
      helpText: ht.type,
      name: 'type',
      store: this.typeStore,
      displayField: 'display',
      valueField: 'value',
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
  ];
  
  if ( this.payload.data.type == 'target' ) {

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
      disabled: this.readOnly,
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
      width: this.COMBO_WIDTH,
      disabled: this.readOnly
    } );
  }
  
  if ( ! this.isNew ) {
    items.push( {
      xtype: 'textfield',
      fieldLabel: 'Method',
      itemCls: 'required-field',
      helpText: '',
      name: 'method',
      allowBlank: false,
      width: this.COMBO_WIDTH,
      disabled: true
    } );
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
