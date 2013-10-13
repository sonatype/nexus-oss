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
NX.define('Nexus.logging.app.view.Loggers', {
  extend: 'Ext.grid.EditorGridPanel',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
    'Nexus.logging.app.Icons',
    'Nexus.logging.app.store.Logger',
    'Nexus.logging.app.view.LoggerLevel'
  ],

  xtype: 'nx-logging-view-loggers',

  title: 'Loggers',

  stripeRows: true,
  border: false,
  autoHeight: true,
  clicksToEdit: 1,

  viewConfig: {
    emptyText: "No loggers defined.",
    deferEmptyText: false
  },

  loadMask: {
    msg: 'Loading...',
    msgCls: 'loading-indicator'
  },

  columns: [
    {
      id: 'name',
      header: 'Name',
      dataIndex: 'name',
      sortable: true
    },
    {
      id: 'level',
      header: 'Level',
      dataIndex: 'level',
      sortable: true,
      width: 80,
      tooltip: 'Double click to edit',
      editor: {xtype: 'nx-logging-combo-logger-level'}
    }
  ],

  autoExpandColumn: 'name',

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
        icons = Nexus.logging.app.Icons;

    Ext.apply(me, {
      store: NX.create('Nexus.logging.app.store.Logger'),
      tbar: [
        {
          id: 'nx-logging-button-refresh-loggers',
          text: 'Refresh',
          tooltip: 'Refresh loggers',
          iconCls: icons.get('loggers_refresh').cls
        },
        {
          id: 'nx-logging-button-add-logger',
          text: 'Add',
          tooltip: 'Add new logger',
          iconCls: icons.get('loggers_add').cls
        },
        {
          id: 'nx-logging-button-remove-loggers',
          text: 'Remove',
          tooltip: 'Remove selected logger',
          iconCls: icons.get('loggers_remove').cls
        },
        {
          id: 'nx-logging-button-mark',
          text: 'Mark',
          tooltip: 'Add a mark in Nexus log file',
          iconCls: icons.get('loggers_mark').cls
        }
      ]
    });

    me.constructor.superclass.initComponent.apply(me, arguments);
  }
});