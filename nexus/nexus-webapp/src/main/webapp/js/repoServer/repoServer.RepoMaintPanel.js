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
 * Repository Maintenance panel layout and controller
 */

/* config options:
  {
    id: the is of this panel instance [required]
    title: title of this panel (shows in tab)
  }
*/

/** Old repository browser code - don't extend, see the new version below */

Sonatype.repoServer.RepoMaintPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  var forceStatuses = false;
  Ext.apply(this, config, defaultConfig);

  this.browseIndex = false;
  
  this.oldSearchText = '';
  this.searchTask = new Ext.util.DelayedTask( this.startSearch, this, [this]);

  Sonatype.Events.addListener( 'repositoryChanged', this.onRepoChange, this );
  Sonatype.Events.addListener( 'groupChanged', this.onRepoChange, this );

  this.actions = {
    view : {
      text: 'View',
      scope:this,
      handler: this.viewHandler
    },
    refreshList : {
      text: 'Refresh',
      iconCls: 'st-icon-refresh',
      scope:this,
      handler: this.reloadAll
    },
        putInService : {
          text: 'Put in Service',
          scope:this,
          handler: this.putInServiceHandler
        },
        putOutOfService : {
          text: 'Put Out of Service',
          scope:this,
          handler: this.putOutOfServiceHandler
        },
        allowProxy : {
          text: 'Allow Proxy',
          scope:this,
          handler: this.allowProxyHandler
        },
        blockProxy : {
          text: 'Block Proxy',
          scope:this,
          handler: this.blockProxyHandler
        }
  };
  
  this.browseTypeButtonConfig = {
          text: 'User Managed Repositories',
          value: 'user',
          tooltip: 'Click to browse other types of repositories.',
          handler: this.switchBrowseType,
          scope: this,
          menu: {
            items: [
              {
                text: 'User Managed Repositories',
                value: 'user',
                scope: this,
                handler: this.switchBrowseType
              },
              {
                text: 'Nexus Managed Repositories',
                value: 'nexus',
                scope: this,
                handler: this.switchBrowseType
              }
            ]
          }
        };
  
  this.browseTypeButton = new Ext.Button( this.browseTypeButtonConfig );
  
  this.detailPanelConfig = {
    //region: 'center',
    autoScroll: false,
    //autoWidth: true,
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
    labelWidth: 100,
    layoutConfig: {
      labelSeparator: ''
    },

    items: [
    ]
};
      

  this.groupRecordConstructor = Ext.data.Record.create([
    {name:'id'},
    {name:'repoType', convert: function(s, parent){return 'group';}},
    {name:'resourceURI'},
    {name:'format'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
    {name:'sStatus'},
    {name:'contentUri', mapping:'resourceURI', convert: this.restToContentUrl }
  ]);

  this.groupsReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.groupRecordConstructor );

  this.groupsDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.groups,
    reader: this.groupsReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: false,
    listeners: {
      'load' : {
        fn: function() {
          this.reposDataStore.insert( 0, this.groupsDataStore.data.items );
        },
        scope: this
      }
    }
  });

  
  // START: Repo list ******************************************************
  this.repoRecordConstructor = Ext.data.Record.create([
    {name:'id'},
    {name:'repoType'},
    {name:'format'},
    {name:'resourceURI'},
    {name:'status'},
    {name:'localStatus'/*, mapping: 'status'*/, convert: function(s, parent){return parent.status?parent.status.localStatus:null;}},
    {name:'remoteStatus'/*, mapping: 'status'*/, convert: function(s, parent){return parent.status?parent.status.remoteStatus:null;}},
    {name:'proxyMode'/*, mapping: 'status'*/, convert: function(s, parent){return parent.status?parent.status.proxyMode:null;}},
    {name:'sStatus', /*mapping:'status', */convert: this.statusTextMaker},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
//  {name:'effectiveLocalStorageUrl'},
    {name:'repoPolicy'},
    {name:'contentUri', mapping:'resourceURI', convert: this.restToContentUrl },
    {name:'remoteUri'},
    {name:'userManaged'},
    {name:'exposed'}
  ]);

  this.reposReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.repoRecordConstructor );

  this.repoStatusTask = {
    run: function() {
      Ext.Ajax.request( {
        url: Sonatype.config.repos.urls.repositoryStatuses + (this.forceStatuses ? '?forceCheck' : ''),
        callback: this.statusCallback,
        scope: this
      } );
      this.forceStatuses = false;
    },
    interval: 5000, // poll every 2 seconds
    scope: this
  };

  //@ext: must use data.Store (not JsonStore) to pass in reader instead of using fields config array
  this.reposDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.repositories,
    reader: this.reposReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true,
    listeners: {
      'load' : {
        fn: function() {
          this.searchField.triggers[1].hide();
          this.groupsDataStore.reload();
          Ext.TaskMgr.start(this.repoStatusTask);
        },
        scope: this
      }
    }
  });
  
  this.allReposDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.allRepositories,
    reader: this.reposReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true,
    listeners: {
      'load' : {
        fn: function() {
          this.searchField.triggers[1].hide();
          this.allReposDataStore.each(function(item,i,len){
              if ( item.data.userManaged == true )
              {
                  this.allReposDataStore.remove( item );
              }
              else
              {
                  item.data.sStatus = 'In Service';
              }
            },this);
        },
        scope: this
      }
    }
  });

  this.reposGridPanel = new Ext.grid.GridPanel({
    //title: 'Repositories',
    id: 'st-repos-maint-grid',
    
    region: 'north',
    layout:'fit',
    collapsible: true,
    split:true,
    height: 200,
    minHeight: 150,
    maxHeight: 400,
    frame: false,
    autoScroll: true,
    selModel: new Ext.grid.RowSelectionModel({
      singleSelect: true
    }),
    tbar: [
      this.actions.refreshList,
      this.browseTypeButton
    ],

    //grid view options
    ds: this.reposDataStore,
    sortInfo:{field: 'name', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      {header: 'Repository', dataIndex: 'name', width:175, renderer: function(value, metadata, record, rowIndex, colIndex, store){
          return record.get('repoType') == 'group' ? ( '<b>' + value + '</b>' ) : value;
        }},
      {header: 'Type', dataIndex: 'repoType', width:50},
      {header: 'Format', dataIndex: 'format', width:50},
      {header: 'Status', dataIndex: 'sStatus', width:250},
      {header: 'Repository Path', dataIndex: 'contentUri', id: 'repo-maint-url-col', width:250,renderer: function(s){return '<a href="' + s + ((s != null && (s.charAt(s.length)) == '/') ? '' : '/') +'" target="_blank">' + s + '</a>';},menuDisabled:true}      
    ],
    autoExpandColumn: 'repo-maint-url-col',
    disableSelection: false,
    viewConfig: {
      emptyText: 'No repositories currently configured'
    }
  });
  this.reposGridPanel.on('rowclick', this.repoRowClickHandler, this);
  this.reposGridPanel.on('rowcontextmenu', this.onContextClickHandler, this);
  // END: Repo List ******************************************************
  // *********************************************************************

  this.browseLocalMenuItem = new Ext.menu.CheckItem(          
    {
      text: 'Browse local storage',
      value: 0,
      checked: true,
      group:'browse-group',
      checkHandler: this.browseSelectorHandler,
      scope:this
    }
  );
  this.browseIndexMenuItem = new Ext.menu.CheckItem(
    {
      text: 'Browse index',
      value: 1,
      checked: false,
      group:'browse-group',
      checkHandler: this.browseSelectorHandler,
      scope:this
    }
  );
  this.browseSelector = new Ext.Toolbar.Button(          
    {
      text: 'Browse local storage',
      icon: Sonatype.config.resourcePath + '/images/icons/page_white_stack.png',
      value: 0,
      cls: 'x-btn-text-icon',
      menu:{
        id:'browse-content-menu',
        width:200,
        items: [
          this.browseLocalMenuItem,
          this.browseIndexMenuItem
        ]
      }
    }
  );
  
  Sonatype.repoServer.RepoMaintPanel.superclass.constructor.call(this, {
    layout: 'border',
    autoScroll: false,
    width: '100%',
    height: '100%',
    stateful: true,
    listeners: {
      'beforedestroy': {
        fn: function(){
          Ext.TaskMgr.stop( this.repoStatusTask );
          Sonatype.Events.removeListener( 'repositoryChanged', this.onRepoChange, this );
          Sonatype.Events.removeListener( 'groupChanged', this.onRepoChange, this );
        },
        scope: this
      }
    },
    items: [
      this.reposGridPanel,
      {
        xtype: 'panel',
        id: 'repo-maint-info',
        title: 'Repository Content',
        layout: 'card',
        region: 'center',
        activeItem: 0,
        deferredRender: false,
        autoScroll: false,
        frame: false,
        tbar: [
          {
            text: 'Refresh',
            iconCls: 'st-icon-refresh',
            scope:this,
            handler: this.reloadTree
          },
          ' ',
          'Path Lookup:',
          {
            xtype: 'nexussearchfield',
            searchPanel: this,
            width: 400,
            enableKeyEvents: true,
            listeners: {
              'keyup': {
                fn: function( field, event ) {
                  var key = event.getKey();
                  if ( ! event.isNavKeyPress() ) {
                    this.searchTask.delay( 200 );
                  }
                },
                scope: this
              },
              'render': function(c) {
                Ext.QuickTips.register({
                  target: c.getEl(),
                  text: 'Enter a complete path to lookup, for example org/sonatype/nexus'
                });
              }
            }
          },
          ' ',
          this.browseSelector
        ],
        items: [
          {
            xtype: 'panel',
            layout: 'fit',
            html: '<div class="little-padding">Select a repository to view it</div>'
          }
        ]
      }
    ]
  });

  this.formCards = this.findById('repo-maint-info');
};


