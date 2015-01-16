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
 * Mark log window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.logging.LogMark', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-log-mark',
  requires: [
    'NX.Conditions',
    'NX.I18n'
  ],
  ui: 'nx-inset',

  defaultFocus: 'message',

  items: {
    xtype: 'nx-settingsform',
    api: {
      submit: 'NX.direct.logging_Log.mark'
    },
    settingsFormSuccessMessage: NX.I18n.get('ADMIN_LOG_VIEWER_MARK_SUCCESS'),
    settingsFormSubmitOnEnter: true,
    editableMarker: NX.I18n.get('ADMIN_LOG_VIEWER_MARK_ERROR'),
    items: [
      {
        xtype: 'textfield',
        name: 'message',
        itemId: 'message',
        fieldLabel: NX.I18n.get('ADMIN_LOG_VIEWER_MARK_MESSAGE')
      }
    ],
    buttons: [
      { text: NX.I18n.get('ADMIN_LOG_VIEWER_MARK_BUTTON'), action: 'add', formBind: true, ui: 'nx-primary' },
      { text: NX.I18n.get('GLOBAL_DIALOG_ADD_CANCEL_BUTTON'), handler: function () {
        this.up('nx-drilldown').showChild(0, true);
      }}
    ]
  },

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.editableCondition = NX.Conditions.isPermitted('nexus:logconfig', 'update');

    me.callParent();
  }
});
