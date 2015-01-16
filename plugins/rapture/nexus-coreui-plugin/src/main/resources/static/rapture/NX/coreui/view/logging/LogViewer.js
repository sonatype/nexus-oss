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
 * Log Viewer panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.logging.LogViewer', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-log-viewer',
  requires: [
    'NX.I18n'
  ],

  layout: 'fit',

  items: {
    xtype: 'textarea',
    cls: 'nx-log-viewer-field',
    readOnly: true,
    hideLabel: true,
    emptyText: NX.I18n.get('ADMIN_LOG_VIEWER_EMPTY_STATE'),
    inputAttrTpl: 'wrap="off"'
  },

  tbar: [
    {
      xtype: 'button',
      text: NX.I18n.get('ADMIN_LOG_VIEWER_DOWNLOAD_BUTTON'),
      glyph: 'xf019@FontAwesome' /* fa-download */,
      action: 'download'
    },
    '-',
    {
      xtype: 'button',
      text: NX.I18n.get('ADMIN_LOG_VIEWER_MARK_BUTTON'),
      glyph: 'xf11e@FontAwesome' /* fa-flag-checkered */,
      action: 'mark',
      disabled: true
    },
    '->',
    {
      xtype: 'label',
      text: NX.I18n.get('ADMIN_LOG_VIEWER_REFRESH_INTERVAL')
    },
    {
      xtype: 'combo',
      itemId: 'refreshPeriod',
      width: 140,
      editable: false,
      value: 0,
      store: [
        [0, NX.I18n.get('ADMIN_LOG_VIEWER_MANUAL_ITEM')],
        [20, NX.I18n.get('ADMIN_LOG_VIEWER_20_SECONDS_ITEM')],
        [60, NX.I18n.get('ADMIN_LOG_VIEWER_MINUTE_ITEM')],
        [120, NX.I18n.get('ADMIN_LOG_VIEWER_2_MINUTES_ITEM')],
        [300, NX.I18n.get('ADMIN_LOG_VIEWER_5_MINUTES_ITEM')]
      ],
      queryMode: 'local'
    },
    {
      xtype: 'combo',
      itemId: 'refreshSize',
      width: 120,
      editable: false,
      value: 25,
      store: [
        [25, NX.I18n.get('ADMIN_LOG_VIEWER_25KB_ITEM')],
        [50, NX.I18n.get('ADMIN_LOG_VIEWER_50KB_ITEM')],
        [100, NX.I18n.get('ADMIN_LOG_VIEWER_100KB_ITEM')]
      ],
      queryMode: 'local'
    }
  ]

});
