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
 * Add task window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.task.TaskAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-task-add',

  title: 'Create new task',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = {
      xtype: 'nx-settingsform',
      items: [
        {
          xtype: 'combo',
          fieldLabel: 'Type',
          itemCls: 'required-field',
          helpText: "The type of service that will be scheduled to run.",
          name: 'typeId',
          store: me.taskTypeStore,
          displayField: 'name',
          valueField: 'id',
          forceSelection: true,
          editable: false,
          mode: 'local',
          triggerAction: 'all',
          emptyText: 'Select...',
          selectOnFocus: false,
          allowBlank: false
        },
        {
          xtype: 'checkbox',
          fieldLabel: 'Enabled',
          helpText: 'This flag determines if the task is currently active. To disable this task for a period of time, de-select this checkbox.',
          name: 'enabled',
          allowBlank: false,
          checked: true,
          editable: true
        },
        {
          name: 'name',
          fieldLabel: 'Name',
          helpText: 'A name for the scheduled task.',
          emptyText: 'enter a name'
        },
        {
          xtype: 'nx-email',
          name: 'alertEmail',
          fieldLabel: 'Alert Email',
          helpText: 'The email address where an email will be sent in case that task execution will fail.',
          allowBlank: true
        },
        {
          xtype: 'nx-coreui-formfield-settingsfieldset'
        }
      ],

      getValues: function () {
        var values = me.down('form').getForm().getFieldValues(),
            task = {
              typeId: values.typeId,
              enabled: values.enabled,
              name: values.name,
              alertEmail: values.alertEmail,
              properties: {}
            };

        Ext.apply(task.properties, me.down('nx-coreui-formfield-settingsfieldset').exportProperties());
        return task;
      },

      markInvalid: function (errors) {
        return me.down('nx-coreui-formfield-settingsfieldset').markInvalid(errors);
      }

    };

    me.callParent(arguments);
  }

});
