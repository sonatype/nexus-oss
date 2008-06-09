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
 * Role Edit/Create panel layout and controller
 */

Sonatype.repoServer.RoleEditPanel = function( config ){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  this.roleGridPanel = new Ext.grid.GridPanel({
    id: 'st-roles-grid',
    region: 'north',
    layout: 'fit',
    collapsible: true,
    split: true,
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
        id: 'role-add-btn',
        text:'Add',
        icon: Sonatype.config.resourcePath + '/images/icons/add.png',
        cls: 'x-btn-text-icon',
//        handler: this.addRoleHandler,
        scope: this
      },
      {
        id: 'role-delete-btn',
        text: 'Delete',
        icon: Sonatype.config.resourcePath + '/images/icons/delete.png',
        cls: 'x-btn-text-icon',
//        handler: this.deleteRoleHandler,
        scope: this
      }
    ],

    //grid view options
    ds: new Ext.data.SimpleStore({fields:['name','description'], 
      data:[
        ['nexusAdmin', 'Nexus administrator'],
        ['allReposRW', 'Full access to all repositories (use with caution)'],
        ['darksideRepoRW', 'darkside.com repository R/W access'],
        ['darksideRepoRead', 'darkside.com repository read only access'],
        ['lightsideRepoRW', 'lightside.org repository R/W access'],
        ['lightsideRepoRead', 'lightside.org repository read only access'],
        ['bountyRepoRW', 'bounty.net repository R/W access']
      ]}),
//    sortInfo:{field: 'name', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      { header: 'Role', dataIndex: 'name', width: 120 },
      { header: 'Description', dataIndex: 'description', width: 200, id: 'role-description-column' }
    ],
    autoExpandColumn: 'role-description-column',
    disableSelection: false,
    viewConfig: {
      emptyText: 'Click "Add" to create a new role'
    }
  });

  this.assignedRoot = new Ext.tree.TreeNode({text: 'root'});
  this.assignedRoot.appendChild([
    new Ext.tree.TreeNode({
      id: 'firearmsUser',
      text: '<b>firearmsUser</b>',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      icon: Sonatype.config.resourcePath + '/ext-2.0.2/resources/images/default/tree/folder.gif',
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'pilot',
      text: '<b>spacePilot</b>',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      icon: Sonatype.config.resourcePath + '/ext-2.0.2/resources/images/default/tree/folder.gif',
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'bountyRepoRW',
      text: '<b>bountyRepoRW</b>',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      icon: Sonatype.config.resourcePath + '/ext-2.0.2/resources/images/default/tree/folder.gif',
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'pillage',
      text: 'pillage',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'plunder',
      text: 'plunder',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'manhunt',
      text: 'manhunt',
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
      text: '<b>nexusAdmin</b>',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      icon: Sonatype.config.resourcePath + '/ext-2.0.2/resources/images/default/tree/folder.gif',
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'allReposRW',
      text: '<b>allReposRW</b>',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      icon: Sonatype.config.resourcePath + '/ext-2.0.2/resources/images/default/tree/folder.gif',
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'darksideRepoRead',
      text: '<b>darksideRepoRead</b>',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      icon: Sonatype.config.resourcePath + '/ext-2.0.2/resources/images/default/tree/folder.gif',
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'darksideRepoRW',
      text: '<b>darksideRepoRW</b>',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      icon: Sonatype.config.resourcePath + '/ext-2.0.2/resources/images/default/tree/folder.gif',
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'lightsideRepoRead',
      text: '<b>lightsideRepoRead</b>',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      icon: Sonatype.config.resourcePath + '/ext-2.0.2/resources/images/default/tree/folder.gif',
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'lightsideRepoRW',
      text: '<b>lightsideRepoRW</b>',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      icon: Sonatype.config.resourcePath + '/ext-2.0.2/resources/images/default/tree/folder.gif',
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'forceLightning',
      text: 'forceLightning',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'forcePush',
      text: 'forcePush',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'forceChoke',
      text: 'forceChoke',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    })
  ]);

  this.roleFormPanel = new Ext.form.FormPanel({
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
        fieldLabel: 'Role Name',
        itemCls: 'required-field',
//        helpText: ht.workingDirectory,
        name: 'rolename',
        value: 'bountyHunter',
        anchor: Sonatype.view.FIELD_OFFSET,
        allowBlank: false
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Description',
        itemCls: 'required-field',
//        helpText: ht.workingDirectory,
        name: 'description',
        value: 'bounty hunter role',
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
            id: '_included-roles-tree', //note: unique ID is assinged before instantiation
            title: 'Included Roles and Permissions',
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
            id: id + '_available-roles-tree', //note: unique ID is assinged before instantiation
            title: 'Available Roles and Permissions',
            border: true, //note: this seem to have no effect w/in form panel
            bodyBorder: true, //note: this seem to have no effect w/in form panel
            //note: this style matches the expected behavior
            bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
            width: 225,
            height: Ext.isGecko ? 345 : 300,
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

  Sonatype.repoServer.RoleEditPanel.superclass.constructor.call( this, {
    layout: 'border',
    items: [
      this.roleGridPanel,
      {
        xtype: 'panel',
        id: 'role-form',
        title: 'Role Properties',
        layout: 'card',
        region: 'center',
        split: true,
        activeItem: 0,
        deferredRender: false,
        autoScroll: false,
        frame: false,
        items: [
          this.roleFormPanel
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


Ext.extend( Sonatype.repoServer.RoleEditPanel, Ext.Panel, {
});
