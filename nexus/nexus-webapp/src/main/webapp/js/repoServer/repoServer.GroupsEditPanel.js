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
 * Repository Groups Edit/Create panel layout and controller
 */

Sonatype.repoServer.GroupsEditPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  var tfStore = new Ext.data.SimpleStore({fields:['value'], data:[['True'],['False']]});
  var ht = Sonatype.repoServer.resources.help.groups;
  
  //A record to hold the contentClasses
  this.contentClassRecordConstructor = Ext.data.Record.create([
    {name:'provider'},
    {name:'format'},
    {name:'description', sortType:Ext.data.SortTypes.asUCString}
  ]);
  
  //Reader and datastore that queries the server for the list of content classes
  this.contentClassesReader = new Ext.data.JsonReader({root: 'data', id: 'provider'}, this.contentClassRecordConstructor );
  this.contentClassesDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.groupRepoTypes,
    reader: this.contentClassesReader,
    sortInfo: {field: 'description', direction: 'ASC'},
    autoLoad: true
  });
  
  this.repositoryDataStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    fields: [
      { name: 'id' },
      { name: 'format' },
      { name: 'repoType' },
      { name: 'name', sortType: Ext.data.SortTypes.asUCString }
    ],
    sortInfo: { field: 'name', direction: 'asc' },
    url: Sonatype.config.repos.urls.repositories,
  autoLoad: true
  } );

  this.loadDataModFuncs = {
    group : {
      repositories : this.loadRepoListHelper.createDelegate(this)
    }
  };
  
  this.submitDataModFuncs = {
    group : {
      repositories : this.exportRepoListHelper.createDelegate(this)
    }
  };
    
  this.formConfig = {};
  this.formConfig.group = {
    region: 'center',
    width: '100%',
    height: '100%',
    autoScroll: true,
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
    labelWidth: 100,
    layoutConfig: {
      labelSeparator: ''
    },
        
    items: [
      //id field is swapped out for new group forms
      {
        xtype: 'textfield',
        fieldLabel: 'Group ID',
        itemCls: 'required-field',
        helpText: ht.id,
        name: 'id',
        width: 200,
        allowBlank:false,
        disabled: true,
        validator: Sonatype.utils.validateId
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Group Name',
        itemCls: 'required-field',
        helpText: ht.name,
        name: 'name',
        width: 200,
        allowBlank:false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Provider',
        itemCls: 'required-field',
        helpText: ht.provider,
        name: 'provider',
        width: this.COMBO_WIDTH,
        store: this.contentClassesDataStore,
        displayField:'description',
        valueField: 'provider',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false,
        listeners: {
          select: this.providerSelectHandler,
          scope: this
        }
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Format',
        itemCls: 'required-field',
        helpText: ht.format,
        name: 'format',
        width: 100,
        disabled: true,
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
            id: '_group-repos-tree', //note: unique ID is assinged before instantiation
            title: 'Ordered Group Repositories',
            cls: 'required-field',
            border: true, //note: this seem to have no effect w/in form panel
            bodyBorder: true, //note: this seem to have no effect w/in form panel
            //note: this style matches the expected behavior
            bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
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

            enableDD: true,
            //ddGroup: 'group-repos',
            ddScroll: true,
            dropConfig: {
              allowContainerDrop: true,
              onContainerDrop: function(source, e, data){
                if ( this.onContainerOver( source, e, data ) == this.dropAllowed ) {
                  this.tree.root.appendChild(data.node);
                  return true;
                }
                else {
                  return false;
                }
              },
              onContainerOver:function(source, e, data){
                return this.dropAllowed;
              },
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
            xtype: 'twinpanelcontroller'
          },
          {
            xtype: 'treepanel',
            id: id + '_group-all-repos-tree', //note: unique ID is assinged before instantiation
            title: 'Available Repositories',
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
        id: 'savebutton',
        text: 'Save',
        disabled: true
      },
      {
        id: 'cancelbutton',
        text: 'Cancel'
      }
    ]
  };
  
  this.nameConcatinator = function(val, parent) {
    return Sonatype.utils.joinArrayObject(val, 'name');
  };
  
  this.restToContentUrl = function(r){
    if (r.indexOf(Sonatype.config.host) > -1) {
      return r.replace(Sonatype.config.repos.urls.groups, Sonatype.config.content.groups);
    }
    else {
      return Sonatype.config.host + r.replace(Sonatype.config.repos.urls.groups, Sonatype.config.content.groups);
    }
  };

  // START: Repo list ******************************************************
  this.groupRecordConstructor = Ext.data.Record.create([
    {name:'resourceURI'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
    {name:'repositories'},
    {name:'sRepositories', mapping:'repositories', convert: this.nameConcatinator},
    {name:'contentUri', mapping:'resourceURI', convert: this.restToContentUrl },
    {name:'format'}
  ]);

  this.groupsReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.groupRecordConstructor );

  //@ext: must use data.Store (not JsonStore) to pass in reader instead of using fields config array
  this.groupsDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.groups,
    reader: this.groupsReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true
  });
  
  this.sp = Sonatype.lib.Permissions;

  this.groupsGridPanel = new Ext.grid.GridPanel({
    title: 'Repository Groups',
    id: 'st-groups-grid',
    
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
        id: 'group-refresh-btn',
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.reloadAll
      },
      {
        id: 'group-add-btn',
        text:'Add',
        icon: Sonatype.config.resourcePath + '/images/icons/add.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.addResourceHandler,
        disabled: !this.sp.checkPermission('nexus:repogroups', this.sp.CREATE)
      },
      {
        id: 'group-delete-btn',
        text: 'Delete',
        icon: Sonatype.config.resourcePath + '/images/icons/delete.png',
        cls: 'x-btn-text-icon',
        scope:this,
        handler: this.deleteResourceHandler,
        disabled: !this.sp.checkPermission('nexus:repogroups', this.sp.DELETE)
      }
    ],

    //grid view options
    ds: this.groupsDataStore,
    sortInfo:{field: 'name', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      {header: 'Group', dataIndex: 'name', width:175},
      {header: 'Format', dataIndex: 'format', width:60},
      {header: 'Repositories', dataIndex: 'sRepositories', width:300},
      {header: 'Group Path', dataIndex: 'contentUri', id: 'groups-config-url-col', width:300,renderer: function(s){return '<a href="' + s + ((s != null && (s.charAt(s.length)) == '/') ? '' : '/') +'" target="_blank">' + s + '</a>';},menuDisabled:true}
    ],
    autoExpandColumn: 'groups-config-url-col',
    disableSelection: false,
    viewConfig: {
      emptyText: 'Click "Add" to create a Repository Group'
    }
  });
  this.groupsGridPanel.getSelectionModel().on('rowselect', this.rowSelect, this);
