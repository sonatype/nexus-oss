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
/*global Ext, NX*/

/**
 * Task Schedule Weekly field set.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.task.TaskScheduleWeekly', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-task-schedule-weekly',
  requires: [
    'NX.util.DateFormat'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
        weekDays = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'],
        days = [];

    Ext.Array.each(weekDays, function (day)
    {
      days.push({
        xtype: 'checkbox',
        name: 'recurringDay-' + (weekDays.indexOf(day) + 1),
        boxLabel: Ext.String.capitalize(day),
        recurringDayValue: weekDays.indexOf(day) + 1,
        submitValue: false
      });
    });

    me.items = [
      {
        xtype: 'datefield',
        name: 'startDate',
        itemId: 'startDate',
        fieldLabel: 'Start Date',
        helpText: 'The date this task should start running.',
        allowBlank: false,
        format: 'm/d/Y',
        value: new Date(),
        submitValue: false
      },
      {
        xtype: 'timefield',
        name: 'startTime',
        itemId: 'startTime',
        fieldLabel: 'Recurring Time',
        helpText: 'The time this task should start on days it will run in your time zone ' +
            NX.util.DateFormat.getTimeZone() + '.',
        allowBlank: false,
        format: 'H:i',
        submitValue: false
      },
      {
        xtype: 'fieldcontainer',
        fieldLabel: 'Days',
        helpText: 'Days of week when this task should run.',
        items: days
      }
    ];

    me.callParent(arguments);
  }

});