/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext, NX*/

/**
 * Tasks controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Tasks', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Conditions',
    'NX.Dialogs',
    'NX.Messages',
    'NX.Permissions',
    'NX.I18n'
  ],

  masters: 'nx-coreui-task-list',

  stores: [
    'Task',
    'TaskType'
  ],
  views: [
    'task.TaskAdd',
    'task.TaskFeature',
    'task.TaskList',
    'task.TaskSchedule',
    'task.TaskScheduleFieldSet',
    'task.TaskScheduleForm',
    'task.TaskScheduleAdvanced',
    'task.TaskScheduleDaily',
    'task.TaskScheduleHourly',
    'task.TaskScheduleManual',
    'task.TaskScheduleMonthly',
    'task.TaskScheduleOnce',
    'task.TaskScheduleWeekly',
    'task.TaskSettings',
    'task.TaskSettingsForm',
    'formfield.SettingsFieldSet'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-task-feature' },
    { ref: 'list', selector: 'nx-coreui-task-list' },
    { ref: 'info', selector: 'nx-coreui-task-feature nx-info-panel' },
    { ref: 'schedule', selector: 'nx-coreui-task-schedule' },
    { ref: 'settings', selector: 'nx-coreui-task-settings' }
  ],
  icons: {
    'task-default': {
      file: 'time.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/System/Tasks',
    text: NX.I18n.get('ADMIN_TASKS_TITLE'),
    description: NX.I18n.get('ADMIN_TASKS_SUBTITLE'),
    view: { xtype: 'nx-coreui-task-feature' },
    iconConfig: {
      file: 'time.png',
      variants: ['x16', 'x32']
    },
    visible: function() {
      return NX.Permissions.check('nexus:tasks', 'read');
    }
  },
  permission: 'nexus:tasks',

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.callParent();

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.onRefresh
        }
      },
      component: {
        'nx-coreui-task-list': {
          beforerender: me.onRefresh
        },
        'nx-coreui-task-list button[action=new]': {
          click: me.showAddWindow
        },
        'nx-coreui-task-add form': {
          submitted: me.onSettingsSubmitted
        },
        'nx-coreui-task-settings-form': {
          submitted: me.onSettingsSubmitted
        },
        'nx-coreui-task-schedule-form': {
          submitted: me.onSettingsSubmitted
        },
        'nx-coreui-task-feature button[action=run]': {
          click: me.runTask,
          afterrender: me.bindRunButton
        },
        'nx-coreui-task-feature button[action=stop]': {
          click: me.stopTask,
          afterrender: me.bindStopButton
        },
        'nx-coreui-task-add combo[name=typeId]': {
          select: me.changeTaskType
        }
      }
    });
  },

  /**
   * @override
   * Returns a description of task suitable to be displayed.
   * @param {NX.coreui.model.Task} model selected model
   */
  getDescription: function(model) {
    return model.get('name') + ' (' + model.get('typeName') + ')';
  },

  /**
   * @override
   * Load task model into detail tabs.
   * @param {NX.coreui.view.task.TaskList} list task grid
   * @param {NX.coreui.model.Task} model selected model
   */
  onSelection: function(list, model) {
    var me = this,
        settings = me.getSettings(),
        schedule = me.getSchedule(),
        taskTypeModel;

    if (Ext.isDefined(model)) {
      me.showSummary(model);
      taskTypeModel = me.getTaskTypeStore().getById(model.get('typeId'));
      if (taskTypeModel) {
        if (!settings) {
          me.getFeature().addTab({ xtype: 'nx-coreui-task-settings', title: NX.I18n.get('ADMIN_TASKS_DETAILS_SETTINGS_TAB'), weight: 20 });
        }
        me.showSettings(model);
      }
      else {
        if (settings) {
          me.getFeature().removeTab(settings);
        }
      }
      if (taskTypeModel && model.get('schedule') !== 'internal') {
        if (!schedule) {
          me.getFeature().addTab({ xtype: 'nx-coreui-task-schedule', title: NX.I18n.get('ADMIN_TASKS_DETAILS_SCHEDULE_TAB'), weight: 30 });
        }
        me.showSchedule(model);
      }
      else {
        if (schedule) {
          me.getFeature().removeTab(schedule);
        }
      }
    }
  },

  /**
   * @private
   * Displays task summary.
   * @param {NX.coreui.model.Task} model task model
   */
  showSummary: function(model) {
    var me = this,
      info = {};

    info[NX.I18n.get('ADMIN_TASKS_SUMMARY_ID')] = model.getId();
    info[NX.I18n.get('ADMIN_TASKS_SUMMARY_NAME')] = model.get('name');
    info[NX.I18n.get('ADMIN_TASKS_SUMMARY_TYPE')] = model.get('typeName');
    info[NX.I18n.get('ADMIN_TASKS_SUMMARY_STATUS')] = model.get('statusDescription');
    info[NX.I18n.get('ADMIN_TASKS_SUMMARY_NEXT_RUN')] = model.get('nextRun');
    info[NX.I18n.get('ADMIN_TASKS_SUMMARY_LAST_RUN')] = model.get('lastRun');
    info[NX.I18n.get('ADMIN_TASKS_SUMMARY_LAST_RESULT')] = model.get('lastRunResult');

    me.getInfo().showInfo(info);
  },

  /**
   * @private
   * Displays task settings.
   * @param {NX.coreui.model.Task} model task model
   */
  showSettings: function(model) {
    this.getSettings().loadRecord(model);
  },

  /**
   * @private
   * Displays task schedule.
   * @param {NX.coreui.model.Task} model task model
   */
  showSchedule: function(model) {
    this.getSchedule().loadRecord(model);
  },

  /**
   * @private
   */
  showAddWindow: function() {
    var me = this,
      feature = me.getFeature();

    // Show the first panel in the create wizard, and set the breadcrumb
    feature.setItemName(1, NX.I18n.get('ADMIN_TASKS_CREATE_TITLE'));
    me.loadCreateWizard(1, true, Ext.widget({
      xtype: 'panel',
      layout: {
        type: 'vbox',
        align: 'stretch',
        pack: 'start'
      },
      items: [
        { xtype: 'nx-drilldown-actions' },
        {
          xtype: 'nx-coreui-task-add',
          taskTypeStore: this.getTaskTypeStore(),
          flex: 1
        }
      ]
    }));
  },

  /**
   * @private
   * Change settings according to selected task type (in add window).
   * @combo {Ext.form.field.ComboBox} combobox task type combobox
   */
  changeTaskType: function(combobox) {
    var form = combobox.up('nx-settingsform'),
        taskTypeModel;

    taskTypeModel = this.getTaskTypeStore().getById(combobox.value);
    form.down('nx-coreui-formfield-settingsfieldset').setFormFields(taskTypeModel.get('formFields'));
  },

  /**
   * @private
   * (Re)load task type store && reset all cached combo stores.
   */
  onRefresh: function() {
    var me = this,
        list = me.getList();

    if (list) {
      me.getTaskTypeStore().load();
    }
  },

  /**
   * @private
   */
  onSettingsSubmitted: function(form, action) {
    var me = this,
        win = form.up('nx-coreui-task-add');

    if (win) {
      me.loadStoreAndSelect(action.result.data.id, false);
    } else {
      me.loadStore(Ext.emptyFn);
    }
  },

  /**
   * @override
   * @protected
   * Enable 'New' when user has 'create' permission and there is at least one task type.
   */
  bindNewButton: function(button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:tasks', 'create'),
            NX.Conditions.storeHasRecords('TaskType')
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @override
   * @private
   * Enable 'Run' when user has 'read' permission and task is 'runnable'.
   */
  bindRunButton: function(button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:tasksrun', 'read'),
            NX.Conditions.gridHasSelection('nx-coreui-task-list', function(model) {
              return model.get('runnable');
            })
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @override
   * @private
   * Enable 'Stop' when user has 'delete' permission and task is 'stoppable'.
   */
  bindStopButton: function(button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:tasksrun', 'delete'),
            NX.Conditions.gridHasSelection('nx-coreui-task-list', function(model) {
              return model.get('stoppable');
            })
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @override
   * Delete task.
   * @param model task to be deleted
   */
  deleteModel: function(model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.coreui_Task.remove(model.getId(), function(response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Task deleted: ' + description, type: 'success'
        });
      }
    });
  },

  /**
   * @override
   * Run selected task.
   */
  runTask: function() {
    var me = this,
        bookmark = NX.Bookmarks.getBookmark(),
        model, modelId, description;

    modelId = decodeURIComponent(bookmark.getSegment(1));
    model = me.getList().getStore().getById(modelId);
    description = me.getDescription(model);

    if (model) {
      description = me.getDescription(model);
      NX.Dialogs.askConfirmation('Confirm?', 'Run ' + description + ' task?', function() {
        NX.direct.coreui_Task.run(model.getId(), function(response) {
          me.loadStore();
          if (Ext.isObject(response) && response.success) {
            NX.Messages.add({
              text: 'Task started: ' + description, type: 'success'
            });
          }
        });
      }, {scope: me});
    }
  },

  /**
   * @override
   * Stop selected task.
   */
  stopTask: function() {
    var me = this,
      bookmark = NX.Bookmarks.getBookmark(),
      model, modelId, description;

    modelId = decodeURIComponent(bookmark.getSegment(1));
    model = me.getList().getStore().getById(modelId);
    description = me.getDescription(model);

    if (model) {
      description = me.getDescription(model);
      NX.Dialogs.askConfirmation('Confirm?', 'Stop ' + description + ' task?', function() {
        NX.direct.coreui_Task.stop(model.getId(), function(response) {
          me.loadStore();
          if (Ext.isObject(response) && response.success) {
            NX.Messages.add({
              text: 'Task stopped: ' + description, type: 'success'
            });
          }
        });
      }, { scope: me });
    }
  }

});
