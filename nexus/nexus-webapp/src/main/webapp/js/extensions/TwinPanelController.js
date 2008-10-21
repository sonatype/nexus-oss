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
 * Target Edit/Create panel layout and controller
 */
  
Sonatype.ext.TwinPanelController = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  Sonatype.ext.TwinPanelController.superclass.constructor.call(this, {
  	layout: 'table',
	style: 'padding-top: 100px; padding-right: 10px; padding-left: 10px',
	width: 45,
	defaults: {
	  style: 'margin-bottom: 3px'
    },
	layoutConfig: {
	  columns: 1
    },
    items: [
      {
    	xtype: 'button',
    	handler: this.addOne,
    	scope: this,
    	tooltip: 'Add',
        icon: Sonatype.config.extPath + '/resources/images/default/grid/page-prev.gif',
        cls: 'x-btn-icon'
      },
      {
      	xtype: 'button',
    	handler: this.addAll,
    	scope: this,
      	tooltip: 'Add All',
        icon: Sonatype.config.extPath + '/resources/images/default/grid/page-first.gif',
        cls: 'x-btn-icon'
      },
      {
      	xtype: 'button',
    	handler: this.removeOne,
    	scope: this,
      	tooltip: 'Remove',
        icon: Sonatype.config.extPath + '/resources/images/default/grid/page-next.gif',
        cls: 'x-btn-icon'
      },
      {
    	xtype: 'button',
    	handler: this.removeAll,
    	scope: this,
    	tooltip: 'Remove All',
        icon: Sonatype.config.extPath + '/resources/images/default/grid/page-last.gif',
        cls: 'x-btn-icon'
      }
    ]
  });
  
};


Ext.extend(Sonatype.ext.TwinPanelController, Ext.Panel, {
  addOne : function() {
    this.moveItems( 2, 0, false );
  },

  addAll : function() {
    this.moveItems( 2, 0, true );
  },

  removeOne : function() {
    this.moveItems( 0, 2, false );
  },

  removeAll : function() {
    this.moveItems( 0, 2, true );
  },
  
  moveItems : function( fromIndex, toIndex, moveAll ) {
    var fromPanel = this.ownerCt.getComponent( fromIndex );
    var toPanel = this.ownerCt.getComponent( toIndex );

    var dragZone = fromPanel.dragZone;
    var dropZone = toPanel.dropZone;
    var fn = toPanel.dropConfig.onContainerOver.createDelegate( dropZone, [ dragZone, null ], 0 );
    var checkIfDragAllowed = function( node ) {
      return fn( { node: node } ) == dropZone.dropAllowed;
    }
    
    if ( fromPanel && toPanel ) {
      var fromRoot = fromPanel.root;
      var toRoot = toPanel.root;
      if ( moveAll ) {
        for ( var i = 0; i < fromRoot.childNodes.length; i++ ) {
          var node = fromRoot.childNodes[i];
          if ( checkIfDragAllowed( node ) ) {
            toRoot.appendChild( node );
            i--;
          }
        }
      }
      else {
        var selectedNode = fromPanel.getSelectionModel().getSelectedNode();
        if ( selectedNode && checkIfDragAllowed( selectedNode ) ) {
          toRoot.appendChild( selectedNode );
        }
      }
    }
  },
});

Ext.reg('twinpanelcontroller', Sonatype.ext.TwinPanelController);
