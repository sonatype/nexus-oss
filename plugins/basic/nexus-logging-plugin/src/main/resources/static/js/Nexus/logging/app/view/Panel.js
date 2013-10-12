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
 * Logging panel.
 *
 * @since 2.7
 */
NX.define('Nexus.logging.app.view.Panel', {
  extend: 'Ext.Panel',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
    'Nexus.logging.app.view.Loggers',
    'Nexus.logging.app.view.Log'
  ],

  title: 'Logging',

  border: false,

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    Ext.apply(me, {
      items: [
        {
          xtype: 'panel',
          layout: 'fit',
          html: 'Allows changing the logging configuration.'
        },
        {
          xtype: 'tabpanel',
          border: false,
          layoutOnTabChange: true,
          items: [
            { xtype: 'nx-logging-view-loggers' },
            { xtype: 'nx-logging-view-log' }
          ],
          activeItem: 0
        }
      ]
    });

    me.constructor.superclass.initComponent.apply(me, arguments);
  }

}, function () {
  Ext.reg('nx-logging-view-panel', Nexus.logging.app.view.Panel);
});