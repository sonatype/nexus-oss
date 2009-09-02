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
// must pass in feedUrl that's local to our domain, 'cause we ain't got no proxy yet

// config: feedUrl required

Sonatype.repoServer.SearchResultGrid = function(config) {

  Ext.apply(this, config);
  
  this.sp = Sonatype.lib.Permissions;

  var resultRecordConstructor = Ext.data.Record.create([
      {name:'groupId'},
      {name:'artifactId'},
      {name:'version'},
      {name:'repoId'},
      {name:'resourceURI'},
      {name:'contextId'},
      {name:'classifier'},
      {name:'packaging'},
      {name:'pomLink'},
      {name:'artifactLink'}
  ]);

  var resultReader = new Ext.data.JsonReader({
      root: 'data',
      totalProperty: 'totalCount'
    },
    resultRecordConstructor );

  var requestProxy = new Ext.data.HttpProxy({
    url: Sonatype.config.repos.urls.index,
    method: 'GET'
    //headers: {Accept: 'application/json'}
  });
  
  this.totalRecords = 0;
  
  //@todo: create stand alone data reader to read update/create responses as well
  //@ext: must use data.Store (not JsonStore) to pass in reader instead of using fields config array
  this.store = new Ext.data.Store({
    proxy: requestProxy,
    reader: resultReader,
    listeners: {
      'beforeload': {
        fn: function( store, options ) {
          requestProxy.getConnection().on( 'requestcomplete',
            function( conn, response, options ) {
              if ( response.responseText ) {
                var statusResp = Ext.decode(response.responseText);
                if ( statusResp ) {
                  this.totalRecords = statusResp.totalCount;
                  if ( statusResp.tooManyResults ) {
                    this.setWarningLabel( 'Too many results, please refine the search condition.' );
                  }
                  else {
                    this.clearWarningLabel();
                  }
                }
              }
            }, this, { single: true } );
          return true;
        },
        scope: this
      },
      'load': {
        fn: function( store, records, options ) {
          this.updateRowTotals( this );
        },
        scope: this
      }
    }
  });
  
  this.store.setDefaultSort('groupId', "ASC");

  this.columns = [
    {
      id: 'source',
      header: "Source Index",
      dataIndex: 'contextId',
      sortable:true
    },{
      id: 'group',
      header: "Group",
      dataIndex: 'groupId',
      sortable:true
    },{
      id: 'artifact',
      header: "Artifact",
      dataIndex: 'artifactId',
      sortable:true
    },{
      id: 'version',
      header: "Version",
      dataIndex: 'version',
      sortable:true
    },{
      id: 'packaging',
	  header: "Packaging",
	  dataIndex: 'packaging',
	  sortable:true
    },{
      id: 'classifier',
      header: "Classifier",
      dataIndex: 'classifier',
      sortable:true
    }
  ];

  this.fetchMoreButton = new Ext.SplitButton({
    text: 'Fetch Next 50',
    icon: Sonatype.config.resourcePath + '/images/icons/search.gif',
    cls: 'x-btn-text-icon',
    value: '50',
    handler: this.fetchMoreRows,
    disabled: true,
    scope: this,
    menu: {
      items: [
        {
          text: 'Fetch Next 50',
          value: '50',
          scope: this,
          checked: true,
          group: 'fetch-more-records',
          handler: this.fetchMoreRows
        },
        {
          text: 'Fetch Next 100',
          value: '100',
          scope: this,
          checked: false,
          group: 'fetch-more-records',
          handler: this.fetchMoreRows
        },
        {
          text: 'Fetch Next 200',
          value: '200',
          scope: this,
          checked: false,
          group: 'fetch-more-records',
         handler: this.fetchMoreRows
        },
        {
          text: 'Fetch All',
          value: '0',
          scope: this,
          checked: false,
          group: 'fetch-more-records',
          handler: this.fetchMoreRows
        }
      ]
    }
    
  });

  this.clearButton = new Ext.Button({
    text: 'Clear Results',
    icon: Sonatype.config.resourcePath + '/images/icons/clear.gif',
    cls: 'x-btn-text-icon',
    handler: this.clearResults,
    disabled: true,
    scope: this
  });

  this.fetchMoreBar = new Ext.Toolbar({
    ctCls: 'search-all-tbar',
    items: [ 
      'Displaying 0 records',
      { xtype: 'tbspacer' },
      this.clearButton
    ]
  });

  Sonatype.repoServer.SearchResultGrid.superclass.constructor.call(this, {
      region: 'center',
      id: 'search-result-grid',
      loadMask: {msg:'Loading Results...'},
      stripeRows: true,
      sm: new Ext.grid.RowSelectionModel({
          singleSelect: true
      }),
      
      bbar: this.fetchMoreBar,

      viewConfig: {
          forceFit:true,
          enableRowBody:true,
          getRowClass : this.applyRowClass
      },
      
      listeners: {
        rowcontextmenu: this.rowContextMenuHandler, 
        render: function(panel){
          panel.body.on(
            {
              'mousedown': function(e, t){
                var i = t.getAttribute('index');
                this.toggleExtraInfo(parseInt(i, 10));
                e.stopEvent();
                return false;
              },
              'click' : function(e, t){
                e.stopEvent();
                return false;
              },
              delegate:'a.pom-link',
              scope:panel
            });
        },
        scope: this
      }
  });
};

