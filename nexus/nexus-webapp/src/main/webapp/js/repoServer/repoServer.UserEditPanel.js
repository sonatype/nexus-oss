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
 * User Edit/Create panel layout and controller
 */

Sonatype.repoServer.UserEditPanel = function( config ){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  this.userGridPanel = new Ext.grid.GridPanel({
    id: 'st-users-grid',
    region: 'north',
    layout:'fit',
    collapsible: true,
    split:true,
    height: 200,
    minHeight: 150,
    maxHeight: 400,
    frame: false,
    autoScroll: true,
    tbar: [
      {
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
//        handler: this.reloadAll,
        scope: this
      },
      {
        id: 'user-add-btn',
        text:'Add',
        icon: Sonatype.config.resourcePath + '/images/icons/add.png',
        cls: 'x-btn-text-icon',
//        handler: this.addUserHandler,
        scope: this
      },
      {
        id: 'user-delete-btn',
        text: 'Delete',
        icon: Sonatype.config.resourcePath + '/images/icons/delete.png',
        cls: 'x-btn-text-icon',
//        handler: this.deleteUserHandler,
        scope: this
      }
    ],

    //grid view options
    ds: new Ext.data.SimpleStore({fields:['id','name','email','status','roles'], 
      data:[
        ['admin', 'Yoda', 'swamp@dagobah.com', 'active', 'nexusAdmin, allReposRW'],
        ['palpatine', 'Darth Sidious', 'emperor@galaxy.com', 'blocked', 'nexusAdmin, darksideRepoRW'],
        ['ani', 'Darth Vader', 'fastestever@podracer.net', 'closed', 'darksideRepoRead'],
        ['owkenobi', 'Obi-Wan Kenobi', 'ed@bigfish.com', 'active', 'lightsideRepoRead'],
        ['liam', 'Qui-Gon Jinn', 'firstghost@force.org', 'active', 'lightsideRepoRW']
      ]}),
//    sortInfo:{field: 'name', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      { header: 'User ID', dataIndex: 'id', width: 120 },
      { header: 'Name', dataIndex: 'name', width: 200 },
      { header: 'Email', dataIndex: 'email', width: 150,
        renderer: function( s ) {
          return '<a href="mailto:' + s + '">' + s + '</a>';
        },
        menuDisabled:true
      },
      { header: 'Status', dataIndex: 'status', width: 100 },
      { header: 'Roles', dataIndex: 'roles', id: 'users-role-column' }
    ],
    autoExpandColumn: 'users-role-column',
    disableSelection: false,
    viewConfig: {
      emptyText: 'Click "Add" to create a new user'
    }
  });

  this.assignedRoot = new Ext.tree.TreeNode({text: 'root'});
  this.assignedRoot.appendChild([
    new Ext.tree.TreeNode({
      id: 'bountyRepoRW',
      text: 'bountyRepoRW',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    })
  ]);

  this.availableRoot = new Ext.tree.TreeNode({text: 'root'});
  this.availableRoot.appendChild([
    new Ext.tree.TreeNode({
      id: 'nexusAdmin',
      text: 'nexusAdmin',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'allReposRW',
      text: 'allReposRW',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'darksideRepoRead',
      text: 'darksideRepoRead',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'darksideRepoRW',
      text: 'darksideRepoRW',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'lightsideRepoRead',
      text: 'lightsideRepoRead',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'lightsideRepoRW',
      text: 'lightsideRepoRW',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    })
  ]);

  this.userFormPanel = new Ext.form.FormPanel({
    width: '100%',
    height: '100%',
    autoScroll: true,
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
    labelWidth: 150,
    layoutConfig: {
      labelSeparator: ''
    },
    items: [
      {
        xtype: 'textfield',
        fieldLabel: 'User ID',
        itemCls: 'required-field',
//        helpText: ht.workingDirectory,
        name: 'userid',
        value: 'slave1',
        anchor: Sonatype.view.FIELD_OFFSET,
        allowBlank: false
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Name',
        itemCls: 'required-field',
//        helpText: ht.workingDirectory,
        name: 'name',
        value: 'Jango Fett',
        anchor: Sonatype.view.FIELD_OFFSET,
        allowBlank: false
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Email Address',
        itemCls: 'required-field',
//        helpText: ht.workingDirectory,
        name: 'email',
        value: 'theoriginal@clones.com',
        anchor: Sonatype.view.FIELD_OFFSET,
        allowBlank: false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Account Status',
        itemCls: 'required-field',
//        helpText: ht.ruleType,
        name: 'status',
        //hiddenName: 'connectionTimeout',
        width: 75,
        store: new Ext.data.SimpleStore({fields:['value'],data:[['active'],['blocked'],['closed']]}),
        displayField: 'value',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText: 'Select...',
        selectOnFocus: true,
        allowBlank: false
/*
        listeners: {
          'select': {
            fn: function(combo, record, index) {
              this.updateTreePanel( index == this.BLOCKING_TYPE_INDEX,
                combo.findParentByType( 'form' ).id );
            },
            scope: this
          }
        }
*/
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Password',
        itemCls: 'required-field',
//        helpText: ht.workingDirectory,
        name: 'password',
        value: 'aaaaaa',
        inputType: 'password',
        anchor: Sonatype.view.FIELD_OFFSET,
        allowBlank: false
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Password (repeat)',
        itemCls: 'required-field',
//        helpText: ht.workingDirectory,
        name: 'password2',
        value: 'aaaaaa',
        inputType: 'password',
        anchor: Sonatype.view.FIELD_OFFSET,
        allowBlank: false
      },
      {
        xtype: 'panel',
        layout: 'column',
        autoHeight: true,
        style: 'padding: 10px 0 0 0',
        
        items: [
          {
            xtype: 'treepanel',
            id: '_user-roles-tree', //note: unique ID is assinged before instantiation
            title: 'Assigned Roles',
            cls: 'required-field',
            border: true, //note: this seem to have no effect w/in form panel
            bodyBorder: true, //note: this seem to have no effect w/in form panel
            //note: this style matches the expected behavior
            bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
            style: 'padding: 0 20px 0 0',
            width: 225,
            height: 300,
            animate:true,
            lines: false,
            autoScroll:true,
            containerScroll: true,
            //@note: root node must be instantiated uniquely for each instance of treepanel
            //@ext: can TreeNode be registerd as a component with an xtype so this new root node
            //      may be instantiated uniquely for each form panel that uses this config?
            rootVisible: false,
            root: this.assignedRoot,

            enableDD: true,
            //ddGroup: 'group-repos',
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
              padding: [0,0,274,0]
            },
            // added Field values to simulate form field validation
            invalidText: 'One or more repository is required',
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
            xtype: 'treepanel',
            id: id + '_all-user-roles-tree', //note: unique ID is assinged before instantiation
            title: 'Available Roles',
            border: true, //note: this seem to have no effect w/in form panel
            bodyBorder: true, //note: this seem to have no effect w/in form panel
            //note: this style matches the expected behavior
            bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
            width: 225,
            height: Ext.isGecko ? 315 : 300,
            animate:true,
            lines: false,
            autoScroll:true,
            containerScroll: true,
            //@note: root node must be instantiated uniquely for each instance of treepanel
            //@ext: can TreeNode be registerd as a component with an xtype so this new root node
            //      may be instantiated uniquely for each form panel that uses this config?
            rootVisible: false,
            root: this.availableRoot,

            enableDD: true,
            //ddGroup: 'group-repos',
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
              padding: [0,0,274,0]
            }
          }
        ]
      }
    ],
    buttons: [
      {
        text: 'Save',
//        handler: this.saveBtnHandler,
        disabled: true,
        scope: this
      },
      {
        text: 'Cancel',
//        handler: this.cancelBtnHandler,
        scope: this
      }
    ]
  });


  Sonatype.repoServer.UserEditPanel.superclass.constructor.call( this, {
    layout: 'border',
    items: [
      this.userGridPanel,
      {
        xtype: 'panel',
        id: 'user-forms',
        title: 'User Properties',
        layout: 'card',
        region: 'center',
        split: true,
        activeItem: 0,
        deferredRender: false,
        autoScroll: false,
        frame: false,
        items: [
          this.userFormPanel
/*
          {
            xtype: 'panel',
            layout: 'fit',
            id: 'user-no-form',
            html: '<div class="little-padding">Select a user to edit, or click "Add" to create a new one.</div>'
          }
*/
        ]
      }
    ]
  });
};


Ext.extend( Sonatype.repoServer.UserEditPanel, Ext.Panel, {
});
