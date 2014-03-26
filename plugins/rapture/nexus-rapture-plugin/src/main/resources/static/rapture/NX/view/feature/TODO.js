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
 * Panel shown in case no feature panel was configured.
 * FIXME: remove me
 *
 * @since 3.0
 */
Ext.define('NX.view.feature.TODO', {
  extend: 'Ext.Panel',
  alias: 'widget.nx-feature-todo',

  layout: {
    type: 'vbox',
    align: 'stretch',
    pack: 'start'
  },

  items: [
    {
      xtype: 'label',
      text: 'TODO',
      style: {
        'color': '#000000',
        'font-size': '20px',
        'font-weight': 'bold',
        'text-align': 'center',
        'padding': '20px'
      }
    },
    {
      xtype: 'label',
      text: 'Something more useful to be placed here in the future',
      style: {
        'text-align': 'center'
      }
    }
  ]

});