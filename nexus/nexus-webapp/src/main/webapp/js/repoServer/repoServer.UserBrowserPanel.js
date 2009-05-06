Ext.tree.UserTreeLoader = function(config){
    Ext.tree.UserTreeLoader.superclass.constructor.call(this, config);
};

Ext.extend(Ext.tree.UserTreeLoader, Ext.tree.SonatypeTreeLoader, {  
  //override to request data according ot Sonatype's Nexus REST service
  requestData : function(node, callback){
      if(this.fireEvent("beforeload", this, node, callback) !== false){
          this.transId = Ext.Ajax.request({
              method:this.requestMethod,
              url: this.getResourceURIFromId( node.id ),
              success: this.handleResponse,
              failure: this.handleFailure,
              options: { dontForceLogout: true },
              scope: this,
              argument: {callback: callback, node: node},
              //disableCaching: false,                        
              params: this.getParams(node)
          });
      }else{
          // if the load is cancelled, make sure we notify
          // the node that we are done
          if(typeof callback == "function"){
              callback();
          }
      }
  },  
  processResponse : function(response, node, callback){
      var json = response.responseText;
      try {
          var o = eval("("+json+")");
          
          var roles = o.data.roles;
          var privs = o.data.privileges;
          
          node.beginUpdate();
          if ( roles ) {
            for(var i = 0, len = roles.length; i < len; i++){
                var n = this.createNode(node.id, roles[i], true );
                if(n){
                    node.appendChild(n);
                }
            }
          }
          if ( privs ) {
            for(var i = 0, len = privs.length; i < len; i++){
              var n = this.createNode(node.id, privs[i], false);
              if(n){
                  node.appendChild(n);
              }
            }
          }
          node.endUpdate();
          if(typeof callback == "function"){
              callback(this, node);
          }
      }catch(e){
          this.handleFailure(response);
      }
  },
  
  createNode : function(parentId, id, isRole){
      // apply baseAttrs, nice idea Corey!
      attr = {};
      if(this.baseAttrs){
          Ext.applyIf(attr, this.baseAttrs);
      }

      if ( isRole ) {
        var role = this.roleDataStore.getAt( this.roleDataStore.findBy( 
            function( rec, recid ) {
              return rec.id == id;
            }, this ) );
        attr.id = parentId + '$$' + Sonatype.config.repos.urls.roles + '/' + id + '/';
        attr.text = role.data.name;
        attr.qtip = role.data.description;
        attr.leaf = false;
      }
      else {
        var priv = this.privDataStore.getAt( this.privDataStore.findBy( 
            function( rec, recid ) {
              return rec.id == id;
            }, this ) );
        attr.id = parentId + '$$' + Sonatype.config.repos.urls.privileges + '/' + id + '/';
        attr.text = priv.data.name;
        attr.qtip = priv.data.description;
        attr.leaf = true;
      }

      if(this.applyLoader !== false){
          attr.loader = this;
      }
      
      if(typeof attr.uiProvider == 'string'){
         attr.uiProvider = this.uiProviders[attr.uiProvider] || eval(attr.uiProvider);
      }
      
      attr.singleClickExpand = true;                                      //diff
      
      return(isRole ?
                      new Ext.tree.AsyncTreeNode(attr) :
                      new Ext.tree.TreeNode(attr));
  },
  getResourceURIFromId : function( id ){
    var index = id.lastIndexOf( '$$' );
    if ( index == -1 ) {
      return id;
    }
    else {
      return id.substring( index + 2 );
    }
  }
});

Sonatype.repoServer.UserBrowsePanel = function( config ) {
  var config = config || {};
  var defaultConfig = { 
    titleColumn: 'name'
  };
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
      { name: 'description'}
    ],
    listeners: {
      load: {
        fn: function(){
            this.rolesLoaded = true;
            if ( this.privsLoaded ){
              this.root.reload();
              this.privsLoaded = false;
              this.rolesLoaded = false;
            }
          },
        scope: this
      }
    }
  } );
  
  this.roleDataStore.load();
  
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
        fn: function(){
            this.privsLoaded = true;
            if ( this.rolesLoaded ){
              this.root.reload();
              this.privsLoaded = false;
              this.rolesLoaded = false;
            }
          },
        scope: this
      }
    }
  } );
  
  this.privDataStore.load();
  
  Sonatype.repoServer.RepositoryBrowsePanel.superclass.constructor.call( this, {
    anchor: '0 -2',
    bodyStyle: 'background-color:#FFFFFF',
    animate: true,
    lines: false,
    autoScroll: true,
    containerScroll: true,
    rootVisible: true,
    enableDD: false,
    tbar: [
      {
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.refreshHandler
      }
    ],
    loader: new Ext.tree.UserTreeLoader( {
      url: '',
      roleDataStore: this.roleDataStore, 
      privDataStore: this.privDataStore,
      listeners: {
        loadexception: this.treeLoadExceptionHandler,
        scope: this
      }
    } ),
    listeners: {
      click: this.nodeClickHandler,
      scope: this
    } 
  } );

  new Ext.tree.TreeSorter( this, { folderSort: true } );

  var root = new Ext.tree.AsyncTreeNode( {
    text: this.payload.data[this.titleColumn],
    id: this.payload.data.resourceURI + '/',
    singleClickExpand: true,
    expanded: true
  } );
  
  this.setRootNode( root );
};

Ext.extend( Sonatype.repoServer.UserBrowsePanel, Ext.tree.TreePanel, {
  nodeClickHandler: function( node, e ) {
    if ( e.target.nodeName == 'A' ) return; // no menu on links

    if ( this.nodeClickEvent ) {
      Sonatype.Events.fireEvent( this.nodeClickEvent, node );
    }
  },

  refreshHandler: function( button, e ) {
    this.root.setText( this.payload ? this.payload.get( this.titleColumn ) : '/' );
    this.root.id = this.payload.data.resourceURI;
    
    //the load listener on these stores will reload the node
    this.roleDataStore.reload();
    this.privDataStore.reload();
  },
  treeLoadExceptionHandler : function( treeLoader, node, response ) {
    if ( !response.status == '200' ) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();
      }
      node.setText( node.text + ' (Error retrieving data)' );
    }
  }
  
} );

Sonatype.Events.addListener( 'userViewInit', function( cardPanel, rec, gridPanel ) {
  if ( rec.data.resourceURI ) {
    cardPanel.add( 
        new Sonatype.repoServer.UserBrowsePanel( 
            { payload: rec,
              tabTitle: 'Role Tree'} ) );
  }
} );