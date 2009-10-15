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
 * Role Edit/Create panel layout and controller
 */

Sonatype.repoServer.RoleEditPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Roles'
  };
  Ext.apply( this, config, defaultConfig );

  this.sp = Sonatype.lib.Permissions;
  
  this.externalMappingStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'defaultRole.roleId',
    fields: [
      { name: 'defaultRole' },
      { name: 'mappedRoles' }
    ],
    url: Sonatype.config.repos.urls.externalRolesAll,
    autoLoad: false
  } );
  
  this.sourceStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'roleHint',
    autoLoad: false,
    url: Sonatype.config.repos.urls.userLocators,
    sortInfo: { field: 'description', direction: 'ASC' },
    fields: [
      { name: 'roleHint' },
      { name: 'description', sortType:Ext.data.SortTypes.asUCString }
    ],
    listeners: {
      load: {
        fn: function ( store, records, options ){
          for ( var i = 0; i < records.length; i++ ) {
            var rec = records[i];
            var v = rec.data.roleHint;
            if ( v == 'allConfigured' || v == 'mappedExternal' || v == 'default' ) {
              store.remove( rec );
            }
          }
          
          if ( this.sp.checkPermission( 'security:roles', this.sp.CREATE )
              && store.getCount() > 0 && this.toolbarAddButton.menu.items.length == 1) {
            this.toolbarAddButton.menu.add( {
              text: 'External Role Mapping',
              handler: this.mapExternalRoles,
              scope: this
            } );
          }
        },
        scope: this
      }
    }
  } );
  
  Sonatype.Events.on( 'roleAddMenuInit', this.onAddMenuInit, this );

  Sonatype.repoServer.RoleEditPanel.superclass.constructor.call( this, {
    addMenuInitEvent: 'roleAddMenuInit',
    deleteButton: this.sp.checkPermission( 'security:roles', this.sp.DELETE ),
    rowClickEvent: 'roleViewInit',
    url: Sonatype.config.repos.urls.roles,
    dataAutoLoad: true,
    dataId: 'id',
    dataBookmark: 'id',
    dataStores: [
      this.sourceStore,
      this.externalMappingStore
    ],
    columns: [
      { 
        name: 'name', 
        sortType: Ext.data.SortTypes.asUCString,
        header: 'Name',
        width: 200,
        renderer: function( value, meta, rec, index ) { 
          return rec.data.mapping ? ( '<b>' + value + '</b' ) : value;
        }
      },
      {
        name: 'id'
      },
      {
        name: 'resourceURI'
      },
      { 
        name: 'mapping',
        header: 'Mapping',
        width: 100,
        mapping: 'id', 
        convert: this.convertMapping.createDelegate( this )
      },
      { 
        name: 'sessionTimeout',
        header: 'Session Timeout',
        width: 100
      },
      { 
        name: 'description',
        header: 'Description',
        width: 175,
        autoExpand: true
      },
      {
        name: 'roles'
      },
      {
        name: 'privileges'
      },
      {
        name: 'userManaged'
      }
    ],
    listeners: {
      beforedestroy: {
        fn: function(){
          Sonatype.Events.un( 'roleAddMenuInit', this.onAddMenuInit, this );
        },
        scope: this
      }
    }
  } );
};


Ext.extend( Sonatype.repoServer.RoleEditPanel, Sonatype.panels.GridViewer, {
  convertMapping: function( value, parent ) {
    var mappingRec = this.externalMappingStore.getById( value );
    if ( mappingRec ) {
      var mappings = mappingRec.data.mappedRoles;
      var s = '';
      for ( var i = 0; i < mappings.length; i++ ) {
        if ( s ) s += ', ';
        s += mappings[i].source;
      }
      return s;
    }
    else {
      return '';
    }
  },
  onAddMenuInit: function( menu ) {
    menu.add( '-' );
    if ( this.sp.checkPermission( 'security:roles', this.sp.CREATE ) ) {
      menu.add( {
        text: 'Nexus Role',
        autoCreateNewRecord: true,
        handler: function( container, rec, item, e ) {
          rec.beginEdit();
          rec.set( 'source', 'default' );
          rec.commit();
          rec.endEdit();
        },
        scope: this
      } );
    }
  },
  
  mapExternalRoles: function() {
    new Sonatype.repoServer.ExternapRoleMappingPopup( {
      hostPanel: this,
      sourceStore: this.sourceStore
    } ).show();
  }
} );


