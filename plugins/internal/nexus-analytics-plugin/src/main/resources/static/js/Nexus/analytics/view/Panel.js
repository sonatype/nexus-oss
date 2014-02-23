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
 * Analytics panel.
 *
 * @since 2.8
 */
NX.define('Nexus.analytics.view.Panel', {
  extend: 'Ext.Panel',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
    'Nexus.analytics.Icons',
    'Nexus.analytics.view.Settings',
    'Nexus.analytics.view.Events'
  ],

  xtype: 'nx-analytics-view-panel',
  title: 'Analytics',
  cls: 'nx-analytics-view-panel',

  border: false,
  layout: {
    type: 'vbox',
    align: 'stretch'
  },

  /**
   * @override
   */
  initComponent: function() {
    var me = this,
        icons = Nexus.analytics.Icons;

    Ext.apply(me, {
      items: [
        {
          xtype: 'panel',
          cls: 'nx-analytics-view-panel-description',
          border: false,
          html: icons.get('analytics').variant('x32').img +
              '<div>Analytics helps Sonatype make Nexus better by capturing key anonymous usage details and metrics.</div>',
          height: 60,
          flex: 0
        },
        {
          xtype: 'tabpanel',
          flex: 1,
          border: false,
          plain: true,
          layoutOnTabChange: true,
          items: [
            { xtype: 'nx-analytics-view-settings' },
            { xtype: 'nx-analytics-view-events' }
          ],
          activeTab: 0,

          listeners: {
            afterrender: function(tabpanel) {
              // default to have the events tab hidden
              var tab = me.down('nx-analytics-view-events');
              tabpanel.hideTabStripItem(tab);
            }
          }
        }
      ]
    });

    me.constructor.superclass.initComponent.apply(me, arguments);
  }
});