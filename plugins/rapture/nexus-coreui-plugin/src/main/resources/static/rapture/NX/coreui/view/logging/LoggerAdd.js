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
 * Add logger window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.logging.LoggerAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-logger-add',

  title: 'Save search filter',
  defaultFocus: 'name',

  items: {
    xtype: 'nx-settingsform',
    items: [
      {
        xtype: 'textfield',
        name: 'name',
        itemId: 'name',
        fieldLabel: 'Name',
        helpText: 'Logger name.',
        emptyText: 'enter a logger name'
      },
      {
        xtype: 'combo',
        name: 'level',
        fieldLabel: 'Level',
        helpText: 'Select logger level.',
        editable: false,
        value: 'INFO',
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
    ]
  }

});