Sonatype.repoServer.ExternapRoleMappingPopup = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Map External Role'
  };
  Ext.apply( this, config, defaultConfig );
  
  this.roleStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'roleId',
    fields: [
      { name: 'roleId' },
      { name: 'source' },
      { name: 'name', sortType: Ext.data.SortTypes.asUCString }
    ],
    sortInfo: { field: 'name', direction: 'asc' },
    url: Sonatype.config.repos.urls.plexusRolesAll,
    autoLoad: true
  } );

  Sonatype.repoServer.ExternapRoleMappingPopup.superclass.constructor.call( this, {
    closable: true,
    autoWidth: false,
    width: 400,
    autoHeight: true,
    modal: true,
    constrain: true,
    resizable: false,
    draggable: false,
    items: [
      {
        xtype: 'form',
        layoutConfig: {
          labelSeparator: ''
        },
        labelWidth: 60,
        frame: true,  
        defaultType: 'textfield',
        monitorValid: true,
        items:[
          { 
            xtype: 'combo',
            fieldLabel: 'Realm',
            itemCls: 'required-field',
            helpText: 'Security realm to select roles from.',
            name: 'source',
            anchor: Sonatype.view.FIELD_OFFSET,
            width: 200,
            store: this.sourceStore,
            displayField: 'description',
            valueField: 'roleHint',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText: 'Select...',
            selectOnFocus: true,
            allowBlank: false,
            listeners: {
              select: {
                fn: this.onSourceSelect,
                scope: this
              }
            }
          },
          { 
            xtype: 'combo',
            fieldLabel: 'Role',
            itemCls: 'required-field',
            helpText: 'External role to map.',
            name: 'roleId',
            anchor: Sonatype.view.FIELD_OFFSET,
            width: 200,
            store: this.roleStore,
            displayField: 'name',
            valueField: 'roleId',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            lastQuery:'',
            emptyText: 'Select...',
            selectOnFocus: true,
            allowBlank: false
          }
        ],
        buttons: [
          {
            text: 'Create Mapping',
            formBind: true,
            handler: this.createRoleMapping,
            scope: this,
            disabled: true
          },
          {
            text: 'Cancel',
            formBind: false,
            handler: function( button, e ) {
              this.close();
            },
            scope: this
          }
        ]
      }
    ]
  } );
};

Ext.extend( Sonatype.repoServer.ExternapRoleMappingPopup, Ext.Window, {
  onSourceSelect: function( combo, rec, index ) {
    var roleCombo = this.find( 'name', 'roleId' )[0];        
    roleCombo.clearValue();
    roleCombo.store.filter('source', rec.data.roleHint);
  },

 createRoleMapping: function( button, e ) {
    if ( this.hostPanel ) {
      var roleId = this.find( 'name', 'roleId' )[0].getValue();
      var roleRec = this.roleStore.getById( roleId );
      var sourceId = this.find( 'name', 'source' )[0].getValue();
      var handler = this.hostPanel.addActionHandler.createDelegate( this.hostPanel, [ function( rec, item, e ) {
          rec.beginEdit();
          rec.set( 'source', sourceId );
          rec.set( 'mapping', sourceId );
          rec.commit();
          rec.endEdit();
        },
        {
          autoCreateNewRecord: true,
          text: "Role Mapping"
        }],
        0
      );
      handler();
      
      var defaultData = {
        id: roleId,
        name: roleRec.data.name,
        description: 'External mapping for ' + roleRec.data.name + ' (' + sourceId + ')',
        sessionTimeout: 60        
      };
      
      this.hostPanel.cardPanel.getLayout().activeItem.find( 'name', 'id' )[0].disable();
      this.hostPanel.cardPanel.getLayout().activeItem.getComponent(0).presetData = defaultData;
      
      this.close();
    }
  }
} );

