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
/*global NX, Ext, Nexus, Sonatype*/

/**
 * Loggers grid.
 *
 * @since 2.7
 */
NX.define('Nexus.logging.app.view.Log', {
  extend: 'Ext.Panel',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  xtype: 'nx-logging-view-log',

  title: 'Log',
  cls: 'nx-logging-view-log',
  layout: 'fit',

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
        icons = Nexus.logging.app.Icons;

    Ext.apply(me, {
      items: {
        xtype: 'textarea',
        readOnly: true,
        hideLabel: true,
        emptyText: 'Log not loaded',
        anchor: '100% 100%'
      },

      tbar: [
        {
          id: 'nx-logging-button-refresh-log',
          text: 'Refresh',
          tooltip: 'Refresh loggers',
          iconCls: icons.get('loggers_refresh').cls
        }
      ]
    });

    me.constructor.superclass.initComponent.apply(me, arguments);
  },

  showLog: function (text) {
    this.down('textarea')[0].setValue(text);
  }
});