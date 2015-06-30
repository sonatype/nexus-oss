/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext*/

/**
 * Notification panel.
 *
 * @since 3.0
 */
Ext.define('NX.view.message.Panel', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-message-panel',
  requires: [
    'NX.I18n'
  ],

  border: false,
  ui: 'nx-messages',
  width: 200,
  stateful: true,
  stateId: 'nx-message-panel',

  layout: 'fit',

  tbar: [
    {
      xtype: 'button',
      text: 'Clear',
      glyph: 'xf056@FontAwesome' /* fa-minus-circle */,
      action: 'clear'
    },
    {
      xtype: 'button',
      text: 'Close',
      glyph: 'xf057@FontAwesome' /* fa-times-circle */,
      action: 'close'
    }
  ],

  items: [
    {
      xtype: 'gridpanel',
      store: 'Message',
      columns: [
        {
          xtype: 'nx-iconcolumn',
          dataIndex: 'type',
          width: 25,
          iconNamePrefix: 'message-',
          iconVariant: 'x16'
        },
        { dataIndex: 'text', flex: 1 }
      ],
      hideHeaders: true,
      viewConfig: {
        emptyText: NX.I18n.get('Message_Panel_Empty'),
        deferEmptyText: false
      }
    }
  ]
});
