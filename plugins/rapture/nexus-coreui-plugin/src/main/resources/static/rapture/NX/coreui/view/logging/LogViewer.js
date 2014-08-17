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
 * Log Viewer panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.logging.LogViewer', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-log-viewer',

  layout: 'fit',

  items: {
    xtype: 'textarea',
    cls: 'nx-coreui-log-viewer',
    readOnly: true,
    hideLabel: true,
    emptyText: 'Refresh to display log'
  },

  tbar: [
    {
      xtype: 'button',
      text: 'Download',
      tooltip: 'Download log file',
      glyph: 'xf019@FontAwesome' /* fa-download */,
      action: 'download'
    },
    '-',
    {
      xtype: 'button',
      text: 'Mark',
      tooltip: 'Add a mark in Nexus log file',
      glyph: 'xf11e@FontAwesome' /* fa-flag-checkered */,
      action: 'mark',
      disabled: true
    },
    '->',
    {
      xtype: 'combo',
      itemId: 'refreshPeriod',
      width: 200,
      editable: false,
      value: 0,
      store: [
        [0, 'Refresh manually'],
        [20, 'Refresh every 20 seconds'],
        [60, 'Refresh every minute'],
        [120, 'Refresh every 2 minutes'],
        [300, 'Refresh every 5 minutes']
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
        [25, 'Last 25KB'],
        [50, 'Last 50KB'],
        [100, 'Last 100KB']
      ],
      queryMode: 'local'
    }
  ]

});