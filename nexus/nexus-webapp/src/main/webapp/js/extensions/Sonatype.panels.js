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
    titleColumn: 'name',
    singleSelect: true,
    id: Ext.id()
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
    listeners: {
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
    ],
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
      panel = new Ext.Panel( { 
        id: id,
        layout: 'card',
        activeItem: 0,
        deferredRender: false,
        autoScroll: false,
        frame: false,
        border: false,
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
      menu.showAt(e.getXY());
    }
  }
} );
