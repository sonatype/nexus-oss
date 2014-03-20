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
 * Add capability window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.capability.CapabilityAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-capability-add',

  title: 'Create new capability',

  initComponent: function () {
    var me = this;

    me.items = {
      xtype: 'nx-settingsform',
      items: [
        {
          xtype: 'fieldset',
          autoHeight: true,
          collapsed: false,
          border: false,
          items: {
            xtype: 'combo',
            fieldLabel: 'Type',
            itemCls: 'required-field',
            helpText: "Type of configured capability",
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
            allowBlank: false,
            anchor: '96%'
          }
        },
        {
          xtype: 'fieldset',
          title: 'About',
          autoHeight: true,
          autoScroll: true,
          collapsible: true,
          collapsed: false,
          items: {
            xtype: 'nx-coreui-capability-about',
            title: undefined
          }
        },
        {
          xtype: 'nx-coreui-capability-settingsfieldset',
          title: 'Settings'
        }
      ],

      getValues: function () {
        return me.down('nx-coreui-capability-settingsfieldset').exportCapability(this.getForm())
      },

      markInvalid: function (errors) {
        return me.down('nx-coreui-capability-settingsfieldset').markInvalid(this.getForm(), errors)
      }

    };

    me.callParent(arguments);
  }

});
