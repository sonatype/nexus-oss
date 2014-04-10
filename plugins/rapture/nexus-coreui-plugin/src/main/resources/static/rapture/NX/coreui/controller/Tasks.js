/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Tasks controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Tasks', {
  extend: 'NX.controller.MasterDetail',

  list: 'nx-coreui-task-list',

  stores: [
    'Task',
    'TaskType'
  ],
  views: [
    'task.TaskFeature',
    'task.TaskList'
  ],
  refs: [
    {
      ref: 'list',
      selector: 'nx-coreui-task-list'
    },
    {
      ref: 'info',
      selector: 'nx-coreui-task-feature nx-info-panel'
    }
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
    view: { xtype: 'nx-coreui-task-feature' },
    iconConfig: {
      file: 'time.png',
      variants: ['x16', 'x32']
    },
    visible: function () {
      return NX.Permissions.check('nexus:tasks', 'read');
    }
  },
  permission: 'nexus:tasks',

  init: function () {
    var me = this;

    me.callParent();

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadTaskType
        }
      },
      component: {
        'nx-coreui-task-list': {
          beforerender: me.loadTaskType
        },
        'nx-coreui-task-list button[action=run]': {
          click: me.runTask,
          afterrender: me.bindRunButton
        },
        'nx-coreui-task-list button[action=stop]': {
          click: me.stopTask,
          afterrender: me.bindStopButton
        }
      },
      store: {
        '#TaskType': {
          load: me.onTaskTypeLoad,
          datachanged: me.onTaskTypeLoad
        }
      }
    });
  },

  getDescription: function (model) {
    return model.get('name') + ' (' + model.get('typeName') + ')';
  },

  onSelection: function (list, model) {
    var me = this;

    if (Ext.isDefined(model)) {
      me.getInfo().showInfo({
        'Id': model.get('id'),
        'Name': model.get('name'),
        'Type': model.get('typeName'),
        'Status': model.get('statusDescription'),
        'Next Run': NX.util.DateFormat.timestamp(model.get('nextRun')),
        'Last Run': NX.util.DateFormat.timestamp(model.get('lastRun')),
        'Last Result': model.get('lastRunResult')
      });
    }
  },

  loadTaskType: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.getTaskTypeStore().load();
    }
  },

  onTaskTypeLoad: function () {
    var me = this;
    me.reselect();
  },

  /**
   * @override
   * @protected
   * Enable 'New' when user has 'create' permission and there is at least one task type.
   */
  bindNewButton: function (button) {
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
  bindRunButton: function (button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:tasksrun', 'read'),
            NX.Conditions.gridHasSelection('nx-coreui-task-list', function (model) {
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
  bindStopButton: function (button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:tasksrun', 'delete'),
            NX.Conditions.gridHasSelection('nx-coreui-task-list', function (model) {
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
  deleteModel: function (model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.coreui_Task.delete(model.getId(), function (response) {
      me.loadStore();
      if (Ext.isDefined(response) && response.success) {
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
  runTask: function () {
    var me = this,
        model = me.selectedModel(),
        description;

    if (model) {
      description = me.getDescription(model);
      NX.Dialogs.askConfirmation('Confirm?', 'Run ' + description + ' task?', function () {
        NX.direct.coreui_Task.run(model.getId(), function (response) {
          me.loadStore();
          if (Ext.isDefined(response) && response.success) {
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
  stopTask: function () {
    var me = this,
        model = me.selectedModel(),
        description;

    if (model) {
      description = me.getDescription(model);
      NX.Dialogs.askConfirmation('Confirm?', 'Stop ' + description + ' task?', function () {
        NX.direct.coreui_Task.stop(model.getId(), function (response) {
          me.loadStore();
          if (Ext.isDefined(response) && response.success) {
            NX.Messages.add({
              text: 'Task stopped: ' + description, type: 'success'
            });
          }
        });
      }, { scope: me });
    }
  }

});