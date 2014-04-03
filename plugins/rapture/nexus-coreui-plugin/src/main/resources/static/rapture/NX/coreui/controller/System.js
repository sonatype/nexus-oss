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
 * System settings related controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.System', {
  extend: 'Ext.app.Controller',

  views: [
    'system.General',
    'system.Http',
    'system.Notifications'
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature([
      {
        mode: 'admin',
        path: '/System',
        group: true,
        iconConfig: {
          file: 'cog.png',
          variants: ['x16', 'x32']
        },
        weight: 1000
      },
      {
        mode: 'admin',
        path: '/System/General',
        view: 'NX.coreui.view.system.General',
        iconConfig: {
          file: 'wrench.png',
          variants: ['x16', 'x32']
        },
        visible: function () {
          return NX.Permissions.check('nexus:settings', 'read');
        }
      },
      {
        mode: 'admin',
        path: '/System/HTTP',
        view: 'NX.coreui.view.system.Http',
        iconConfig: {
          file: 'transmit.png',
          variants: ['x16', 'x32']
        },
        visible: function () {
          return NX.Permissions.check('nexus:settings', 'read');
        }
      },
      {
        mode: 'admin',
        path: '/System/Notifications',
        view: 'NX.coreui.view.system.Notifications',
        iconConfig: {
          file: 'emails.png',
          variants: ['x16', 'x32']
        },
        visible: function () {
          return NX.Permissions.check('nexus:settings', 'read');
        }
      },
    ]);
  }
});