Sonatype.repoServer.DefaultRoleEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    uri: Sonatype.config.repos.urls.roles,
    labelWidth: 100,
    referenceData: Sonatype.repoServer.referenceData.roles,
    dataModifiers: {
      load: {
      },
      submit: { 
        roles : this.saveRolesTreeHelper.createDelegate(this),
        privileges : this.savePrivilegesTreeHelper.createDelegate(this)
      }
    },
    validationModifiers: {
      roles : this.treeValidationError.createDelegate(this),
      privileges : this.treeValidationError.createDelegate(this)
    }
  };
  Ext.apply( this, config, defaultConfig );
  
  this.roleDataStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    url: Sonatype.config.repos.urls.roles,
    sortInfo: { field: 'name', direction: 'ASC' },
    autoLoad: false,
    fields: [
      { name: 'id' },
      { name: 'name', sortType:Ext.data.SortTypes.asUCString }
    ],
    listeners: {
      load: {
        fn: function(){
          this.rolesLoaded = true;
          if ( this.privsLoaded == true ) {
            this.resetHandler();
          }
        },
        scope: this 
      }
    }
  } );
  
  this.privDataStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    url: Sonatype.config.repos.urls.privileges,
    sortInfo: { field: 'name', direction: 'ASC' },
    autoLoad: false,
    fields: [
      { name: 'id' },
      { name: 'name', sortType:Ext.data.SortTypes.asUCString }
    ],
    listeners: {
      load: {
        fn: function(){
          this.privsLoaded = true;
          if ( this.rolesLoaded == true ) {
            this.resetHandler();
          }
        },
        scope: this 
      }
    }
  } );

  var ht = Sonatype.repoServer.resources.help.roles;
  
  this.COMBO_WIDTH = 300;

  this.checkPayload();
  
  var items = [
    {
      name: 'internalResourceHeader',
      xtype: 'panel',
      layout: 'table',
      hidden: true,
      style: 'font-size: 18px; padding: 5px 0px 5px 15px',
      items: [{
          html: '<b>This is an internal Nexus resource which cannot be edited or deleted.</b><br><hr/>'
      }]
    },
    {
      xtype: 'textfield',
      fieldLabel: 'Role Id',
      itemCls: 'required-field',
      labelStyle: 'margin-left: 15px; width: 185px;',
      helpText: ht.id,
      name: 'id',
      allowBlank: false,
      width: this.COMBO_WIDTH
    },
    {
      xtype: 'textfield',
      fieldLabel: 'Name',
      itemCls: 'required-field',
      labelStyle: 'margin-left: 15px; width: 185px;',
      helpText: ht.name,
      name: 'name',
      allowBlank: false,
      width: this.COMBO_WIDTH
    },
    {
      xtype: 'textfield',
      fieldLabel: 'Description',
      labelStyle: 'margin-left: 15px; width: 185px;',
      helpText: ht.description,
      name: 'description',
      allowBlank: true,
      width: this.COMBO_WIDTH
    },
    {
      xtype: 'numberfield',
      fieldLabel: 'Session Timeout',
      itemCls: 'required-field',
      labelStyle: 'margin-left: 15px; width: 185px;',
      helpText: ht.sessionTimeout,
      name: 'sessionTimeout',
      allowBlank: false,
      width: this.COMBO_WIDTH,
      value: 60,
      validator: function( value ){
      // decimal values are not allowed
      return (value - 0) % 1 == 0 ? true : 'Only integer values are allowed';
      }
    },
    {
      xtype: 'panel',
      layout: 'table',
      layoutConfig: {
        columns: 3
      },
      autoHeight: true,
      style: 'padding: 10px 0 0 0',
      name: 'privileges',
      
      items: [
        {
          xtype: 'multiselecttreepanel',
          name: 'roles-privs-tree', //note: unique ID is assinged before instantiation
          title: 'Selected Roles / Privileges',
          cls: 'required-field',
          border: true, //note: this seem to have no effect w/in form panel
          bodyBorder: true, //note: this seem to have no effect w/in form panel
          //note: this style matches the expected behavior
          bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
          width: 400,
          height: 300,
          animate:true,
          lines: false,
          autoScroll:true,
          containerScroll: true,
          //@note: root node must be instantiated uniquely for each instance of treepanel
          //@ext: can TreeNode be registerd as a component with an xtype so this new root node
          //      may be instantiated uniquely for each form panel that uses this config?
          rootVisible: false,
          root: new Ext.tree.TreeNode({text: 'root'}),
          enableDD: true,
          ddScroll: true,
          dropConfig: {
            allowContainerDrop: true,
            onContainerDrop: function(source, e, data){
              if ( data.nodes ){
                for ( var i = 0 ; i < data.nodes.length ; i++ ){
                  this.tree.root.appendChild(data.nodes[i]);
                }
              }
              return true;
            },
            onContainerOver:function(source, e, data){return this.dropAllowed;},
            // passign padding to make whole treePanel the drop zone.  This is dependent
            // on a sonatype fix in the Ext.dd.DropTarget class.  This is necessary
            // because treepanel.dropZone.setPadding is never available in time to be useful.
            padding: [0,0,274,0]
          },
          // added Field values to simulate form field validation
          invalidText: 'One or more roles or privileges are required',
          validate: function(){
            return (this.root.childNodes.length > 0);
          },
          invalid: false,
          listeners: {
            'append' : {
              fn: function(tree, parentNode, insertedNode, i) {
                if (tree.invalid) {
                  //remove error messaging
                  tree.getEl().child('.x-panel-body').setStyle({
                    'background-color' : '#FFFFFF',
                    border : '1px solid #B5B8C8'
                  });
                  Ext.form.Field.msgFx['normal'].hide(tree.errorEl, tree);
                }
              },
              scope: this
            },
            'remove' : {
              fn: function(tree, parentNode, removedNode) {
                if(tree.root.childNodes.length < 1) {
                  this.markTreeInvalid(tree);
                }
                else if (tree.invalid) {
                  //remove error messaging
                  tree.getEl().child('.x-panel-body').setStyle({
                    'background-color' : '#FFFFFF',
                    border : '1px solid #B5B8C8'
                  });
                  Ext.form.Field.msgFx['normal'].hide(tree.errorEl, tree);
                }
              },
              scope: this
            }
          }
        },
        {
          xtype: 'twinpanelcontroller',
          name: 'twinpanel',
          halfSize: true
        },
        {
          xtype: 'multiselecttreepanel',
          name: 'all-roles-privs-tree', //note: unique ID is assinged before instantiation
          title: 'Available Roles / Privileges',
          border: true, //note: this seem to have no effect w/in form panel
          bodyBorder: true, //note: this seem to have no effect w/in form panel
          //note: this style matches the expected behavior
          bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
          width: 400,
          height: 300,
          animate:true,
          lines: false,
          autoScroll:true,
          containerScroll: true,
          //@note: root node must be instantiated uniquely for each instance of treepanel
          //@ext: can TreeNode be registerd as a component with an xtype so this new root node
          //      may be instantiated uniquely for each form panel that uses this config?
          rootVisible: false,
          root: new Ext.tree.TreeNode({text: 'root'}),
          enableDD: true,
          ddScroll: true,
          dropConfig: {
            allowContainerDrop: true,
            onContainerDrop: function(source, e, data){
              if ( data.nodes ){
                for ( var i = 0 ; i < data.nodes.length ; i++ ){
                  this.tree.root.appendChild(data.nodes[i]);
                }
              }
              return true;
            },
            onContainerOver:function(source, e, data){return this.dropAllowed;},
            // passign padding to make whole treePanel the drop zone.  This is dependent
            // on a sonatype fix in the Ext.dd.DropTarget class.  This is necessary
            // because treepanel.dropZone.setPadding is never available in time to be useful.
            padding: [0,0,274,0]
          }
        }
        ]
      }
  ];

  Sonatype.repoServer.DefaultUserEditor.superclass.constructor.call( this, {
    items: items,
    dataStores: [this.privDataStore,this.roleDataStore],
    listeners: {
      submit: {
        fn: this.submitHandler,
        scope: this
      },
      load: {
        fn: this.loadHandler,
        scope: this
      }
    }
  } );
};

