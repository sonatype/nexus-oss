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
 * Service Schedule Edit/Create panel layout and controller
 */
  
Sonatype.repoServer.SchedulesEditPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  var ht = Sonatype.repoServer.resources.help.schedules;
  
  //TODO: this will be calling a rest method at some point
  this.serviceTypeStore = new Ext.data.SimpleStore({fields:['value'], data:[['Synchronize Repositories'],['Purge Snapshots']]});  
  this.scheduleTypeStore = new Ext.data.SimpleStore({fields:['value'], data:[['Off'],['Daily'],['Weekly'],['Monthly'],['Advanced']]});
  this.weekdayStore = new Ext.data.SimpleStore({fields:['value'], data:[['Sunday'],['Monday'],['Tuesday'],['Wednesday'],['Thursday'],['Friday'],['Saturday']]});
  
  this.loadDataModFuncs = {
    schedule : {
    }
  };
  
  this.submitDataModFuncs = {
    schedule : {
    }
  };
  
  this.formConfig = {};
  this.formConfig.scheduleOff = {
    region: 'center',
    width: '100%',
    height: '100%',
    autoScroll: true,
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
    labelWidth: 175,
    layoutConfig: {
      labelSeparator: ''
    },
        
    items: [
      {
        xtype: 'hidden',
        name: 'id'
      },      
      {
        xtype: 'checkbox',
        fieldLabel: 'Enabled',
        helpText: ht.enabled,
        name: 'enabled'    
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Name',
        itemCls: 'required-field',
        helpText: ht.name,
        name: 'name',
        width: 200,
        allowBlank:false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Service Type',
        itemCls: 'required-field',
        helpText: ht.serviceType,
        name: 'serviceType',
        store: this.serviceTypeStore,
        displayField:'value',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false    
      },
      {
        xtype: 'combo',
        fieldLabel: 'Recurrence',
        itemCls: 'required-field',
        helpText: ht.serviceSchedule,
        name: 'serviceSchedule',
        store: this.scheduleTypeStore,
        displayField:'value',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false    
      },
      {
        xtype: 'panel',
        id: 'schedule-config-card-panel',
        header: false,
        layout: 'card',
        region: 'center',
        activeItem: 0,
        bodyStyle: 'padding:15px',
        deferredRender: false,
        autoScroll: false,
        frame: false,
        items: [
          {
	          xtype: 'fieldset',
    	      checkboxToggle:false,
    	      title: 'No Schedule Settings',
    	      anchor: Sonatype.view.FIELDSET_OFFSET,
    	      collapsible: false,
    	      autoHeight:true,
    	      layoutConfig: {
	            labelSeparator: ''
	          }
          },
          {
            xtype: 'fieldset',
    	      checkboxToggle:false,
    	      title: 'Daily Schedule Settings',
    	      anchor: Sonatype.view.FIELDSET_OFFSET,
    	      collapsible: false,
    	      autoHeight:true,
    	      layoutConfig: {
	            labelSeparator: ''
	          },
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'Start Date',
                itemCls: 'required-field',
                helpText: ht.startDate,
                name: 'startDate',
                width: 200,
                allowBlank:false
              }
            ]
          },
          {
            xtype: 'fieldset',
    	      checkboxToggle:false,
    	      title: 'Weekly Schedule Settings',
    	      anchor: Sonatype.view.FIELDSET_OFFSET,
    	      collapsible: false,
    	      autoHeight:true,
    	      layoutConfig: {
	            labelSeparator: ''
	          },
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'Start Date',
                itemCls: 'required-field',
                name: 'startDate',
                helpText: ht.startDate,
                width: 200,
                allowBlank:false
              }
            ]
          },
          {
            xtype: 'fieldset',
    	      checkboxToggle:false,
    	      title: 'Monthly Schedule Settings',
    	      anchor: Sonatype.view.FIELDSET_OFFSET,
    	      collapsible: false,
    	      autoHeight:true,
    	      layoutConfig: {
	            labelSeparator: ''
	          },
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'Start Date',
                helpText: ht.startDate,
                itemCls: 'required-field',
                name: 'startDate',
                width: 200,
                allowBlank:false
              }
            ]
          },
          {
            xtype: 'fieldset',
    		    checkboxToggle:false,
    		    title: 'Advanced Schedule Settings',
    		    anchor: Sonatype.view.FIELDSET_OFFSET,
    		    collapsible: false,
    		    autoHeight:true,
    		    layoutConfig: {
              labelSeparator: ''
            },
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'CRON command',
                itemCls: 'required-field',
                name: 'cronCommand',
                helpText: ht.cronCommand,
                width: 200,
                allowBlank:false
              }
            ]
          },
          {
            xtype: 'fieldset',
		    checkboxToggle:false,
		    title: 'Advanced Schedule Settings',
		    anchor: Sonatype.view.FIELDSET_OFFSET,
		    collapsible: false,
		    autoHeight:true,
		    layoutConfig: {
	          labelSeparator: ''
	        },
            items: [
              {
                xtype: 'textfield',
                fieldLabel: 'cron command',
                itemCls: 'required-field',
                name: 'cronCommand',
                width: 200,
                allowBlank:false
              }
            ]
          }
        ]
      },
      {
        xtype: 'editorgrid',
        title: 'Service Parameters',
        id: '_service_param_grid',
        collapsible: true,
        split: true,
        height: 200,
        minHeight: 150,
        maxHeight: 400,
        frame: false,
        autoScroll: true,
        clicksToEdit:2,
        tbar: [
          {
            id: 'schedule-add-btn',
            text:'Add',
            icon: Sonatype.config.resourcePath + '/images/icons/add.png',
            cls: 'x-btn-text-icon',
            scope: this,
            handler: this.addParameterResourceHandler
          },
          {
            id: 'schedule-delete-btn',
            text: 'Delete',
            icon: Sonatype.config.resourcePath + '/images/icons/delete.png',
            cls: 'x-btn-text-icon',
            scope:this,
            handler: this.deleteParameterResourceHandler
          }
        ],
        //grid view options
        //This will be assigned later at add time
        ds: null,
        sortInfo:{field: 'name', direction: "ASC"},
        loadMask: true,
        deferredRender: false,
        columns: [
          {
            header: 'Name', 
            dataIndex: 'name', 
            width:175, 
            id: 'schedule-config-parameter-name-col',
            editor: new Ext.form.TextField({
               allowBlank: false
            })
          },
          {
            header: 'Value', 
            dataIndex: 'value', 
            width:175, 
            id: 'schedule-config-parameter-value-col',
            editor: new Ext.form.TextField({
              allowBlank: false
            })
          }
        ],
        autoExpandColumn: 'schedule-config-parameter-name-col',
        disableSelection: false,
        viewConfig: {
          emptyText: 'Click "Add" to create a new parameter.'
        }
      }
	  ],
    buttons: [
      {
        text: 'Save'
      },
      {
        text: 'Cancel'
      }
    ]
  };
    
  // START: Repo list ******************************************************
  this.scheduleRecordConstructor = Ext.data.Record.create([
    {name:'resourceURI'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
    {name:'serviceType'},
    {name:'serviceSchedule'}
  ]);
  
  this.serviceParameterRecordConstructor = Ext.data.Record.create([
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
    {name:'value'}
  ]);

  this.schedulesReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.scheduleRecordConstructor );

  //@ext: must use data.Store (not JsonStore) to pass in reader instead of using fields config array
  this.schedulesDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.schedules,
    reader: this.schedulesReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true
  });

  this.schedulesGridPanel = new Ext.grid.GridPanel({
    title: 'Scheduled Services',
    id: 'st-schedules-grid',
    
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
        scope: this,
        handler: this.reloadAll
      },
      {
        id: 'schedule-add-btn',
        text:'Add',
        icon: Sonatype.config.resourcePath + '/images/icons/add.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.addResourceHandler
      },
      {
        id: 'schedule-delete-btn',
        text: 'Delete',
        icon: Sonatype.config.resourcePath + '/images/icons/delete.png',
        cls: 'x-btn-text-icon',
        scope:this,
        handler: this.deleteResourceHandler
      }
    ],

    //grid view options
    ds: this.schedulesDataStore,
    sortInfo:{field: 'name', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      {header: 'Name', dataIndex: 'name', width:175, id: 'schedule-config-name-col'},
      {header: 'Service Type', dataIndex: 'serviceType', width:175, id: 'schedule-config-service-type-col'},
      {header: 'Schedule', dataIndex: 'serviceSchedule', width:175, id: 'schedule-config-service-schedule-col'}
    ],
    autoExpandColumn: 'schedule-config-name-col',
    disableSelection: false,
    viewConfig: {
      emptyText: 'Click "Add" to create a scheduled service.'
    }
  });
  this.schedulesGridPanel.on('rowclick', this.rowClick, this);

  Sonatype.repoServer.SchedulesEditPanel.superclass.constructor.call(this, {
    layout: 'border',
    autoScroll: false,
    width: '100%',
    height: '100%',
    items: [
      this.schedulesGridPanel,
      {
        xtype: 'panel',
        id: 'schedule-config-forms',
        title: 'Scheduled Service Configuration',
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
            html: '<div class="little-padding">Select a scheduled service to edit it, or click "Add" to create a new one.</div>'
          }
        ]
      }
    ]
  });

  this.formCards = this.findById('schedule-config-forms');
};


