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

Sonatype.repoServer.UserEditPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Users'
  };
  Ext.apply( this, config, defaultConfig );

  this.sp = Sonatype.lib.Permissions;
  
  this.actions = {
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

  Sonatype.repoServer.UserEditPanel.superclass.constructor.call( this, {
    addMenuInitEvent: 'userAddMenuInit',
    deleteButton: this.sp.checkPermission( 'nexus:users', this.sp.DELETE ),
    rowClickEvent: 'userViewInit',
    rowContextClickEvent: 'userMenuInit',
    url: Sonatype.config.repos.urls.plexusUsersAllConfigured,
    dataAutoLoad: true,
    dataId: 'userId',
    columns: [
      { 
        name: 'resourceURI',
        mapping: 'userId',
        convert: function( value, parent ) {
          return parent.source == 'default' ? 
            ( Sonatype.config.repos.urls.users + '/' + value ) :
            ( Sonatype.config.repos.urls.plexusUser + '/' + value );
        }
      },
      { 
        name: 'userId', 
        sortType: Ext.data.SortTypes.asUCString,
        header: 'User ID',
        width: 100
      },
      { 
        name: 'source',
        header: 'Realm',
        width: 50
      },
      { 
        name: 'name',
        header: 'Name',
        width: 175
      },
      { 
        name: 'email',
        header: 'Email',
        width: 175
      },
      { name: 'roles' },
      { 
        name: 'displayRoles', 
        mapping: 'roles', 
        convert: this.combineRoles.createDelegate( this ),
        header: 'Roles',
        autoExpand: true
      }
    ],
    listeners: {
      beforedestroy: {
        fn: function(){
          Sonatype.Events.removeListener( 'userMenuInit', this.onUserMenuInit, this );
        },
        scope: this
      }
    },
    tbar: [
      {
        text: 'Map User Roles',
        icon: Sonatype.config.resourcePath + '/images/icons/page_white_put.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.mapRolesHandler
      }
    ]
  } );

  Sonatype.Events.addListener( 'userMenuInit', this.onUserMenuInit, this );
};


Ext.extend( Sonatype.repoServer.UserEditPanel, Sonatype.panels.GridViewer, {
  combineRoles: function( val, parent ) {
    var s = '';
    if ( val ) {
      for ( var i = 0; i < val.length; i++ ) {
        var roleName = val[i].name;
        if ( s ) {
          s += ', ';
        }
        s += roleName;
      }
    }

    return s;
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
      Sonatype.utils.defaultToNo();
        
      Sonatype.MessageBox.show({
        animEl: this.gridPanel.getEl(),
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

  onUserMenuInit: function( menu, userRecord ) {
    if ( userRecord.data.source == 'default' // && userRecord.data.userManaged == true
        ) {

      if ( userRecord.data.resourceURI.substring( 0, 4 ) != 'new_' ) {
        if ( this.sp.checkPermission( 'nexus:usersreset', this.sp.DELETE ) ) {
          menu.add(this.actions.resetPasswordAction);
        }

        if ( this.sp.checkPermission( 'nexus:users', this.sp.EDIT ) ) {
          menu.add( this.actions.changePasswordAction );
        }
      }
    }
  },
  
  mapRolesHandler: function( button, e ) {
    this.createChildPanel( { 
      id: 'new_mapping',
      hostPanel: this,
      data: {
        name: 'User Role Mapping'
      }
    } );
  },
  
  deleteActionHandler: function( button, e ) {
    if ( this.gridPanel.getSelectionModel().hasSelection() ) {
      var rec = this.gridPanel.getSelectionModel().getSelected();
      if ( rec.data.source == 'default' ) {
        return Sonatype.repoServer.UserEditPanel.superclass.deleteActionHandler.call( this, button, e );
      }
      else {
        Sonatype.MessageBox.show( {
          animEl: this.gridPanel.getEl(),
          title: 'Delete',
          msg: 'Cannot delete a user from external realm.',
          buttons: Sonatype.MessageBox.OK,
          scope: this,
          icon: Sonatype.MessageBox.WARNING
        } );
      }
    }
  }
} );

Sonatype.repoServer.DefaultUserEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    uri: Sonatype.config.repos.urls.users,
    labelWidth: 100,
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

  this.roleDataStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    url: Sonatype.config.repos.urls.roles,
    sortInfo: { field: 'name', direction: 'ASC' },
    autoLoad: true,
    fields: [
      { name: 'id' },
      { name: 'name', sortType:Ext.data.SortTypes.asUCString }
    ]
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
    items: items,
    listeners: {
      submit: {
        fn: this.submitHandler,
        scope: this
      }
    }
  } );
};

