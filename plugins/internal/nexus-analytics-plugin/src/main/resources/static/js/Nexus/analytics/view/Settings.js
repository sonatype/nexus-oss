/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
 * Settings panel.
 *
 * @since 2.8
 */
NX.define('Nexus.analytics.view.Settings', {
  extend: 'Ext.Panel',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
    'Nexus.analytics.Icons'
  ],

  xtype: 'nx-analytics-view-settings',
  title: 'Settings',
  id: 'nx-analytics-view-settings',
  cls: 'nx-analytics-view-settings',

  border: false,
  //layout: 'fit',

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
        icons = Nexus.analytics.Icons;

    Ext.apply(me, {
      items: [
        {
          xtype: 'form',
          border: false,
          style: {
            padding: '10px'
          },
          defaults: {
            hideLabel: true,
            inputValue: 'true' // use 'true' instead of 'on'
          },
          items: [
            {
              xtype: 'checkbox',
              name: 'collection',
              boxLabel: 'Enable analytics event collection'
            },
            {
              xtype: 'checkbox',
              name: 'autosubmit',
              boxLabel: 'Enable automatic analytics event submission to Sonatype'
            }
          ],

          buttonAlign: 'left',
          buttons: [
            {
              xtype: 'button',
              text: 'Save',
              id: 'nx-analytics-view-settings-button-save',
              formBind: true
            }
          ]
        }
      ]
    });

    me.constructor.superclass.initComponent.apply(me, arguments);
  },

  /**
   * Get form values.
   *
   * @public
   */
  getValues: function () {
    return this.down('form').getForm().getValues();
  },

  /**
   * Set form values.
   *
   * @public
   */
  setValues: function(values) {
    this.down('form').getForm().setValues(values);
  }
});