Ext.extend(Sonatype.repoServer.RepoMaintPanel, Sonatype.repoServer.AbstractRepoPanel, {
  //default values
  title : 'Repositories',
  
//contentUriColRender: function(value, p, record, rowIndex, colIndex, store) {
//  return String.format('<a target="_blank" href="{0}">{0}</a>', value);
//},
  
  statusTextMaker : function(statusObj, parent){
    if ( ! parent.status ) return '<I>retrieving...</I>';
    
    var s = statusObj;
    var remoteStatus = (''+s.remoteStatus).toLowerCase();
    var sOut = (s.localStatus == 'inService') ? 'In Service' : 'Out of Service';
    
    if (parent.repoType == 'proxy'){

      if(s.proxyMode.search(/blocked/) === 0){
        sOut += (s.proxyMode == 'blockedAuto')
          ? ' - Remote Automatically Blocked'
          : ' - Remote Manually Blocked';
        sOut += (remoteStatus == 'available')
          ? ' and Available'
          : ' and Unavailable';
      }
      else { //allow
        if (s.localStatus == 'inService'){
          if (remoteStatus != 'available') {
            sOut += remoteStatus == 'unknown'
              ? ' - <I>checking remote...</I>'
              : ' - Attempting to Proxy and Remote Unavailable';
          }
        }
        else { //Out of service
          sOut += (remoteStatus == 'available')
            ? ' - Remote Available'
            : ' - Remote Unavailable';
        }
      }
    }
    
    return sOut;
  },

  onContextClickHandler : function(grid, index, e){
    this.onContextHideHandler();
    if ( this.browseTypeButton.value == 'nexus' )
    {
        return;
    }
    var repoStatusPriv = this.sp.checkPermission('nexus:repostatus', this.sp.EDIT);
    
    if ( e.target.nodeName == 'A' ) return; // no menu on links
    
    this.ctxRow = this.reposGridPanel.view.getRow(index);
    this.ctxRecord = this.reposGridPanel.store.getAt(index);
    Ext.fly(this.ctxRow).addClass('x-node-ctx');
    
    var isGroup = this.ctxRecord.get('repoType') == 'group';

    //@todo: would be faster to pre-render the six variations of the menu for whole instance
    var menu = new Sonatype.menu.Menu({
      id:'repo-maint-grid-ctx',
      payload: this.ctxRecord,
      scope: this,
      items: [
        this.actions.view
      ]
    });

    if(repoStatusPriv && this.ctxRecord.get('repoType') == 'proxy'){
      menu.add((this.ctxRecord.get('proxyMode') == 'allow')
                 ? this.actions.blockProxy
                 : this.actions.allowProxy
              );
    }
    
    if (repoStatusPriv && !isGroup ) {
      menu.add((this.ctxRecord.get('localStatus') == 'inService') 
               ? this.actions.putOutOfService
               : this.actions.putInService
            );
    }

      
    Sonatype.Events.fireEvent( 'repositoryMenuInit', menu, this.ctxRecord );
    
    menu.on('hide', this.onContextHideHandler, this);
    e.stopEvent();
    menu.showAt(e.getXY());
  },

  onContextHideHandler : function(){
    if(this.ctxRow){
      Ext.fly(this.ctxRow).removeClass('x-node-ctx');
      this.ctxRow = null;
      this.ctxRecord = null
    }
  },
  
  onBrowseContextClickHandler : function(node, e){
    this.onBrowseContextHideHandler();
    
    var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
    
    if ( rec.get( 'exposed' ) == false )
      return;
        
    
    var isProxyRepo = (node.getOwnerTree().root.attributes.repoType == 'proxy');
    var isGroup = (node.getOwnerTree().root.attributes.repoType == 'group');
    
    node.attributes.repoRecord = rec;
    this.ctxBrowseNode = node;
    this.ctxBrowseNode.data = this.ctxBrowseNode.attributes;
    
    var menu = new Sonatype.menu.Menu({
      id: 'repo-maint-browse-ctx',
      payload: node
    });

    if ( this.browseTypeButton.value != 'nexus' ) {
      Sonatype.Events.fireEvent( 'repositoryContentMenuInit', menu,
        this.reposGridPanel.store.getById(node.getOwnerTree().root.contentUrl),
        this.ctxBrowseNode );
    }

    if ( ! menu.items.first() ) return;

    menu.on('hide', this.onBrowseContextHideHandler, this);
    e.stopEvent();
    menu.showAt(e.getXY());
  },

  onBrowseContextHideHandler : function(){
    if(this.ctxBrowseNode){
      this.ctxBrowseNode = null;
    }
  },
  
  //for downloading artifacts from the browse view
  downloadHandler : function( item, button, event ){
    event.stopEvent();
    if(this.ctxBrowseNode){
      window.open(this.restToContentUrl(this.ctxBrowseNode.id));
    }
  },
  
  //for downloading artifacts from the browse view of the remote repository
  downloadFromRemoteHandler : function( item, button, event ){
    event.stopEvent();
    if(this.ctxBrowseNode){
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();      
      window.open(this.restToRemoteUrl(this.ctxBrowseNode,rec));
    }
  },  
  
  updateRepoStatuses : function(repoStatus){
    var rec = this.reposDataStore.getById(Sonatype.config.host + Sonatype.config.repos.urls.repositories + '/' + repoStatus.id);
    rec.beginEdit();
    rec.set('localStatus', repoStatus.localStatus);
    rec.set('remoteStatus', (repoStatus.remoteStatus)?repoStatus.remoteStatus:null);
    rec.set('proxyMode', (repoStatus.proxyMode)?repoStatus.proxyMode:null);
    rec.set('sStatus', this.statusTextMaker(repoStatus, rec.data));
    rec.commit();
    rec.endEdit();
    
    if(repoStatus.dependentRepos){
      Ext.each(repoStatus.dependentRepos, this.updateRepoStatuses, this);
    }
    
    Ext.TaskMgr.start(this.repoStatusTask);
  },
  
  beforeRenderHandler : function(component){
  },
  
  repoRowClickHandler : function(grid, rowIndex, e){
    var rec = grid.store.getAt(rowIndex);
    this.viewRepo(rec);
  },
  
  //rec is grid store record
  viewRepo : function(rec){
    var repoType = rec.get('repoType'); 
    if ( repoType != 'virtual' ) {
      this.browseSelector.enable();
    }
    else {
      this.browseLocalMenuItem.setChecked( true );
      this.browseSelector.disable();
    }
    this.searchField.triggers[1].show();
    
    //change in behavior.  Always load a new detail view until we work out all the cache
    // and browse dependencies
    
    var id = rec.id;
    //var config = this.detailPanelConfig;
    //config.id = id;
    //config = this.configUniqueIdHelper(id, rec.get('name'), rec.get('repoType'), config);
    var panel = new Ext.FormPanel(this.makeBrowseTree(id, rec.get('name'), rec.get('repoType')));
    panel.__repoRec = rec;
    
    //panel.on('beforerender', this.beforeRenderHandler, this);
//  panel.on('show', function(tp){
//    var temp = new Ext.tree.TreeSorter(tp, {folderSort:true});
//  },
//  this,
//  {single: true}
//  );
    
    var oldItem = this.formCards.getLayout().activeItem;
    this.formCards.remove(oldItem, true);
    this.formCards.insert(1, panel);

    if ( this.formCards.tbar ) {
      if ( this.formCards.tbar.oldSize ) {
        this.formCards.tbar.setSize( this.formCards.tbar.oldSize );
      }
      this.formCards.tbar.show();
    }
    
    //always set active and re-layout
    this.formCards.getLayout().setActiveItem(panel);
    panel.doLayout();
    

    //old behavior
//  var id = rec.id; //note: rec.id is unique for new repos and equal to resourceURI for existing ones
//  var panel = this.formCards.findById(id);
//  
//  if(!panel){ //create form and populate current data
//    var config = this.detailPanelConfig;
//    config.id = id;
//    config = this.configUniqueIdHelper(id, rec.get('name'), config);
//    panel = new Ext.Panel(config);
//
//    panel.on('beforerender', this.beforeRenderHandler, this);
//    
//    this.formCards.add(panel);
//  }
//  
//  //always set active and re-layout
//  this.formCards.getLayout().setActiveItem(panel);
//  panel.doLayout();    
  },
  
  //creates a unique config object with specific IDs on the two tree items
  configUniqueIdHelper : function(id, name, repoType, config){
    //@note: there has to be a better way to do this.  Depending on offsets is very error prone

    var newConfig = config;

    var trees = [
      {obj : newConfig.items[0], postpend : '_repo-browse'}
//    {obj : newConfig.items[0].items[0], postpend : '_repo-browse'}
    ];

    for (var i = 0; i<trees.length; i++) {
      trees[i].obj.title = name + ' Repository Content';
      trees[i].obj.id = id + trees[i].postpend;
      trees[i].obj.root = new Ext.tree.AsyncTreeNode({
                            text: name,
                            id: id + '/content/',
                            singleClickExpand: true,
                            expanded: true,
                            repoType: repoType
                          });
                          
      trees[i].obj.loader = new Ext.tree.SonatypeTreeLoader({
        dataUrl: '', //note: all node ids are their own full path
        listeners: {
          loadexception: this.treeLoadExceptionHandler,
          scope: this
        }
      });
    }

    return newConfig;
  },
  
  makeBrowseTree : function(id, name, repoType){
    var tp = new Ext.tree.TreePanel(
    {
      anchor: '0 -2',
      id: id + '_repo-browse',
      loader: null, //note: created uniquely per repo
      //note: this style matches the expected behavior
      bodyStyle: 'background-color:#FFFFFF',//; border: 1px solid #99BBE8',
      animate:true,
      lines: false,
      autoScroll:true,
      containerScroll: true,
      rootVisible: true,
      enableDD: false,
      loader : new Ext.tree.SonatypeTreeLoader({
        dataUrl: '', //note: all node ids are their own full path
        listeners: {
          loadexception: this.treeLoadExceptionHandler,
          scope: this
        }
      }),
      listeners: {
        contextmenu: this.onBrowseContextClickHandler,
        scope: this,
        expandnode: this.indexBrowserExpandFollowup
      }
    });
    
//    loader = new Ext.tree.SonatypeTreeLoader({
//      dataUrl: '', //note: all node ids are their own full path
//      listeners: {
//        loadexception: this.treeLoadExceptionHandler,
//        scope: this
//      },
//    });
    
    var temp = new Ext.tree.TreeSorter(tp, {folderSort:true});
    //note: async treenode needs to be added after sorter to avoid race condition where child node can appear unsorted
    
    var rNode = new Ext.tree.AsyncTreeNode({
      text: name,
      id: this.getBrowsePath( id ),
      singleClickExpand: true,
      expanded: true,
      repoType: repoType,
      listeners: {
        load: {
          fn: this.indexBrowserExpandFollowup,
          scope: this
        }
      }
    });
    rNode.contentUrl = id;
    
    tp.setRootNode(rNode);
    
    var uniqueConfig = {
      id : id + '_repo-browse-top',
      autoScroll: false,
      border: false,
      frame: false,
      collapsible: false,
      collapsed: false,
      labelWidth: 100,
      layoutConfig: {
        labelSeparator: ''
      },
      items: [tp]
    };
    
    return uniqueConfig;
  },
  
  treeLoadExceptionHandler : function(treeLoader, node, response){
    if (response.status == 503){
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();
      }
      node.setText(node.text + ' (Out of Service)');
    }
    else if ( response.status == 404 ) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();
      }
      node.setText( node.text + ( node.isRoot ? ' (Not Available)' : ' (Not Found)'));
    }
    else if ( response.status == 401) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();   
      }
      node.setText( node.text + ' (Access Denied)' );
    }
  },

  statusCallback : function(options, success, response) {
    if ( response.status != 202 ) {
      Ext.TaskMgr.stop(this.repoStatusTask);
    }

    if ( success ) {
      var statusResp = Ext.decode(response.responseText);
      if (statusResp.data) {
        var data = statusResp.data;
        for ( var i = data.length - 1; i >= 0; i-- ) {
          var item = data[i];
          var rec = this.reposDataStore.getById(item.resourceURI.replace(Sonatype.config.repos.urls.repositoryStatuses,Sonatype.config.repos.urls.repositories));
          if (rec) {
            rec.beginEdit();
            rec.set('status', item.status);
            rec.set('localStatus', item.status.localStatus);
            rec.set('remoteStatus', item.status.remoteStatus);
            rec.set('proxyMode', item.status.proxyMode);
            rec.set('sStatus', this.statusTextMaker(item.status, item));
            rec.commit(true);
            rec.endEdit();
          }
        }
        if ( data.length ) {
          this.reposGridPanel.getView().refresh();
        }
      }
    }
    else {
      Sonatype.MessageBox.alert('Status retrieval failed');
    }
  },
  
  onRepoChange: function() {
    this.reloadAll();
  },

  reloadAll : function( button, event ){
    if ( button ) {
      this.forceStatuses = true;
    }

    this.allReposDataStore.reload();
    this.reposDataStore.reload();
    this.formCards.items.each(function(item, i, len){
      this.remove(item, true);
    }, this.formCards);
    
    this.formCards.add({
      xtype: 'panel',
      layout: 'fit',
      html: '<div class="little-padding">Select a repository to view it</div>'
    });
    this.formCards.getLayout().setActiveItem(0);
  },
  
  startSearch: function( p ) {
    var field = p.searchField;
    var searchText = field.getRawValue();

    var activePanel = p.formCards.getLayout().activeItem;
    if ( activePanel ) {
      var treePanel = activePanel.items.first();
      if ( searchText ) {
        field.triggers[0].show();
        var justEdited = p.oldSearchText.length > searchText.length;

        var findMatchingNodes = function( root, textToMatch ) {
          var n = textToMatch.indexOf( '/' );
          var remainder = '';
          if ( n > -1 ) {
            remainder = textToMatch.substring( n + 1 );
            textToMatch = textToMatch.substring( 0, n );
          }

          var matchingNodes = [];
          var found = false;
          for ( var i = 0; i < root.childNodes.length; i++ ) {
            var node = root.childNodes[i];

            var text = node.text;
            if ( text == textToMatch ) {
              node.enable();
              node.ensureVisible();
              node.expand();
              found = true;
              if ( ! node.isLeaf() ) {
                var autoComplete = false;
                if ( ! remainder && node.childNodes.length == 1 ) {
                  remainder = node.firstChild.text;
                  autoComplete = true;
                }
                if ( remainder ) {
                  var s = findMatchingNodes( node, remainder );
                  if ( autoComplete || ( s && s != remainder ) ) {
                    return textToMatch + '/' + ( s ? s : remainder );
                  }
                }
              }
            }
            else if ( text.substring( 0, textToMatch.length ) == textToMatch ) {
              matchingNodes[matchingNodes.length] = node;
              node.enable();
              if ( matchingNodes.length == 1 ) {
                node.ensureVisible();
              }
            }
            else {
              node.disable();
              node.collapse( false, false );
            }
          }
          
          // if only one non-exact match found, suggest the name
          return ! found && matchingNodes.length == 1 ?
            matchingNodes[0].text + '/' : null;
        };
        
        var s = findMatchingNodes( treePanel.root, searchText );

        p.oldSearchText = searchText;

        // if auto-complete is suggested, and the user hasn't just started deleting
        // their own typing, try the suggestion
        if ( s && ! justEdited && s != searchText ) {
          field.setRawValue( s );
          p.startSearch( p );
        }

      }
      else {
        p.stopSearch( p );
      }
    }
  },

  stopSearch: function( p ) {
    p.searchField.triggers[0].hide();
    p.oldSearchText = '';

    var activePanel = p.formCards.getLayout().activeItem;
    if ( activePanel ) {
      var treePanel = activePanel.items.first();

      var enableAll = function( root ) {
        for ( var i = 0; i < root.childNodes.length; i++ ) {
          var node = root.childNodes[i];
          node.enable();
          node.collapse( false, false );
          enableAll( node );
        }
      };
      enableAll( treePanel.root );
    }
  },

  browseSelectorHandler: function( item, e ) {
    if ( this.browseSelector.value != item.value ) {
      this.browseSelector.value = item.value;
      this.browseSelector.setText( item.text );
      this.browseIndex = item.value == 1

      this.reloadTree();
    }
  },
  
  getBrowsePath: function( baseUrl ) {
    return baseUrl + this.getBrowsePathSnippet() + '/'; 
  },

  getBrowsePathSnippet: function() {
    return this.browseIndex ?
      Sonatype.config.browseIndexPathSnippet : Sonatype.config.browsePathSnippet;
  },
  
  reloadTree: function() {
    var activePanel = this.formCards.getLayout().activeItem;
    if ( activePanel ) {
      var treePanel = activePanel.items.first();
      if ( treePanel ) {
        var root = treePanel.root;
        var i = root.text.search(/\(.*\)$/);
        if(i > -1){
          root.setText(root.text.slice(0, i-1));
        }
        root.id = this.getBrowsePath( root.contentUrl );
        root.attributes.localStorageUpdated = false;
        root.reload();
      }
    }
  },
  
  indexBrowserExpandFollowup: function( node ) {
    if ( this.browseIndex && ! node.attributes.localStorageUpdated && node.firstChild ) {
      node.attributes.localStorageUpdated = true;
      Ext.Ajax.request({
        url: node.id.replace( Sonatype.config.browseIndexPathSnippet, Sonatype.config.browsePathSnippet ) + '?isLocal',
        suppressStatus: 404,
        success: function( response, options ) {
          var decodedResponse = Ext.decode( response.responseText );
          if ( decodedResponse.data ) {
            var data = decodedResponse.data;
            for ( var j = 0; j < node.childNodes.length; j++ ) {
              var indexNode = node.childNodes[j];
              indexNode.attributes.localStorageUpdated = true;
              for ( var i = 0; i < data.length; i++ ) {
                var contentNode = data[i];
                if ( contentNode.text == indexNode.text ) {
                  indexNode.ui.iconNode.className = 'x-tree-node-nexus-icon';
                  indexNode.attributes.localStorageUpdated = false;
                  break;
                }
              }
            }
          }
        },
        failure: function( response, options ) {
          for ( var j = 0; j < node.childNodes.length; j++ ) {
            node.childNodes[j].attributes.localStorageUpdated = true;
          }
        },
        scope: this
      });
    }
  },
  switchBrowseType: function( button, event ) {
    this.setBrowseType( this, button.value );
    
    if ( button.value == 'nexus' )
    {
        this.reposGridPanel.store = this.allReposDataStore;
        this.reposGridPanel.getView().refresh();
    }
    else if ( button.value == 'user' )
    {
        this.reposGridPanel.store = this.reposDataStore;
        this.reposGridPanel.getView().refresh();
    }
  },

  setBrowseType: function( panel, t ) {
    if ( t != panel.browseTypeButton.value ) {
      var items = panel.browseTypeButtonConfig.menu.items;
      panel.browseTypeButton.value = t;
      for ( var i = 0; i < items.length; i++ ) {
        if ( items[i].value == t ) {
          panel.browseTypeButton.setText( items[i].text );
        }
      }
    }
  }
});