Ext.extend( Sonatype.repoServer.DefaultUserEditor, Sonatype.ext.FormPanel, {
  combineRoles: function( val ) {
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
  },

  isValid: function() {
    return this.form.isValid() && this.find( 'name', 'roles' )[0].validate();
  },
  
  saveHandler: function( button, event ) {
    var password = this.form.getValues().password;
    this.referenceData = ( this.isNew && password ) ?
      Sonatype.repoServer.referenceData.userNew : Sonatype.repoServer.referenceData.users;
    
    return Sonatype.repoServer.DefaultUserEditor.superclass.saveHandler.call( this, button, event );
  },

  submitHandler: function( form, action, receivedData ) {
    if ( this.isNew ) {
      receivedData.source = 'default';
      receivedData.displayRoles = this.combineRoles( receivedData.roles );
      return;
    }

    var rec = this.payload;
    rec.beginEdit();
    rec.set( 'name', receivedData.name );
    rec.set( 'email', receivedData.email );
    rec.set( 'displayRoles', this.combineRoles( receivedData.roles ) );
    rec.commit();
    rec.endEdit();
  }
} );

Sonatype.repoServer.UserMappingEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    uri: Sonatype.config.repos.urls.plexusUser,
    dataModifiers: {
      load: {
        roles: function( arr, srcObj, fpanel ) {
          var arr2 = [];
          var externalRoles = 0;
          for ( var i = 0; i < arr.length; i++ ) {
            var a = arr[i];
            var readOnly = false;
            if ( a.source != 'default' ) {
              readOnly = true;
              externalRoles++;
            }
            arr2.push( {
              id: a.roleId,
              name: a.name,
              readOnly: readOnly
            } );
          }
          var roleBox = fpanel.find( 'name', 'roles' )[0];
          roleBox.setValue( arr2 );
          roleBox.nexusRolesEmptyOnLoad = ( arr.length == externalRoles );

          return arr;
        }
      },
      submit: { 
        roles: function( value, fpanel ) {
          return fpanel.find( 'name', 'roles' )[0].getValue();
        }
      }
    },
    referenceData: {
      userId: '',
      source: '',
      roles: []
    },
    cancelButton: config.payload.id == 'new_mapping'
  };
  Ext.apply( this, config, defaultConfig );

//  if ( ! Sonatype.lib.Permissions.checkPermission( 'nexus:ldapuserrolemap',
//      Sonatype.lib.Permissions.EDIT ) ) {
//    this.readOnly = true;
//  }

  this.roleDataStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    url: Sonatype.config.repos.urls.roles,
    sortInfo: { field: 'name', direction: 'ASC' },
    autoLoad: true,
    fields: [
      { name: 'id' },
      { name: 'name', sortType:Ext.data.SortTypes.asUCString }
    ]
  } );

  var ht = Sonatype.repoServer.resources.help.users;
  
  this.COMBO_WIDTH = 300;
  
  var useridField = this.payload.id == 'new_mapping' ? 
    {
      xtype: 'trigger',
      triggerClass: 'x-form-search-trigger',
      fieldLabel: 'Enter a User ID',
      itemCls: 'required-field',
      labelStyle: 'margin-left: 15px; width: 185px;',
      name: 'userId',
      allowBlank: false,
      width: this.COMBO_WIDTH,
      listeners: {
        specialkey: {
          fn: function(f, e){
            if(e.getKey() == e.ENTER){
              this.onTriggerClick();
            }
          }
        }
      },
      onTriggerClick: this.loadUserId.createDelegate( this ),
      listeners: {
        change: {
          fn: function( control, newValue, oldValue ) {
            if ( newValue != this.lastLoadedId ) {
              this.loadUserId();
            }
          },
          scope: this
        }
      }
    } :
    {
      xtype: 'textfield',
      fieldLabel: 'User ID',
      itemCls: 'required-field',
      labelStyle: 'margin-left: 15px; width: 185px;',
      name: 'userId',
      disabled: true,
      allowBlank: false,
      width: this.COMBO_WIDTH
    };
    

  Sonatype.repoServer.UserMappingEditor.superclass.constructor.call( this, {
    items: [
      useridField,
      {
        xtype: 'textfield',
        fieldLabel: 'Realm',
        itemCls: 'required-field',
        labelStyle: 'margin-left: 15px; width: 185px;',
        name: 'source',
        disabled: true,
        allowBlank: false,
        width: this.COMBO_WIDTH
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Name',
        itemCls: 'required-field',
        labelStyle: 'margin-left: 15px; width: 185px;',
        name: 'name',
        disabled: true,
        allowBlank: false,
        width: this.COMBO_WIDTH
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Email',
        itemCls: 'required-field',
        labelStyle: 'margin-left: 15px; width: 185px;',
        name: 'email',
        disabled: true,
        allowBlank: false,
        width: this.COMBO_WIDTH
      },
      {
        xtype: 'twinpanelchooser',
        titleLeft: 'Selected Roles',
        titleRight: 'Available Roles',
        name: 'roles',
        valueField: 'id',
        store: this.roleDataStore,
        required: true
      }
    ],
    listeners: {
      cancel: {
        fn: function() {
          this.payload.hostPanel.recordRemoveHandler( null, this.payload, 0 );
        },
        scope: this
      }
    }
  } );
};

