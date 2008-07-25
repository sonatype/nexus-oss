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
 * Privilege Edit/Create panel layout and controller
 */
  
Sonatype.repoServer.PrivilegeEditPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  var ht = Sonatype.repoServer.resources.help.privileges;
  
  this.typeStore = new Ext.data.SimpleStore({fields:['value','display'], data:[['repositoryTarget','Repository Target'],['application','Application']]});
  
  this.methodStore = new Ext.data.SimpleStore({fields:['value','display'], data:[['create','Create'],['read','Read'],['update','Update'],['delete','Delete']]});
  
  this.actions = {
    refresh : new Ext.Action({
      text: 'Refresh',
      iconCls: 'st-icon-refresh',
      scope:this,
      handler: this.reloadAll
    }),
    deleteAction : new Ext.Action({
      text: 'Delete',
      scope:this,
      handler: this.deleteHandler
    })
  };
  
  //Methods that will take the incoming json data and map over to the ui controls
  this.loadDataModFunc = {
  };
  
  //Methods that will take the data from the ui controls and map over to json
  this.submitDataModFunc = {
    method : this.saveTreeHelper.createDelegate(this)
  };
  
  this.privilegeRecordConstructor = Ext.data.Record.create([
    {name:'resourceUri'},
    {name:'id'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
    {name:'type'},
    {name:'method'}
  ]);
  
  this.repoTargetRecordConstructor = Ext.data.Record.create([
    {name:'id'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString}
  ]);
  
  
  //Reader and datastore that queries the server for the list of currently defined privileges
  this.privilegesReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.privilegeRecordConstructor );
  this.privilegesDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.privileges,
    reader: this.privilegesReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true
  });
  
  this.repoTargetReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.repoTargetRecordConstructor );
  this.repoTargetDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.repoTargets,
    reader: this.repoTargetReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true
  });
  
  this.COMBO_WIDTH = 300;
  this.CHECKBOX_WIDTH = 80;
  
  //Build the form
  this.formConfig = {
    region: 'center',
    width: '100%',
    height: '100%',
    autoScroll: true,
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
    labelWidth: 200,
    layoutConfig: {
      labelSeparator: ''
    },
        
    items: [
      {
        xtype: 'hidden',
        name: 'id'
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Name',
        itemCls: 'required-field',
        labelStyle: 'margin-left: 15px; width: 185px;',
        helpText: ht.name,
        name: 'name',
        allowBlank: false,
        width: this.COMBO_WIDTH
      },
      {
        xtype: 'combo',
        fieldLabel: 'Type',
        labelStyle: 'margin-left: 15px; width: 185px;',
        itemCls: 'required-field',
        helpText: ht.type,
        name: 'type',
        store: this.typeStore,
        displayField:'display',
        valueField:'value',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false,
        width: this.COMBO_WIDTH,
        value: 'repositoryTarget',
        disabled: true
      },
      {
        xtype: 'combo',
        fieldLabel: 'Repository Target',
        labelStyle: 'margin-left: 15px; width: 185px;',
        itemCls: 'required-field',
        helpText: ht.type,
        name: 'repositoryTargetId',
        store: this.repoTargetDataStore,
        displayField:'name',
        valueField:'id',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false,
        width: this.COMBO_WIDTH
      },
      {
        xtype: 'panel',
        layout: 'column',
        items: [
          {
            xtype: 'label',
            cls: 'x-form-item-label',
            ctCls: 'x-form-item',
            style: 'margin-left: 15px;',
            width: 190,
            text: 'Method(s)'
          },
          {
            xtype: 'panel',
            layout: 'column',
            items: [
              {
                xtype: 'treepanel',
                id: 'methods_tree',
                title: 'Selected Methods',
                cls: 'required-field',
                border: true, //note: this seem to have no effect w/in form panel
                bodyBorder: true, //note: this seem to have no effect w/in form panel
                //note: this style matches the expected behavior
                bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
                style: 'padding: 0 20px 0 0',
                width: 225,
                height: 150,
                animate:true,
                lines: false,
                autoScroll:true,
                containerScroll: true,
                //@note: root node must be instantiated uniquely for each instance of treepanel
                //@ext: can TreeNode be registerd as a component with an xtype so this new root node
                //      may be instantiated uniquely for each form panel that uses this config?
                rootVisible: false,

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
                  padding: [0,0,274,0]
                },
                // added Field values to simulate form field validation
                invalidText: 'One or more method(s) are required',
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
                        this.markTreeInvalid(tree);
                      }
                    },
                    scope: this
                  }
                }
              },
              {
                xtype: 'treepanel',
                id: 'all_methods_tree', 
                title: 'Available Methods',
                border: true, //note: this seem to have no effect w/in form panel
                bodyBorder: true, //note: this seem to have no effect w/in form panel
                //note: this style matches the expected behavior
                bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
                width: 225,
                height: 150,
                animate:true,
                lines: false,
                autoScroll:true,
                containerScroll: true,
                //@note: root node must be instantiated uniquely for each instance of treepanel
                //@ext: can TreeNode be registerd as a component with an xtype so this new root node
                //      may be instantiated uniquely for each form panel that uses this config?
                rootVisible: false,

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
                  padding: [0,0,274,0]
                }
              }
            ]
          }
        ]
      },
      {
      }
      ],
    buttons: [
      {
        id: 'savebutton',
        text: 'Save'
      },
      {
        id: 'cancelbutton',
        text: 'Cancel'
      }
    ]
  };

  this.privilegesGridPanel = new Ext.grid.GridPanel({
    title: 'Privileges',
    id: 'st-privileges-grid',
    
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
        id: 'privilege-refresh-btn',
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.reloadAll
      },
      {
        id: 'privilege-add-btn',
        text:'Add',
        icon: Sonatype.config.resourcePath + '/images/icons/add.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.addResourceHandler
      },
      {
        id: 'privilege-delete-btn',
        text: 'Delete',
        icon: Sonatype.config.resourcePath + '/images/icons/delete.png',
        cls: 'x-btn-text-icon',
        scope:this,
        handler: this.deleteHandler
      }
    ],

    //grid view options
    ds: this.privilegesDataStore,
    sortInfo:{field: 'name', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      {header: 'Name', dataIndex: 'name', width:175, id: 'privilege-config-name-col'},
      {header: 'Type', dataIndex: 'type', width:175, id: 'privilege-config-type-col'},
      {header: 'Method', dataIndex: 'method', width:175, id: 'privilege-config-method-col'}
    ],
    autoExpandColumn: 'privilege-config-name-col',
    disableSelection: false,
    viewConfig: {
      emptyText: 'Click "Add" to create a new Privilege.'
    }
  });
  this.privilegesGridPanel.on('rowcontextmenu', this.contextClick, this);

  Sonatype.repoServer.PrivilegeEditPanel.superclass.constructor.call(this, {
    layout: 'border',
    autoScroll: false,
    width: '100%',
    height: '100%',
    items: [
      this.privilegesGridPanel,
      {
        xtype: 'panel',
        id: 'privilege-config-forms',
        title: 'Privilege Configuration',
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
            html: '<div class="little-padding">Click "Add" to create a new privilege.</div>'
          }
        ]
      }
    ]
  });

  this.formCards = this.findById('privilege-config-forms');
};


