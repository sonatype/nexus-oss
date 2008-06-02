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
  
  //List of schedule types
  this.scheduleTypeStore = new Ext.data.SimpleStore({fields:['value'], data:[['None'],['Once'],['Daily'],['Weekly'],['Monthly'],['Advanced']]});
  //List of weekdays
  this.weekdaysList = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
  
  //Methods that will take the incoming json data and map over to the ui controls
  this.loadDataModFuncs = {
    none : {
      serviceSchedule : Sonatype.utils.capitalize,
      serviceProperties : this.importServicePropertiesHelper.createDelegate(this)
    },
    once : {
      serviceSchedule : Sonatype.utils.capitalize,
      serviceProperties : this.importServicePropertiesHelper.createDelegate(this),
      startDate : this.importStartDateHelper.createDelegate(this),
      startTime : this.importStartTimeHelper.createDelegate(this)
    },
    daily : {
      serviceSchedule : Sonatype.utils.capitalize,
      serviceProperties : this.importServicePropertiesHelper.createDelegate(this),
      startDate : this.importStartDateHelper.createDelegate(this),
      startTime : this.importStartTimeHelper.createDelegate(this),
      recurringTime : this.importRecurringTimeHelper.createDelegate(this)       
    },
    weekly : {
      serviceSchedule : Sonatype.utils.capitalize,
      serviceProperties : this.importServicePropertiesHelper.createDelegate(this),
      startDate : this.importStartDateHelper.createDelegate(this),
      startTime : this.importStartTimeHelper.createDelegate(this),
      recurringTime : this.importRecurringTimeHelper.createDelegate(this),
      recurringDay : this.importRecurringDayHelper.createDelegate(this)
    },
    monthly : {
      serviceSchedule : Sonatype.utils.capitalize,
      serviceProperties : this.importServicePropertiesHelper.createDelegate(this),
      startDate : this.importStartDateHelper.createDelegate(this),
      startTime : this.importStartTimeHelper.createDelegate(this),
      recurringTime : this.importRecurringTimeHelper.createDelegate(this),
      recurringDay : this.importMonthlyRecurringDayHelper.createDelegate(this)
    },
    advanced : {
      serviceSchedule : Sonatype.utils.capitalize,
      serviceProperties : this.importServicePropertiesHelper.createDelegate(this)
    }
  };
  
  //Methods that will take the data from the ui controls and map over to json
  this.submitDataModFuncs = {
    none : {
      serviceSchedule : Sonatype.utils.lowercase,
      serviceProperties : this.exportServicePropertiesHelper.createDelegate(this)
    },
    once : {
      serviceSchedule : Sonatype.utils.lowercase,
      serviceProperties : this.exportServicePropertiesHelper.createDelegate(this),
      startDate : this.exportStartDateHelper.createDelegate(this),
      startTime : this.exportStartTimeHelper.createDelegate(this)
    },
    daily : {
      serviceSchedule : Sonatype.utils.lowercase,
      serviceProperties : this.exportServicePropertiesHelper.createDelegate(this),
      startDate : this.exportStartDateHelper.createDelegate(this),
      startTime : this.exportStartTimeHelper.createDelegate(this),
      recurringTime : this.exportRecurringTimeHelper.createDelegate(this)
    },
    weekly : {
      serviceSchedule : Sonatype.utils.lowercase,
      serviceProperties : this.exportServicePropertiesHelper.createDelegate(this),
      startDate : this.exportStartDateHelper.createDelegate(this),
      startTime : this.exportStartTimeHelper.createDelegate(this),
      recurringTime : this.exportRecurringTimeHelper.createDelegate(this),
      recurringDay : this.exportRecurringDayHelper.createDelegate(this)
    },
    monthly : {
      serviceSchedule : Sonatype.utils.lowercase,
      serviceProperties : this.exportServicePropertiesHelper.createDelegate(this),
      startDate : this.exportStartDateHelper.createDelegate(this),
      startTime : this.exportStartTimeHelper.createDelegate(this),
      recurringTime : this.exportRecurringTimeHelper.createDelegate(this),
      recurringDay : this.exportMonthlyRecurringDayHelper .createDelegate(this)
    },
    advanced : {
      serviceSchedule : Sonatype.utils.lowercase,
      serviceProperties : this.exportServicePropertiesHelper.createDelegate(this)
    }
  };
  
  //A record to hold the name and id of a repository
  this.repositoryRecordConstructor = Ext.data.Record.create([
    {name:'id'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
  ]);
  
  //A record to hold the name and id of a repository group
  this.repositoryGroupRecordConstructor = Ext.data.Record.create([
    {name:'id'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
  ]);
  
  //Simply a record to hold details of each service type 
  this.serviceTypeRecordConstructor = Ext.data.Record.create([
    {name:'id', sortType:Ext.data.SortTypes.asUCString},
    {name:'name'},
    {name:'properties'}
  ]);
    
  //A record that holds the data for each service in the system
  this.scheduleRecordConstructor = Ext.data.Record.create([
    {name:'resourceURI'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
    {name:'serviceTypeName'},
    {name:'serviceTypeId'},
    {name:'serviceStatus'},
    {name:'serviceSchedule'},
    {name:'nextRunTime'},
    {name:'lastRunTime'},
    {name:'lastRunResult'}
  ]);
  
  //Reader and datastore that queries the server for the list of repositories
  this.repositoryReader = new Ext.data.JsonReader({root: 'data', id: 'id'}, this.repositoryRecordConstructor );  
  this.repositoryDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.repositories,
    reader: this.repositoryReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true
  });
  
  //Reader and datastore that queries the server for the list of repository groups
  this.repositoryGroupReader = new Ext.data.JsonReader({root: 'data', id: 'id'}, this.repositoryGroupRecordConstructor );  
  this.repositoryGroupDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.groups,
    reader: this.repositoryGroupReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true
  });
  
  //Reader and datastore that queries the server for the list of service types
  this.serviceTypeReader = new Ext.data.JsonReader({root: 'data', id: 'id'}, this.serviceTypeRecordConstructor );  
  this.serviceTypeDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.scheduleTypes,
    reader: this.serviceTypeReader,
    sortInfo: {field: 'id', direction: 'ASC'},
    autoLoad: true
  });
  
  //Reader and datastore that queries the server for the list of currently defined services
  this.schedulesReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.scheduleRecordConstructor );
  this.schedulesDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.schedules,
    reader: this.schedulesReader,
    sortInfo: {field: 'name', direction: 'ASC'},
    autoLoad: true
  });
  
  //This is the dynamically generated array that contains the items that will be shown as properties for the different
  //service types, there will be 1 item in this array for each serviceType retrieved from the serviceTypeDataStore
  this.serviceTypePanelItems = [];
  
  //Populate the dynamic content
  this.populateServiceTypePanelItems();
  
  //Build the form
  this.formConfig = {};
  this.formConfig.schedule = {
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
        store: this.serviceTypeDataStore,
        displayField:'name',
        valueField:'id',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false,
        width: 200
      },
      {
        xtype: 'panel',
        id: 'service-type-config-card-panel',
        header: false,
        layout: 'card',
        region: 'center',
        activeItem: 0,
        bodyStyle: 'padding:15px',
        deferredRender: false,
        autoScroll: false,
        frame: false,
        items: this.serviceTypePanelItems
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
        allowBlank: false,
        width: 200  
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
    	      title: 'Schedule Settings',
    	      anchor: Sonatype.view.FIELDSET_OFFSET,
    	      collapsible: false,
    	      autoHeight:true,
    	      layoutConfig: {
	            labelSeparator: ''
	          },
	          items: [
              {
                xtype: 'label',
                text: 'Without recurrence, this service can only be run manually.'
              }
            ]
          },
          {
            xtype: 'fieldset',
    	      checkboxToggle:false,
    	      title: 'Schedule Settings',
    	      anchor: Sonatype.view.FIELDSET_OFFSET,
    	      collapsible: false,
    	      autoHeight:true,
    	      layoutConfig: {
	            labelSeparator: ''
	          },
            items: [
              {
                xtype: 'datefield',
                fieldLabel: 'Start Date',
                itemCls: 'required-field',
                helpText: ht.startDate,
                name: 'startDate',
                disabled:true,
                allowBlank:false
              },
              {
                xtype: 'timefield',
                fieldLabel: 'Start Time',
                itemCls: 'required-field',
                helpText: ht.startTime,
                name: 'startTime',
                width: 75,                
                disabled:true,
                allowBlank:false
              }
            ]
          },
          {
            xtype: 'fieldset',
    	      checkboxToggle:false,
    	      title: 'Schedule Settings',
    	      anchor: Sonatype.view.FIELDSET_OFFSET,
    	      collapsible: false,
    	      autoHeight:true,
    	      layoutConfig: {
	            labelSeparator: ''
	          },
            items: [
              {
                xtype: 'datefield',
                fieldLabel: 'Start Date',
                itemCls: 'required-field',
                helpText: ht.startDate,
                name: 'startDate',
                disabled:true,
                allowBlank:false
              },
              {
                xtype: 'timefield',
                fieldLabel: 'Start Time',
                itemCls: 'required-field',
                helpText: ht.startTime,
                name: 'startTime',
                width: 75,                
                disabled:true,
                allowBlank:false
              },
              {
                xtype: 'timefield',
                fieldLabel: 'Recurring Time',
                itemCls: 'required-field',
                helpText: ht.recurringTime,
                name: 'recurringTime',
                width: 75,
                disabled:true,
                allowBlank:false
              }
            ]
          },
          {
            xtype: 'fieldset',
    	      checkboxToggle:false,
    	      title: 'Schedule Settings',
    	      anchor: Sonatype.view.FIELDSET_OFFSET,
    	      collapsible: false,
    	      autoHeight:true,
    	      layoutConfig: {
	            labelSeparator: ''
	          },
            items: [
              {
                xtype: 'datefield',
                fieldLabel: 'Start Date',
                itemCls: 'required-field',
                helpText: ht.startDate,
                name: 'startDate',
                disabled:true,
                allowBlank:false
              },
              {
                xtype: 'timefield',
                fieldLabel: 'Start Time',
                itemCls: 'required-field',
                helpText: ht.startTime,
                name: 'startTime',
                width: 75,
                disabled:true,
                allowBlank:false
              },
              {
                xtype: 'timefield',
                fieldLabel: 'Recurring Time',
                itemCls: 'required-field',
                helpText: ht.recurringTime,
                name: 'recurringTime',
                width: 75,
                disabled:true,
                allowBlank:false
              },
              {
                xtype: 'panel',
                layout: 'column',
                autoHeight: true,
                style: 'padding: 10px 0 0 0',
                
                items: [
                  {
                    xtype: 'treepanel',
                    id: '_weekdays-tree', //note: unique ID is assinged before instantiation
                    title: 'Selected Days',
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
                    invalidText: 'One or more day is required',
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
                    id: id + '_all_weekdays_tree', //note: unique ID is assinged before instantiation
                    title: 'Available Days',
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
            xtype: 'fieldset',
    	      checkboxToggle:false,
    	      title: 'Schedule Settings',
    	      anchor: Sonatype.view.FIELDSET_OFFSET,
    	      collapsible: false,
    	      autoHeight:true,
    	      layoutConfig: {
	            labelSeparator: ''
	          },
            items: [
              {
                xtype: 'datefield',
                fieldLabel: 'Start Date',
                itemCls: 'required-field',
                helpText: ht.startDate,
                name: 'startDate',
                disabled:true,
                allowBlank:false
              },
              {
                xtype: 'timefield',
                fieldLabel: 'Start Time',
                itemCls: 'required-field',
                helpText: ht.startTime,
                name: 'startTime',
                width: 75,
                disabled:true,
                allowBlank:false
              },
              {
                xtype: 'timefield',
                fieldLabel: 'Recurring Time',
                itemCls: 'required-field',
                helpText: ht.recurringTime,
                name: 'recurringTime',
                width: 75,
                disabled:true,
                allowBlank:false
              },
              {
                xtype: 'panel',
                layout: 'column',
                items: [
                  {
                    width: 180,
                    xtype: 'label',
                    text: 'Days'
                  },
                  {
                    xtype: 'panel',
                    layout: 'column',
                    items: [
                      {
                        xtype: 'panel',
                        width: 50,
                        items: [
                          {
                            xtype: 'checkbox',
                            boxLabel: '1',
                            name: 'day1'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '8',
                            name: 'day8'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '15',
                            name: 'day15'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '22',
                            name: 'day22'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '29',
                            name: 'day29'
                          }
                        ]                    
                      },
                      {
                        xtype: 'panel',
                        width: 50,
                        items: [
                          {
                            xtype: 'checkbox',
                            boxLabel: '2',
                            name: 'day2'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '9',
                            name: 'day9'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '16',
                            name: 'day16'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '23',
                            name: 'day23'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '30',
                            name: 'day30'
                          }
                        ]                    
                      },
                      {
                        xtype: 'panel',
                        width: 50,
                        items: [
                          {
                            xtype: 'checkbox',
                            boxLabel: '3',
                            name: 'day3'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '10',
                            name: 'day10'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '17',
                            name: 'day17'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '24',
                            name: 'day24'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '31',
                            name: 'day31'
                          }
                        ]                    
                      },
                      {
                        xtype: 'panel',
                        width: 50,
                        items: [
                          {
                            xtype: 'checkbox',
                            boxLabel: '4',
                            name: 'day4'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '11',
                            name: 'day11'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '18',
                            name: 'day18'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '25',
                            name: 'day25'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: 'Last',
                            name: 'dayLast'
                          }
                        ]                    
                      },
                      {
                        xtype: 'panel',
                        width: 50,
                        items: [
                          {
                            xtype: 'checkbox',
                            boxLabel: '5',
                            name: 'day5'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '12',
                            name: 'day12'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '19',
                            name: 'day19'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '26',
                            name: 'day26'
                          }
                        ]                    
                      },
                      {
                        xtype: 'panel',
                        width: 50,
                        items: [
                          {
                            xtype: 'checkbox',
                            boxLabel: '6',
                            name: 'day6'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '13',
                            name: 'day13'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '20',
                            name: 'day20'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '27',
                            name: 'day27'
                          }
                        ]                    
                      },
                      {
                        xtype: 'panel',
                        width: 50,
                        items: [
                          {
                            xtype: 'checkbox',
                            boxLabel: '7',
                            name: 'day7'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '14',
                            name: 'day14'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '21',
                            name: 'day21'
                          },
                          {
                            xtype: 'checkbox',
                            boxLabel: '28',
                            name: 'day28'
                          }
                        ]                    
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            xtype: 'fieldset',
    		    checkboxToggle:false,
    		    title: 'Schedule Settings',
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
                  disabled:true,
                  allowBlank:false
                }
              ]
            }
          ]
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
      {header: 'Type', dataIndex: 'serviceTypeName', width:175, id: 'schedule-config-service-type-col'},
      {header: 'Status', dataIndex: 'serviceStatus', width:175, id: 'schedule-config-service-status-col'},
      {header: 'Schedule', dataIndex: 'serviceSchedule', width:175, id: 'schedule-config-service-schedule-col'},
      {header: 'Next Run', dataIndex: 'nextRunTime', width:175, id: 'schedule-config-service-next-run-col'},
      {header: 'Last Run', dataIndex: 'lastRunTime', width:175, id: 'schedule-config-service-last-run-col'},
      {header: 'Last Result', dataIndex: 'lastRunResult', width:175, id: 'schedule-config-service-last-result-col'}
    ],
    autoExpandColumn: 'schedule-config-service-last-result-col',
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
  //Populate the dynamic content, based upon the currently defined service types from the server
  populateServiceTypePanelItems : function() {
    //If the items haven't loaded yet, wait until they are.   This of course requires that their be at least
    //1 service type available at ALL times on the server.
    //TODO: could probably use better logic here
    if (this.serviceTypeDataStore.data.items.length < 1){
      return this.populateServiceTypePanelItems.defer(300, this, arguments);
    }
    //Add the default card, simply an empty formset
    this.serviceTypePanelItems[0] =
    {
      xtype:'fieldset',
      id:'emptyItem',
      checkboxToggle:false,
      title: 'Service Settings',
      anchor: Sonatype.view.FIELDSET_OFFSET,
      collapsible: false,
      autoHeight:true,
      layoutConfig: {
        labelSeparator: ''
      }
    };
    //Now add the dynamic content
    this.serviceTypeDataStore.each(function(item, i, len){
      var items = [];
      for(var j=0;j<item.data.properties.length;j++){
        var curRec = item.data.properties[j];
        //Note that each item is disabled initially, this is because the select handler for the serviceType
        //combo box handles enabling/disabling as necessary, so each inactive card isn't also included in the form
        if(curRec.type == 'string'){
          items[j] = 
          {
            xtype: 'textfield',
            fieldLabel: curRec.name,
            itemCls: curRec.required ? 'required-field' : '',
            helpText: curRec.helpText,
            name: 'serviceProperties_' + curRec.id,
            allowBlank:false,
            disabled:true,
            width: 200
          };
        }
        else if(curRec.type == 'number'){
          items[j] =
          {
            xtype: 'numberfield',
            fieldLabel: curRec.name,
            itemCls: curRec.required ? 'required-field' : '',
            helpText: curRec.helpText,
            name: 'serviceProperties_' + curRec.id,
            allowBlank:false,
            disabled:true,
            width: 200
          };
        }
        else if(curRec.type == 'date'){
          items[j] =
          {
            xtype: 'datefield',
            fieldLabel: curRec.name,
            itemCls: curRec.required ? 'required-field' : '',
            helpText: curRec.helpText,
            name: 'serviceProperties_' + curRec.id,
            allowBlank:false,
            disabled:true
          };
        }
        else if(curRec.type == 'repository'){
          items[j] =
          {
            xtype: 'combo',
            fieldLabel: curRec.name,
            itemCls: curRec.required ? 'required-field' : '',
            helpText: curRec.helpText,
            name: 'serviceProperties_' + curRec.id,
            store: this.repositoryDataStore,
            displayField:'name',
            valueField:'id',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText:'Select...',
            selectOnFocus:true,
            allowBlank: curRec.required ? false : true,
            disabled: true,
            width: 200,
            minListWidth: 200
          };
        }
        else if(curRec.type == 'repositoryGroup'){
          items[j] =
          {
            xtype: 'combo',
            fieldLabel: curRec.name,
            itemCls: curRec.required ? 'required-field' : '',
            helpText: curRec.helpText,
            name: 'serviceProperties_' + curRec.id,
            store: this.repositoryGroupDataStore,
            displayField:'name',
            valueField:'id',
            editable: false,
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            emptyText:'Select...',
            selectOnFocus:true,
            allowBlank: curRec.required ? false : true,
            disabled: true,
            width: 200,
            minListWidth: 200
          };
        }
      }  

      this.serviceTypePanelItems[i + 1] =
      {
        xtype:'fieldset',
        id:item.data.id,
        checkboxToggle:false,
        title: 'Service Settings',
        anchor: Sonatype.view.FIELDSET_OFFSET,
        collapsible: false,
        autoHeight:true,
        layoutConfig: {
          labelSeparator: ''
        },
        items:items
      };
    }, this);
  },
  //Dump the currently stored data and requery for everything
  reloadAll : function(){
    this.schedulesDataStore.reload();
    this.repositoryDataStore.reload();
    this.repositoryGroupDataStore.reload();
    this.serviceTypeDataStore.reload();
    this.formCards.items.each(function(item, i, len){
      if(i>0){this.remove(item, true);}
    }, this.formCards);
    
    this.formCards.getLayout().setActiveItem(0);
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
  },
  
  saveHandler : function(formInfoObj){
    if (formInfoObj.formPanel.form.isValid()) {
      var isNew = formInfoObj.isNew;
      var serviceSchedule = formInfoObj.formPanel.find('name', 'serviceSchedule')[0].getValue().toLowerCase();
      var createUri = Sonatype.config.repos.urls.schedules;
      var updateUri = (formInfoObj.resourceUri) ? formInfoObj.resourceUri : '';
      var form = formInfoObj.formPanel.form;
    
      form.doAction('sonatypeSubmit', {
        method: (isNew) ? 'POST' : 'PUT',
        url: isNew ? createUri : updateUri,
        waitMsg: isNew ? 'Creating scheduled service...' : 'Updating scheduled service configuration...',
        fpanel: formInfoObj.formPanel,
        dataModifiers: this.submitDataModFuncs[serviceSchedule],
        serviceDataObj : Sonatype.repoServer.referenceData.schedule[serviceSchedule],
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

    var config = Ext.apply({}, this.formConfig.schedule, {id:id});
    config = this.configUniqueIdHelper(id, config);
    var formPanel = new Ext.FormPanel(config);
    
    formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
    formPanel.form.on('actionfailed', this.actionFailedHandler, this);
    formPanel.on('beforerender', this.beforeFormRenderHandler, this);
    formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
    
    var serviceTypeField = formPanel.find('name', 'serviceType')[0];
    serviceTypeField.on('select', this.serviceTypeSelectHandler, formPanel);
    
    var serviceScheduleField = formPanel.find('name', 'serviceSchedule')[0];
    serviceScheduleField.on('select', this.serviceScheduleSelectHandler, formPanel);
    
    var buttonInfoObj = {
        formPanel : formPanel,
        isNew : true
      };
    
    //save button event handler
    formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
    //cancel button event handler
    formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
    
    this.loadWeekdayListHelper([], {}, formPanel);
    
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
      var config = Ext.apply({}, this.formConfig.schedule, {id:id});
      config = this.configUniqueIdHelper(id, config);
      
      formPanel = new Ext.FormPanel(config);
      formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
      formPanel.form.on('actionfailed', this.actionFailedHandler, this);
      formPanel.on('beforerender', this.beforeFormRenderHandler, this);
      formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
  
      //On load need to make sure and set the proper schedule type card as active    
      var schedulePanel = formPanel.find('id', 'schedule-config-card-panel')[0];
      if (rec.data.serviceSchedule == 'once'){
        schedulePanel.activeItem = 1;
      }
      else if (rec.data.serviceSchedule == 'daily'){
        schedulePanel.activeItem = 2;
      }
      else if (rec.data.serviceSchedule == 'weekly'){
        schedulePanel.activeItem = 3;
      }
      else if (rec.data.serviceSchedule == 'monthly'){
        schedulePanel.activeItem = 4;
      }
      else if (rec.data.serviceSchedule == 'advanced'){
        schedulePanel.activeItem = 5;
      }
      //Then enable each field in the active card (after this point, select handler takes care of all
      //enable/disable transitions
      if (schedulePanel.items.items[schedulePanel.activeItem].items){
        schedulePanel.items.items[schedulePanel.activeItem].items.each(function(item){
          item.disabled=false;
        });
      }
      
      //Need to do the same w/ service type and make sure the correct card is active.  Dynamic cards, so little more generic
      var serviceTypePanel = formPanel.find('id', 'service-type-config-card-panel')[0];
      serviceTypePanel.items.each(function(item,i,len){
        if (item.id == rec.data.serviceTypeId){
          serviceTypePanel.activeItem = i;
          item.items.each(function(item){
            item.disabled=false;
          });
        }
      });
            
      formPanel.find('name', 'serviceType')[0].on('select', this.serviceTypeSelectHandler, formPanel);
      formPanel.find('name', 'serviceSchedule')[0].on('select', this.serviceScheduleSelectHandler, formPanel);

      var buttonInfoObj = {
        formPanel : formPanel,
        isNew : false, //not a new route form, see assumption
        resourceUri : rec.data.resourceURI
      };

      formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
      formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
      
      this.loadWeekdayListHelper([], {}, formPanel);

      this.formDataLoader(formPanel, rec.data.resourceURI, this.loadDataModFuncs[rec.data.serviceSchedule]);
      
      this.formCards.add(formPanel);
    }

    //always set active
    this.formCards.getLayout().setActiveItem(formPanel);
  },
  
  serviceTypeSelectHandler : function(combo, record, index){
    var serviceTypePanel = this.find('id', 'service-type-config-card-panel')[0];
    //First disable all the items currently on screen, so they wont be validated/submitted etc
    serviceTypePanel.getLayout().activeItem.items.each(function(item){
      item.disable();
    });
    //Then find the proper card to activate (based upon id of the serviceType)
    //Then enable the fields in that card
    serviceTypePanel.items.each(function(item,i,len){
      if (item.id == record.data.id){
        serviceTypePanel.getLayout().setActiveItem(item);
        item.items.each(function(item){
          item.enable();
        });
      }
    },serviceTypePanel);
    serviceTypePanel.doLayout();
  },
  
  serviceScheduleSelectHandler : function(combo, record, index){
    var schedulePanel = this.find('id', 'schedule-config-card-panel')[0];
    //First disable all the items currently on screen, so they wont be validated/submitted etc
    schedulePanel.getLayout().activeItem.items.each(function(item){
      item.disable();
    });
    //Then find the proper card to activate (based upon the selected schedule type)
    if (record.data.value == 'Once'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(1));
    }
    else if (record.data.value == 'Daily'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(2));
    }
    else if (record.data.value == 'Weekly'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(3));
    }
    else if (record.data.value == 'Monthly'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(4));
    }
    else if (record.data.value == 'Advanced'){
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(5));
    }
    else {
      schedulePanel.getLayout().setActiveItem(schedulePanel.items.itemAt(0));
    }
    //Then enable the fields in that card
    schedulePanel.getLayout().activeItem.items.each(function(item){
      item.enable();
    });
    schedulePanel.doLayout();
  },
  
  //creates a unique config object with specific IDs on the two grid item
  configUniqueIdHelper : function(id, config){
    //@note: there has to be a better way to do this.  Depending on offsets is very error prone
    var newConfig = config;

    newConfig.items[5].items[3].items[3].items[0].id = id + '_weekdays-tree';
    newConfig.items[5].items[3].items[3].items[0].root = new Ext.tree.TreeNode({text: 'root'});
    newConfig.items[5].items[3].items[3].items[1].id = id + '_all_weekdays-tree';
    newConfig.items[5].items[3].items[3].items[1].root = new Ext.tree.TreeNode({text: 'root'});

    return newConfig;
  },

  loadWeekdayListHelper : function(arr, srcObj, fpanel){
    var selectedTree = Ext.getCmp(fpanel.id + '_weekdays-tree');
    var allTree = Ext.getCmp(fpanel.id + '_all_weekdays-tree');

    var weekday;

    for(var i=0; i<arr.length; i++){
      weekday = arr[i];
      selectedTree.root.appendChild(
        new Ext.tree.TreeNode({
          id: weekday,
          text: weekday,
          payload: weekday, //sonatype added attribute
          allowChildren: false,
          draggable: true,
          leaf: true
        })
      );
    }

    if(this.weekdaysList){
      for(var i=0; i<this.weekdaysList.length; i++){
        weekday = this.weekdaysList[i];

        if(typeof(selectedTree.getNodeById(weekday)) == 'undefined'){
          allTree.root.appendChild(
            new Ext.tree.TreeNode({
              id: weekday,
              text: weekday,
              payload: weekday, //sonatype added attribute
              allowChildren: false,
              draggable: true,
              leaf: true
            })
          );
        }
      }
    }
    else {
      //@todo: race condition or error retrieving repos list
    }
    
    return arr; //return arr, even if empty to comply with sonatypeLoad data modifier requirement
  },
  exportStartDateHelper : function(val, fpanel){
    //as there is no Date transfer in json, i pass the long representation of the date.
    var selectedStartDate = "";
    
    var startDateFields = fpanel.find('name', 'startDate');
    //Need to find the startDate that is currently enabled, as multiple cards will have a startDate field
    for(var i=0; i<startDateFields.length; i++){
      if (!startDateFields[i].disabled){
        selectedStartDate = startDateFields[i];
        break;
      }
    }
    
    return '' + selectedStartDate.getValue().getTime();
  },  
  exportStartTimeHelper : function(val, fpanel){
    var selectedStartTime = "";
    
    var startTimeFields = fpanel.find('name', 'startTime');
    //Need to find the startTime that is currently enabled, as multiple cards will have a startDate field
    for(var i=0; i<startTimeFields.length; i++){
      if (!startTimeFields[i].disabled){
        selectedStartTime = startTimeFields[i];
        break;
      }
    }
    
    //rest api is using 24 hour clock
    var hours = parseInt(selectedStartTime.getValue().substring(0, selectedStartTime.getValue().indexOf(':'))) + (selectedStartTime.getValue().indexOf('AM') == -1 ? 12 : 0);
    var minutes = selectedStartTime.getValue().substring(selectedStartTime.getValue().indexOf(':') + 1, selectedStartTime.getValue().indexOf(':') + 3);
    
    return hours + ':' + minutes;
  },  
  exportRecurringTimeHelper : function(val, fpanel){
    var selectedRecurringTime = "";
    
    var recurringTimeFields = fpanel.find('name', 'recurringTime');
    //Need to find the recurringTime that is currently enabled, as multiple cards will have a startDate field
    for(var i=0; i<recurringTimeFields.length; i++){
      if (!recurringTimeFields[i].disabled){
        selectedRecurringTime = recurringTimeFields[i];
        break;
      }
    }
    
    //rest api is using 24 hour clock
    var hours = parseInt(selectedRecurringTime.getValue().substring(0, selectedRecurringTime.getValue().indexOf(':'))) + (selectedRecurringTime.getValue().indexOf('AM') == -1 ? 12 : 0);
    var minutes = selectedRecurringTime.getValue().substring(selectedRecurringTime.getValue().indexOf(':') + 1, selectedRecurringTime.getValue().indexOf(':') + 3);
    
    return hours + ':' + minutes;
  },
  exportRecurringDayHelper : function(val, fpanel){
    var selectedTree = Ext.getCmp(fpanel.id + '_weekdays-tree');

    var outputArr = [];
    var nodes = selectedTree.root.childNodes;

    //Pretty simple here, just go through the weekdays selected and output the payload
    //which is just the name of the weekday (monday, tuesday, etc.)
    for(var i = 0; i < nodes.length; i++){
      outputArr[i] = nodes[i].attributes.payload;
    }

    return outputArr;
  },
  exportMonthlyRecurringDayHelper : function(val, fpanel){
    var outputArr = [];
    var j = 0;
    //Another pretty simple conversion, just find all selected check boxes
    //and send along the day number
    for (var i = 1; i <= 31; i++){
      if (fpanel.find('name','day' + i)[0].getValue()){
        outputArr[j++] = '' + i;
      }
    }
    //and last if necessary
    if (fpanel.find('name','dayLast')[0].getValue()){
      outputArr[j] = 'last';
    }
    return outputArr;
  },
  exportServicePropertiesHelper : function(val, fpanel){
    var outputArr = [];
    
    var servicePropertiesPanel = fpanel.find('id', 'service-type-config-card-panel')[0];
    var i = 0;
    //These are dynamic fields here, so some pretty straightforward generic logic below
    servicePropertiesPanel.getLayout().activeItem.items.each(function(item, i, len){
      var value;
      
      if (item.xtype == 'datefield'){
        //long representation is used, not actual date
        //force to a string, as that is what the current api requires
        value = '' + item.getValue().getTime();
      }
      else if (item.xtype == 'textfield'){
        value = item.getValue();
      }
      else if (item.xtype == 'numberfield'){
        //force to a string, as that is what the current api requires
        value = '' + item.getValue();
      }
      else if (item.xtype == 'combo'){
        value = item.getValue();
      }
      outputArr[i] = 
      {
        id:item.getName().substring('serviceProperties_'.length),
        value:value
      };
      //The server is currently requiring this, would definitely be nice to not need to know this in the ui
      Ext.apply(outputArr[i], {'@class':'org.sonatype.nexus.rest.model.ScheduledServicePropertyResource'});
      i++;
    }, servicePropertiesPanel.getLayout().activeItem);
    
    return outputArr;
  },
  importStartDateHelper : function(val, srcObj, fpanel){    
    var selectedStartDate = "";
    
    var startDateFields = fpanel.find('name', 'startDate');
    //Find the correct startDate field, as their will be multiples, all but 1 disabled
    for(var i=0; i<startDateFields.length; i++){
      if (!startDateFields[i].disabled){
        selectedStartDate = startDateFields[i];
        break;
      }
    }
    
    //translate the long representation back into date
    var importedDate = new Date(Number(val));
    selectedStartDate.setValue(importedDate);
    return importedDate;
  },
  importStartTimeHelper : function(val, srcObj, fpanel){
    var selectedStartTime = "";
    
    var startTimeFields = fpanel.find('name', 'startTime');
    //Find the correct startTime field, as their will be multiples, all but 1 disabled
    for(var i=0; i<startTimeFields.length; i++){
      if (!startTimeFields[i].disabled){
        selectedStartTime = startTimeFields[i];
        break;
      }
    }
    var hours = parseInt(val.substring(0, val.indexOf(':')));
    var minutes = val.substring(val.indexOf(':') + 1, val.indexOf(':') + 3);    
    var afternoon = false;
    
    //Note api uses 24 hour format, ui is currently showing 12 hour format, so conversion here
    if (hours > 12){
      hours = hours - 12;
      afternoon = true;
    }
    var importedTime = hours + ':' + minutes + (afternoon ? ' PM' : ' AM');
    selectedStartTime.setValue(importedTime);
    return importedTime;
  },
  importRecurringTimeHelper : function(val, srcObj, fpanel){
    var selectedRecurringTime = "";
    
    var recurringTimeFields = fpanel.find('name', 'recurringTime');
    //Find the correct recurringTime field, as their will be multiples, all but 1 disabled
    for(var i=0; i<recurringTimeFields.length; i++){
      if (!recurringTimeFields[i].disabled){
        selectedRecurringTime = recurringTimeFields[i];
        break;
      }
    }
    var hours = parseInt(val.substring(0, val.indexOf(':')));
    var minutes = val.substring(val.indexOf(':') + 1, val.indexOf(':') + 3);    
    var afternoon = false;
    
    //Note api uses 24 hour format, ui is currently showing 12 hour format, so conversion here
    if (hours > 12){
      hours = hours - 12;
      afternoon = true;
    }
    var importedTime = hours + ':' + minutes + (afternoon ? ' PM' : ' AM');
    selectedRecurringTime.setValue(importedTime);
    return importedTime;
  },
  importRecurringDayHelper : function(arr, srcObj, fpanel){
    var selectedTree = Ext.getCmp(fpanel.id + '_weekdays-tree');
    var allTree = Ext.getCmp(fpanel.id + '_all_weekdays-tree');

    //Iterate through the list, and add any selected items to the selected tree
    for(var i=0; i<arr.length; i++){
      arr[i] = Sonatype.utils.capitalize(arr[i]);
      selectedTree.root.appendChild(
        new Ext.tree.TreeNode({
          id: arr[i],
          text: arr[i],
          payload: arr[i],
          allowChildren: false,
          draggable: true,
          leaf: true
        })
      );
    }
    
    //If not in the selected list, add to the available list
    for(var i=0; i<this.weekdaysList.length; i++){
      if(typeof(selectedTree.getNodeById(this.weekdaysList[i])) != 'undefined'){
        allTree.root.removeChild(allTree.getNodeById(this.weekdaysList[i]));
      }
    }
    
    return arr;
  },
  importMonthlyRecurringDayHelper : function(arr, srcObj, fpanel){
    //simply look at each item, and select the proper checkbox
    for(var i=0; i<arr.length; i++){
      var checkbox = fpanel.find('name','day' + arr[i])[0];
      if (checkbox == null){
        checkbox = fpanel.find('name','dayLast')[0];
      }
      checkbox.setValue(true);
    }
    
    return arr;
  },
  importServicePropertiesHelper : function(val, srcObj, fpanel){
    //Maps the incoming json properties to the generic component
    //Uses the id of the serviceProperty item as the key, so the id _must_ be unique within a service type
    for(var i=0;i<srcObj.serviceProperties.length;i++){
      var servicePropertyItem = fpanel.find('name','serviceProperties_' + srcObj.serviceProperties[i].id)[0];
      if (servicePropertyItem != null){
        if (servicePropertyItem.xtype == 'datefield'){
          servicePropertyItem.setValue(new Date(Number(srcObj.serviceProperties[i].value)));
        }
        else if (servicePropertyItem.xtype == 'textfield'){
          servicePropertyItem.setValue(srcObj.serviceProperties[i].value);
        }
        else if (servicePropertyItem.xtype == 'numberfield'){
          servicePropertyItem.setValue(Number(srcObj.serviceProperties[i].value));
        }
        else if (servicePropertyItem.xtype == 'combo'){
          servicePropertyItem.setValue(srcObj.serviceProperties[i].value);
        }
      }
    }
    return val;
  }
});
