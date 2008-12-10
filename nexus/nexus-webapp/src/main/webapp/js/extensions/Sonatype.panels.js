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

Ext.namespace( 'Sonatype.panels' );


/*
 * A helper panel creating a tabbed container inside itself if more than 
 * one component is added.
 */
Sonatype.panels.AutoTabPanel = function( config ) {
  var config = config || { };
  var defaultConfig = {
    layout: 'card',
    activeItem: 0,
    deferredRender: false,
    autoScroll: false,
    frame: false,
    border: false
  };
  Ext.apply( this, config, defaultConfig );
  Sonatype.panels.AutoTabPanel.superclass.constructor.call( this, {} );
};

Ext.extend( Sonatype.panels.AutoTabPanel, Ext.Panel, {
  add: function( c ) {
    if ( this.items && this.items.length > 0 ) {
      if ( ! this.tabPanel ) {
        var first = this.getComponent( 0 );
        this.remove( first, false );
        first.setTitle( first.tabTitle );
  
        this.tabPanel = new Ext.TabPanel( { 
          activeItem: 0,
          deferredRender: false,
          autoScroll: false,
          frame: false,
          border: false,
          items: [ first ]
        } );
  
        Sonatype.panels.AutoTabPanel.superclass.add.call( this, this.tabPanel );
        if ( this.getLayout() && this.getLayout().setActiveItem ) {
          this.getLayout().setActiveItem( this.tabPanel );
        }
      }

      if ( ! c.title && c.tabTitle ) {
        c.title = c.tabTitle;
      }
      return this.tabPanel.add( c );
    }
    else {
      return Sonatype.panels.AutoTabPanel.superclass.add.call( this, c );
    }
  }
} );


/*
 * A viewer panel offering a grid on top and a details pane at the bottom.
 * 
 * Config options:
 *
 * columns: a column config, combining the properties of Ext.data.Record and 
 *          Ext.grid.ColumnModel. All items are used in the Record constructor.
 *          Additionally, items with a 'header' property are used in the ColumnsModel
 *          config, with the Record's 'name' becoming ColumnModel's 'dataIndex'.
 *          
 * dataAutoLoad: the 'autoLoad' property for the data store (defaults to true).
 * 
 * dataId: the id property in the record set (defaults to 'resourceURI').
 * 
 * dataRoot: the root property of the response (defaults to 'data').
 * 
 * dataSortInfo: the 'sortInfo' property for the data store.
 * 
 * rowClickEvent: event name to fire when a row is clicked.
 * 
 * rowContextClickEvent: event name to fire when a row is right-clicked.
 * 
 * singleSelect: the 'singleSelect' property for the grid's selection model (defaults to true).
 * 
 * titleColumn: the name of the column to get a record title from (defaults to 'name').
 *          
 * url: the URl to load the data from.
 */
