/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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
    'NX.I18n'
  ],

  tooltip: NX.I18n.get('GLOBAL_HEADER_HELP_TOOLTIP'),
  glyph: 'xf059@FontAwesome', // fa-question-circle

  // hide the menu button arrow
  arrowCls: '',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.menu = [
      {
        // text and iconCls is dynamic
        tooltip: 'Help and documentation for the currently selected feature',
        action: 'feature'
      },
      '-',
      {
        text: NX.I18n.get('GLOBAL_HEADER_HELP_ABOUT'),
        iconCls: 'nx-icon-nexus-x16',
        tooltip: 'About Sonatype Nexus',
        action: 'about'
      },
      {
        text: NX.I18n.get('GLOBAL_HEADER_HELP_DOCUMENTATION'),
        iconCls: 'nx-icon-help-manual-x16',
        tooltip: 'Sonatype Nexus product documentation',
        action: 'docs'
      },
      {
        text: NX.I18n.get('GLOBAL_HEADER_HELP_KB'),
        iconCls: 'nx-icon-help-kb-x16',
        tooltip: 'Sonatype Nexus knowledge base',
        action: 'kb'
      },
      {
        text: NX.I18n.get('GLOBAL_HEADER_HELP_COMMUNITY'),
        iconCls: 'nx-icon-help-community-x16',
        tooltip: 'Sonatype Nexus community information',
        action: 'community'
      },
      {
        text: NX.I18n.get('GLOBAL_HEADER_HELP_ISSUES'),
        iconCls: 'nx-icon-help-issues-x16',
        tooltip: 'Sonatype Nexus issue and bug tracker',
        action: 'issues'
      },
      '-',
      {
        text: NX.I18n.get('GLOBAL_HEADER_HELP_SUPPORT'),
        iconCls: 'nx-icon-help-support-x16',
        tooltip: 'Sonatype Nexus product support',
        action: 'support'
      }
    ];

    me.callParent();
  }

});
