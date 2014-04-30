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
 * Task settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.task.TaskSettings', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-task-settings',

  items: [
    {
      xtype: 'hiddenfield',
      name: 'id'
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
    { xtype: 'nx-coreui-formfield-settingsfieldset' }
  ],

  /**
   * @override
   * Imports task into settings field set.
   * @param {NX.model.Task} model task model
   */
  loadRecord: function (model) {
    var me = this,
        taskTypeModel = NX.getApplication().getStore('TaskType').getById(model.get('typeId')),
        settingsFieldSet = me.down('nx-coreui-formfield-settingsfieldset');

    me.callParent(arguments);
    if (taskTypeModel) {
      settingsFieldSet.importProperties(model.get('properties'), taskTypeModel.get('formFields'));
    }
  },

  /**
   * @override
   * Exports task from settings field set.
   * @returns {Object} form values
   */
  getValues: function () {
    var me = this,
        values = me.getForm().getFieldValues(),
        task = {
          id: values.id,
          enabled: values.enabled,
          name: values.name,
          alertEmail: values.alertEmail,
          properties: {}
        };

    Ext.apply(task.properties, me.down('nx-coreui-formfield-settingsfieldset').exportProperties());
    return task;
  },

  /**
   * Mark fields in this form invalid in bulk.
   * @param {Object/Object[]/Ext.data.Errors} errors
   * Either an array in the form `[{id:'fieldId', msg:'The message'}, ...]`,
   * an object hash of `{id: msg, id2: msg2}`, or a {@link Ext.data.Errors} object.
   */
  markInvalid: function (errors) {
    this.down('nx-coreui-formfield-settingsfieldset').markInvalid(errors);
  }

});
