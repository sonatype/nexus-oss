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
 * Repository Maintenance panel layout and controller
 */

/* config options:
  {
    id: the is of this panel instance [required]
    title: title of this panel (shows in tab)
    editMode: true, to allow edit control of repositories
  }
*/


Sonatype.repoServer.RepoMaintPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  var forceStatuses = false;
  Ext.apply(this, config, defaultConfig);

  Sonatype.Events.addListener( 'repositoryChanged', this.onRepoChange, this );
  Sonatype.Events.addListener( 'groupChanged', this.onRepoChange, this );

  this.actions = {
    view : new Ext.Action({
      text: 'View',
      scope:this,
      handler: this.viewHandler
    }),
    refreshList : new Ext.Action({
      text: 'Refresh',
      iconCls: 'st-icon-refresh',
      scope:this,
      handler: this.reloadAll
    }),
    download : new Ext.Action({
      text: 'Download',
      scope:this,
      handler: this.downloadHandler
    }),
    downloadFromRemote : new Ext.Action({
      text: 'Download From Remote',
      scope:this,
      handler: this.downloadFromRemoteHandler
    }),
    viewRemote : new Ext.Action({
      text: 'View Remote',
      scope:this,
      handler: this.downloadFromRemoteHandler
    })
  };
  
  if (this.editMode) {
    Ext.apply(this.actions,
      {
        clearCache : new Ext.Action({
          text: 'Clear Cache',
          scope:this,
          handler: this.clearCacheHandler
        }),
        deleteRepoItem : new Ext.Action({
          text: 'Delete',
          scope:this,
          handler: this.deleteRepoItemHandler
        }),
        reIndex : new Ext.Action({
          text: 'Re-Index',
          scope:this,
          handler: this.reIndexHandler
        }),
        rebuildAttributes : new Ext.Action({
          text: 'Rebuild Attributes',
          scope:this,
          handler: this.rebuildAttributesHandler
        }),
        putInService : new Ext.Action({
          text: 'Put in Service',
          scope:this,
          handler: this.putInServiceHandler
        }),
        putOutOfService : new Ext.Action({
          text: 'Put Out of Service',
          scope:this,
          handler: this.putOutOfServiceHandler
        }),
        allowProxy : new Ext.Action({
          text: 'Allow Proxy',
          scope:this,
          handler: this.allowProxyHandler
        }),
        blockProxy : new Ext.Action({
          text: 'Block Proxy',
          scope:this,
          handler: this.blockProxyHandler
        }),
        uploadArtifact: new Ext.Action({
          text: 'Upload Artifact...',
          scope: this,
          handler: this.uploadArtifactHandler
        })
      }
    );
  };
  
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
    //  items: [
    //    {
    //      xtype: 'panel',
    //      layout: 'column',
    //      autoWidth: true,
    //      //height: 400,
    //      autoHeight: true,
    //      //style: 'padding: 10px 0 0 0',

    items: [
//    {
//      //columnWidth: .5,
//      xtype: 'panel',
//      layout: 'fit',
//      //title: 'Additional Repository Info',
//      //autoHeight: true,
//      autoScroll: false,
//      border: false,
//      frame: true,
//      collapsible: false,
//      collapsed: false
//      //,
//      //contentEl: '<p>Additional Repository Info Here</p>'
//    },

//    {
//      xtype: 'treepanel',
//      anchor: '0 -2',
//      id: '_repo-browse', //note: unique ID is assinged before instantiation
//      title: 'Repository Content',
//      border: true,
//      bodyBorder: true,
//      loader: null, //note: created uniquely per repo
//      //note: this style matches the expected behavior
//      bodyStyle: 'background-color:#FFFFFF; border: 1px solid #99BBE8',
//      animate:true,
//      lines: false,
//      autoScroll:true,
//      containerScroll: true,
//      rootVisible: true,
//      enableDD: false,
//      tools: [
//        {
//          id: 'refresh',
//          handler: function(e, toolEl, panel){
//            var i = panel.root.text.search(/\(Out of Service\)/);
//            if(i > -1){
//              panel.root.setText(panel.root.text.slice(0, i-1));
//            }
//            panel.root.reload();
//          }
//        }
//      ],
//      listeners: {
//        contextmenu: this.onBrowseContextClickHandler,
//        scope: this
//      }
//    }
    ]
};
      

  this.restToContentUrl = function(r) {
    var isGroup = r.indexOf( Sonatype.config.repos.urls.groups ) > -1;
    var hasHost = r.indexOf(Sonatype.config.host) > -1;
    
    r = r.replace(Sonatype.config.browsePathSnippet, '');

    if ( isGroup ) {
      r = r.replace(Sonatype.config.repos.urls.groups, Sonatype.config.content.groups);
    }
    else {
      r = r.replace(Sonatype.config.repos.urls.repositories, Sonatype.config.content.repositories);
    }

    return hasHost ? r : ( Sonatype.config.host + r );
  };
  
  this.restToRemoteUrl = function(restUrl, repoRecord) {
    return repoRecord.get('remoteUri') + restUrl.replace(Sonatype.config.browsePathSnippet, '').replace(repoRecord.get('resourceURI'), '');
  };

  this.groupRecordConstructor = Ext.data.Record.create([
    {name:'repoType', convert: function(s, parent){return 'group';}},
    {name:'resourceURI'},
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
    {name:'repoType'},
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
    {name:'remoteUri'}
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
          this.groupsDataStore.reload();
          Ext.TaskMgr.start(this.repoStatusTask);
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
      this.actions.refreshList
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
      {header: 'Status', dataIndex: 'sStatus', width:300},
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

  Sonatype.repoServer.RepoMaintPanel.superclass.constructor.call(this, {
    layout: 'border',
    autoScroll: false,
    width: '100%',
    height: '100%',
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
        title: 'Repository Information',
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
  editMode : false,
  
//contentUriColRender: function(value, p, record, rowIndex, colIndex, store) {
//  return String.format('<a target="_blank" href="{0}">{0}</a>', value);
//},
  
  statusTextMaker : function(statusObj, parent){
    if ( ! parent.status ) return '<I>retrieving...</I>';
    
    var s = statusObj;
    var sOut = (s.localStatus == 'inService') ? 'In Service' : 'Out of Service';
    
    if (parent.repoType == 'proxy'){
      if(s.proxyMode.search(/blocked/) === 0){
        sOut += (s.proxyMode == 'blockedAuto')
          ? ' - Remote Automatically Blocked'
          : ' - Remote Manually Blocked';
        sOut += (s.remoteStatus == 'available')
          ? ' and Available'
          : ' and Unavailable';
      }
      else { //allow
        if (s.localStatus == 'inService'){
          if (s.remoteStatus != 'available') {
            sOut += s.remoteStatus == 'unknown'
              ? ' - <I>checking remote...</I>'
              : ' - Attempting to Proxy and Remote Unavailable';
          }
        }
        else { //Out of service
          sOut += (s.remoteStatus == 'available')
            ? ' - Remote Available'
            : ' - Remote Unavailable';
        }
      }
    }
    
    return sOut;
  },

  onContextClickHandler : function(grid, index, e){
    this.onContextHideHandler();
    
    if ( e.target.nodeName == 'A' ) return; // no menu on links
    
    this.ctxRow = this.reposGridPanel.view.getRow(index);
    this.ctxRecord = this.reposGridPanel.store.getAt(index);
    Ext.fly(this.ctxRow).addClass('x-node-ctx');
    
    var isGroup = this.ctxRecord.get('repoType') == 'group';

    //@todo: would be faster to pre-render the six variations of the menu for whole instance
    var menu = new Ext.menu.Menu({
      id:'repo-maint-grid-ctx',
      items: [
        this.actions.view
      ]
    });
    
    if (this.editMode) {
      if(this.ctxRecord.get('repoType') != 'virtual'){
        menu.add(this.actions.clearCache);
      }
      
      menu.add(this.actions.reIndex);
      menu.add(this.actions.rebuildAttributes);

      if(this.ctxRecord.get('repoType') == 'proxy'){
        menu.add((this.ctxRecord.get('proxyMode') == 'allow')
                   ? this.actions.blockProxy
                   : this.actions.allowProxy
                );
      }
      
      if ( !isGroup ) {
        menu.add((this.ctxRecord.get('localStatus') == 'inService') 
                 ? this.actions.putOutOfService
                 : this.actions.putInService
              );
      }

      if (this.ctxRecord.get('repoType') == 'hosted'
      && this.ctxRecord.get('repoPolicy') == 'release'){
        menu.add(this.actions.uploadArtifact);
      }
    }
    
    menu.on('hide', this.onContextHideHandler, this);
    e.stopEvent();
    menu.showAt(e.getXY());
  },

  onContextHideHandler : function(){
    if(this.ctxRow){
      Ext.fly(this.ctxRow).removeClass('x-node-ctx');
      this.ctxRow = null;
      this.ctxRecord = null;
    }
  },
  
  onBrowseContextClickHandler : function(node, e){
    this.onBrowseContextHideHandler();
    
    var isVirtualRepo = (node.getOwnerTree().root.attributes.repoType == 'virtual');
    var isProxyRepo = (node.getOwnerTree().root.attributes.repoType == 'proxy');
    var isGroup = (node.getOwnerTree().root.attributes.repoType == 'group');
    
    if (node.isLeaf() || this.editMode){
      this.ctxBrowseNode = node;
      
      var menu = new Ext.menu.Menu({
        id:'repo-maint-browse-ctx'
      });
      
      if (this.editMode) {
        if (!isVirtualRepo){
          menu.add(this.actions.clearCache);
        }
        menu.add(this.actions.reIndex);
        menu.add(this.actions.rebuildAttributes);
      }
      
      if (node.isLeaf()){
        if (isProxyRepo){
          menu.add(this.actions.downloadFromRemote);
        }
        menu.add(this.actions.download);
      }
      
      if (this.editMode && !node.isRoot && !isGroup){
        menu.add(this.actions.deleteRepoItem);
        if (isProxyRepo && !node.isLeaf()){
          menu.add(this.actions.viewRemote);
        }
      }

      menu.on('hide', this.onBrowseContextHideHandler, this);
      e.stopEvent();
      menu.showAt(e.getXY());
    }
  },

  onBrowseContextHideHandler : function(){
    if(this.ctxBrowseNode){
      this.ctxBrowseNode = null;
    }
  },
  
  //for downloading artifacts from the browse view
  downloadHandler : function(){
    if(this.ctxBrowseNode){
      window.open(this.restToContentUrl(this.ctxBrowseNode.id));
    }
  },
  
  //for downloading artifacts from the browse view of the remote repository
  downloadFromRemoteHandler : function(){
    if(this.ctxBrowseNode){
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();      
      window.open(this.restToRemoteUrl(this.ctxBrowseNode.id,rec));
    }
  },  
  
  deleteRepoItemHandler : function(){
    if (this.ctxBrowseNode){      
      var url = Sonatype.config.repos.urls.repositories + this.ctxBrowseNode.id.slice(Sonatype.config.host.length + Sonatype.config.repos.urls.repositories.length);
      //make sure to provide /content path for repository root requests like ../repositories/central
      if (/.*\/repositories\/[^\/]*$/i.test(url)){
        url += '/content';
      }
      Sonatype.MessageBox.show({
        animEl: this.reposGridPanel.getEl(),
        title : 'Delete Repository Item?',
        msg : 'Delete the selected file/folder?',
        buttons: Sonatype.MessageBox.YESNO,
        scope: this,
        icon: Sonatype.MessageBox.QUESTION,
        fn: function(btnName){
          if (btnName == 'yes' || btnName == 'ok') {
            Ext.Ajax.request({
              url: url,
              callback: this.deleteRepoItemCallback,
              scope: this,
              method: 'DELETE'
            });
          }
        }
      });
    }
  },
  
  deleteRepoItemCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      if (this.ctxRecord || this.reposGridPanel.getSelectionModel().hasSelection()){
        this.viewRepo((this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected());
      }
    }
    else {
      Sonatype.MessageBox.alert('The server did not delete the file/folder from the repository');
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
//  var sp = Sonatype.lib.Permissions;
//  if(sp.checkPermission(Sonatype.user.curr.repoServer.configRepos, sp.EDIT)){
//    component.buttons[0].disabled = false;
//  }
  },
  
  repoRowClickHandler : function(grid, rowIndex, e){
    var rec = grid.store.getAt(rowIndex);
    this.viewRepo(rec);
  },
  
  //rec is grid store record
  viewRepo : function(rec){
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
      title: name + ' Repository Content',
      border: true,
      bodyBorder: true,
      loader: null, //note: created uniquely per repo
      //note: this style matches the expected behavior
      bodyStyle: 'background-color:#FFFFFF; border: 1px solid #99BBE8',
      animate:true,
      lines: false,
      autoScroll:true,
      containerScroll: true,
      rootVisible: true,
      enableDD: false,
      tools: [
        {
          id: 'refresh',
          handler: function(e, toolEl, panel){
            var i = panel.root.text.search(/\(Out of Service\)/);
            if(i > -1){
              panel.root.setText(panel.root.text.slice(0, i-1));
            }
            panel.root.reload();
          }
        }
      ],
      loader : new Ext.tree.SonatypeTreeLoader({
        dataUrl: '', //note: all node ids are their own full path
        listeners: {
          loadexception: this.treeLoadExceptionHandler,
          scope: this
        }
      }),
      listeners: {
        contextmenu: this.onBrowseContextClickHandler,
        scope: this
      }
    });
    
    loader = new Ext.tree.SonatypeTreeLoader({
      dataUrl: '', //note: all node ids are their own full path
      listeners: {
        loadexception: this.treeLoadExceptionHandler,
        scope: this
      }
    });
    
    var temp = new Ext.tree.TreeSorter(tp, {folderSort:true});
    //note: async treenode needs to be added after sorter to avoid race condition where child node can appear unsorted
    
    var rNode = new Ext.tree.AsyncTreeNode({
      text: name,
      id: id + '/content/',
      singleClickExpand: true,
      expanded: true,
      repoType: repoType
    });
    
    tp.setRootNode(rNode);
    
    var uniqueConfig = {
      id : id + '_repo-browse-top',
      autoScroll: false,
      border: false,
      frame: true,
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
      node.setText(node.text + ' (Out of Service)');
    }
    else if ( response.status == 404 ) {
      if ( Sonatype.MessageBox.isVisible() ) {
        Sonatype.MessageBox.hide();
      }
      node.setText( node.text + ( node.isRoot ? ' (Not Available)' : ' (Not Found)'));
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
            rec.commit();
            rec.endEdit();
          }
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

  reloadAll : function(){
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
    
    this.forceStatuses = true;
    
// note: it looks like the reload takes care of reselecting the previously selected row
//  if (this.reposGridPanel.getSelectionModel().hasSelection()){
//    var rec = this.reposGridPanel.getSelectionModel().getSelected();
//    var id = rec.id;
//    
//    var reselect = function(recs, opts, success){ this.reposGridPanel.getSelectionModel().selectRecords([this.reposDataStore.getById(id)])};
//    
//    this.reposDataStore.reload();
//      this.store.reload({
//        callback: reselect,
//        scope: this
//      });
//  }
//  else{
//    this.reposDataStore.reload();
//  }
  }

});