Sonatype.panels.GridViewer = function( config ) {
  var config = config || { };
  var defaultConfig = {
    dataAutoLoad: true,
    dataId: 'resourceURI',
    dataRoot: 'data',
    dataSortInfo: { field: 'name', direction: 'asc' },
    titleColumn: 'name',
    singleSelect: true
  };
  Ext.apply( this, config, defaultConfig );
  
  var fields = [];
  var columns = [];
  if ( config.columns ) {
    for ( var i = 0; i < config.columns.length; i++ ) {
      var c = config.columns[i];
      fields.push( {
        name: c.name,
        mapping: c.mapping,
        type: c.type,
        sortType: c.sortType,
        sortDir: c.sortDir,
        convert: c.convert,
        dateFormat: c.dateFormat,
        defaultValue: c.defaultValue
      } );
      
      if ( c.header ) {
        if ( c.autoExpand ) {
          if ( ! c.id ) {
            c.id = Ext.id();
          }
          this.autoExpandColumn = c.id;
        }
        columns.push( {
          header: c.header,
          dataIndex: c.name,
          id: c.id,
          width: c.width,
          align: c.align,
          renderer: c.renderer
        } );
      }
    }
  }

  this.dataStore = new Ext.data.JsonStore( {
    root: this.dataRoot,
    id: this.dataId,
    fields: fields,
    url: this.url,
    autoLoad: this.dataAutoLoad,
    sortInfo: this.dataSortInfo,
    listeners: {
      remove: {
        fn: this.recordRemoveHandler,
        scope: this
      }, 
      update: {
        fn: this.recordUpdateHandler,
        scope: this
      }
    }
  } );

  this.gridPanel = new Ext.grid.GridPanel( {
    region: 'north',
    layout: 'fit',
    collapsible: true,
    split: true,
    height: 200,
    minHeight: 100,
    maxHeight: 500,
    frame: false,
    autoScroll: true,
    selModel: new Ext.grid.RowSelectionModel( {
      singleSelect: this.singleSelect
    } ),

    ds: this.dataStore,
//    sortInfo: { field: 'name', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: columns,
    autoExpandColumn: this.autoExpandColumn,
    disableSelection: false,
    viewConfig: {
      emptyText: 'No records currently available'
    },
    
    listeners: {
      rowclick: {
        fn: this.rowClickHandler,
        scope: this
      },
      rowcontextmenu: {
        fn: this.rowContextMenuHandler,
        scope: this
      }
    }
  } );
  
  this.cardPanel = new Ext.Panel( {
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
        html: '<div class="little-padding">Select a record to view the details.</div>'
      }
    ]
  } );
  
  Sonatype.panels.GridViewer.superclass.constructor.call( this, {
    layout: 'border',
    autoScroll: false,
    width: '100%',
    height: '100%',
    tbar: [
      {
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.refreshHandler
      }
    ].concat( this.tbar ? this.tbar : [] ),
    items: [
      this.gridPanel,
      this.cardPanel
    ]
  } );
};

Ext.extend( Sonatype.panels.GridViewer, Ext.Panel, {
  clearCards: function() {
    this.cardPanel.items.each( function( item, i, len ) {
      if ( i > 0 ) { this.remove( item, true ); }
    }, this.cardPanel );
    
    this.cardPanel.getLayout().setActiveItem( 0 );
  },
  
  createChildPanel: function( rec, recreateIfExists ) {
    var id = this.id + rec.id;

    var panel = this.cardPanel.findById( id );

    if ( recreateIfExists ) {
      if ( panel ) {
        this.cardPanel.remove( panel, true );
        panel = null;
      }
      else {
        return;
      }
    }

    if ( ! panel ) {
      panel = new Sonatype.panels.AutoTabPanel( { 
        id: id,
        title: rec.get( this.titleColumn )
      } );

      Sonatype.Events.fireEvent( this.rowClickEvent, panel, rec );

      if ( panel.items ) {
        this.cardPanel.add( panel );
      }
      else {
        return;
      }
    }

    this.cardPanel.getLayout().setActiveItem( panel );
    panel.doLayout();
  },
  
  recordRemoveHandler: function( store, rec, index ) {
    var id = this.id + rec.id;

    var panel = this.cardPanel.findById( id );

    if ( panel ) {
      var resetActiveItem = this.cardPanel.getLayout().activeItem == panel; 
      this.cardPanel.remove( panel, true );
      
      if ( resetActiveItem ) {
        this.cardPanel.getLayout().setActiveItem( 0 );
      }
    }
  },
  
  recordUpdateHandler: function( store, rec, op ) {
    if ( op == Ext.data.Record.COMMIT ) {
      this.createChildPanel( rec, true );
    }
  },

  refreshHandler: function( button, e ) {
    this.clearCards();
    this.gridPanel.store.reload();
  },

  rowClickHandler: function( grid, index, e ) {
    if ( e.target.nodeName == 'A' ) return; // no menu on links

    if ( this.rowClickEvent ) {
      var rec = grid.store.getAt( index );
      
      this.createChildPanel( rec );
    }
  },
  
  rowContextMenuHandler: function( grid, index, e ) {
    if ( e.target.nodeName == 'A' ) return; // no menu on links

    if ( this.rowContextClickEvent ) { 
      var rec = grid.store.getAt( index );
  
      var menu = new Sonatype.menu.Menu({
        payload: rec,
        scope: this,
        items: []
      });
  
      Sonatype.Events.fireEvent( this.rowContextClickEvent, menu, rec );
      if ( ! menu.items.first() ) return;

      e.stopEvent();
      menu.showAt( e.getXY() );
    }
  }
} );

