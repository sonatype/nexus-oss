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
/*global NX, Ext, Nexus*/

/**
 * Capability Settings FieldSet.
 *
 * @since 2.7
 */
Ext.define('NX.coreui.view.capability.CapabilitySettingsFieldSet', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-capability-settingsfieldset',

  requires: [
    'NX.coreui.view.capability.factory.CapabilityCheckboxFactory',
    'NX.coreui.view.capability.factory.CapabilityComboFactory',
    'NX.coreui.view.capability.factory.CapabilityDateFieldFactory',
    'NX.coreui.view.capability.factory.CapabilityNumberFieldFactory',
    'NX.coreui.view.capability.factory.CapabilityTextAreaFactory',
    'NX.coreui.view.capability.factory.CapabilityTextFieldFactory'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    Ext.apply(me, {
      autoHeight: false,
      autoScroll: true,
      collapsed: false,
      items: []
    });

    me.callParent(arguments);
  },

  /**
   * @property
   */
  capabilityType: undefined,

  /**
   * Renders fields for a capability type.
   * @param capabilityTypeModel capability type to rendered
   */
  setCapabilityType: function (capabilityTypeModel) {
    var me = this,
        item;

    me.capabilityType = capabilityTypeModel;

    me.removeAll();

    if (me.capabilityType) {
      me.add({
        xtype: 'checkbox',
        fieldLabel: 'Enabled',
        helpText: 'This flag determines if the capability is currently enabled. To disable this capability for a period of time, de-select this checkbox.',
        name: 'enabled',
        allowBlank: false,
        checked: true,
        editable: true
      });

      if (me.capabilityType.get('formFields')) {
        Ext.each(me.capabilityType.get('formFields'), function (formField) {
          var factory = Ext.ClassManager.getByAlias('nx.capability.factory.' + formField.type);
          if (!factory) {
            factory = Ext.ClassManager.getByAlias('nx.capability.factory.string');
          }
          if (factory) {
            item = Ext.apply(factory.create(formField), {
              requiresPermission: true,
              name: 'property.' + formField.id,
              factory: factory
            });
            if (item.xtype === 'combo' && item.store) {
              item.store.on('load', function () {
                if (item.store) {
                  item.setValue(item.getValue());
                }
              }, me, {single: true});
            }
            me.add(item);
          }
        });
      }
    }
  },

  /**
   * Exports form as a capability.
   * @returns {Object} capability
   */
  exportCapability: function () {
    var me = this,
        form = me.up('form').getForm(),
        values = form.getFieldValues(),
        value,
        capability = {
          id: values.id,
          notes: values.notes,
          typeId: me.capabilityType.get('id'),
          enabled: values.enabled,
          properties: {}
        };

    if (me.capabilityType && me.capabilityType.get('formFields')) {
      Ext.each(me.capabilityType.get('formFields'), function (formField) {
        value = values['property.' + formField.id];
        if (Ext.isDefined(value)) {
          capability.properties[formField.id] = String(value);
        }
      });
    }

    return capability;
  },

  /**
   * Imports capability.
   * @param {NX.model.Capability} capabilityModel to import
   * @param {NX.model.CapabilityType} capabilityTypeModel
   */
  importCapability: function (capabilityModel, capabilityTypeModel) {
    var me = this,
        form = me.up('form').getForm(),
        data = Ext.apply({}, { enabled: capabilityModel.get('enabled') });

    me.setCapabilityType(capabilityTypeModel);

    if (me.capabilityType && me.capabilityType.get('formFields')) {
      Ext.each(me.capabilityType.get('formFields'), function (formField) {
        data['property.' + formField.id] = '';
      });
    }

    if (capabilityModel.get('properties')) {
      Ext.Object.each(capabilityModel.get('properties'), function (key, value) {
        data['property.' + key] = value;
      });
    }

    form.setValues(data);
  },

  /**
   * Mark fields in this form invalid in bulk.
   * @param {Object/Object[]/Ext.data.Errors} errors
   * Either an array in the form `[{id:'fieldId', msg:'The message'}, ...]`,
   * an object hash of `{id: msg, id2: msg2}`, or a {@link Ext.data.Errors} object.
   */
  markInvalid: function (errors) {
    var me = this,
        form = me.up('form').getForm(),
        remainingMessages = [],
        key, marked, field;

    if (Ext.isDefined(errors)) {
      for (key in errors) {
        if (errors.hasOwnProperty(key)) {
          marked = false;
          if (form) {
            field = form.findField('property.' + key);
            if (!field) {
              field = form.findField(key);
            }
            if (field) {
              marked = true;
              field.markInvalid(errors[key]);
            }
          }
          if (!marked) {
            remainingMessages.push(errors[key]);
          }
        }
      }
    }

    if (remainingMessages.length > 0) {
      NX.Messages.add({ text: remainingMessages.join('\n'), type: 'warning' });
    }
  }

});