// END: List ******************************************************
  // *********************************************************************

  Sonatype.repoServer.GroupsEditPanel.superclass.constructor.call(this, {
    layout: 'border',
    autoScroll: false,
    items: [
      this.groupsGridPanel,
      {
        xtype: 'panel',
        id: 'group-config-forms',
        title: 'Repository Group Configuration',
        layout: 'card',
        region: 'center',
        split: true,
        activeItem: 0,
        deferredRender: false,
        autoScroll: false,
        frame: false,
        items: [
          {
            xtype: 'panel',
            layout: 'fit',
            id: 'group-no-form',
            html: '<div class="little-padding">Select a Repository Group to edit it, or click "Add" to create a new one.</div>'
          }
        ]
      }
    ]
  });

  this.formCards = this.findById('group-config-forms');
};


Ext.extend(Sonatype.repoServer.GroupsEditPanel, Ext.Panel, {
//contentUriColRender: function(value, p, record, rowIndex, colIndex, store) {
//  return String.format('<a target="_blank" href="{0}">{0}</a>', value);
//},

  reloadAll : function(){
    this.contentClassesDataStore.reload();
    this.groupsDataStore.reload();
    this.repositoryDataStore.reload();
    this.formCards.items.each(function(item, i, len){
      if(i>0){this.remove(item, true);}
    }, this.formCards);
    
    this.formCards.getLayout().setActiveItem(0);
  },
  
  markTreeInvalid : function(tree, errortext) {
    var elp = tree.getEl();
    
    if(!tree.errorEl){
        tree.errorEl = elp.createChild({cls:'x-form-invalid-msg'});
        tree.errorEl.setWidth(elp.getWidth(true)); //note removed -20 like on form fields
    }
    tree.invalid = true;
    var oldErrorText = tree.invalidText
    if (errortext){
      tree.invalidText = errortext;
    }
    tree.errorEl.update(tree.invalidText);
    tree.invalidText = oldErrorText;
    elp.child('.x-panel-body').setStyle({
      'background-color' : '#fee',
      border : '1px solid #dd7870'
    });
    Ext.form.Field.msgFx['normal'].show(tree.errorEl, tree);
    
  },
  
  // formInfoObj : {formPanel, isNew, [resourceURI]}
  saveHandler : function(formInfoObj){
    var allValid = false;
    allValid = formInfoObj.formPanel.form.isValid();
    
    //form validation of repository treepanel
    var grpTree = Ext.getCmp(formInfoObj.formPanel.id + '_group-repos-tree');
    var grpTreeValid = grpTree.validate.call(grpTree);
    
    if (!grpTreeValid) {
      this.markTreeInvalid(grpTree,null);
    }
    
    allValid = (allValid && grpTreeValid);
    
    if (allValid) {
      var isNew = formInfoObj.isNew;
      var uri = (isNew) ? Sonatype.config.repos.urls.groups : formInfoObj.resourceURI;
      var form = formInfoObj.formPanel.form;

      form.doAction('sonatypeSubmit', {
        method: (isNew) ? 'POST' : 'PUT',
        url: uri,
        waitMsg: isNew ? 'Creating repository group...' : 'Updating repository group configuration...',
        fpanel: formInfoObj.formPanel,
        dataModifiers: this.submitDataModFuncs.group,
        serviceDataObj : Sonatype.repoServer.referenceData.group,
        isNew : isNew //extra option to send to callback, instead of conditioning on HTTP method
      });
    }
  },

  // formInfoObj : {formPanel, isNew, [resourceURI]}
  cancelHandler : function(formInfoObj) {
    var formLayout = this.formCards.getLayout();
    var gridSelectModel = this.groupsGridPanel.getSelectionModel();
    var store = this.groupsGridPanel.getStore();

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
    }
    else{
      gridSelectModel.clearSelections();
    }
  },

  addResourceHandler : function() {
    var id = 'new_group_' + new Date().getTime();

    var config = Ext.apply({}, this.formConfig.group, {id:id});
    config = this.configUniqueIdHelper(id, config);
    var formPanel = new Ext.FormPanel(config);

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

    //load available repos tree list.  note: this is kind of awkward to reuse the data mod function
    this.loadRepoListHelper([], {}, formPanel);
    
    //add new form
    this.formCards.add(formPanel);

    //add place holder to grid
    var newRec = new this.groupRecordConstructor({
        name : 'New Group',
        resourceURI : 'new',
        repositories : []
      },
      id); //use "new_group_" id instead of resourceURI like the reader does
    this.groupsDataStore.insert(0, [newRec]);
    this.groupsGridPanel.getSelectionModel().selectRow(0);
    
    //always set active and re-layout
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

  deleteResourceHandler : function(){
    if (this.groupsGridPanel.getSelectionModel().hasSelection()){
      var selections = this.groupsGridPanel.getSelectionModel().getSelections();

      if ( selections.length == 1 ) {
        var rec = this.groupsGridPanel.getSelectionModel().getSelected();

        if(rec.data.resourceURI == 'new'){
          this.cancelHandler({
            formPanel : Ext.getCmp(rec.id),
            isNew : true
          });
        }
        else {
          Sonatype.utils.defaultToNo();
          Sonatype.MessageBox.show({
            animEl: this.groupsGridPanel.getEl(),
            title : 'Delete Group',
            msg : 'Delete the ' + rec.get('name') + ' group?',
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
      else {
        Sonatype.utils.defaultToNo();
        Sonatype.MessageBox.show({
          animEl: this.groupsGridPanel.getEl(),
          title : 'Delete Groups',
          msg : 'Delete ' + selections.length + ' groups?',
          buttons: Sonatype.MessageBox.YESNO,
          scope: this,
          icon: Sonatype.MessageBox.QUESTION,
          fn: function(btnName){
            Ext.each( selections, function(rec) {
              if(rec.data.resourceURI == 'new'){
                this.cancelHandler({
                  formPanel : Ext.getCmp(rec.id),
                  isNew : true
                });
              }
              else {
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
            }, this );
          }
        });
      }
    }
  },
  
  //loads the list of repos for populating the list in each form
  processRepoList : function(options, isSuccess, response){
    if(isSuccess){
      var d = Ext.decode(response.responseText).data;
      var rList = [];
      
      Ext.each(d, function(item, i, allArr){
          rList[i] = {
            id : item.resourceURI.slice(item.resourceURI.lastIndexOf('/')+1),
            name : item.name,
            resourceURI : item.resourceURI,
            format : item.format
          };
        },
        this);
      
      this.reposList = rList;
    }
    else {
      this.reposList = [];
      Sonatype.MessageBox.alert('Could not receive the list of available repositories.');
    }
  },
  
  deleteCallback : function(options, isSuccess, response){
    if(isSuccess){
      var resourceId = options.cbPassThru.resourceId;
      var formLayout = this.formCards.getLayout();
      var gridSelectModel = this.groupsGridPanel.getSelectionModel();
      var store = this.groupsGridPanel.getStore();

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

      Sonatype.Events.fireEvent( 'groupChanged' );
    }
    else {
      Sonatype.MessageBox.alert('The server did not delete the group.');
    }
  },

  //(Ext.form.BasicForm, Ext.form.Action)
  actionCompleteHandler : function(form, action) {
    //@todo: handle server error response here!!

    if (action.type == 'sonatypeSubmit'){
      var isNew = action.options.isNew;

      if (isNew) {
        //successful create
        var sentData = action.output.data;
        //group state data doesn't have resourceURI in it like the list data
        sentData.resourceURI = action.getUrl() + '/' + sentData.id; //add this to match the list data field to create the record
        //@ext: this application of the convert function is needed because Ext only
        //      applies the convert functions in Ext.data.XXXReader.readRecords(), 
        //      not in the constructor yielded from Ext.data.Record.create()
        sentData.sRepositories = this.nameConcatinator(sentData.repositories);
        sentData.contentUri = this.restToContentUrl(sentData.resourceURI);

        var newRec = new this.groupRecordConstructor(sentData, action.options.fpanel.id); //form and grid data id match, keep the new id

        this.groupsDataStore.remove(this.groupsDataStore.getById(action.options.fpanel.id)); //remove old one
        this.groupsDataStore.addSorted(newRec);
        this.groupsGridPanel.getSelectionModel().selectRecords([newRec], false);

        var idTextField = action.options.fpanel.find('name', 'id')[0];
        idTextField.disable();
    action.options.fpanel.find('name', 'format')[0].disable();
//note: the label doesn't delete with its text field.  So just disable for now        
//        var resourceId = idTextField.getValue();
        
//        //insert ID hidden field
//        action.options.fpanel.insert(0, {
//          xtype: 'hidden',
//          name: 'id',
//          value: resourceId
//        });
//        //remove ID text field
//        idTextField.setVisible(false);
//        action.options.fpanel.remove(idTextField, true);
//        
//        action.options.fpanel.doLayout();
        
        //remove button click listeners
        action.options.fpanel.buttons[0].purgeListeners();
        action.options.fpanel.buttons[1].purgeListeners();

        var buttonInfoObj = {
            formPanel : action.options.fpanel,
            isNew : false,
            resourceURI : sentData.resourceURI
          };

        //save button event handler
        action.options.fpanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
        //cancel button event handler
        action.options.fpanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
      }
      else {
        var sentData = action.output.data;
        var rec = this.groupsDataStore.getById(action.options.fpanel.id);
        
        rec.beginEdit();
        rec.set('name', sentData.name);
        rec.set('format', sendData.format);
        rec.set('repositories', sentData.repositories);
        rec.set('sRepositories', Sonatype.utils.joinArrayObject(sentData.repositories, 'name'));
        rec.commit();
        rec.endEdit();
        
        var sortState = this.groupsDataStore.getSortState();
        this.groupsDataStore.sort(sortState.field, sortState.direction);
      }

      Sonatype.Events.fireEvent( 'groupChanged' );
    }
    else{
      //When load is complete, reload to update the repos
      this.loadRepoListHelper([], {}, action.options.fpanel);
    }
  },

  //(Ext.form.BasicForm, Ext.form.Action)
  actionFailedHandler : function(form, action){
    if(action.failureType == Ext.form.Action.CLIENT_INVALID){
      Sonatype.MessageBox.alert('Missing or Invalid Fields', 'Please change the missing or invalid fields.').setIcon(Sonatype.MessageBox.WARNING);
    }
    //@note: server validation error are now handled just like client validation errors by marking the field invalid
    //@new note: because of the fact that repositories field is not directly linked to list of repos, need to manually handle this case
    else if(action.failureType == Ext.form.Action.SERVER_INVALID){
      for(var i=0; i<action.result.errors.length; i++){
        if (action.result.errors[i].id == 'repositories'){
          var grpTree = Ext.getCmp(this.formCards.layout.activeItem.id + '_group-repos-tree');
          this.markTreeInvalid(grpTree,action.result.errors[i].msg);
        }
      }
    }
    else if(action.failureType == Ext.form.Action.CONNECT_FAILURE){
      Sonatype.utils.connectionError( action.response, 'There is an error communicating with the server.' )
    }
    else if(action.failureType == Ext.form.Action.LOAD_FAILURE){
      Sonatype.MessageBox.alert('Load Failure', 'The data failed to load from the server.').setIcon(Sonatype.MessageBox.ERROR);
    }
    
    //@todo: need global alert mechanism for fatal errors.
  },

  beforeFormRenderHandler : function(component){
    if(component.id.search(/new_/) === 0){
      component.items.items[0].disabled = false; // endable ID field for new repo forms only
    }
    
    var sp = Sonatype.lib.Permissions;
    if(sp.checkPermission('nexus:repogroups', sp.EDIT)){
      component.buttons[0].disabled = false;
    }
  },

  formDataLoader : function(formPanel, resourceURI, modFuncs){
    formPanel.getForm().doAction('sonatypeLoad', {url:resourceURI, method:'GET', fpanel:formPanel, dataModifiers: modFuncs, scope: this});
  },

  rowSelect : function( selectionModel, index, rec ){
    var id = rec.id; //note: rec.id is unique for new resources and equal to resourceURI for existing ones
    var formPanel = this.formCards.findById(id);

    //assumption: new group forms already exist in formCards, so they won't get into this case
    if(!formPanel){ //create form and populate current data
      var config = Ext.apply({}, this.formConfig.group, {id:id});
      config = this.configUniqueIdHelper(id, config);
      
      formPanel = new Ext.FormPanel(config);
      formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
      formPanel.form.on('actionfailed', this.actionFailedHandler, this);
      formPanel.on('beforerender', this.beforeFormRenderHandler, this);
      formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
      
      var buttonInfoObj = {
        formPanel : formPanel,
        isNew : false, //not a new group form, see assumption
        resourceURI : rec.data.resourceURI
      };

      //save button event handler
      formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
      //cancel button event handler
      formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
      
      formPanel.find( 'name' , 'format' )[0].disable();
      
      this.formDataLoader(formPanel, rec.data.resourceURI, this.loadDataModFuncs.group);
      this.formCards.add(formPanel);
    }

    //always set active and re-layout
    this.formCards.getLayout().setActiveItem(formPanel);
    formPanel.doLayout();
  },

  //creates a unique config object with specific IDs on the two tree items
  configUniqueIdHelper : function(id, config){
    //@note: there has to be a better way to do this.  Depending on offsets is very error prone

    var newConfig = config;
    
//    if(id.search(/new_/) == 0){
//      // enable editableID field for new repo forms only
//      newConfig.items[0] = {
//        xtype: 'textfield',
//        fieldLabel: 'Group ID',
//        name: 'id',
//        width: 200,
//        allowBlank:false
//      };
//    }
//    else {
//      newConfig.items[0] = {
//        xtype: 'hidden',
//        name: 'id',
//        value: '0'
//      };
//    }

    var trees = [
      {obj : newConfig.items[4].items[0], postpend : '_group-repos-tree'},
      {obj : newConfig.items[4].items[2], postpend : '_group-all-repos-tree'}
    ];

    for (var i = 0; i<trees.length; i++) {
      trees[i].obj.id = id + trees[i].postpend;
      trees[i].obj.root = new Ext.tree.TreeNode({text: 'root'});
    }

    return newConfig;
  },
  
  // requires being scoped to GroupsConfigPanel to get shared reposList data
  loadRepoListHelper : function(arr, srcObj, fpanel){
    var grpTree = Ext.getCmp(fpanel.id + '_group-repos-tree');
    var allTree = Ext.getCmp(fpanel.id + '_group-all-repos-tree');
    
  var format = fpanel.find('name', 'format')[0].value;
  
  if ( arr.length == 0) {
      for (var i=grpTree.root.childNodes.length - 1; i>=0; i--){
        if ( grpTree.root.childNodes[i].attributes.payload.format != format) {
        grpTree.root.childNodes[i].remove();
      }
      }
  }
    
    for (var i=allTree.root.childNodes.length - 1; i>=0; i--){
      allTree.root.childNodes[i].remove();
    }
    
    for(var i=0; i<arr.length; i++){
      var repo = this.repositoryDataStore.getById(arr[i].id);
    if ( typeof(format) == 'undefined'
       || repo.data.format == format) {
        grpTree.root.appendChild(
          new Ext.tree.TreeNode({
            id: repo.data.id,
            text: repo.data.name,
            payload: repo.data, //sonatype added attribute
          
            allowChildren: false,
            draggable: true,
            leaf: true
          })
        );
      }
    }
    
    this.repositoryDataStore.each(function(item, i, len){
      var assignedNode = grpTree.getNodeById(item.data.id);
      if(typeof(assignedNode) == 'undefined' 
        && (typeof(format) == 'undefined'
       || item.data.format == format)){        
        allTree.root.appendChild(
          new Ext.tree.TreeNode({
            id: item.data.id,
            text: item.data.name,
            payload: item.data, //sonatype added attribute

            allowChildren: false,
            draggable: true,
            leaf: true
          })
        );
      }
    }, this);
        
    return arr; //return arr, even if empty to comply with sonatypeLoad data modifier requirement
  },
  
  exportRepoListHelper : function(val, fpanel){
    var grpTree = Ext.getCmp(fpanel.id + '_group-repos-tree');
    
    var outputArr = [];
    var nodes = grpTree.root.childNodes;
    
    for(var i = 0; i < nodes.length; i++){
      var repo = nodes[i].attributes.payload;
      outputArr[i] = {
        id: repo.id,
        name: repo.name,
        resourceURI: repo.resourceURI
      };
    }
    
    return outputArr;
  },

  providerSelectHandler: function( combo, rec, index ) {
    combo.ownerCt.find( 'name', 'format' )[0].setValue( rec.data.format );
    this.loadRepoListHelper([], {}, this.formCards.getLayout().activeItem);
  }
});
