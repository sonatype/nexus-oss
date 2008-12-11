/*
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
/*
 * Repo permissions panel layout and controller
 */

Sonatype.repoServer.RepoPermissionsPanel = function( config ){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  this.permissionGridPanel = new Ext.grid.GridPanel({
    id: 'st-permissions-grid',
    region: 'west',
    layout:'fit',
    collapsible: true,
    split:true,
    width: 150,
    height: 200,
    minHeight: 150,
    maxHeight: 400,
    frame: false,
    autoScroll: true,
/*
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
*/
    //grid view options
    ds: new Ext.data.SimpleStore({fields:['name'], 
      data:[
        ['bountyRepoAdmin'],
        ['bountyRepoGuest'],
        ['bountyRepoRW']
      ]}),
//    sortInfo:{field: 'name', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      { header: 'Permissions', dataIndex: 'name', width: 120 }
    ],
//    autoExpandColumn: 'users-role-column',
    disableSelection: false
//    viewConfig: {
//      emptyText: 'Click "Add" to create a new user'
//    }
  });
/*
  this.permissionsRoot = new Ext.tree.TreeNode({text: 'root'});
  this.permissionsRoot.appendChild([
    new Ext.tree.TreeNode({
      id: 'bountyRepoAdmin',
      text: 'bountyRepoAdmin',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'bountyRepoGuest',
      text: 'bountyRepoGuest',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    }),
    new Ext.tree.TreeNode({
      id: 'bountyRepoRW',
      text: 'bountyRepoRW',
//      payload: repo, //sonatype added attribute
      
      allowChildren: false,
      draggable: true,
      leaf: true
    })
  ]);
*/
  this.permissionFormPanel = new Ext.form.FormPanel({
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
        xtype: 'fieldset',
        labelWidth: 70,
        checkboxToggle:false,
        collapsed: false,
        collapsible: false,
        autoHeight:true,
        title: 'Permissions',
        layoutConfig: {
          labelSeparator: ''
        },
        items: [
          {
            xtype: 'textfield',
            fieldLabel: 'Name',
            itemCls: 'required-field',
//        helpText: ht.workingDirectory,
            name: 'name',
            value: 'bountyRepoRW',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank: false
          },
          {
            xtype: 'textfield',
            fieldLabel: 'Path',
            itemCls: 'required-field',
//        helpText: ht.workingDirectory,
            name: 'path',
            value: '/content/repositories/bounty/.*',
            anchor: Sonatype.view.FIELD_OFFSET,
            allowBlank: false
          },
          {
            xtype: 'panel',
            layout: 'table',
            layoutConfig: {
              columns: 2
            },
            autoHeight: true,
            items: [
              {
                xtype: 'checkbox',
                boxLabel: 'Read',
                name: 'read'
              },
              {
                xtype: 'checkbox',
                boxLabel: 'Re-Index',
                name: 'reindex'
              },
              {
                xtype: 'checkbox',
                boxLabel: 'Deploy &nbsp;',
                name: 'deploy'
              },
              {
                xtype: 'checkbox',
                boxLabel: 'Rebuild Attributes',
                name: 'rebuildAttributes'
              },
              {
                xtype: 'checkbox',
                boxLabel: 'Delete',
                name: 'delete'
              },
              {
                xtype: 'checkbox',
                boxLabel: 'Clear Cache',
                name: 'clearCache'
              }
            ]
/*
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
*/
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

  Sonatype.repoServer.RepoPermissionsPanel.superclass.constructor.call( this, {
    layout: 'border',
    width: 500,
    autoWidth: false,
    resizable: false,
    draggable: false,
    autoHeight: false,
    height: 250,
    title: 'Bounty Repository Permissions',
    modal: true,
    closable: true,
    items: [
      this.permissionGridPanel,
      {
        xtype: 'panel',
        id: 'user-forms',
        layout: 'card',
        region: 'center',
        split: true,
        activeItem: 0,
        deferredRender: false,
        autoScroll: false,
        frame: false,
        items: [
          this.permissionFormPanel
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

Ext.extend( Sonatype.repoServer.RepoPermissionsPanel, Ext.Window, {
});
  