Ext.extend(Sonatype.repoServer.PrivilegeEditPanel, Ext.Panel, {
  //Dump the currently stored data and requery for everything
  reloadAll : function(){
    this.privilegesDataStore.removeAll();
    this.privilegesDataStore.reload();
    this.repoTargetDataStore.removeAll();
    this.repoTargetDataStore.reload();
    this.formCards.items.each(function(item, i, len){
      if(i>0){this.remove(item, true);}
    }, this.formCards);
    
    this.formCards.getLayout().setActiveItem(0);
  },
  
  saveHandler : function(formInfoObj){
    var allValid = false;
    allValid = formInfoObj.formPanel.form.isValid()
    
    //form validation of repository treepanel
    var selectedTree = formInfoObj.formPanel.find('id', 'methods_tree')[0];
    var treeValid = selectedTree.validate.call(selectedTree);
    
    if (!treeValid) {
      this.markTreeInvalid(selectedTree);
    }
    
    allValid = (allValid && treeValid);
    
    if (allValid) {
      var isNew = formInfoObj.isNew;
      var createUri = Sonatype.config.repos.urls.privileges;
      var updateUri = (formInfoObj.resourceUri) ? formInfoObj.resourceUri : '';
      var form = formInfoObj.formPanel.form;
    
      form.doAction('sonatypeSubmit', {
        method: (isNew) ? 'POST' : 'PUT',
        url: isNew ? createUri : updateUri,
        waitMsg: isNew ? 'Creating Privilege...' : 'Updating Privilege...',
        fpanel: formInfoObj.formPanel,
        dataModifiers: this.submitDataModFunc,
        serviceDataObj : Sonatype.repoServer.referenceData.privileges.repositoryTarget,
        isNew : isNew //extra option to send to callback, instead of conditioning on method
      });
    }
  },
  
  cancelHandler : function(formInfoObj) {
    var formLayout = this.formCards.getLayout();
    var gridSelectModel = this.privilegesGridPanel.getSelectionModel();
    var store = this.privilegesGridPanel.getStore();
    
    this.formCards.remove(formInfoObj.formPanel.id, true);
    //select previously selected form, or the default view (index == 0)
    var newIndex = this.formCards.items.length - 1;
    newIndex = (newIndex >= 0) ? newIndex : 0;
    formLayout.setActiveItem(newIndex);

    //delete row from grid if canceling a new repo form
    if(formInfoObj.isNew){
      store.remove( store.getById(formInfoObj.formPanel.id) );
    }
    
    //select the coordinating row in the grid, or none if back to default
    var i = store.indexOfId(formLayout.activeItem.id);
    if (i >= 0){
      gridSelectModel.selectRow(i);
      this.rowClick(this.privilegesGridPanel, i, null);
    }
    else{
      gridSelectModel.clearSelections();
    }
  },
  
  addResourceHandler : function() {
    var id = 'new_item_' + new Date().getTime();

    var config = Ext.apply({}, this.formConfig, {id:id});
    
    config = this.initializeTreeRoots(id, config);
    
    var formPanel = new Ext.FormPanel(config);
    
    this.populateTree(formPanel);
    
    formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
    formPanel.form.on('actionfailed', this.actionFailedHandler, this);
    formPanel.on('beforerender', this.beforeFormRenderHandler, this);
    formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
        
    var buttonInfoObj = {
        formPanel : formPanel,
        isNew : true
      };
    
    //save button event handler
    formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
    //cancel button event handler
    formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
    
    //add place holder to grid
    var newRec = new this.privilegeRecordConstructor({
        id : 'new_item',
        name : 'New Privilege',
        resourceURI : 'new'
      },
      id); //use "new_item" id instead of resourceURI like the reader does
    this.privilegesDataStore.insert(0, [newRec]);
    this.privilegesGridPanel.getSelectionModel().selectRow(0);
    
    //add new form
    this.formCards.add(formPanel);
    
    //always set active and re-layout
    this.formCards.getLayout().setActiveItem(formPanel);
    formPanel.doLayout();
  },
  
  afterLayoutFormHandler : function(formPanel, fLayout){
    // register required field quicktip, but have to wait for elements to show up in DOM
    var temp = function(){
      var els = Ext.select('.required-field .x-form-item-label, .required-field .x-panel-header-text', this.getEl());
      els.each(function(el, els, i){
        Ext.QuickTips.register({
          target: el,
          cls: 'required-field',
          title: '',
          text: 'Required Field',
          enabled: true
        });
      });
    }.defer(300, formPanel);
  },
    
  deleteHandler : function(){
    if (this.ctxRecord || this.privilegesGridPanel.getSelectionModel().hasSelection()){
      var rec = this.ctxRecord ? this.ctxRecord : this.privilegesGridPanel.getSelectionModel().getSelected();

      if(rec.data.resourceURI == 'new'){
        this.cancelHandler({
          formPanel : Ext.getCmp(rec.id),
          isNew : true
        });
      }
      else {
        //@note: this handler selects the "No" button as the default
        //@todo: could extend Sonatype.MessageBox to take the button to select as a param
        Sonatype.MessageBox.getDialog().on('show', function(){
          this.focusEl = this.buttons[2]; //ack! we're offset dependent here
          this.focus();
        },
        Sonatype.MessageBox.getDialog(),
        {single:true});
        
        Sonatype.MessageBox.show({
          animEl: this.privilegesGridPanel.getEl(),
          title : 'Delete Privilege?',
          msg : 'Delete the ' + rec.get('name') + ' Privilege?',
          buttons: Sonatype.MessageBox.YESNO,
          scope: this,
          icon: Sonatype.MessageBox.QUESTION,
          fn: function(btnName){
            if (btnName == 'yes' || btnName == 'ok') {
              Ext.Ajax.request({
                callback: this.deleteCallback,
                cbPassThru: {
                  resourceId: rec.id
                },
                scope: this,
                method: 'DELETE',
                url:rec.data.resourceURI
              });
            }
          }
        });
      }
    }
  },
  
  deleteCallback : function(options, isSuccess, response){
    if(isSuccess){
      var resourceId = options.cbPassThru.resourceId;
      var formLayout = this.formCards.getLayout();
      var gridSelectModel = this.privilegesGridPanel.getSelectionModel();
      var store = this.privilegesGridPanel.getStore();

      if(formLayout.activeItem.id == resourceId){
        this.formCards.remove(resourceId, true);
        //select previously selected form, or the default view (index == 0)
        var newIndex = this.formCards.items.length - 1;
        newIndex = (newIndex >= 0) ? newIndex : 0;
        formLayout.setActiveItem(newIndex);
      }
      else {
        this.formCards.remove(resourceId, true);
      }

      store.remove( store.getById(resourceId) );

      //select the coordinating row in the grid, or none if back to default
      var i = store.indexOfId(formLayout.activeItem.id);
      if (i >= 0){
        gridSelectModel.selectRow(i);
      }
      else{
        gridSelectModel.clearSelections();
      }
    }
    else {
      Sonatype.MessageBox.alert('The server did not delete the privilege.');
    }
  },
      
  //(Ext.form.BasicForm, Ext.form.Action)
  actionCompleteHandler : function(form, action) {
    //@todo: handle server error response here!!

    if (action.type == 'sonatypeSubmit'){
      this.reloadAll();
    }
  },
  
  updatePrivilegeRecord : function(rec, receivedData){
        rec.beginEdit();
        rec.set('id', receivedData.id);
        rec.set('name', receivedData.name);
        rec.set('resourceURI', receivedData.resourceURI);
        rec.commit();
        rec.endEdit();
  },

  //(Ext.form.BasicForm, Ext.form.Action)
  actionFailedHandler : function(form, action){
    if(action.failureType == Ext.form.Action.CLIENT_INVALID){
      Sonatype.MessageBox.alert('Missing or Invalid Fields', 'Please change the missing or invalid fields.').setIcon(Sonatype.MessageBox.WARNING);
    }
//@note: server validation error are now handled just like client validation errors by marking the field invalid
//  else if(action.failureType == Ext.form.Action.SERVER_INVALID){
//    Sonatype.MessageBox.alert('Invalid Fields', 'The server identified invalid fields.').setIcon(Sonatype.MessageBox.ERROR);
//  }
    else if(action.failureType == Ext.form.Action.CONNECT_FAILURE){
      Sonatype.utils.connectionError( action.response, 'There is an error communicating with the server.' )
    }
    else if(action.failureType == Ext.form.Action.LOAD_FAILURE){
      Sonatype.MessageBox.alert('Load Failure', 'The data failed to load from the server.').setIcon(Sonatype.MessageBox.ERROR);
    }

    //@todo: need global alert mechanism for fatal errors.
  },
  
  beforeFormRenderHandler : function(component){
    var sp = Sonatype.lib.Permissions;
    if(sp.checkPermission(Sonatype.user.curr.repoServer.configPrivileges, sp.EDIT)){
      component.buttons[0].disabled = false;
    }
  },
  
  contextClick : function(grid, index, e){
    this.contextHide();
    
    if ( e.target.nodeName == 'A' ) return; // no menu on links
    
    this.ctxRow = this.privilegesGridPanel.view.getRow(index);
    this.ctxRecord = this.privilegesGridPanel.store.getAt(index);
    Ext.fly(this.ctxRow).addClass('x-node-ctx');

    //@todo: would be faster to pre-render the six variations of the menu for whole instance
    var menu = new Ext.menu.Menu({
      id:'privileges-grid-ctx',
      items: [
        this.actions.refresh
      ]
    });
    
    if (this.ctxRecord.data.type != 'application'){
      menu.add(this.actions.deleteAction);
    }
    
    //TODO: Add additional menu items
    
    menu.on('hide', this.contextHide, this);
    e.stopEvent();
    menu.showAt(e.getXY());
  },
  
  contextHide : function(){
    if(this.ctxRow){
      Ext.fly(this.ctxRow).removeClass('x-node-ctx');
      this.ctxRow = null;
      this.ctxRecord = null;
    }
  },
  
  populateTree : function(fpanel) {
    var allTree = fpanel.find('id', 'all_methods_tree')[0];
    
    this.methodStore.each(function(item, i, len){
      allTree.root.appendChild(
        new Ext.tree.TreeNode({
          id: item.data.value,
          text: item.data.display,
          payload: item.data.value, //sonatype added attribute
          allowChildren: false,
          draggable: true,
          leaf: true
        })
      );
    }, this);
  },
  
  saveTreeHelper : function(val, fpanel){
    var selectedTree = fpanel.find('id', 'methods_tree')[0];

    var outputArr = [];
    var nodes = selectedTree.root.childNodes;

    //Pretty simple here, just go through the weekdays selected and output the payload
    //which is just the name of the weekday (monday, tuesday, etc.)
    for(var i = 0; i < nodes.length; i++){
      outputArr[i] = nodes[i].attributes.payload;
    }

    return outputArr;
  },
  
  initializeTreeRoots : function(id, config){
    //@note: there has to be a better way to do this.  Depending on offsets is very error prone
    var newConfig = config;

    newConfig.items[4].items[1].items[0].root = new Ext.tree.TreeNode({text: 'root'});
    newConfig.items[4].items[1].items[1].root = new Ext.tree.TreeNode({text: 'root'});

    return newConfig;
  },
  
  markTreeInvalid : function(tree) {
    var elp = tree.getEl();
    
    if(!tree.errorEl){
        tree.errorEl = elp.createChild({cls:'x-form-invalid-msg'});
        tree.errorEl.setWidth(elp.getWidth(true)); //note removed -20 like on form fields
    }
    tree.invalid = true;
    tree.errorEl.update(tree.invalidText);
    elp.child('.x-panel-body').setStyle({
      'background-color' : '#fee',
      border : '1px solid #dd7870'
    });
    Ext.form.Field.msgFx['normal'].show(tree.errorEl, tree);  
  }
});
