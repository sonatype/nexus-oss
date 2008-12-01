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
/*
 * User Edit/Create panel layout and controller
 */
  
var NEXUS_USER_REALM = 'Nexus';

Sonatype.repoServer.UserEditPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  Sonatype.Events.addListener( 'userMenuInit', this.onUserMenuInit, this );

  this.editorMap = {};

  this.roleCombiner = function(val, parent) {
    var s = '';
    if ( val ) {
      for ( var i = 0; i < val.length; i++ ) {
        var roleName = val[i];
        var rec = this.roleDataStore.getAt( this.roleDataStore.find( 'id', roleName ) );
        if ( rec ) {
          roleName = rec.get( 'name' );
        }
        if ( s ) {
          s += ', ';
        }
        s += roleName;
      }
    }

    return s;
  };
  
  this.actions = {
    refresh: {
      text: 'Refresh',
      iconCls: 'st-icon-refresh',
      scope:this,
      handler: this.reloadAll
    },
    deleteAction: {
      text: 'Delete',
      scope:this,
      handler: this.deleteHandler
    },
    resetPasswordAction: {
      text: 'Reset Password',
      scope: this,
      handler: this.resetPasswordHandler
    },
    changePasswordAction: {
      text: 'Set Password',
      scope: this,
      handler: this.changePasswordHandler
    }
  };
  
  //A record to hold the name and id of a repository
  this.userRecordConstructor = Ext.data.Record.create([
    {name:'resourceURI'},
    {name:'userId', sortType:Ext.data.SortTypes.asUCString},
    {name:'name'},
    {name:'email'},
    {name:'status'},
    {name:'roles'},
    {name:'readOnly'},
    {name:'displayRoles', mapping:'roles', convert: this.roleCombiner.createDelegate(this)},
    {name:'stRealm'}
  ]);
  
  //Reader and datastore that queries the server for the list of currently defined users
  this.usersReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.userRecordConstructor );
  this.usersDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.users,
    reader: this.usersReader,
    sortInfo: {field: 'userId', direction: 'ASC'},
    autoLoad: false
  });
  
  //A record to hold the name and id of a role
  this.roleRecordConstructor = Ext.data.Record.create([
    {name:'id'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString}
  ]);
  
  this.roleReader = new Ext.data.JsonReader({root: 'data', id: 'id'}, this.roleRecordConstructor );  
  this.roleDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.roles,
    reader: this.roleReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true,
    listeners: {
      'load': {
        fn: function() {
          Sonatype.Events.fireEvent( 'userListInit', this, this );
        },
        scope: this
      }
    }
  });
  
  this.sp = Sonatype.lib.Permissions;

  this.usersGridPanel = new Ext.grid.GridPanel({
    title: 'Users',
    id: 'st-users-grid',
    
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
        id: 'user-refresh-btn',
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.reloadAll
      },
      {
        id: 'user-add-btn',
        text:'Add',
        icon: Sonatype.config.resourcePath + '/images/icons/add.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.addResourceHandler,
        disabled: !this.sp.checkPermission('nexus:users', this.sp.CREATE)
      },
      {
        id: 'user-delete-btn',
        text: 'Delete',
        icon: Sonatype.config.resourcePath + '/images/icons/delete.png',
        cls: 'x-btn-text-icon',
        scope:this,
        handler: this.deleteToolbarHandler,
        disabled: !this.sp.checkPermission('nexus:users', this.sp.DELETE)
      }
    ],

    //grid view options
    ds: this.usersDataStore,
    sortInfo:{field: 'userId', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      {header: 'User ID', dataIndex: 'userId', width:100, id: 'user-config-userid-col'},
      {header: 'Realm', dataIndex: 'stRealm', width:50, id: 'user-config-realm-col'},
      {header: 'User Managed', dataIndex: 'readOnly', width:100, id: 'user-config-readonly-col'},
      {header: 'Name', dataIndex: 'name', width:175, id: 'user-config-name-col'},
      {header: 'Email', dataIndex: 'email', width:175, id: 'user-config-email-col'},
      {header: 'Status', dataIndex: 'status', width:75, id: 'user-config-status-col'},
      {header: 'Roles', dataIndex: 'displayRoles', width:175, id: 'user-config-roles-col'}
    ],
    autoExpandColumn: 'user-config-roles-col',
    disableSelection: false,
    viewConfig: {
      emptyText: 'Click "Add" to create a new User.'
    }
  });
  this.usersGridPanel.on('rowclick', this.rowClick, this);
  this.usersGridPanel.on('rowcontextmenu', this.contextClick, this);

  Sonatype.repoServer.UserEditPanel.superclass.constructor.call(this, {
    layout: 'border',
    autoScroll: false,
    width: '100%',
    height: '100%',
    listeners: {
      'beforedestroy': {
        fn: function(){
          Sonatype.Events.removeListener( 'userMenuInit', this.onUserMenuInit, this );
        },
        scope: this
      }
    },
    items: [
      this.usersGridPanel,
      {
        xtype: 'panel',
        id: 'user-config-forms',
        title: 'User Configuration',
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
            html: '<div class="little-padding">Select a user to edit it, or click "Add" to create a new one.</div>'
          }
        ]
      }
    ]
  });

  this.formCards = this.findById('user-config-forms');
};


