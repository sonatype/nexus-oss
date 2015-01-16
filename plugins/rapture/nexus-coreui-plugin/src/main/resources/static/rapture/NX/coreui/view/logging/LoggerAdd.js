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
 * Add logger window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.logging.LoggerAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-logger-add',
  requires: [
    'NX.I18n'
  ],
  ui: 'nx-inset',

  defaultFocus: 'name',

  items: {
    xtype: 'nx-settingsform',
    items: [
      {
        xtype: 'textfield',
        name: 'name',
        itemId: 'name',
        fieldLabel: NX.I18n.get('ADMIN_LOGGING_CREATE_NAME')
      },
      {
        xtype: 'combo',
        name: 'level',
        fieldLabel: NX.I18n.get('ADMIN_LOGGING_CREATE_LEVEL'),
        editable: false,
        value: 'INFO',
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
    ],
    buttons: [
      { text: NX.I18n.get('ADMIN_LOGGING_NEW_BUTTON'), action: 'add', formBind: true, ui: 'nx-primary' },
      { text: NX.I18n.get('GLOBAL_DIALOG_ADD_CANCEL_BUTTON'), handler: function () {
        this.up('nx-drilldown').showChild(0, true);
      }}
    ]
  }

});
