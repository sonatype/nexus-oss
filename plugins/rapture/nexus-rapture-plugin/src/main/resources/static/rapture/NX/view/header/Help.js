/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
 * Help button.
 *
 * @since 3.0
 */
Ext.define('NX.view.header.Help', {
  extend: 'Ext.button.Button',
  alias: 'widget.nx-header-help',

  tooltip: 'Help',
  glyph: 'xf059@FontAwesome', // fa-question-circle

  // hide the menu button arrow
  arrowCls: '',

  menu: [
    { text: 'Feature', action: 'feature' },
    '-',
    {
      text: 'About Sonatype Nexus '
          + NX.State.getValue('status')['edition'] + ' ' + NX.State.getValue('status')['version'],
      iconCls: 'nx-icon-nexus-x16',
      action: 'about'
    },
    { text: 'Product Manual', iconCls: 'nx-icon-help-manual-x16', action: 'manual' },
    { text: 'Product Support', iconCls: 'nx-icon-help-support-x16', action: 'support' }
  ]
});
