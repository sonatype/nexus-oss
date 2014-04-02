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
/**
 * Component CLM Security Issues panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.component.ComponentSecurity', {
  extend: 'Ext.Panel',
  alias: 'widget.nx-coreui-component-security',

  buttonConfig: {
    tooltip: 'Security Issues',
    iconCls: NX.Icons.cls('component-security', 'x32')
  },

  items: [
    {
      xtype: 'panel',
      margin: 5,
      layout: 'hbox',
      style: {
        marginBottom: '10px'
      },
      items: [
        { xtype: 'component', html: NX.Icons.img('component-security', 'x16') },
        { xtype: 'label',
          itemId: 'title',
          margin: '0 0 0 5',
          style: {
            'color': '#000000',
            'font-size': '16px',
            'font-weight': 'bold',
            'text-align': 'center'
          }
        }
      ]
    }
  ]

});
