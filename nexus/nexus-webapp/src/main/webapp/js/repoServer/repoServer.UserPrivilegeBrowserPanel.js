Sonatype.repoServer.UserPrivilegeBrowsePanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );
  
  this.roleDataStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    url: Sonatype.config.repos.urls.roles,
    sortInfo: { field: 'name', direction: 'ASC' },
    autoLoad: true,
    fields: [
      { name: 'id' },
      { name: 'name', sortType:Ext.data.SortTypes.asUCString },
      { name: 'description'},
      { name: 'privileges'},
      { name: 'roles'}
    ],
    listeners: {
      load: {
        fn: function( store, records, options ){
          this.rolesLoaded = true;
          if ( this.privsLoaded ){
            this.loadPrivilegeList();
            this.privsLoaded = false;
            this.rolesLoaded = false;
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
    autoLoad: true,
    fields: [
      { name: 'id' },
      { name: 'name', sortType:Ext.data.SortTypes.asUCString },
      { name: 'description'}
    ],
    listeners: {
      load: {
        fn: function( store, records, options ){
          this.privsLoaded = true;
          if ( this.rolesLoaded ){
            this.loadPrivilegeList();
            this.privsLoaded = false;
            this.rolesLoaded = false;
          }
        },
        scope: this
      }
    }
  } );
  
  this.roleDataStore.load();
  this.privDataStore.load();
  
  Sonatype.repoServer.UserPrivilegeBrowsePanel.superclass.constructor.call( this, {
    region: 'center',
    width: '100%',
    height: '100%',
    autoScroll: true,
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
    tbar: [
      {
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.refreshHandler
      }
    ],
    items : [
        {
          xtype: 'panel',
          layout: 'column',
          items: [
            {
              xtype :'panel',
              layout :'auto',
              columnWidth: .5,
              items :[
                {
                  xtype: 'panel',
                  style :'padding: 10px 0 10px 0',
                  html: 'Select a privilege to view the role(s) in the user<br>that grant the privilege.'
                },
                {
                  xtype :'treepanel',                  
                  name :'privilege-list',
                  title :'Privileges',
                  border :true,
                  bodyBorder :true,
                  bodyStyle :'background-color:#FFFFFF; border: 1px solid #B5B8C8',
                  style :'padding: 0 50px 0 0',
                  width :325,
                  height :275,
                  animate :true,
                  lines :false,
                  autoScroll :true,
                  containerScroll :true,
                  rootVisible :false,
                  ddScroll: false,
                  enableDD: false,
                  root :new Ext.tree.TreeNode( {
                    text :'root',
                    draggable: false
                  })
                }
              ]
            },
            {
              xtype :'panel',
              layout :'auto',
              columnWidth: .5,
              items :[
                {
                  xtype: 'panel',
                  style :'padding: 10px 0 10px 0',
                  html: 'List of roles in the user that grant the selected privilege.<br>Expand the role to find nested role(s) that contain<br>the privilege.'
                },
                {
                  xtype :'treepanel',
                  name :'role-tree',
                  title :'Role Containment',
                  border :true,
                  bodyBorder :true,
                  bodyStyle :'background-color:#FFFFFF; border: 1px solid #B5B8C8',
                  width :325,
                  height :275,
                  animate :true,
                  lines :false,
                  autoScroll :true,
                  containerScroll :true,
                  rootVisible :false,
                  ddScroll: false,
                  enableDD: false,
                  root :new Ext.tree.TreeNode( {
                    text :'root',
                    draggable: false
                  })
                }
              ]
            }
          ]
        }
      ]
    });
};

Ext.extend( Sonatype.repoServer.UserPrivilegeBrowsePanel, Ext.FormPanel, {
  refreshHandler: function( button, e ) {
    //the load listener on these stores will reload the privileges
    this.roleDataStore.reload();
    this.privDataStore.reload();
    var tree = this.find('name', 'role-tree')[0];
    while (tree.root.lastChild) {
      tree.root.removeChild(tree.root.lastChild);
    }  
  },
  handleNodeClicked : function( id ) {
    var tree = this.find('name', 'role-tree')[0];
    while (tree.root.lastChild) {
      tree.root.removeChild(tree.root.lastChild);
    }    
    var routeArray = this.getPrivilegeRouteArray( id );
    if ( routeArray ){
      
      for ( var i = 0 ; i < routeArray.length ; i++ ){
        var roles = routeArray[i].split('||');
        if ( roles ){
          var base = tree.root;
          for ( var j = 0 ; j < roles.length ; j++ ){
            var nodeId = ( base == tree.root ) ? roles[j] : ( base.id + '$$' + roles[j] );
            var foundNode = base.findChild('id', nodeId);
            
            if ( foundNode ){
              base = foundNode;
            }
            else {
              base = base.appendChild(
                new Ext.tree.TreeNode({
                  id: nodeId,
                  text: roles[j],
                  payload: roles[j],
                  allowChildren: ( j + 1 == roles.length ) ? false : true,
                  draggable: false,
                  leaf: ( j + 1 == roles.length ) ? true : false,
                  icon: Sonatype.config.extPath + '/resources/images/default/tree/folder.gif'
                })
              );
            }
          }
        }
      }
    }
  },
  loadPrivilegeList : function(){
    var privilegeList = this.find('name', 'privilege-list')[0];
    while (privilegeList.root.lastChild) {
      privilegeList.root.removeChild(privilegeList.root.lastChild);
    }
    var privilegeRecs = this.privDataStore.getRange();
    for ( var i = 0 ; i < privilegeRecs.length ; i++ ){
      if ( this.userHasPrivilege( privilegeRecs[i].data.id ) ){
        privilegeList.root.appendChild(
            new Ext.tree.TreeNode({
              id: privilegeRecs[i].data.id,
              text: privilegeRecs[i].data.name,
              payload: privilegeRecs[i].data, //sonatype added attribute
              allowChildren: false,
              draggable: false,
              leaf: true,
              qtip: privilegeRecs[i].data.description,
              listeners: {
                click: {
                  fn: function( node, event ){
                    this.handleNodeClicked( node.id );
                  },
                  scope: this
                }
              }
            })
          );
      }
    }
  },
  userHasPrivilege : function( privId ){
    if ( this.payload.data.roles ){
      for ( var i = 0 ; i < this.payload.data.roles.length ; i++ ){
        if ( this.roleHasPrivilege( privId, this.getRoleIdFromPayload( this.payload.data.roles[i] ) ) ){
          return true;
        }
      }
    }
    return false;
  },
  roleHasPrivilege : function( privId, roleId ){
    var role = this.roleDataStore.getAt( this.roleDataStore.findBy( 
        function( rec, recid ) {
          return rec.id == roleId;
        }, this ) );
    
    if(role == null || role.data == null) {
    	return false;
    }
    
    if ( role.data.privileges ){
      for ( var i = 0 ; i < role.data.privileges.length ; i++ ){
        if ( role.data.privileges[i] == privId ){
          return true;
        }
      }
    }
    if ( role.data.roles ){
      for ( var i = 0 ; i < role.data.roles.length ; i++ ){
        if ( this.roleHasPrivilege( privId, role.data.roles[i] ) ){
          return true;
        }
      }
    }
    return false;
  },
  getPrivilegeRouteArray : function( privId ){
    var userRoles = this.payload.data.roles;
    
    var routeArray = [];
    
    if ( userRoles ){
      for ( var i = 0 ; i < userRoles.length ; i++ ) {
        var role = this.roleDataStore.getAt( this.roleDataStore.findBy( 
            function( rec, recid ) {
              return rec.id == this.getRoleIdFromPayload( userRoles[i] );
            }, this ) );
        var childRouteArray = this.getPrivilegeRoleRouteArray( privId, role.data );
        if ( childRouteArray ){
          for ( var j = 0 ; j < childRouteArray.length ; j++ ){
            routeArray[routeArray.length] = childRouteArray[j];
          }
        }        
      }
    }
    
    routeArray.sort();
    
    return routeArray;
  },
  getPrivilegeRoleRouteArray : function( privId, role ){
    var routeArray = [];
    if ( role.roles ){
      for ( var i = 0 ; i < role.roles.length ; i++ ){
        var childRole = this.roleDataStore.getAt( this.roleDataStore.findBy( 
            function( rec, recid ) {
              return rec.id == role.roles[i];
            }, this ) );
        var childRouteArray = this.getPrivilegeRoleRouteArray( privId, childRole.data );
        if ( childRouteArray ){
          for ( var j = 0 ; j < childRouteArray.length ; j++ ){
            routeArray[routeArray.length] = role.name + '||' + childRouteArray[j];
          }
        }
      }
    }
    if ( role.privileges ){
      for ( var i = 0 ; i < role.privileges.length ; i++ ){
        // Found a priv, add this role
        if ( role.privileges[i] == privId ){
          routeArray[routeArray.length] = role.name;
          break;
        }
      }
    }
    
    return routeArray;
  },
  getRoleIdFromPayload : function( role ) {
    if ( role.roleId ) {
      return role.roleId;
    }
    
    return role;
  }
} );

Sonatype.Events.addListener( 'userViewInit', function( cardPanel, rec, gridPanel ) {
  if ( rec.data.resourceURI ) {
    cardPanel.add( 
        new Sonatype.repoServer.UserPrivilegeBrowsePanel( 
            { payload: rec,
              tabTitle: 'Privilege Trace'} ) );
  }
} );