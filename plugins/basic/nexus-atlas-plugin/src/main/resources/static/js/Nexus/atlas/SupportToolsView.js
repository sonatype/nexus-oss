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
 * Support-tools view.
 *
 * @since 2.7
 */
NX.define('Nexus.atlas.SupportToolsView', {
  extend: 'Ext.Container',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
      'Nexus.atlas.SystemInformationPanel',
      'Nexus.atlas.SupportZipPanel'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var self = this;

    Ext.apply(self, {
      cls: 'nx-atlas-SupportToolsView',
      layout: 'border',
      items: [
        {
          region: 'north',
          html: 'Support tools provides a collection of modules to help keep your Nexus instance healthy.'
        },
        {
          xtype: 'tabpanel',
          region: 'center',
          frame: false,
          border: false,

          items: [
            NX.create('Nexus.atlas.SystemInformationPanel'),
            NX.create('Nexus.atlas.SupportZipPanel')
          ],
          activeTab: 0
        }
      ]
    });

    self.constructor.superclass.initComponent.apply(self, arguments);
  }

}, function () {
  var type = this,
      sp = Sonatype.lib.Permissions;

  NX.log.debug('Adding global view: ' + type.$className);

  // install panel into main NX navigation
  Sonatype.Events.on('nexusNavigationInit', function (panel) {
    panel.add({
      enabled: sp.checkPermission('nexus:atlas', sp.READ),

      sectionId: 'st-nexus-config',
      title: 'Support Tools',
      tabId: 'support-tools',
      tabCode: type
    });

    NX.log.debug('Registered global view: ' + type.$className);
  });
});