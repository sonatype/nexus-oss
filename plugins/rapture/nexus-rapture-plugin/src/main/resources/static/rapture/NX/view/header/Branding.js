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
 * Banding header panel.
 * TODO: implement
 *
 * @since 3.0
 */
Ext.define('NX.view.header.Branding', {
  extend: 'Ext.container.Container',
  alias: 'widget.nx-header-branding',

  // HACK: Adding some bogus style here to show branding panel

  style: {
    backgroundColor: '#3f5c9a'
  },
  padding: 15,

  layout: {
    type: 'hbox',
    align: 'stretch',
    pack: 'start'
  },

  items: [
    {
      xtype: 'label',
      text: 'MegaCorp Repositories',
      flex: 1,
      style: {
        'color': '#FFFFFF',
        'font-size': '30px',
        'font-variant': 'small-caps'
      }
    },
    {
      xtype: 'label',
      html: 'Big is for babies.<br/>Go MEGA or go home!',
      style: {
        'color': '#CCCCCC',
        'font-size': '15px'
      }
    }
  ]
});
