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
 * Loggers grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.logging.LoggerList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-logger-list',

  store: 'Logger',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function () {
        return 'logger-default';
      }
    },
    { header: 'Name', dataIndex: 'name', flex: 1 },
    {
      header: 'Level',
      dataIndex: 'level',
      editor: {
        xtype: 'combo',
        editable: false,
        store: [
          ['TRACE', 'TRACE'],
          ['DEBUG', 'DEBUG'],
          ['INFO', 'INFO'],
          ['WARN', 'WARN'],
          ['ERROR', 'ERROR'],
          ['OFF', 'OFF'],
          ['DEFAULT', 'DEFAULT']
        ],
        queryMode: 'local'
      }
    }
  ],

  emptyText: 'No loggers defined',

  tbar: [
    {
      xtype: 'button',
      text: 'New',
      tooltip: 'Add new logger',
      glyph: 'xf055@FontAwesome' /* fa-plus-circle */,
      action: 'new',
      disabled: true
    },
    {
      xtype: 'button',
      text: 'Delete',
      tooltip: 'Remove selected logger',
      glyph: 'xf056@FontAwesome' /* fa-minus-circle */,
      action: 'delete',
      disabled: true
    },
    '-',
    {
      xtype: 'button',
      text: 'Reset',
      tooltip: 'Reset loggers to their default levels',
      glyph: 'xf0e2@FontAwesome' /* fa-undo */,
      action: 'reset',
      disabled: true
    }
  ],

  plugins: [
    { pluginId: 'editor', ptype: 'rowediting', clicksToEdit: 1, errorSummary: false },
    { ptype: 'gridfilterbox', emptyText: 'No logger matched criteria "$filter"' }
  ]

});