Sonatype.panels.TreePanel = function( config ) {
  var config = config || {};
  var defaultConfig = { 
    titleColumn: 'name'
  };
  Ext.apply( this, config, defaultConfig );

  Sonatype.panels.TreePanel.superclass.constructor.call( this, {
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
    loader : new Ext.tree.TreeLoader( {
      requestMethod: 'GET',
      url: this.url,
      listeners: {
        loadexception: this.treeLoadExceptionHandler,
        scope: this
      },
      requestData: function( node, callback ) {
        if ( this.fireEvent( "beforeload", this, node, callback ) !== false ) {
          this.transId = Ext.Ajax.request({
            method: this.requestMethod,
            // Sonatype: nodes contain a relative request path
            url: this.url + node.attributes.path,
            success: this.handleResponse,
            failure: this.handleFailure,
            scope: this,
            argument: { callback: callback, node: node }
          });
        }
        else {
          if ( typeof callback == "function" ) {
            callback();
          }
        }
      },
      createNode : function( attr ) {
        if ( this.baseAttrs ) {
          Ext.applyIf( attr, this.baseAttrs );
        }
        if ( this.applyLoader !== false ) {
          attr.loader = this;
        }
        if ( typeof attr.uiProvider == 'string' ) {
          attr.uiProvider = this.uiProviders[attr.uiProvider] || eval( attr.uiProvider );
        }

        // Sonatype: node name is supplied as 'nodeName' instead of 'text'
        if ( ! attr.text && attr.nodeName ) {
          attr.text = attr.nodeName;
        }
        if ( ! attr.id ) {
          attr.id = ( this.url + attr.path ).replace( /\//g, '_' );
        }

        if ( attr.nodeType ) {
          return new Ext.tree.TreePanel.nodeTypes[attr.nodeType](attr);
        }
        else {
          return attr.leaf ?
            new Ext.tree.TreeNode( attr ) :
            new Ext.tree.AsyncTreeNode( attr );
        }
      },
      processResponse : function( response, node, callback ) {
        var json = response.responseText;
        try {
          var o = eval( "(" + json + ")" );
          if ( o.data ) {
            o = o.data;
            node.beginUpdate();
  
            // Sonatype: 
            // - tree response contains the current node, not just an array of children
            // - node name is supplied as 'nodeName' instead of 'text'
            if ( ! node.isRoot ) {
              node.setText( o.nodeName );
              Ext.apply( node.attributes, o, { } );
            }
            for ( var i = 0, len = o.children.length; i < len; i++ ){
              var n = this.createNode( o.children[i] );
              if ( n ) {
                node.appendChild( n );
              }
            }
  
            node.endUpdate();
          }

          if ( typeof callback == "function" ) {
            callback( this, node );
          }
        }
        catch ( e ) {
          this.handleFailure( response );
        }
      }
    } ),
    listeners: {
      click: {
        fn: this.nodeClickHandler,
        scope: this
      },
      contextMenu: {
        fn: this.nodeContextMenuHandler,
        scope: this
      } 
    } 
  } );

  new Ext.tree.TreeSorter( this, { folderSort:true } );

  var root = new Ext.tree.AsyncTreeNode( {
    text: this.payload ? this.payload.get( this.titleColumn ) : '/',
    path: '/',
    singleClickExpand: true,
    expanded: true
  } );
  
  this.setRootNode( root );
};

Ext.extend( Sonatype.panels.TreePanel, Ext.tree.TreePanel, {
  nodeClickHandler: function( node, e ) {
    if ( e.target.nodeName == 'A' ) return; // no menu on links

    if ( this.nodeClickEvent ) {
      Sonatype.Events.fireEvent( this.nodeClickEvent, node );
    }
  },
  
  nodeContextMenuHandler: function( node, e ) {
    if ( e.target.nodeName == 'A' ) return; // no menu on links

    if ( this.nodeContextMenuEvent ) { 
  
      var menu = new Sonatype.menu.Menu({
        payload: node,
        scope: this,
        items: []
      });
  
      Sonatype.Events.fireEvent( this.nodeContextMenuEvent, menu, node );
      if ( ! menu.items.first() ) return;

      e.stopEvent();
      menu.showAt( e.getXY() );
    }
  },

  refreshHandler: function( button, e ) {
    this.root.setText( this.payload ? this.payload.get( this.titleColumn ) : '/' );
    this.root.attributes.localStorageUpdated = false;
    this.root.reload();
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
