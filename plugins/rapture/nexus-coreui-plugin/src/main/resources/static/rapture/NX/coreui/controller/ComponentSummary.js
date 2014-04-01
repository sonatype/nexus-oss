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
 * Component details summary controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.ComponentSummary', {
  extend: 'Ext.app.Controller',

  views: [
    'component.ComponentSummary'
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'component-summary': {
        file: 'information.png',
        variants: ['x16', 'x32']
      }
    });

    me.listen({
      component: {
        'nx-coreui-component-detail': {
          componentavailable: me.onComponentAvailable,
          componentunavailable: me.onComponentUnavailable
        }
      }
    });
  },

  onComponentAvailable: function (detailPanel, componentRef) {
    var panel = detailPanel.down('nx-coreui-component-summary');

    if (!panel) {
      panel = detailPanel.add({ xtype: 'nx-coreui-component-summary' });
    }

    panel.down('#title').setText('Summary of ' + componentRef.uri);
  },

  onComponentUnavailable: function (detailPanel) {
    var panel = detailPanel.down('nx-coreui-component-summary');

    if (panel) {
      detailPanel.remove(panel);
    }
  }

});