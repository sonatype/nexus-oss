/*
 * Copyright (c) 2008-2013 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/**
 * Authenticate current user button.
 */
NX.define('Nexus.wonderland.AuthenticateButton', {
  extend: 'Ext.Button',

  requires: [
    'Nexus.wonderland.Icons'
  ],

  xtype: 'nx-wonderland-authbutton',

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
        icons = Nexus.wonderland.Icons;

    Ext.apply(me, {
      cls: 'x-btn-text-icon',
      iconCls: icons.get('lock').cls
    });

    // TODO: Sort out how we can intercept the click event, deal with authwindow, then fire event back upstream?
    // TODO: Perhaps have to add a new event?

    me.constructor.superclass.initComponent.apply(me, arguments);
  }
});