Ext.extend(Sonatype.repoServer.UserEditPanel, Ext.Panel, {
  //Dump the currently stored data and requery for everything
  reloadAll : function(){
    this.formCards.items.each(function(item, i, len){
      if(i>0){this.remove(item, true);}
    }, this.formCards);

    this.editorMap = {};
    this.roleDataStore.removeAll();
    this.usersDataStore.removeAll();
    this.roleDataStore.reload();
    
    this.formCards.getLayout().setActiveItem(0);
  },
  
  cancelHandler : function(formObj) {
    var formLayout = this.formCards.getLayout();
    var gridSelectModel = this.usersGridPanel.getSelectionModel();
    var store = this.usersGridPanel.getStore();
  
    this.formCards.remove(formObj.id, true);
  
    if (this.formCards.items.length > 1){
      formLayout.setActiveItem(this.formCards.items.length - 1);
      //select the coordinating row in the grid, or none if back to default
      var i = store.indexOfId(formLayout.activeItem.id);
      if (i >= 0){
        gridSelectModel.selectRow(i);
        var rec = store.getById(formLayout.activeItem.id);
        if (rec.data.readOnly == false
        		&& this.sp.checkPermission('nexus:users', this.sp.DELETE)){
        	this.usersGridPanel.getTopToolbar().items.get('user-delete-btn').enable();
        }
        else{
        	this.usersGridPanel.getTopToolbar().items.get('user-delete-btn').disable();
        }
      }
      else{
        gridSelectModel.clearSelections();
      }
    }
    else{
      formLayout.setActiveItem(0);
      gridSelectModel.clearSelections();
    }
    
    //delete row from grid if canceling a new repo form
    if(formObj.isNew){
      store.remove( store.getById(formObj.id) );
    }
  },
  
  addResourceHandler : function() {
    var id = 'new_user_' + new Date().getTime();

    var newRec = new this.userRecordConstructor({
        userId : 'New User',
        resourceURI : 'new',
        readOnly : false
      },
      id
    ); //use "new_user_" id instead of resourceURI like the reader does
    
    var formPanel = new Sonatype.repoServer.DefaultUserEditor( {
      payload: newRec,
      listeners: {
        cancel: {
          fn: this.cancelHandler,
          scope: this
        },
        submit: {
          fn: this.actionCompleteHandler,
          scope: this
        }
      }
    } );
    
    //add place holder to grid
    this.usersDataStore.insert(0, [newRec]);
    this.usersGridPanel.getSelectionModel().selectRow(0);
    
    this.usersGridPanel.getTopToolbar().items.get('user-delete-btn').enable();
    
    //add new form
    this.formCards.add(formPanel);
    
    //always set active and re-layout
    this.formCards.getLayout().setActiveItem(formPanel);
    formPanel.doLayout();
  },
  
  changePasswordHandler : function( rec ) {
    var userId = rec.get( 'userId' );

    var w = new Ext.Window({
      title: 'Set Password',
      closable: true,
      autoWidth: false,
      width: 350,
      autoHeight: true,
      modal:true,
      constrain: true,
      resizable: false,
      draggable: false,
      items: [
        {
          xtype: 'form',
          labelAlign: 'right',
          labelWidth:110,
          frame:true,  
          defaultType:'textfield',
          monitorValid:true,
          items:[
            {
              xtype: 'panel',
              style: 'padding-left: 70px; padding-bottom: 10px',
              html: 'Enter a new password for user ' + userId
            },
            { 
              fieldLabel: 'New Password', 
              inputType: 'password',
              name: 'newPassword',
              width: 200,
              allowBlank: false 
            },
            { 
              fieldLabel: 'Confirm Password', 
              inputType: 'password',
              name: 'confirmPassword',
              width: 200,
              allowBlank: false,
              validator: function( s ) {
                var firstField = this.ownerCt.find( 'name', 'newPassword' )[0];
                if ( firstField && firstField.getRawValue() != s ) {
                  return "Passwords don't match";
                }
                return true;
              }
            }
          ],
          buttons: [
            {
              text: 'Set Password',
              formBind: true,
              scope: this,
              handler: function(){
                var newPassword = w.find('name', 'newPassword')[0].getValue();

                Ext.Ajax.request({
                  scope: this,
                  method: 'POST',
                  jsonData: {
                    data: {
                      userId: userId,
                      newPassword: newPassword
                    }
                  },
                  url: Sonatype.config.repos.urls.usersSetPassword,
                  success: function(response, options){
                    w.close();
                    Sonatype.MessageBox.show( {
                      title: 'Password Changed',
                      msg: 'Password change request completed successfully.',
                      buttons: Sonatype.MessageBox.OK,
                      icon: Sonatype.MessageBox.INFO,
                      animEl: 'mb3'
                    } );
                  },
                  failure: function(response, options){
                    Sonatype.utils.connectionError( response, 'There is a problem changing the password.' )
                  }
                });
              }
            },
            {
              text: 'Cancel',
              formBind: false,
              scope: this,
              handler: function(){
                w.close();
              }
            }
          ]
        }
      ]
    });

    w.show();
  },
  
  resetPasswordHandler : function( rec ) {
    if ( rec.data.resourceURI != 'new' ) {
      Sonatype.MessageBox.getDialog().on('show', function(){
        this.focusEl = this.buttons[2]; //ack! we're offset dependent here
        this.focus();
      },
      Sonatype.MessageBox.getDialog(),
      {single:true});
        
      Sonatype.MessageBox.show({
        animEl: this.usersGridPanel.getEl(),
        title : 'Reset user password?',
        msg : 'Reset the ' + rec.get('userId') + ' user password?',
        buttons: Sonatype.MessageBox.YESNO,
        scope: this,
        icon: Sonatype.MessageBox.QUESTION,
        fn: function(btnName){
          if (btnName == 'yes' || btnName == 'ok') {
            Ext.Ajax.request({
              callback: this.resetPasswordCallback,
              cbPassThru: {
                resourceId: rec.id
              },
              scope: this,
              method: 'DELETE',
              url: Sonatype.config.repos.urls.usersReset + '/' + rec.data.userId
            });
          }
        }
      });
    } 
  },
  
  resetPasswordCallback : function(options, isSuccess, response){
    if(isSuccess){
      Sonatype.MessageBox.alert('The password has been reset.');
    }
    else {
      Sonatype.MessageBox.alert('The server did not reset the password.');
    }
  },
  
  deleteToolbarHandler : function(){
    if (this.ctxRecord || this.usersGridPanel.getSelectionModel().hasSelection()){
      this.deleteHandler( this.ctxRecord ? this.ctxRecord : this.usersGridPanel.getSelectionModel().getSelected() );
    }
  },
  
  deleteHandler : function( rec ){
    if(rec.data.resourceURI == 'new'){
      this.cancelHandler(Ext.getCmp(rec.id));
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
        animEl: this.usersGridPanel.getEl(),
        title : 'Delete User?',
        msg : 'Delete the ' + rec.get('userId') + ' user?',
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
  },
  
  deleteCallback : function(options, isSuccess, response){
    if(isSuccess){
      var resourceId = options.cbPassThru.resourceId;
      var formLayout = this.formCards.getLayout();
      var gridSelectModel = this.usersGridPanel.getSelectionModel();
      var store = this.usersGridPanel.getStore();

      if(formLayout.activeItem.id == resourceId){
        this.formCards.remove(resourceId, true);
        if (this.formCards.items.length > 0){
          formLayout.setActiveItem(this.formCards.items.length - 1);
          //select the coordinating row in the grid, or none if back to default
          var i = store.indexOfId(formLayout.activeItem.id);
          if (i >= 0){
            gridSelectModel.selectRow(i);
            var rec = store.getById(formLayout.activeItem.id);
            if (rec.data.readOnly == false
            		&& this.sp.checkPermission('nexus:users', this.sp.DELETE)){
            	this.usersGridPanel.getTopToolbar().items.get('user-delete-btn').enable();
            }
            else{
            	this.usersGridPanel.getTopToolbar().items.get('user-delete-btn').disable();
            }
          }
          else{
            gridSelectModel.clearSelections();
          }
        }
        else{
            formLayout.setActiveItem(0);
            gridSelectModel.clearSelections();
        }
      }
      else {
        this.formCards.remove(resourceId, true);
      }

      store.remove( store.getById(resourceId) );
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not delete the user.', null, null, true );
    }
  },
      
  //(Ext.form.BasicForm, Ext.form.Action)
  actionCompleteHandler : function(form, action, receivedData) {
    var isNew = action.options.isNew;
    if (isNew) {
      //successful create        
      var dataObj = {
        userId : receivedData.userId,
        name : receivedData.name,
        resourceURI : receivedData.resourceURI,
        email : receivedData.email,
        status : receivedData.status,
        roles : receivedData.roles,
        readOnly : receivedData.readOnly,
        displayRoles : this.roleCombiner(receivedData.roles),
        stRealm: NEXUS_USER_REALM
      };
      
      var newRec = new this.userRecordConstructor(
        dataObj,
        action.options.fpanel.id);
      
      this.usersDataStore.remove(this.usersDataStore.getById(action.options.fpanel.id)); //remove old one
      this.usersDataStore.addSorted(newRec);
      this.usersGridPanel.getSelectionModel().selectRecords([newRec], false);
      action.options.fpanel.payload = newRec;
    }
    else {
      var i = this.usersDataStore.indexOfId(action.options.fpanel.id);
      var rec = this.usersDataStore.getAt(i);

      this.updateUserRecord(rec, receivedData);
      
      var sortState = this.usersDataStore.getSortState();
      this.usersDataStore.sort(sortState.field, sortState.direction);
    }
  },
  
  updateUserRecord : function(rec, receivedData){
    if ( receivedData ) {
      rec.beginEdit();
      rec.set('name', receivedData.name);
      rec.set('userId', receivedData.userId);
      rec.set('email', receivedData.email);
      rec.set('status', receivedData.status);
      rec.set('roles', receivedData.roles);
      rec.set('readOnly', receivedData.readOnly);
      rec.set('displayRoles', this.roleCombiner(receivedData.roles));
      rec.commit();
      rec.endEdit();
    }
  },

  rowClick : function(grid, rowIndex, e){
    var rec = grid.store.getAt(rowIndex);
    if (rec) {
	    if (rec.data.readOnly == false
	            && this.sp.checkPermission('nexus:users', this.sp.DELETE)) {
	      grid.getTopToolbar().items.get('user-delete-btn').enable();
	    } else {
	      grid.getTopToolbar().items.get('user-delete-btn')
	        .disable();
	    }
	    var id = rec.id; //note: rec.id is unique for new resources and equal to resourceURI for existing ones
	    var formPanel = this.formCards.findById(id);
	    
	    //assumption: new route forms already exist in formCards, so they won't get into this case
	    if(!formPanel){ //create form and populate current data
	      var editor = this.editorMap[rec.data.stRealm];
	      if ( editor ) {
  	      formPanel = new editor( {
            payload: rec,
            readOnly: rec.data.readOnly ||
              ! this.sp.checkPermission( 'nexus:users', this.sp.EDIT ),
            listeners: {
              cancel: {
                fn: this.cancelHandler,
                scope: this
              },
              submit: {
                fn: this.actionCompleteHandler,
                scope: this
              }
            }
          } );
  	  
  	      this.formCards.add(formPanel);
  	      this.formCards.getLayout().setActiveItem(formPanel);    
  	      formPanel.doLayout();
	      }
	    }
	    else{
	      //always set active
	      this.formCards.getLayout().setActiveItem(formPanel);
	    }
    }
    else {
    	grid.getTopToolbar().items.get('user-delete-btn').disable();
    }
  },
  
  contextClick : function(grid, index, e){
    this.contextHide();
    
    if ( e.target.nodeName == 'A' ) return; // no menu on links
    
    this.ctxRow = this.usersGridPanel.view.getRow(index);
    this.ctxRecord = this.usersGridPanel.store.getAt(index);
    Ext.fly(this.ctxRow).addClass('x-node-ctx');

    //@todo: would be faster to pre-render the six variations of the menu for whole instance
    var menu = new Sonatype.menu.Menu({
      id:'users-grid-ctx',
      payload: this.ctxRecord,
      items: [
        this.actions.refresh
      ]
    });

    Sonatype.Events.fireEvent( 'userMenuInit', menu, this.ctxRecord );
    
    menu.on('hide', this.contextHide, this);
    e.stopEvent();
    menu.showAt(e.getXY());
  },
  
  contextHide : function(){
    if(this.ctxRow){
      Ext.fly(this.ctxRow).removeClass('x-node-ctx');
      this.ctxRow = null;
      this.ctxRecord = null;
    }
  },

  onUserMenuInit: function( menu, userRecord, a, b, c ) {
    if ( userRecord.data.readOnly == false && userRecord.data.stRealm == NEXUS_USER_REALM ) {

      if ( this.sp.checkPermission( 'nexus:users', this.sp.DELETE ) ) {
        menu.add( this.actions.deleteAction );
      }

      if ( userRecord.data.resourceURI != 'new' ) {
        if ( this.sp.checkPermission( 'nexus:usersreset', this.sp.DELETE ) ) {
          menu.add(this.actions.resetPasswordAction);
        }

        if ( this.sp.checkPermission( 'nexus:users', this.sp.EDIT ) ) {
          menu.add( this.actions.changePasswordAction );
        }
      }
    }
  },
  
  addRecords: function( users, realm, form ) {
    if ( form ) {
      this.editorMap[realm] = form;
    }

    for ( var i = 0; i < users.length; i++ ) {
      users[i].stRealm = realm;
    }

    this.usersDataStore.loadData( { data: users }, true );
    this.usersDataStore.sort( 'userId' );
  }
});

Sonatype.repoServer.DefaultUserEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    uri: Sonatype.config.repos.urls.users,
    dataModifiers: {
      load: {
        roles: function( arr, srcObj, fpanel ) {
          fpanel.find( 'name', 'roles' )[0].setValue( arr );
          return arr;
        }
      },
      submit: { 
        roles: function( value, fpanel ) {
          return fpanel.find( 'name', 'roles' )[0].getValue();
        }
      }
    }
  };
  Ext.apply( this, config, defaultConfig );
  
  //List of user statuses
  this.statusStore = new Ext.data.SimpleStore( { fields: ['value', 'display'],
    data: [['active', 'Active'], ['disabled', 'Disabled']] } );
  
  //A record to hold the name and id of a role
  this.roleRecordConstructor = Ext.data.Record.create( [
    { name: 'id' },
    { name: 'name', sortType: Ext.data.SortTypes.asUCString }
  ] );
  this.roleReader = new Ext.data.JsonReader(
    { root: 'data', id: 'id' }, this.roleRecordConstructor );  
  this.roleDataStore = new Ext.data.Store( {
    url: Sonatype.config.repos.urls.roles,
    reader: this.roleReader,
    sortInfo: { field: 'name', direction: 'ASC' },
    autoLoad: true
  } );

  var ht = Sonatype.repoServer.resources.help.users;
  
  this.COMBO_WIDTH = 300;

  this.checkPayload();
  var items = [
    {
      xtype: 'textfield',
      fieldLabel: 'User ID',
      itemCls: 'required-field',
      labelStyle: 'margin-left: 15px; width: 185px;',
      helpText: ht.userId,
      name: 'userId',
      disabled: ! this.isNew,
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
      fieldLabel: 'Email',
      itemCls: 'required-field',
      labelStyle: 'margin-left: 15px; width: 185px;',
      helpText: ht.email,
      name: 'email',
      allowBlank: false,
      width: this.COMBO_WIDTH
    },
    {
      xtype: 'combo',
      fieldLabel: 'Status',
      labelStyle: 'margin-left: 15px; width: 185px;',
      itemCls: 'required-field',
      helpText: ht.status,
      name: 'status',
      store: this.statusStore,
      displayField:'display',
      valueField:'value',
      editable: false,
      forceSelection: true,
      mode: 'local',
      triggerAction: 'all',
      emptyText:'Select...',
      selectOnFocus:true,
      allowBlank: false,
      width: this.COMBO_WIDTH
    }
  ];
  
  if ( this.isNew ) {
    items.push( {
      xtype: 'textfield',
      fieldLabel: 'New Password (optional)',
      inputType: 'password',
      labelStyle: 'margin-left: 15px; width: 185px;',
      helpText: ht.password,
      name: 'password',
      allowBlank: true,
      width: this.COMBO_WIDTH
    } );
    items.push( {
      xtype: 'textfield',
      fieldLabel: 'Confirm Password',
      inputType: 'password',
      labelStyle: 'margin-left: 15px; width: 185px;',
      helpText: ht.reenterPassword,
      name: 'confirmPassword',
      allowBlank: true,
      width: this.COMBO_WIDTH,
      validator: function( s ) {
        var firstField = this.ownerCt.find( 'name', 'password' )[0];
        if ( firstField && firstField.getRawValue() != s ) {
          return "Passwords don't match";
        }
        return true;
      }
    } );
  }

  items.push( {
    xtype: 'twinpanelchooser',
    titleLeft: 'Selected Roles',
    titleRight: 'Available Roles',
    name: 'roles',
    valueField: 'id',
    store: this.roleDataStore,
    required: true
  } );

  Sonatype.repoServer.DefaultUserEditor.superclass.constructor.call( this, {
    items: items
  } );

  this.on( 'submit', this.submitCleanup, this );
};

