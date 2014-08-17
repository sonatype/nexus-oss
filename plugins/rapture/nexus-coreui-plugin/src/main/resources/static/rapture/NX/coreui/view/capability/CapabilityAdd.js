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
 * Add capability window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.capability.CapabilityAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-capability-add',

  title: 'Create new capability',

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
          helpText: "Type of configured capability.",
          name: 'typeId',
          store: me.capabilityTypeStore,
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
          xtype: 'fieldset',
          title: 'About',
          autoHeight: true,
          autoScroll: true,
          collapsible: true,
          collapsed: false,
          border: '1 0 1 0',
          items: {
            xtype: 'nx-coreui-capability-about',
            bodyPadding: '0 0 5 0'
          }
        },
        {
          xtype: 'checkbox',
          fieldLabel: 'Enabled',
          helpText: 'This flag determines if the capability is currently enabled. To disable this capability for a period of time, de-select this checkbox.',
          name: 'enabled',
          allowBlank: false,
          checked: true,
          editable: true
        },
        {
          xtype: 'nx-coreui-formfield-settingsfieldset'
        }
      ],

      getValues: function () {
        var values = me.down('form').getForm().getFieldValues(),
            capability = {
              typeId: values.typeId,
              enabled: values.enabled,
              properties: {}
            };

        Ext.apply(capability.properties, me.down('nx-coreui-formfield-settingsfieldset').exportProperties(values));
        return capability;
      },

      markInvalid: function (errors) {
        return me.down('nx-coreui-formfield-settingsfieldset').markInvalid(errors);
      }

    };

    me.callParent(arguments);
  }

});
