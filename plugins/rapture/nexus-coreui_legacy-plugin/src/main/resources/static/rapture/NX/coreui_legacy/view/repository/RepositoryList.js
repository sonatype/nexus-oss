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
 * Repository grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui_legacy.view.repository.RepositoryList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui_legacy-repository-list',
  requires: [
    'NX.I18n'
  ],

  store: 'NX.coreui_legacy.store.Repository',

  /*
   * @override
   */
  initComponent: function() {
    var me = this;

    me.columns = {
      items: [
        {
          xtype: 'nx-iconcolumn',
          width: 36,
          iconVariant: 'x16',
          iconName: function() {
            return 'repository-default';
          }
        },
        { header: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_NAME_COLUMN'), dataIndex: 'name', flex: 2 },
        { header: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_TYPE_COLUMN'), dataIndex: 'type',
          renderer: function(value) {
            return Ext.String.capitalize(value);
          }
        },
        { header: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_FORMAT_COLUMN'), dataIndex: 'formatName' },
        { header: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_STATUS_COLUMN'), renderer: me.renderStatus, flex: 1 },
        { header: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_URL_COLUMN'), dataIndex: 'url', xtype: 'nx-linkcolumn', flex: 2 }
      ],
      defaults: {
        tdCls: 'nx-middle-align'
      }
    };

    me.callParent(arguments);
  },

  viewConfig: {
    emptyText: 'No repositories defined',
    deferEmptyText: false
  },

  dockedItems: [{
    xtype: 'toolbar',
    dock: 'top',
    cls: 'nx-actions nx-borderless',
    items: [
      {
        xtype: 'button',
        text: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_NEW_BUTTON'),
        glyph: 'xf055@FontAwesome' /* fa-plus-circle */,
        action: 'new',
        disabled: true
      }
    ],
  }],

  plugins: [
    { ptype: 'gridfilterbox', emptyText: NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_FILTER_ERROR') }
  ],

  renderStatus: function(value, metaData, model) {
    var status = (model.get('localStatus') === 'IN_SERVICE') ? NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_IN_SERVICE') : NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_OUT_SERVICE'),
        available = model.get('remoteStatus') === 'AVAILABLE',
        unknown = model.get('remoteStatus') === 'UNKNOWN',
        reason = model.get('remoteReason');

    if (reason) {
      reason = '<br/><I>' + Ext.util.Format.htmlEncode(reason) + '</I>';
    }

    if (model.get('type') === 'proxy') {
      if (model.get('proxyMode').search(/BLOCKED/) === 0) {
        status += model.get('proxyMode') ===
            'BLOCKED_AUTO' ? NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_AUTO_BLOCK') : NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_MANUAL_BLOCK');
        if (available) {
          status += ' and Available';
        }
        else {
          status += ' and Unavailable';
        }
      }
      else { // allow
        if (model.get('localStatus') === 'IN_SERVICE') {
          if (!available && unknown) {
            status += unknown ? NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_CHECK_REMOTE') : NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_PROXY');
          }
        }
        else { // Out of service
          status += available ? NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_REMOTE_AVAILABLE') : NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_LIST_REMOTE_UNAVAILABLE');
        }
      }
    }

    if (reason) {
      status += reason;
    }
    return status;
  }

});