/** New repository browser starts here */

Sonatype.repoServer.RepositoryPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Repositories'
  };
  Ext.apply( this, config, defaultConfig );

  var sp = Sonatype.lib.Permissions;

  var toolbar = sp.checkPermission( 'nexus:wastebasket', sp.DELETE ) ? 
    [ {
      id: 'repo-trash-btn',
      text: 'Trash...',
      icon: Sonatype.config.resourcePath + '/images/icons/user-trash.png',
      cls: 'x-btn-text-icon',
      tooltip: { title: 'Trash', text: 'Manage the Trash contents' },
      menu: {
        width:125,
        items: [
          {
            text: 'Empty Trash',
            handler: this.deleteTrashHandler,
            scope: this
          }
        ]
      }
    } ] : null;
  
  Sonatype.repoServer.RepositoryPanel.superclass.constructor.call( this, {
    addMenuInitEvent: 'repositoryAddMenuInit',
    deleteButton: sp.checkPermission( 'nexus:repositories', sp.DELETE ),
    rowClickEvent: 'repositoryViewInit',
    rowContextClickEvent: 'repositoryMenuInit',
    url: Sonatype.config.repos.urls.repositories,
    tabbedChildren: true,
    tbar: toolbar,
    columns: [
      { name: 'resourceURI' },
      { name: 'remoteUri' },
      { name: 'id' },
      {
        name: 'name',
        sortType: Ext.data.SortTypes.asUCString,
        header: 'Repository', 
        width: 175,
        renderer: function( value, metadata, record, rowIndex, colIndex, store ) {
          return record.get('repoType') == 'group' ? ( '<b>' + value + '</b>' ) : value;
        }
      },
      { 
        name: 'repoType',
        header: 'Type', 
        width:50 
      },
      { 
        name: 'format',
        header: 'Format', 
        width: 50 
      },
      { 
        name: 'repoPolicy',
        header: 'Policy',
        width: 70
      },
      { 
        name: 'displayURI',
        header: 'Repository Path',
        autoExpand: true,
        renderer: function( s ) {
          return '<a href="' + s + ((s != null && (s.charAt(s.length)) == '/') ? '' : '/') +
            '" target="_blank">' + s + '</a>';
        },
        mapping: 'resourceURI',
        convert: function( s, parent ) {
          return s.replace(
            Sonatype.config.repos.urls.repositories, Sonatype.config.content.repositories );
        }
      }
    ]
  } );
};

