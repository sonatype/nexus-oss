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
 * Add task window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.task.TaskAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-task-add',
  requires: [
    'NX.Conditions',
    'NX.I18n'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = {
      xtype: 'nx-settingsform',
      api: {
        submit: 'NX.direct.coreui_Task.create'
      },
      settingsFormSuccessMessage: function (data) {
        return NX.I18n.get('ADMIN_TASKS_CREATE_SUCCESS') + data['name'] + ' (' + data['typeName'] + ')';
      },
      editableCondition: NX.Conditions.isPermitted('nexus:tasks', 'create'),
      editableMarker: NX.I18n.get('ADMIN_TASKS_CREATE_ERROR'),
      buttons: [
        { text: NX.I18n.get('ADMIN_TASKS_LIST_NEW_BUTTON'), action: 'add', formBind: true, ui: 'nx-primary' },
        { text: NX.I18n.get('GLOBAL_DIALOG_ADD_CANCEL_BUTTON'), handler: function () {
          this.up('nx-drilldown').showChild(0, true);
        }}
      ],
      items: {
        xtype: 'tabpanel',
        ui: 'nx-light',
        items: [
          {
            xtype: 'panel',
            title: 'Settings',
            ui: 'nx-inset',
            defaults: {
              xtype: 'textfield',
              allowBlank: false
            },
            items: [
              {
                xtype: 'combo',
                fieldLabel: NX.I18n.get('ADMIN_TASKS_CREATE_TYPE'),
                itemCls: 'required-field',
                name: 'typeId',
                store: me.taskTypeStore,
                displayField: 'name',
                valueField: 'id',
                forceSelection: true,
                editable: false,
                mode: 'local',
                triggerAction: 'all',
                emptyText: NX.I18n.get('ADMIN_TASKS_CREATE_TYPE_PLACEHOLDER'),
                selectOnFocus: false
              },
              {
                xtype: 'checkbox',
                fieldLabel: NX.I18n.get('ADMIN_TASKS_CREATE_ENABLED'),
                name: 'enabled',
                checked: true,
                editable: true
              },
              {
                name: 'name',
                fieldLabel: NX.I18n.get('ADMIN_TASKS_CREATE_NAME')
              },
              {
                xtype: 'nx-email',
                name: 'alertEmail',
                fieldLabel: NX.I18n.get('ADMIN_TASKS_CREATE_EMAIL'),
                allowBlank: true
              },
              {
                xtype: 'nx-coreui-formfield-settingsfieldset'
              }
            ]
          },
          {
            xtype: 'panel',
            title: NX.I18n.get('ADMIN_TASKS_CREATE_SCHEDULE'),
            ui: 'nx-inset',
            items: { xtype: 'nx-coreui-task-schedulefieldset' }
          }
        ]
      }
    };

    me.callParent(arguments);

    Ext.override(me.down('form').getForm(), {
      /**
       * @override
       * Additionally, gets value of properties, start timestamp and recurring days checkboxes (if any).
       */
      getValues: function () {
        var values = this.callParent(arguments);

        values.properties = me.down('nx-coreui-formfield-settingsfieldset').exportProperties(values);
        values.recurringDays = me.down('nx-coreui-task-schedulefieldset').getRecurringDays();
        values.startDate = me.down('nx-coreui-task-schedulefieldset').getStartDate();
        return values;
      },

      /**
       * @override
       * Additionally, marks invalid properties.
       */
      markInvalid: function (errors) {
        this.callParent(arguments);
        me.down('nx-coreui-formfield-settingsfieldset').markInvalid(errors);
      }
    });
  }
});
