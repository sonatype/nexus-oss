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
 * Loggers grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.logging.LoggerList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-logger-list',
  requires: [
    'NX.I18n'
  ],

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
    { header: NX.I18n.get('ADMIN_LOGGING_NAME_COLUMN'), dataIndex: 'name', flex: 1 },
    {
      header: NX.I18n.get('ADMIN_LOGGING_LEVEL_COLUMN'),
      dataIndex: 'level',
      editor: {
        xtype: 'combo',
        editable: false,
        store: [
          ['TRACE', NX.I18n.get('ADMIN_LOGGING_TRACE_ITEM')],
          ['DEBUG', NX.I18n.get('ADMIN_LOGGING_DEBUG_ITEM')],
          ['INFO', NX.I18n.get('ADMIN_LOGGING_INFO_ITEM')],
          ['WARN', NX.I18n.get('ADMIN_LOGGING_WARN_ITEM')],
          ['ERROR', NX.I18n.get('ADMIN_LOGGING_ERROR_ITEM')],
          ['OFF', NX.I18n.get('ADMIN_LOGGING_OFF_ITEM')],
          ['DEFAULT', NX.I18n.get('ADMIN_LOGGING_DEFAULT_ITEM')]
        ],
        queryMode: 'local'
      }
    }
  ],

  viewConfig: {
    emptyText: NX.I18n.get('ADMIN_LOGGING_EMPTY_STATE'),
    deferEmptyText: false
  },

  tbar: [
    {
      xtype: 'button',
      text: NX.I18n.get('ADMIN_LOGGING_NEW_BUTTON'),
      glyph: 'xf055@FontAwesome' /* fa-plus-circle */,
      action: 'new',
      disabled: true
    },
    {
      xtype: 'button',
      text: NX.I18n.get('ADMIN_LOGGING_DELETE_BUTTON'),
      glyph: 'xf056@FontAwesome' /* fa-minus-circle */,
      action: 'delete',
      disabled: true
    },
    '-',
    {
      xtype: 'button',
      text: NX.I18n.get('ADMIN_LOGGING_RESET_BUTTON'),
      glyph: 'xf0e2@FontAwesome' /* fa-undo */,
      action: 'reset',
      disabled: true
    }
  ],

  plugins: [
    { pluginId: 'editor', ptype: 'rowediting', clicksToEdit: 1, errorSummary: false },
    { ptype: 'gridfilterbox', emptyText: NX.I18n.get('ADMIN_LOGGING_FILTER_ERROR') }
  ]

});