Ext.extend( Sonatype.repoServer.UserMappingEditor, Sonatype.ext.FormPanel, {
  saveHandler : function( button, event ){
    if ( this.isValid() ) {
      var method = 'PUT';
      var roleBox = this.find( 'name', 'roles' )[0];
      var roles = roleBox.getValue();
      if ( roles.length == 0 ) {
        if ( roleBox.nexusRolesEmptyOnLoad ) {
          // if there weren't any nexus roles on load, and we're not saving any - do nothing
          return;
        }
        else {
          method = 'DELETE';
          roleBox.nexusRolesEmptyOnLoad = true;
        }
      }
      else {
        roleBox.nexusRolesEmptyOnLoad = false;
      }

      var url = Sonatype.config.repos.urls.userToRoles + '/' +
        this.form.findField( 'source').getValue() + '/' +
        this.form.findField( 'userId' ).getValue();

      this.form.doAction( 'sonatypeSubmit', {
        method: method,
        url: url,
        waitMsg: 'Updating records...',
        fpanel: this,
        dataModifiers: this.dataModifiers.submit,
        serviceDataObj: this.referenceData,
        isNew: this.isNew //extra option to send to callback, instead of conditioning on method
      } );
    }
  },
  
  isValid: function() {
    return this.form.findField( 'userId' ).userFound &&
      this.form.findField( 'source' ).getValue() && 
      this.find( 'name', 'roles' )[0].validate();
  },

  loadUserId: function() {
    var testField = this.form.findField( 'userId' );
    testField.clearInvalid();
    testField.userFound = true;
    this.lastLoadedId = testField.getValue();

    this.form.doAction( 'sonatypeLoad', {
      url: this.uri + '/' + testField.getValue(),
      method: 'GET',
      fpanel: this,
      testField: testField,
      suppressStatus: 404,
      dataModifiers: this.dataModifiers.load,
      scope: this
    } );
  },

  actionFailedHandler: function( form, action ) {
    if ( action.response.status == 404 && action.options.testField ) {
      action.options.testField.markInvalid( 'User record not found.' );
      action.options.testField.userFound = false;
    }
    else {
      return Sonatype.repoServer.UserMappingEditor.superclass.actionFailedHandler.call(
        this, form, action );
    }
  }
} );

Sonatype.Events.addListener( 'userListInit', function( userContainer ) {
  var url = Sonatype.config.servicePath + '/plexus_users/LDAP';
  if ( Sonatype.lib.Permissions.checkPermission( 'nexus:ldapuserrolemap', Sonatype.lib.Permissions.READ ) ) {
    Ext.Ajax.request( {
      url: url,
      suppressStatus: 503, // the server will return an error if LDAP is not configured
      success: function( response, options ) {
        var resp = Ext.decode( response.responseText );
        if ( resp.data ) {
          var data = resp.data;
          for ( var i = 0; i < data.length; i++ ) {
            data[i].resourceURI = Sonatype.config.servicePath + '/plexus_user/' + data[i].userId;
            if ( data[i].roles ) {
              for ( var j = 0; j < data[i].roles.length; j++ ) {
                data[i].roles[j] = data[i].roles[j].roleId;
              }
            }
          }
          userContainer.addRecords( data, 'LDAP', Sonatype.repoServer.LdapUserEditor );
        }
      },
      scope: userContainer
    } );
  }
} );

Sonatype.Events.addListener( 'userViewInit', function( cardPanel, rec ) {
  var config = { payload: rec };
  cardPanel.add( rec.data.source == 'default' ?
    new Sonatype.repoServer.DefaultUserEditor( config ) :
    new Sonatype.repoServer.UserMappingEditor( config )
  );
} );

Sonatype.Events.addListener( 'userAddMenuInit', function( menu ) {
  var sp = Sonatype.lib.Permissions;
  
  if ( sp.checkPermission( 'nexus:users', sp.CREATE ) ) {
    var createUserFunc = function( container, rec, item, e ) {
      rec.beginEdit();
      rec.set( 'source', 'default' );
      rec.commit();
      rec.endEdit();
    };

    menu.add( [
      '-',
      {
        text: 'Nexus User',
        autoCreateNewRecord: true,
        handler: createUserFunc
      }
    ] );
  }
} );