Ext.extend( Sonatype.repoServer.DefaultUserEditor, Sonatype.ext.FormPanel, {
  isValid: function() {
    return this.form.isValid() && this.find( 'name', 'roles' )[0].validate();
  },
  
  saveHandler: function( button, event ) {
    var password = this.form.getValues().password;
    this.referenceData = ( this.isNew && password ) ?
      Sonatype.repoServer.referenceData.userNew : Sonatype.repoServer.referenceData.users;
    
    return Sonatype.repoServer.DefaultUserEditor.superclass.saveHandler.call( this, button, event );
  },
  
  submitCleanup: function( form, action, receivedData ) {
    this.find( 'name', 'status' )[0].setValue( receivedData.status );
    
    if ( this.isNew ) {
      var useridField = this.find( 'name', 'userId' )[0];
      if ( useridField ) {
        useridField.disable();
      }
      var passwordField = this.find( 'name', 'password' )[0];
      if ( passwordField ) {
        this.remove( passwordField );
      }
      var passwordField2 = this.find( 'name', 'confirmPassword' )[0];
      if ( passwordField2 ) {
        this.remove( passwordField2 );
      }
    }
  }
} );

Sonatype.Events.addListener( 'userListInit', function( userContainer ) {
  Ext.Ajax.request( {
    url: Sonatype.config.repos.urls.users,
    success: function( response, options ) {
      var resp = Ext.decode( response.responseText );
      if ( resp.data ) {
        userContainer.addRecords( resp.data, NEXUS_USER_REALM, Sonatype.repoServer.DefaultUserEditor );
      }
    },
    scope: userContainer
  } );
} );