Ext.extend( Sonatype.repoServer.DefaultRoleEditor, Sonatype.ext.FormPanel, {
  resetHandler: function( button, event ) {
    this.el.mask('Loading...', 'x-mask-loading');
    Sonatype.repoServer.DefaultRoleEditor.superclass.resetHandler.call(this, button, event);
    
    var allTree = this.find( 'name', 'all-roles-privs-tree' )[0];
    var selectedTree = this.find( 'name', 'roles-privs-tree' )[0];
    
    while ( allTree.root.lastChild ) {
      allTree.root.removeChild( allTree.root.lastChild );
    }
    
    while ( selectedTree.root.lastChild ) {
      selectedTree.root.removeChild( selectedTree.root.lastChild );
    }
    
    this.initializeRolesTreeHelper( allTree );
    if ( this.payload.data.roles ) {
      this.loadTreeHelper( allTree, selectedTree, this.payload.data.roles );
    }
    this.initializePrivilegesTreeHelper( allTree );
    if ( this.payload.data.privileges ) {
      this.loadTreeHelper( allTree, selectedTree, this.payload.data.privileges );
    }
    
    this.sortTree( allTree );
    this.sortTree( selectedTree );
    
    if ( this.presetData ) {
      this.getForm().setValues( this.presetData );
    }
    
    this.el.unmask();
  },
  loadHandler: function() {
    if ( !this.payload.data.userManaged ) {
      this.find('name', 'internalResourceHeader')[0].setVisible( true );
      this.find('name', 'id')[0].disable();
      this.find('name', 'name')[0].disable();
      this.find('name', 'description')[0].disable();
      this.find('name', 'sessionTimeout')[0].disable();
      this.find('name', 'twinpanel')[0].disable();
      this.find('name', 'roles-privs-tree')[0].dragZone.lock();
      this.find('name', 'all-roles-privs-tree')[0].dragZone.lock();
      for ( var i = 0 ; i < this.buttons.length ; i++ ){
        this.buttons[i].disable();
      }
    }
  },
  loadData: function( form, action, receivedData ) {
    Sonatype.repoServer.DefaultRoleEditor.superclass.loadData.call(this);
    
    if ( this.presetData ) {
      this.getForm().setValues( this.presetData );
    }
  },
  isValid: function() {
    var selectedTree = this.find( 'name', 'roles-privs-tree' )[0];
    var treeValid = selectedTree.validate.call(selectedTree);
    
    if (!treeValid) {
      this.markTreeInvalid(selectedTree);
    }
    
    return treeValid && this.form.isValid();
  },
  submitHandler: function( form, action, receivedData ) {
  	receivedData.mapping = this.payload.data.mapping;
  },
  markTreeInvalid : function(tree) {
    var elp = tree.getEl();
    
    if(!tree.errorEl){
        tree.errorEl = elp.createChild({cls:'x-form-invalid-msg'});
        tree.errorEl.setWidth(elp.getWidth(true)); //note removed -20 like on form fields
    }
    tree.invalid = true;
    tree.errorEl.update(tree.invalidText);
    elp.child('.x-panel-body').setStyle({
      'background-color' : '#fee',
      border : '1px solid #dd7870'
    });
    Ext.form.Field.msgFx['normal'].show(tree.errorEl, tree);  
  },
  
  initializeRolesTreeHelper : function( allTree ){    
    this.roleDataStore.each(function(item, i, len){
      if ( this.payload.data.id != item.data.id ){
        allTree.root.appendChild(
          new Ext.tree.TreeNode({
            id: item.data.id,
            text: item.data.name,
            payload: item.data.id, //sonatype added attribute
            allowChildren: false,
            draggable: true,
            leaf: true,
            qtip: item.data.description,
            nodeType: 'role',
            icon: Sonatype.config.extPath + '/resources/images/default/tree/folder.gif'
          })
        );
      }
    }, this);    
  },
  
  initializePrivilegesTreeHelper : function( allTree ){    
    this.privDataStore.each(function(item, i, len){
      allTree.root.appendChild(
        new Ext.tree.TreeNode({
          id: item.data.id,
          text: item.data.name,
          payload: item.data.id, //sonatype added attribute
          allowChildren: false,
          draggable: true,
          leaf: true,
          qtip: item.data.description,
          nodeType: 'priv'
        })
      );
    }, this);
  },
  
  loadTreeHelper : function( allTree, selectedTree, data ){
    var nodes = allTree.root.childNodes;
    for ( var i=0; i<data.length; i++ ) {
      for(var j = 0; j < nodes.length; j++){
        if ( nodes[j].id == data[i] ) {
          selectedTree.root.appendChild( nodes[j] );
          break;
        }
      }
    }
  },
  
  sortTree : function( tree ) {
    var sortTypeFunc = function( node ) {
      return ( node.attributes.nodeType == 'role' ? '0' : '1' ) + node.text.toLowerCase();
    }
    
    tree.sorter = new Ext.tree.SonatypeTreeSorter( tree, { sortType: sortTypeFunc } );
  },
  
  saveRolesTreeHelper : function(val, fpanel){
    var tree = fpanel.find( 'name', 'roles-privs-tree' )[0];

    var outputArr = [];
    var nodes = tree.root.childNodes;

    for(var i = 0; i < nodes.length; i++){
      if (nodes[i].attributes.nodeType == 'role'){
        outputArr[i] = nodes[i].attributes.payload;
      }
    }

    return outputArr;
  },
  
  savePrivilegesTreeHelper : function(val, fpanel){
    var tree = fpanel.find( 'name', 'roles-privs-tree' )[0];
    
    var outputArr = [];
    var nodes = tree.root.childNodes;

    for(var i = 0; i < nodes.length; i++){
      if (nodes[i].attributes.nodeType == 'priv'){
        outputArr[i] = nodes[i].attributes.payload;
      }
    }

    return outputArr;
  },
  
  treeValidationError : function(error, fpanel){
    var tree = fpanel.find( 'name', 'roles-privs-tree' )[0];
    this.markTreeInvalid(tree);
    tree.errorEl.update(error.msg);
  }
} );

Sonatype.Events.addListener( 'roleViewInit', function( cardPanel, rec, gridPanel ) {
  var config = { 
    payload: rec, 
    tabTitle: 'Configuration' 
  };
  
  if ( rec.data.userManaged == false ){
  }
  else {
  }
  
  cardPanel.add( new Sonatype.repoServer.DefaultRoleEditor( config ) );
} );
