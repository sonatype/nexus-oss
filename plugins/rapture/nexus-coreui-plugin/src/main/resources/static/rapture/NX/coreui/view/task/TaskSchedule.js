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
 * Task schedule form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.task.TaskSchedule', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-task-schedule',

  api: {
    submit: 'NX.direct.coreui_Task.updateSchedule'
  },
  settingsFormSuccessMessage: function (data) {
    return 'Task rescheduled: ' + data['name'] + ' (' + data['typeName'] + ')';
  },
  editableCondition: NX.Conditions.isPermitted('nexus:tasks', 'update'),
  editableMarker: 'You do not have permission to update tasks',

  items: [
    {
      xtype: 'hiddenfield',
      name: 'id'
    },
    { xtype: 'nx-coreui-task-schedulefieldset' }
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.callParent(arguments);

    Ext.override(me.getForm(), {
      /**
       * @override
       * Additionally, gets value of start timestamp and recurring days checkboxes (if any).
       */
      getValues: function () {
        var values = this.callParent(arguments);

        values.recurringDays = me.down('nx-coreui-task-schedulefieldset').getRecurringDays();
        values.startTimestamp = me.down('nx-coreui-task-schedulefieldset').getStartTimestamp();
        return values;
      },

      /**
       * @override
       * Additionally, sets values of recurring days checkboxes (if any).
       */
      setValues: function (values) {
        if (values['startTimestamp']) {
          values.startDate = values.startTime = new Date(values['startTimestamp']);
        }
        this.callParent(arguments);
        if (values['recurringDays']) {
          Ext.Array.each(values['recurringDays'], function (day) {
            var checkbox = me.down('checkbox[name=recurringDay-' + day + ']');
            if (checkbox) {
              checkbox.setValue(true);
            }
          });
        }
      }
    });
  }

});
