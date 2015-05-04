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
 * User grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.user.UserList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-user-list',
  requires: [
    'NX.I18n'
  ],

  config: {
    stateful: true,
    stateId: 'nx-coreui-user-list'
  },

  store: 'User',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function() {
        return 'user-default';
      }
    },
    { header: NX.I18n.get('ADMIN_USERS_LIST_ID_COLUMN'), dataIndex: 'userId', stateId: 'userId', flex: 2 },
    { header: NX.I18n.get('ADMIN_USERS_LIST_REALM_COLUMN'), dataIndex: 'realm', stateId: 'realm' },
    { header: NX.I18n.get('ADMIN_USERS_LIST_FIRST_COLUMN'), dataIndex: 'firstName', stateId: 'firstName', flex: 2 },
    { header: NX.I18n.get('ADMIN_USERS_LIST_LAST_COLUMN'), dataIndex: 'lastName', stateId: 'lastName', flex: 2 },
    { header: NX.I18n.get('ADMIN_USERS_LIST_EMAIL_COLUMN'), dataIndex: 'email', stateId: 'email', flex: 2 },
    { header: NX.I18n.get('ADMIN_USERS_LIST_STATUS_COLUMN'), dataIndex: 'status', stateId: 'status' }
  ],

  viewConfig: {
    emptyText: NX.I18n.get('ADMIN_USERS_LIST_EMPTY_STATE'),
    deferEmptyText: false
  },

  dockedItems: [{
    xtype: 'toolbar',
    dock: 'top',
    cls: 'nx-actions nx-borderless',
    items: [
      {
        xtype: 'button',
        text: NX.I18n.get('ADMIN_USERS_LIST_NEW_BUTTON'),
        glyph: 'xf055@FontAwesome' /* fa-plus-circle */,
        action: 'new',
        disabled: true
      },
      '-',
      { xtype: 'label', text: NX.I18n.get('ADMIN_USERS_LIST_SOURCE') },
      { xtype: 'button', text: NX.I18n.get('ADMIN_USERS_LIST_DEFAULT_BUTTON'), action: 'filter', menu: [] },
      { xtype: 'nx-coreui-user-searchbox' }
    ]
  }]

});
