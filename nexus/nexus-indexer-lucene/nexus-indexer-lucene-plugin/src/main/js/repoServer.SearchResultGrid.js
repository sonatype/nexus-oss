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

Sonatype.SearchStore = function(config) {
  var config = config || {};
  var defaultConfig = {
    searchUrl: Sonatype.config.servicePath + '/data_index'
  };
  Ext.apply(this, config, defaultConfig);
  
  Sonatype.SearchStore.superclass.constructor.call(this, {
    proxy: new Ext.data.HttpProxy({
      url: this.searchUrl,
      method: 'GET'
    }),
    reader: new Ext.data.JsonReader(
      {
        root: 'data',
        totalProperty: 'totalCount'
      },
      Ext.data.Record.create([
        {name:'groupId'},
        {name:'artifactId'},
        {name:'version'},
        {name:'repoId'},
        {name:'resourceURI'},
        {name:'contextId'},
        {name:'classifier'},
        {name:'packaging'},
        {name:'extension'},
        {name:'pomLink'},
        {name:'artifactLink'},
        {name:'highlightedFragment'}
      ])
    ),
    listeners: {
      'beforeload': {
        fn: function( store, options ) {
          store.proxy.getConnection().on( 'requestcomplete',
            function( conn, response, options ) {
              if ( response.responseText ) {
                var statusResp = Ext.decode(response.responseText);
                if ( statusResp ) {
                  this.grid.totalRecords = statusResp.totalCount;
                  if ( statusResp.tooManyResults ) {
                    this.grid.setWarningLabel( 'Too many results, please refine the search condition.' );
                  }
                  else {
                    this.grid.clearWarningLabel();
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
          this.grid.updateRowTotals( this.grid );
        },
        scope: this
      }
    }
  });
};

Ext.extend(Sonatype.SearchStore, Ext.data.Store, {
});

Sonatype.repoServer.SearchResultGrid = function(config) {

  Ext.apply(this, config);
  
  this.sp = Sonatype.lib.Permissions;
  
  this.totalRecords = 0;
  
  this.defaultStore = new Sonatype.SearchStore({
    grid: this
  });
  
  this.store = this.defaultStore;
  
  this.defaultColumnModel = new Ext.grid.ColumnModel({
    columns: [
      {
        id: 'source',
        header: "Source Index",
        dataIndex: 'contextId',
        sortable:true
      },
      {
        id: 'group',
        header: "Group",
        dataIndex: 'groupId',
        sortable:true
      },
      {
        id: 'artifact',
        header: "Artifact",
        dataIndex: 'artifactId',
        sortable:true
      },
      {
        id: 'version',
        header: "Version",
        dataIndex: 'version',
        sortable:true,
        renderer: this.formatVersionLink
      },
      {
        id: 'packaging',
        header: "Packaging",
        dataIndex: 'packaging',
        sortable:true,
        renderer: this.formatPackagingLink
      },
      {
        id: 'classifier',
        header: "Classifier",
        dataIndex: 'classifier',
        sortable:true,
        renderer: this.formatClassifierLink
      }
    ]
  });
  
  this.colModel = this.defaultColumnModel;

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
      
      listeners : {
          render : {
            fn : function(grid) {
              grid.body.on({
                'mousedown' : function(e, t) {
                  var i = t.getAttribute('index');
                  this.toggleExtraInfo(parseInt(i, 10));
                  e.stopEvent();
                  return false;
                },
                'click' : function(e, t) {
                  e.stopEvent();
                  return false;
                },
                delegate : 'a.pom-link',
                scope : grid
              });
                  
              var store = grid.getStore ();
			  var view = grid.getView ();
			  grid.tip = new Ext.ToolTip ({
			    target: view.mainBody,
			    delegate: '.x-grid3-row',
                maxWidth: 500,
			    trackMouse: true,
			    renderTo: document.body,
			    listeners: {
			      beforeshow: function (tip) {
			        var rowIndex = view.findRowIndex (tip.triggerElement);
                    var record = store.getAt( rowIndex );
                    
                    if ( !record ) {
                      return false;
                    }
                    
                    var highlightedFragment = record.get('highlightedFragment');
                    
                    if ( Ext.isEmpty( highlightedFragment ) ) {
                      return false;
                    }
                    
			        tip.body.dom.innerHTML = highlightedFragment;
			      }
			    }
			  });
            },
            scope : this
          }
        }
  });
};

Ext.extend(Sonatype.repoServer.SearchResultGrid, Ext.grid.GridPanel, {
  formatCellLink : function( record, store, fieldName ) {
    if ( 'COLLAPSED' != record.get( fieldName ) ) {
      return record.get( fieldName );
    }
    
    var ver = record.get( 'version' );
    var gid = record.get( 'groupId' );
    var aid = record.get( 'artifactId' );
    var pac = record.get( 'packaging' );
    var clas = record.get( 'classifier' );
    
    var extras = '~kw';
    var aggregatedCount = 0;
    if ( 'COLLAPSED' == ver ) {
      aggregatedCount++;
      if ( 'version' == fieldName ) {
        extras += ',versionexpand';
      }
      ver = '';
    }
    else {
      extras += ",versionexpand";
    }
    if ( 'COLLAPSED' == pac ) {
      aggregatedCount++;
      if ( 'packaging' == fieldName ) {
        extras += ',packagingexpand';
      }
      pac = '';
    }
    else {
      extras += ",packagingexpand";
    }
    if ( 'COLLAPSED' == clas ) {
      aggregatedCount++;
      if ( 'classifier' == fieldName ) {
        extras += ',classifierexpand';
      }
      clas = '';
    }
    else {
      extras += ",classifierexpand";
      //special case for classifier, if the classifier is real value (NOT COLLAPSED) need to set that
      //if empty, need to set N/P
      if ( Ext.isEmpty( clas ) ) {
        clas = 'N/P';
      }
    }
    
    // if 0 then is gav search being performed, so simply return string
    if ( aggregatedCount == 0 ) {
      return value;
    }
    // if anything is aggregated, add the drill down link
    else {
      return '<a href="#nexus-search;gav~'+gid+'~'+aid+'~'+ver+'~'+pac+'~'+clas+extras+' " onmousedown="cancel_bubble(event)" onclick="cancel_bubble(event); return true;">Drill Down</a>';
    }
  },
  formatVersionLink : function(value, p, record, rowIndex, colIndex, store) {
    return store.grid.formatCellLink( record, store, 'version' );
  },
  formatPackagingLink : function( value, p, record, rowIndex, colIndex, store) {
    return store.grid.formatCellLink( record, store, 'packaging' );
  },  
  formatClassifierLink : function( value, p, record, rowIndex, colIndex, store) {
    return store.grid.formatCellLink( record, store, 'classifier' );
  },
  
  switchStore : function( grid, store, columnModel ) {
    if ( store == null ) {
      store = grid.defaultStore;
    }
    
    if ( columnModel == null ) {
      columnModel = grid.defaultColumnModel;
    }
    
    if ( store ) {
      this.clearResults();
    }
    
    grid.reconfigure( store, columnModel );
  },
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
  
  updateRowTotals: function( p ) {
    var count = p.store.getCount();

    p.clearButton.setDisabled( count == 0 );
    
    if ( count == 0 || count > p.totalRecords ) {
      p.totalRecords = count;
    }
    
    p.fetchMoreBar.items.items[0].destroy();
    p.fetchMoreBar.items.removeAt( 0 );
    p.fetchMoreBar.insertButton( 0, new Ext.Toolbar.TextItem( 'Displaying ' + count + ' records' ) );
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
    delete this.store.baseParams['dir'];
    delete this.store.baseParams['sort'];
    delete this.store.sortInfo;
    delete this.view.sortState;
    this.store.sortToggle = {};
    this.view.mainHd.select('td').removeClass(this.sortClasses);
    this.fireEvent('sortchange', this.grid, null);
    this.updateRowTotals( this );
    this.clearWarningLabel();
  }
});

cancel_bubble = function (e) {
	if (!e) var e = window.event;
	e.cancelBubble = true;
	if (e.stopPropagation) e.stopPropagation(); 
}