Ext.extend( Sonatype.repoServer.RepositoryPanel, Sonatype.panels.GridViewer, {
  deleteTrashHandler: function( button, e ) {
    Sonatype.utils.defaultToNo();
    
    Sonatype.MessageBox.show( {
      animEl: this.gridPanel,
      title: 'Empty Trash',
      msg : 'Delete the entire contents of the Trash?<br><br>This operation cannot be undone!',
      buttons: Sonatype.MessageBox.YESNO,
      scope: this,
      icon: Sonatype.MessageBox.QUESTION,
      fn: function(btnName){
        if ( btnName == 'yes' || btnName == 'ok' ) {
          Ext.Ajax.request( {
            callback: function( options, success, response ) {
              if ( ! success ) {
                Sonatype.utils.connectionError( response, 'Error emptying the trash!' );
              }
            },
            scope: this,
            method: 'DELETE',
            url:Sonatype.config.repos.urls.trash
          } );
        }
      }
    } );
  }
} );

Sonatype.repoServer.RepositoryBrowsePanel = function( config ) {
  var config = config || {};
  var defaultConfig = { 
    titleColumn: 'name'
  };
  Ext.apply( this, config, defaultConfig );
  
  this.oldSearchText = '';
  this.searchTask = new Ext.util.DelayedTask( this.startSearch, this, [this]);
  this.nodeContextMenuEvent = 'repositoryContentMenuInit';

  this.browseSelector = new Ext.Toolbar.Button(          
    {
      text: 'Browse Local Storage',
      icon: Sonatype.config.resourcePath + '/images/icons/page_white_stack.png',
      value: 0,
      cls: 'x-btn-text-icon',
      menu: {
        items: [
          {
            text: 'Browse Local Storage',
            value: 0,
            checked: true,
            group: 'repo-browse-selector-group',
            checkHandler: this.browseSelectorHandler,
            scope: this
          },
          {
            text: 'Browse Index',
            value: 1,
            checked: false,
            group: 'repo-browse-selector-group',
            checkHandler: this.browseSelectorHandler,
            scope: this,
            disabled: this.payload.data.repoType == 'virtual'
          }
        ]
      }
    }
  );
  
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
      },
      ' ',
      'Path Lookup:',
      {
        xtype: 'nexussearchfield',
        searchPanel: this,
        width: 400,
        enableKeyEvents: true,
        listeners: {
          'keyup': {
            fn: function( field, event ) {
              var key = event.getKey();
              if ( ! event.isNavKeyPress() ) {
                this.searchTask.delay( 200 );
              }
            },
            scope: this
          },
          'render': function(c) {
            Ext.QuickTips.register({
              target: c.getEl(),
              text: 'Enter a complete path to lookup, for example org/sonatype/nexus'
            });
          }
        }
      },
      ' ',
      this.browseSelector
    ],
    loader: new Ext.tree.SonatypeTreeLoader( {
      url: '',
      listeners: {
        loadexception: this.treeLoadExceptionHandler,
        scope: this
      }
    } ),
    listeners: {
      click: this.nodeClickHandler,
      contextMenu: this.nodeContextMenuHandler,
      expandnode: this.indexBrowserExpandFollowup,
      scope: this
    } 
  } );

  new Ext.tree.TreeSorter( this, { folderSort:true } );

  var root = new Ext.tree.AsyncTreeNode( {
    text: this.payload.data[this.titleColumn],
    id: this.payload.data.resourceURI + Sonatype.config.browsePathSnippet + '/',
    singleClickExpand: true,
    expanded: true
  } );
  
  this.setRootNode( root );
};

