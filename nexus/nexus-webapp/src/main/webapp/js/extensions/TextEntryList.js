/*
 * Sonatype Nexus (TM) Open Source Version. Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at
 * http://nexus.sonatype.org/dev/attributions.html This program is licensed to
 * you under Version 3 only of the GNU General Public License as published by
 * the Free Software Foundation. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License Version 3 for more details. You should have received a copy of
 * the GNU General Public License Version 3 along with this program. If not, see
 * http://www.gnu.org/licenses/. Sonatype Nexus (TM) Professional Version is
 * available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc.
 */
/*
 * A widget that will allow custom strings to be entered (and removed) into a
 * listbox.
 */
  
Sonatype.ext.TextEntryList = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  this.textEntryField = new Ext.form.Field( {
    xtype: 'textfield',
    fieldLabel: config.entryLabel,
    helpText: config.entryHelpText,
    name: 'entryName',
    width: 300
  });
  
  this.addEntryButton = new Ext.Button({
    xtype: 'button',
    text: 'Add', 
    style: 'padding-left: 7px',
    minWidth: 100,
    id: 'button-add',
    handler: this.addNewEntry,
    scope: this
  });
  
  this.removeEntryButton = new Ext.Button({
    xtype : 'button',
		text : 'Remove',
		style : 'padding-left: 6px',
		minWidth : 100,
		id : 'button-remove',
		handler : this.removeEntry,
		scope : this
  });

  this.removeAllEntriesButton = new Ext.Button({
    xtype: 'button',
    text: 'Remove All', 
    style: 'padding-left: 6px; margin-top: 5px',
    minWidth: 100,
    id: 'button-remove-all',
    handler: this.removeAllEntries,
    scope: this
  });
  
  this.entryList = new Ext.tree.TreePanel({
    xtype: 'treepanel',
    id: 'entry-list', // note: unique ID is assinged
              // before instantiation
    name: 'entry-list',
    title: config.listLabel,
//    cls: 'required-field',
    border: true, // note: this seem to have no effect w/in form panel
    bodyBorder: true, // note: this seem to have no effect w/in form
        // panel
    // note: this style matches the expected behavior
    bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
    style: 'padding: 0 20px 0 0',
    width: 320,
    height: 150,
    animate:true,
    lines: false,
    autoScroll:true,
    containerScroll: true,
    // @note: root node must be instantiated uniquely for each instance of treepanel
    // @ext: can TreeNode be registerd as a component with an xtype so this new root node
    // may be instantiated uniquely for each form panel that uses this config?
    rootVisible: false,
    enableDD: false,
    root: new Ext.tree.TreeNode({text: 'root'})
    
  });
  
  Sonatype.ext.TextEntryList.superclass.constructor.call(this, {
    autoScroll: true,
    border: false,
    collapsible: false,
    collapsed: false,
    labelWidth: 175,
    layoutConfig: {
      labelSeparator: ''
    },
    items: [
      {
        xtype: 'panel',
        layout: 'column',
        items: [
          {
            xtype: 'panel',
            layout: 'form',
            width: 500,
            items: [
              this.textEntryField
            ]
          },
          {
          xtype: 'panel',
          width: 120,
          items: [
             this.addEntryButton
          ]
          }
        ]
      },
      {
        xtype: 'panel',
        layout: 'column',
        autoHeight: true,
        style: 'padding-left: 180px',
        items: [
          this.entryList,
          { 
            xtype: 'panel',
            width: 120,
            items: [
              this.removeEntryButton,
              this.removeAllEntriesButton
            ]
          }
        ]
      }
    ]
  });
  
};

Ext.extend(Sonatype.ext.TextEntryList, Ext.Panel, {
  
  addEntryNode: function( treePanel, entry ) {
  var id = Ext.id();

    treePanel.root.appendChild(
    new Ext.tree.TreeNode({
      id: id,
      text: entry,
      payload: entry,
      allowChildren: false,
      draggable: false,
      leaf: true,
      nodeType: 'entry',
      icon: Sonatype.config.extPath + '/resources/images/default/tree/leaf.gif'
    })
  );
  },
  
  addNewEntry: function() {
    var entry = this.textEntryField.getRawValue();
    
    if ( entry ) {
      var nodes = this.entryList.root.childNodes;
      for(var i = 0; i < nodes.length; i++){
        if (entry == nodes[i].attributes.payload) {
          this.textEntryField.markInvalid('This entry already exists');
          return;
        }
      }

      this.addEntryNode(this.entryList, entry);
      this.textEntryField.setRawValue('');
    }
  },
  
  removeEntry: function() {
  
    var selectedNode = this.entryList.getSelectionModel().getSelectedNode();
    if ( selectedNode ) {
      this.entryList.root.removeChild( selectedNode );
    }
  },
  
  removeAllEntries: function() {
    var treeRoot = this.entryList.root;

    while ( treeRoot.lastChild ) {
      treeRoot.removeChild( treeRoot.lastChild );
    }
  },
  
  setEntries : function(stringArray){
    var entry;

    for(var i=0; i<stringArray.length; i++){
      entry = stringArray[i];
      this.addEntryNode( this.entryList, entry );
    }
    
    return stringArray; //return stringArray, even if empty to comply with sonatypeLoad data modifier requirement
  },
  
  getEntries : function(){
    var outputArr = [];
    var nodes = this.entryList.root.childNodes;
    
    for(var i = 0; i < nodes.length; i++){
      outputArr[i] = nodes[i].attributes.payload;
    }
    
    return outputArr;
  }
  
});

Ext.reg( 'textentrylist', Sonatype.ext.TextEntryList );
