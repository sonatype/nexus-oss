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
/*global Ext*/

/**
 * Footer panel.
 *
 * @since 3.0
 */
Ext.define('NX.view.Footer', {
  extend: 'Ext.container.Container',
  alias: 'widget.nx-footer',

  layout: 'fit',

  style: {
    backgroundColor: '#444444'
  },

  items: [
    {
      xtype: 'container',
      html: 'Sonatype Nexus&trade; ' +
          '&copy; Copyright Sonatype, Inc.',
      style: {
        'color': '#C6C6C6',
        'font-size': '8px',
        'text-align': 'right',
        'padding': '1px 2px 0px 0px'
      }
    }
  ]
});