Ext.extend(Sonatype.repoServer.SchedulesEditPanel, Ext.Panel, {
  reloadAll : function(){
    this.schedulesDataStore.reload();
    this.formCards.items.each(function(item, i, len){
      if(i>0){this.remove(item, true);}
    }, this.formCards);
    
    this.formCards.getLayout().setActiveItem(0);
  },
  
  saveHandler : function(formInfoObj){
    if (formInfoObj.formPanel.form.isValid()) {
      var isNew = formInfoObj.isNew;
      var repoType = formInfoObj.repoType;
      var createUri = Sonatype.config.repos.urls.schedules;
      var updateUri = (formInfoObj.resourceUri) ? formInfoObj.resourceUri : '';
      var form = formInfoObj.formPanel.form;
    
      form.doAction('sonatypeSubmit', {
        method: (isNew) ? 'POST' : 'PUT',
        url: isNew ? createUri : updateUri,
        waitMsg: isNew ? 'Creating scheduled service...' : 'Updating scheduled service configuration...',
        fpanel: formInfoObj.formPanel,
        dataModifiers: this.submitDataModFuncs.schedule,
        serviceDataObj : Sonatype.repoServer.referenceData.schedule,
        isNew : isNew //extra option to send to callback, instead of conditioning on method
      });
    }
  },
  
  cancelHandler : function(formInfoObj) {
    var formLayout = this.formCards.getLayout();
    var gridSelectModel = this.schedulesGridPanel.getSelectionModel();
    var store = this.schedulesGridPanel.getStore();
    
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
    var id = 'new_schedule_' + new Date().getTime();

    var config = Ext.apply({}, this.formConfig.scheduleOff, {id:id});
    config = this.configUniqueIdHelper(id, config);
    var formPanel = new Ext.FormPanel(config);
    
    formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
    formPanel.form.on('actionfailed', this.actionFailedHandler, this);
    formPanel.on('beforerender', this.beforeFormRenderHandler, this);
    formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
    
    var serviceScheduleField = formPanel.find('name', 'serviceSchedule')[0];
    serviceScheduleField.on('select', this.serviceScheduleSelectHandler, serviceScheduleField);
    
    //Rebuild
    formPanel.find('id', id + '_service_param_grid')[0].store = new Ext.data.SimpleStore(
      {fields:['name','value'], data:[['repository.id','central'],['some.other.id','blah']]}
    );
    
    var buttonInfoObj = {
        formPanel : formPanel,
        isNew : true
      };
    
    //save button event handler
    formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
    //cancel button event handler
    formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
    
    //add place holder to grid
    var newRec = new this.scheduleRecordConstructor({
        name : 'New Scheduled Service',
        resourceURI : 'new'
      },
      id); //use "new_schedule_" id instead of resourceURI like the reader does
    this.schedulesDataStore.insert(0, [newRec]);
    this.schedulesGridPanel.getSelectionModel().selectRow(0);
    
    //add new form
    this.formCards.add(formPanel);
    
    //always set active and re-layout
    this.formCards.getLayout().setActiveItem(formPanel);
    formPanel.doLayout();
  },
  
  addParameterResourceHandler : function() {
    var id = 'new_service_parameter_' + new Date().getTime();
    var newRec = new this.serviceParameterRecordConstructor({
        name : 'New Parameter',
        value : 'New Value'
      },
      id);
    
    this.find('id', this.formCards.layout.activeItem.id + '_service_param_grid')[0].getStore().insert(0,[newRec]);
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
    if (this.schedulesGridPanel.getSelectionModel().hasSelection()){
      var rec = this.schedulesGridPanel.getSelectionModel().getSelected();

      if(rec.data.resourceURI == 'new'){
        this.cancelHandler({
          formPanel : Ext.getCmp(rec.id),
          isNew : true
        });
      }
      else {
        //@note: this handler selects the "No" button as the default
        //@todo: could extend Ext.MessageBox to take the button to select as a param
        Ext.Msg.getDialog().on('show', function(){
          this.focusEl = this.buttons[2]; //ack! we're offset dependent here
          this.focus();
        },
        Ext.Msg.getDialog(),
        {single:true});
        
        Ext.Msg.show({
          animEl: this.schedulesGridPanel.getEl(),
          title : 'Delete Scheduled Service?',
          msg : 'Delete the ' + rec.get('name') + ' scheduled service?',
          buttons: Ext.Msg.YESNO,
          scope: this,
          icon: Ext.Msg.QUESTION,
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
      var gridSelectModel = this.schedulesGridPanel.getSelectionModel();
      var store = this.schedulesGridPanel.getStore();

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
      Ext.MessageBox.alert('The server did not delete the scheduled service.');
    }
  },
  
  deleteParameterResourceHandler : function(){
    var paramGrid = this.find('id', this.formCards.layout.activeItem.id + '_service_param_grid')[0];
    if (paramGrid.getSelectionModel().hasSelection()){
      var rec = paramGrid.getSelectionModel().selection.record;
      paramGrid.getStore().remove(rec);
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
        //scheduled service state data doesn't have resourceURI in it like the list data
        sentData.resourceURI = action.getUrl() + '/' + sentData.id; //add this to match the list data field to create the record
        
        var newRec = new this.scheduleRecordConstructor(sentData, action.options.fpanel.id); //form and grid data id match, keep the new id

        this.schedulesDataStore.remove(this.groupsDataStore.getById(action.options.fpanel.id)); //remove old one
        this.schedulesDataStore.addSorted(newRec);
        this.schedulesGridPanel.getSelectionModel().selectRecords([newRec], false);

        //set the hidden id field in the form for subsequent updates
        action.options.fpanel.find('name', 'id')[0].setValue(respData.id);
        //remove button click listeners
        action.options.fpanel.buttons[0].purgeListeners();
        action.options.fpanel.buttons[1].purgeListeners();

        var buttonInfoObj = {
            formPanel : action.options.fpanel,
            isNew : false,
            resourceUri : respData.resourceURI
          };

        //save button event handler
        action.options.fpanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
        //cancel button event handler
        action.options.fpanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
      }
      else {
        var sentData = action.output.data;

        var i = this.schedulesDataStore.indexOfId(action.options.fpanel.id);
        var rec = this.schedulesDataStore.getAt(i);

        rec.beginEdit();
        rec.set('name', sentData.name);
        rec.set('serviceType', sentData.serviceType);
        rec.set('serviceSchedule', sentData.serviceSchedule);
        rec.commit();
        rec.endEdit();
        
        var sortState = this.schedulesDataStore.getSortState();
        this.schedulesDataStore.sort(sortState.field, sortState.direction);
      }
    }
  },

  //(Ext.form.BasicForm, Ext.form.Action)
  actionFailedHandler : function(form, action){
    if(action.failureType == Ext.form.Action.CLIENT_INVALID){
      Ext.MessageBox.alert('Missing or Invalid Fields', 'Please change the missing or invalid fields.').setIcon(Ext.MessageBox.WARNING);
    }
//@note: server validation error are now handled just like client validation errors by marking the field invalid
//  else if(action.failureType == Ext.form.Action.SERVER_INVALID){
//    Ext.MessageBox.alert('Invalid Fields', 'The server identified invalid fields.').setIcon(Ext.MessageBox.ERROR);
//  }
    else if(action.failureType == Ext.form.Action.CONNECT_FAILURE){
      Ext.MessageBox.alert('Connection Failure', 'There is an error communicating with the server.').setIcon(Ext.MessageBox.ERROR);
    }
    else if(action.failureType == Ext.form.Action.LOAD_FAILURE){
      Ext.MessageBox.alert('Load Failure', 'The data failed to load from the server.').setIcon(Ext.MessageBox.ERROR);
    }

    //@todo: need global alert mechanism for fatal errors.
  },
  
  beforeFormRenderHandler : function(component){
    var sp = Sonatype.lib.Permissions;
    if(sp.checkPermission(Sonatype.user.curr.repoServer.configSchedules, sp.EDIT)){
      component.buttons[0].disabled = false;
    }
  },

  formDataLoader : function(formPanel, resourceUri, modFuncs){
    formPanel.getForm().doAction('sonatypeLoad', {url:resourceUri, method:'GET', fpanel:formPanel, dataModifiers: modFuncs, scope: this});
  },

  rowClick : function(grid, rowIndex, e){
    var rec = grid.store.getAt(rowIndex);
    var id = rec.id; //note: rec.id is unique for new resources and equal to resourceURI for existing ones
    var formPanel = this.formCards.findById(id);

    //assumption: new route forms already exist in formCards, so they won't get into this case
    if(!formPanel){ //create form and populate current data
      var config = Ext.apply({}, this.formConfig.scheduleOff, {id:id});
      config = this.configUniqueIdHelper(id, config);

      formPanel = new Ext.FormPanel(config);
      formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
      formPanel.form.on('actionfailed', this.actionFailedHandler, this);
      formPanel.on('beforerender', this.beforeFormRenderHandler, this);
      formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
      
      var serviceScheduleField = formPanel.find('name', 'serviceSchedule')[0];
      serviceScheduleField.on('check', this.serviceScheduleCheckHandler, serviceScheduleField);

      var buttonInfoObj = {
        formPanel : formPanel,
        isNew : false, //not a new route form, see assumption
        resourceUri : rec.data.resourceURI
      };

      //save button event handler
      formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
      //cancel button event handler
      formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));

      this.formDataLoader(formPanel, rec.data.resourceURI, this.loadDataModFuncs.route);
      this.formCards.add(formPanel);
    }

    //always set active and re-layout
    this.formCards.getLayout().setActiveItem(formPanel);
    formPanel.doLayout();
  },
  
  serviceScheduleSelectHandler : function(combo, record, index){
    var schedulePanel = this.ownerCt.find('id', 'schedule-config-card-panel')[0];
    if (record.data.value == 'Off'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(0));
    }
    else if (record.data.value == 'Daily'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(1));
    }
    else if (record.data.value == 'Weekly'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(2));
    }
    else if (record.data.value == 'Monthly'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(3));
    }
    else if (record.data.value == 'Advanced'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(4));
    }
    schedulePanel.doLayout();
  },
  
  //creates a unique config object with specific IDs on the two grid item
  configUniqueIdHelper : function(id, config){
    //@note: there has to be a better way to do this.  Depending on offsets is very error prone
    var newConfig = config;

    newConfig.items[6].id = id + '_service_param_grid';

    return newConfig;
  }  
});
