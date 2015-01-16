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
 * Add capability window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.capability.CapabilityAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-capability-add',
  requires: [
    'NX.I18n'
  ],
  ui: 'nx-inset',

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
          fieldLabel: NX.I18n.get('ADMIN_CAPABILITIES_CREATE_TYPE'),
          itemCls: 'required-field',
          helpText: NX.I18n.get('ADMIN_CAPABILITIES_CREATE_TYPE_HELP'),
          name: 'typeId',
          store: me.capabilityTypeStore,
          displayField: 'name',
          valueField: 'id',
          forceSelection: true,
          editable: false,
          mode: 'local',
          triggerAction: 'all',
          emptyText: NX.I18n.get('ADMIN_CAPABILITIES_CREATE_TYPE_PLACEHOLDER'),
          selectOnFocus: false,
          allowBlank: false
        },
        {
          xtype: 'fieldset',
          title: NX.I18n.get('ADMIN_CAPABILITIES_CREATE_ABOUT'),
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
          fieldLabel: NX.I18n.get('ADMIN_CAPABILITIES_CREATE_ENABLED'),
          name: 'enabled',
          allowBlank: false,
          checked: true,
          editable: true
        },
        {
          xtype: 'nx-coreui-formfield-settingsfieldset'
        }
      ],

      buttons: [
        { text: NX.I18n.get('ADMIN_CAPABILITIES_LIST_NEW_BUTTON'), action: 'add', formBind: true, ui: 'nx-primary' },
        { text: NX.I18n.get('GLOBAL_DIALOG_ADD_CANCEL_BUTTON'), handler: function () {
          this.up('nx-drilldown').showChild(0, true);
        }}
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
