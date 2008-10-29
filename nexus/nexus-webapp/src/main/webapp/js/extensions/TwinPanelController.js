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
	style: 'padding-top: ' + ( this.halfSize ? 40 : 100 ) +'px; padding-right: 10px; padding-left: 10px',
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
  }
});

Ext.reg( 'twinpanelcontroller', Sonatype.ext.TwinPanelController );


Sonatype.ext.TwinPanelChooser = function( config ){
  var config = config || {};
  var defaultConfig = {
    displayField: 'name'
  };
  Ext.apply( this, config, defaultConfig );

  Sonatype.ext.TwinPanelChooser.superclass.constructor.call( this, {
    layout: 'column',
    autoHeight: true,
    style: 'padding: 10px 0 10px 0',
    
    items: [
      {
        xtype: 'treepanel',
//        id: '_staging-profiles-target-groups-tree', //note: unique ID is assinged before instantiation
        title: this.titleLeft,
        cls: this.required ? 'required-field' : null,
        border: true, //note: this seem to have no effect w/in form panel
        bodyBorder: true, //note: this seem to have no effect w/in form panel
        //note: this style matches the expected behavior
        bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
        width: 225,
        height: this.halfSize ? 150 : 300,
        animate: true,
        lines: false,
        autoScroll: true,
        containerScroll: true,
        //@note: root node must be instantiated uniquely for each instance of treepanel
        //@ext: can TreeNode be registerd as a component with an xtype so this new root node
        //      may be instantiated uniquely for each form panel that uses this config?
        rootVisible: false,
        root: new Ext.tree.TreeNode( { text: 'root' } ),
        enableDD: true,
        ddScroll: true,
        dropConfig: {
          allowContainerDrop: true,
          onContainerDrop: function(source, e, data){
            this.tree.root.appendChild(data.node);
            return true;
          },
          onContainerOver:function(source, e, data){
            return this.dropAllowed;
          },
          // passign padding to make whole treePanel the drop zone.  This is dependent
          // on a sonatype fix in the Ext.dd.DropTarget class.  This is necessary
          // because treepanel.dropZone.setPadding is never available in time to be useful.
          padding: [0,0,( this.halfSize ? 124 : 274 ),0]
        },
        // added Field values to simulate form field validation
        invalidText: 'Select one or more items',
        validate: function(){
          return (this.root.childNodes.length > 0);
        },
        invalid: false,
        listeners: {
          'append' : {
            fn: function(tree, parentNode, insertedNode, i) {
              if (tree.invalid) {
                //remove error messaging
                tree.getEl().child('.x-panel-body').setStyle({
                  'background-color' : '#FFFFFF',
                  border : '1px solid #B5B8C8'
                });
                Ext.form.Field.msgFx['normal'].hide(tree.errorEl, tree);
              }
            },
            scope: this
          },
          'remove' : {
            fn: function(tree, parentNode, removedNode) {
              if(tree.root.childNodes.length < 1) {
                this.markTreeInvalid(tree,null);
              }
              else if (tree.invalid) {
                //remove error messaging
                tree.getEl().child('.x-panel-body').setStyle({
                  'background-color' : '#FFFFFF',
                  border : '1px solid #B5B8C8'
                });
                Ext.form.Field.msgFx['normal'].hide(tree.errorEl, tree);
              }
            },
            scope: this
          }
        }
      },
      {
        xtype: 'twinpanelcontroller',
        halfSize: this.halfSize
      },
      {
        xtype: 'treepanel',
//        id: id + '_staging-profiles-available-groups-tree', //note: unique ID is assinged before instantiation
        title: this.titleRight,
        border: true, //note: this seem to have no effect w/in form panel
        bodyBorder: true, //note: this seem to have no effect w/in form panel
        //note: this style matches the expected behavior
        bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
        width: 225,
        height: ( this.halfSize ? 150 : 300 ) + ( Ext.isGecko ? 15 : 0 ),
        animate:true,
        lines: false,
        autoScroll:true,
        containerScroll: true,
        //@note: root node must be instantiated uniquely for each instance of treepanel
        //@ext: can TreeNode be registerd as a component with an xtype so this new root node
        //      may be instantiated uniquely for each form panel that uses this config?
        rootVisible: false,
        root: new Ext.tree.TreeNode( { text: 'root' } ),
        enableDD: true,
        ddScroll: true,
        dropConfig: {
          allowContainerDrop: true,
          onContainerDrop: function(source, e, data){
            this.tree.root.appendChild(data.node);
            return true;
          },
          onContainerOver:function(source, e, data){return this.dropAllowed;},
          // passign padding to make whole treePanel the drop zone.  This is dependent
          // on a sonatype fix in the Ext.dd.DropTarget class.  This is necessary
          // because treepanel.dropZone.setPadding is never available in time to be useful.
          padding: [0,0,( this.halfSize ? 124 : 274 ),0]
        }
      }
    ]
  });
  
  if ( this.store ) {
    var root = this.getComponent( 2 ).root;
    this.store.each( function( rec ) {
      root.appendChild( new Ext.tree.TreeNode( {
        id: rec.id,
        text: rec.get( this.displayField ),
        payload: rec,
        allowChildren: false,
        draggable: true,
        leaf: true
      }));
    }, this );
  }
};

Ext.extend( Sonatype.ext.TwinPanelChooser, Ext.Panel, {
  
  markTreeInvalid : function( tree, errortext ) {
    if ( tree == null ) {
      tree = this.getComponent( 0 );
    }
    var elp = tree.getEl();
    
    if ( ! tree.errorEl ){
      tree.errorEl = elp.createChild( { cls: 'x-form-invalid-msg' } );
      tree.errorEl.setWidth( elp.getWidth( true ) ); //note removed -20 like on form fields
    }
    tree.invalid = true;
    var oldErrorText = tree.invalidText;
    if ( errortext ) {
      tree.invalidText = errortext;
    }
    tree.errorEl.update( tree.invalidText );
    tree.invalidText = oldErrorText;
    elp.child( '.x-panel-body' ).setStyle( {
      'background-color': '#fee',
      border: '1px solid #dd7870'
    });
    Ext.form.Field.msgFx['normal'].show( tree.errorEl, tree );
  },

  validate: function() {
    var leftTree = this.getComponent( 0 );
    var valid = leftTree.validate.call( leftTree );
    if ( ! valid ) {
      this.markTreeInvalid( leftTree, null );
    }
    return valid;
  },
  
  getValue: function() {
    var output = [];
    var nodes = this.getComponent( 0 ).root.childNodes;
    
    for( var i = 0; i < nodes.length; i++) {
      output.push( this.valueField ? nodes[i].attributes.payload.get( this.valueField ) :
        nodes[i].attributes.payload.data );
    }
    
    return output;
  }
});

Ext.reg( 'twinpanelchooser', Sonatype.ext.TwinPanelChooser );