Ext.extend( Sonatype.repoServer.RepositoryBrowsePanel, Ext.tree.TreePanel, {

  browseSelectorHandler: function( item, e ) {
    if ( this.browseSelector.value != item.value ) {
      this.browseSelector.value = item.value;
      this.browseSelector.setText( item.text );
      this.browseIndex = item.value == 1;

      this.refreshHandler( item, e );
    }
  },

  getBrowsePath: function( baseUrl ) {
    return baseUrl + this.getBrowsePathSnippet() + '/'; 
  },

  getBrowsePathSnippet: function() {
    return this.browseIndex ?
      Sonatype.config.browseIndexPathSnippet : Sonatype.config.browsePathSnippet;
  },
  
  indexBrowserExpandFollowup: function( node ) {
    if ( this.browseIndex && ! node.attributes.localStorageUpdated && node.firstChild ) {
      node.attributes.localStorageUpdated = true;
      Ext.Ajax.request({
        url: node.id.replace( Sonatype.config.browseIndexPathSnippet, Sonatype.config.browsePathSnippet ) + '?isLocal',
        suppressStatus: 404,
        success: function( response, options ) {
          var decodedResponse = Ext.decode( response.responseText );
          if ( decodedResponse.data ) {
            var data = decodedResponse.data;
            for ( var j = 0; j < node.childNodes.length; j++ ) {
              var indexNode = node.childNodes[j];
              indexNode.attributes.localStorageUpdated = true;
              for ( var i = 0; i < data.length; i++ ) {
                var contentNode = data[i];
                if ( contentNode.text == indexNode.text ) {
                  indexNode.ui.iconNode.className = 'x-tree-node-nexus-icon';
                  indexNode.attributes.localStorageUpdated = false;
                  break;
                }
              }
            }
          }
        },
        failure: function( response, options ) {
          for ( var j = 0; j < node.childNodes.length; j++ ) {
            node.childNodes[j].attributes.localStorageUpdated = true;
          }
        },
        scope: this
      });
    }
  },

  nodeClickHandler: function( node, e ) {
    if ( e.target.nodeName == 'A' ) return; // no menu on links

    if ( this.nodeClickEvent ) {
      Sonatype.Events.fireEvent( this.nodeClickEvent, node );
    }
  },
  
  nodeContextMenuHandler: function( node, e ) {
    if ( e.target.nodeName == 'A' ) return; // no menu on links

    if ( this.nodeContextMenuEvent ) { 

      node.attributes.repoRecord = this.payload;
      node.data = node.attributes;
  
      var menu = new Sonatype.menu.Menu( {
        payload: node,
        scope: this,
        items: []
      } );
  
      Sonatype.Events.fireEvent( this.nodeContextMenuEvent, menu, this.payload, node );
      if ( ! menu.items.first() ) return;

      e.stopEvent();
      menu.showAt( e.getXY() );
    }
  },

  refreshHandler: function( button, e ) {
    this.root.setText( this.payload ? this.payload.get( this.titleColumn ) : '/' );
    this.root.attributes.localStorageUpdated = false;
    this.root.id = this.getBrowsePath( this.payload.data.resourceURI );
    this.root.reload();
  },

  startSearch: function( p ) {
    var field = p.searchField;
    var searchText = field.getRawValue();

    var treePanel = p;
    if ( searchText ) {
      field.triggers[0].show();
      var justEdited = p.oldSearchText.length > searchText.length;

      var findMatchingNodes = function( root, textToMatch ) {
        var n = textToMatch.indexOf( '/' );
        var remainder = '';
        if ( n > -1 ) {
          remainder = textToMatch.substring( n + 1 );
          textToMatch = textToMatch.substring( 0, n );
        }

        var matchingNodes = [];
        var found = false;
        for ( var i = 0; i < root.childNodes.length; i++ ) {
          var node = root.childNodes[i];

          var text = node.text;
          if ( text == textToMatch ) {
            node.enable();
            node.ensureVisible();
            node.expand();
            found = true;
            if ( ! node.isLeaf() ) {
              var autoComplete = false;
              if ( ! remainder && node.childNodes.length == 1 ) {
                remainder = node.firstChild.text;
                autoComplete = true;
              }
              if ( remainder ) {
                var s = findMatchingNodes( node, remainder );
                if ( autoComplete || ( s && s != remainder ) ) {
                  return textToMatch + '/' + ( s ? s : remainder );
                }
              }
            }
          }
          else if ( text.substring( 0, textToMatch.length ) == textToMatch ) {
            matchingNodes[matchingNodes.length] = node;
            node.enable();
            if ( matchingNodes.length == 1 ) {
              node.ensureVisible();
            }
          }
          else {
            node.disable();
            node.collapse( false, false );
          }
        }
        
        // if only one non-exact match found, suggest the name
        return ! found && matchingNodes.length == 1 ?
          matchingNodes[0].text + '/' : null;
      };
      
      var s = findMatchingNodes( treePanel.root, searchText );

      p.oldSearchText = searchText;

      // if auto-complete is suggested, and the user hasn't just started deleting
      // their own typing, try the suggestion
      if ( s && ! justEdited && s != searchText ) {
        field.setRawValue( s );
        p.startSearch( p );
      }

    }
    else {
      p.stopSearch( p );
    }
  },

  stopSearch: function( p ) {
    p.searchField.triggers[0].hide();
    p.oldSearchText = '';

    var treePanel = p;

    var enableAll = function( root ) {
      for ( var i = 0; i < root.childNodes.length; i++ ) {
        var node = root.childNodes[i];
        node.enable();
        node.collapse( false, false );
        enableAll( node );
      }
    };
    enableAll( treePanel.root );
  },

  treeLoadExceptionHandler : function( treeLoader, node, response ) {
    if ( response.status == 503 ) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();
      }
      node.setText( node.text + ' (Out of Service)' );
    }
    else if ( response.status == 404 ) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();
      }
      node.setText( node.text + ( node.isRoot ? ' (Not Available)' : ' (Not Found)' ) );
    }
    else if ( response.status == 401 ) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();   
      }
      node.setText( node.text + ' (Access Denied)' );
    }
  }
} );

Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec ) {
  if ( rec.data.resourceURI ) {
    cardPanel.add( new Sonatype.repoServer.RepositoryBrowsePanel( { 
      payload: rec,
      tabTitle: 'Browse'
    } ) );
  }
} );
