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
 * Target Edit/Create panel layout and controller
 */
  
Sonatype.ext.TwinPanelController = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  this.addOneButton = new Ext.Button({
    xtype: 'button',
    handler: this.addOne,
    scope: this,
    tooltip: 'Add',
    icon: Sonatype.config.extPath + '/resources/images/default/grid/page-prev.gif',
    cls: 'x-btn-icon'
  });
  
  this.addAllButton = new Ext.Button({
    xtype: 'button',
    handler: this.addAll,
    scope: this,
    tooltip: 'Add All',
    icon: Sonatype.config.extPath + '/resources/images/default/grid/page-first.gif',
    cls: 'x-btn-icon'
  });
  
  this.removeOneButton = new Ext.Button({
    xtype: 'button',
    handler: this.removeOne,
    scope: this,
    tooltip: 'Remove',
    icon: Sonatype.config.extPath + '/resources/images/default/grid/page-next.gif',
    cls: 'x-btn-icon'
  });
  
  this.removeAllButton = new Ext.Button({
    xtype: 'button',
    handler: this.removeAll,
    scope: this,
    tooltip: 'Remove All',
    icon: Sonatype.config.extPath + '/resources/images/default/grid/page-last.gif',
    cls: 'x-btn-icon'
  });

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
      this.addOneButton,
      this.addAllButton,
      this.removeOneButton,
      this.removeAllButton
    ]
  });
  
};


Ext.extend(Sonatype.ext.TwinPanelController, Ext.Panel, {
  disable : function() {
    this.addOneButton.disable();
    this.addAllButton.disable();
    this.removeOneButton.disable();
    this.removeAllButton.disable();
  },
  enable : function() {
    this.addOneButton.enable();
    this.addAllButton.enable();
    this.removeOneButton.enable();
    this.removeAllButton.enable();
  },
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
      return ( ! node.disabled ) && fn( { node: node } ) == dropZone.dropAllowed;
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
        var selectedNodes = fromPanel.getSelectionModel().getSelectedNodes();
        if ( selectedNodes ){
          for ( var i = 0; i < selectedNodes.length; i++ ) {
            var node = selectedNodes[i];
            if ( checkIfDragAllowed( node ) ) {
              toRoot.appendChild( node );
              i--;
            }
          }
        }
      }
    }
  }
});

Ext.reg( 'twinpanelcontroller', Sonatype.ext.TwinPanelController );


Sonatype.ext.TwinPanelChooser = function( config ){
  var config = config || {};
  var defaultConfig = {
    displayField: 'name',
    nodeIcon: Sonatype.config.extPath + '/resources/images/default/tree/leaf.gif'
  };
  Ext.apply( this, config, defaultConfig );

  Sonatype.ext.TwinPanelChooser.superclass.constructor.call( this, {
    layout: 'column',
    autoHeight: true,
    style: 'padding: 10px 0 10px 0',
    listeners: {
      beforedestroy: {
        fn: function(){
          if ( this.store ) {
            this.loadStore();
            this.store.un( 'load', this.loadStore, this );
          }
        },
        scope: this
      }
    },
    
    items: [
      {
        xtype: 'multiselecttreepanel',
        name: 'targettree',
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
            if ( data.nodes ){
              for ( var i = 0 ; i < data.nodes.length ; i++ ){
                this.tree.root.appendChild(data.nodes[i]);
              }
            }
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
              this.clearInvalid();
            },
            scope: this
          },
          'remove' : {
            fn: function(tree, parentNode, removedNode) {
              if(tree.root.childNodes.length < 1 && this.required) {
                this.markTreeInvalid(tree,null);
              }
              else {
                this.clearInvalid();
              }
            },
            scope: this
          }
        }
      },
      {
        xtype: 'twinpanelcontroller',
        name: 'twinpanel',
        halfSize: this.halfSize
      },
      {
        xtype: 'multiselecttreepanel',
        name: 'sourcetree',
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
            if ( data.nodes ){
              for ( var i = 0 ; i < data.nodes.length ; i++ ){
                this.tree.root.appendChild(data.nodes[i]);
              }
            }
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
    this.loadStore();
    this.store.on( 'load', this.loadStore, this );
  }
};

Ext.extend( Sonatype.ext.TwinPanelChooser, Ext.Panel, {
  disable: function(){
    this.find( 'name', 'twinpanel' )[0].disable();
    this.find( 'name', 'sourcetree' )[0].dragZone.lock();
    this.find( 'name', 'targettree' )[0].dragZone.lock();
  },
  enable: function(){
    this.find( 'name', 'twinpanel' )[0].enable();
    this.find( 'name', 'sourcetree' )[0].dragZone.unlock();
    this.find( 'name', 'targettree' )[0].dragZone.unlock();
  },
  createNode: function( root, rec ) {
    root.appendChild( new Ext.tree.TreeNode( {
      id: rec.id,
      text: rec.data[this.displayField],
      payload: rec,
      allowChildren: false,
      draggable: true,
      leaf: true,
      disabled: rec.data.readOnly,
      icon: this.nodeIcon
    }));
  },
  
  loadStore: function() {
    if ( this.store ) {
      var root = this.getComponent( 2 ).root;
      while ( root.lastChild ) {
        root.removeChild( root.lastChild );
      }
      this.store.each( function( rec ) {
        this.createNode( root, rec );
      }, this );
    }
  },
  
  clearInvalid: function() {
    var tree = this.getComponent( 0 );
    if (tree.invalid) {
      //remove error messaging
      tree.getEl().child('.x-panel-body').setStyle( {
        'background-color' : '#FFFFFF',
        border : '1px solid #B5B8C8'
      } );
      Ext.form.Field.msgFx['normal'].hide( tree.errorEl, tree );
    }
  },
  
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
    if ( ! this.required ) return true;

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
      if ( ! nodes[i].disabled ) {
        output.push( this.valueField ? nodes[i].attributes.payload.data[this.valueField] :
          nodes[i].attributes.payload.data );
      }
    }
    
    return output;
  },

  setValue: function( arr ) {
    if ( ! Ext.isArray( arr ) ) {
      arr = [arr];
    }

    var leftRoot = this.getComponent( 0 ).root;
    while ( leftRoot.lastChild ) {
      leftRoot.removeChild( leftRoot.lastChild );
    }

    var rightRoot = this.getComponent( 2 ).root;
    this.loadStore();
    var nodes = rightRoot.childNodes;
    
    for ( var i = 0; i < arr.length; i++ ) {
      var valueId = arr[i];
      var name = valueId;
      var readOnly = false;
      if ( typeof( valueId ) != 'string' ) {
        name = valueId[this.displayField];
        readOnly = valueId.readOnly;
        valueId = valueId[this.valueField];
      }
      var found = false;
      for ( var j = 0; j < nodes.length; j++ ) {
        var node = nodes[j];
        var nodeValue = this.valueField ?
          node.attributes.payload.data[this.valueField] :
          node.attributes.payload.id;
        if ( nodeValue == valueId ) {
          leftRoot.appendChild( node );
          if ( readOnly ) {
            node.disable();
          }
          found = true;
          break;
        }
      }
      if ( ! found ) {
        var rec = {
          id: valueId,
          data: {
            readOnly: readOnly
          }
        };
        rec.data[this.valueField] = valueId;
        rec.data[this.displayField] = name;
        this.createNode( leftRoot, rec );
      }
    }
  }
});

Ext.reg( 'twinpanelchooser', Sonatype.ext.TwinPanelChooser );
