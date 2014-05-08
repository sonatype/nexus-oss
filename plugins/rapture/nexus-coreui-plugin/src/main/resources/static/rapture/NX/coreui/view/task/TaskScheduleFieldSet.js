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
 * Task Schedule FieldSet.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.task.TaskScheduleFieldSet', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-task-schedulefieldset',

  autoHeight: false,
  autoScroll: true,
  collapsed: false,
  defaults: {
    allowBlank: false
  },
  items: [
    {
      xtype: 'combo',
      name: 'schedule',
      itemId: 'schedule',
      fieldLabel: 'Recurrence',
      helpText: 'The frequency this task will run.  Manual - this task can only be run manually. Once - run the task once at the specified date/time. Daily - run the task every day at the specified time. Weekly - run the task every week on the specified day at the specified time. Monthly - run the task every month on the specified day(s) and time. Advanced - run the task using the supplied cron string.',
      emptyText: 'Select...',
      editable: false,
      store: [
        ['manual', 'Manual'],
        ['once', 'Once'],
        ['hourly', 'Hourly'],
        ['daily', 'Daily'],
        ['weekly', 'Weekly'],
        ['monthly', 'Monthly'],
        ['advanced', 'Advanced']
      ],
      queryMode: 'local',
      listeners: {
        change: function (combo, newValue, oldValue) {
          var oldCmp;

          if (oldValue) {
            oldCmp = combo.ownerCt.down('nx-coreui-task-schedule-' + oldValue);
            if (oldCmp) {
              combo.ownerCt.remove(oldCmp);
            }
          }
          combo.ownerCt.add({ xtype: 'nx-coreui-task-schedule-' + newValue });
          combo.up('form').isValid();
        }
      }
    }
  ],

  /**
   * Exports recurring days.
   * @returns {Array} recurring
   */
  getRecurringDays: function () {
    var me = this,
        days = me.query('checkbox[recurringDayValue]'),
        recurringDays = [];

    Ext.Array.each(days, function (day) {
      if (day.value) {
        recurringDays.push(day.recurringDayValue);
      }
    });

    return recurringDays;
  },

  /**
   * Returns start timestamp out of start date/time.
   * @returns {Number} timestamp
   */
  getStartTimestamp: function () {
    var me = this,
        startDate = me.down('#startDate'),
        startTime = me.down('#startTime'),
        timestamp;

    if (startDate && startTime) {
      startDate = startDate.getValue();
      startTime = startTime.getValue();
      if (startDate && startTime) {
        timestamp = startDate.getTime() + (startTime.getHours() * 60 + startTime.getMinutes()) * 60 * 1000;
      }
    }
    return timestamp;
  }

});