Ext.extend(Sonatype.repoServer.SearchResultGrid, Ext.grid.GridPanel, {
  toggleExtraInfo : function(rowIndex){
    var rowEl = new Ext.Element(this.getView().getRow(rowIndex));
    var input = rowEl.child('.copy-pom-dep', true);
    input.select(); //@todo: why won't this field highlight?!!!!
    rowEl.toggleClass('x-grid3-row-expanded');
  },

  // within this function "this" is actually the GridView
  applyRowClass: function(record, rowIndex, p, ds) {
    var xf = Ext.util.Format;
    var xmlDep = Sonatype.repoServer.RepoServer.pomDepTmpl.apply(record.data);
    //note: wrapper div with overflow:auto and autocomplete="off" are attempts to avoid a FF bug
    p.body = '<span>POM Dependency: </span><input class="copy-pom-dep" type="text" autocomplete="off" value="'+xmlDep+'"/>';
    return 'x-grid3-row-collapsed';
  },

  fetchMoreRows: function( button, event ) {
    if ( button.value != this.fetchMoreButton.value ) {
      this.fetchMoreButton.value = button.value;
      this.fetchMoreButton.setText( button.text );
    }

    var fetched = this.store.getCount();
    var toFetch = this.fetchMoreButton.value;
    if ( toFetch == 0 ) {
      toFetch = this.totalRecords - fetched;
    }
    this.store.load({
      params: {
        from: fetched,
        count: toFetch
      },
      add: true
    });
  },
  
  updateRowTotals: function( p ) {
    var count = p.store.getCount();

    p.clearButton.setDisabled( count == 0 );
    
    if ( count == 0 || count > p.totalRecords ) {
      p.totalRecords = count;
    }
    
    p.fetchMoreBar.items.items[0].destroy();
    p.fetchMoreBar.items.removeAt( 0 );
    p.fetchMoreBar.insertButton( 0, new Ext.Toolbar.TextItem( 'Displaying ' + count + ' records' ) );

    p.fetchMoreButton.setDisabled( count >= p.totalRecords );
  },

  setWarningLabel: function( s ) {
    this.searchPanel.setWarningLabel( s );
  },

  clearWarningLabel: function() {
    this.searchPanel.clearWarningLabel();
  },
  
  clearResults: function() {
    this.store.baseParams = {};
    this.store.removeAll();
    this.updateRowTotals( this );
    this.clearWarningLabel();
  },
  
  makeDownloadItem: function(text, url, event){
	var item = {   
      text: text,
      href: url,
      scope: this,
      handler: this.downloadHandler
	};
	item.targetUrl = url;
	return item;
  },
  
  downloadHandler: function( node, item, event) {
	  event.stopEvent();
	  Sonatype.utils.openWindow( item.targetUrl );
  },  
  
  rowContextMenuHandler: function( grid, rowIndex, e ) {
    var rec = this.store.getAt( rowIndex );
    
    var menu = new Sonatype.menu.Menu( {
      id: 'search-result-context-menu',
      payload: rec
    } );

    if ( this.sp.checkPermission( 'nexus:cache', this.sp.DELETE ) ){
      menu.add( Sonatype.repoServer.DefaultRepoHandler.repoActions.clearCache );
    }

    if ( this.sp.checkPermission( 'nexus:metadata', this.sp.DELETE ) ) {
      menu.add( Sonatype.repoServer.DefaultRepoHandler.repoActions.rebuildMetadata );
    }
    
    if ( this.sp.checkPermission( 'nexus:artifact', this.sp.READ) ) {
	    if ( menu.items.first() ){
	    	menu.add( '-' );
	    }
	    var pomLink = rec.get('pomLink');
	    var artifactLink = rec.get('artifactLink');
	    
	    if ( pomLink ) {
	    	menu.add( this.makeDownloadItem( 'Open POM', pomLink, e) );
	    }
	    if ( artifactLink ) {
	    	menu.add( this.makeDownloadItem( 'Download Artifact', artifactLink, e) );
	    }
    }

    e.stopEvent();
    menu.showAt( e.getXY() );
  }
  
  
});