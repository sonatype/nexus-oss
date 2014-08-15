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
 * Help button.
 *
 * @since 3.0
 */
Ext.define('NX.view.header.Help', {
  extend: 'Ext.button.Button',
  alias: 'widget.nx-header-help',
  requires: [
    'NX.State'
  ],

  tooltip: 'Help',
  glyph: 'xf059@FontAwesome', // fa-question-circle

  // hide the menu button arrow
  arrowCls: '',

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
        edition = NX.State.getValue('status')['edition'].toLowerCase(),
        linkBase = 'http://links.sonatype.com/products/nexus/',
        issuesUrl = linkBase + edition + '/issues',
        manualUrl = linkBase + edition + '/docs',
        supportUrl = linkBase + edition + '/support';

    me.menu = [
      { text: 'Feature', action: 'feature' },
      '-',
      {
        text: 'About',
        iconCls: 'nx-icon-nexus-x16',
        action: 'about'
      },
      {
        text: 'Documentation',
        iconCls: 'nx-icon-help-manual-x16',
        href: manualUrl,
        hrefTarget: manualUrl
      },
      {
        text: 'Support',
        iconCls: 'nx-icon-help-support-x16',
        href: supportUrl,
        hrefTarget: supportUrl
      },
      {
        text: 'Issue Tracker',
        iconCls: 'nx-icon-help-issuetracker-x16',
        href: issuesUrl,
        hrefTarget: issuesUrl
      }
    ];

    me.callParent();
  }

});
