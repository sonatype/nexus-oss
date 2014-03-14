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
Ext.define('NX.coreui.view.capability.CapabilitySummary', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-capability-summary',

  title: 'Summary',

  items: [
    {
      layout: 'column',
      items: [
        {
          xtype: 'nx-info',
          columnWidth: 1
        },
        {
          html: 'Status',
          width: 50
        }
      ]
    },
    {
      xtype: 'form',
      items: {
        xtype: 'fieldset',
        title: 'Notes',
        autoScroll: true,
        collapsed: false,
        hideLabels: true,
        items: {
          xtype: 'textarea',
          helpText: "Optional notes about configured capability",
          name: 'notes',
          allowBlank: true,
          anchor: '100%'
        }
      },
      buttonAlign: 'left',
      buttons: [
        { text: 'Save', action: 'save', ui: 'primary' },
        { text: 'Discard',
          handler: function () {
            var form = this.up('form');
            form.loadRecord(form.getRecord());
          }
        }
      ]
    }
  ],

  showInfo: function (info) {
    this.down('nx-info').showInfo(info);
  }

});
