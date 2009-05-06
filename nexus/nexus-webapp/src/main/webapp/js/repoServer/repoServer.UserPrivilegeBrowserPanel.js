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
              xtype :'treepanel',
              columnWidth: .5,
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
            },
            {
              xtype :'fieldset',
              columnWidth: .5,
              border: false,
              checkboxToggle:false,
              collapsible: false,
              autoHeight:true,
              items: [
                {
                  xtype: 'label',
                  text: 'Select a privilege to see how it is assigned to the user.'
                },
                {
                  xtype: 'textarea',
                  name: 'privRouteList',
                  hideLabel: true,
                  height: 250,
                  width: 500,
                  readOnly: true,
                  wordWrap: false
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
    this.find('name', 'privRouteList')[0].setValue( '' )
  },
  handleNodeClicked : function( id ) {
    var routeArray = this.getPrivilegeRouteArray( id );
    var routeText = '';
    if ( routeArray ){
      for ( var i = 0 ; i < routeArray.length ; i++ ){
        routeText += routeArray[i] + '\n';
      }
    }
    
    this.find('name', 'privRouteList')[0].setValue( routeText );
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
        if ( this.roleHasPrivilege( privId, this.payload.data.roles[i].roleId ) ){
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
              return rec.id == userRoles[i].roleId;
            }, this ) );
        var childRouteArray = this.getPrivilegeRoleRouteArray( privId, role.data );
        if ( childRouteArray ){
          for ( var j = 0 ; j < childRouteArray.length ; j++ ){
            routeArray[routeArray.length] = childRouteArray[j];
          }
        }        
      }
    }
    
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
            routeArray[routeArray.length] = role.name + ' --> ' + childRouteArray[j];
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