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
 * System Information controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.SysInfo', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Permissions',
    'NX.util.Url',
    'NX.util.DownloadHelper',
    'NX.Messages',
    'NX.Windows'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'support.SysInfo'
  ],
  refs: [
    {
      ref: 'sysInfo',
      selector: 'nx-coreui-support-sysinfo'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'admin',
      path: '/Support/System Information',
      description: 'Shows system information',
      view: { xtype: 'nx-coreui-support-sysinfo' },
      iconConfig: {
        file: 'globe_place.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('nexus:atlas', 'read');
      }
    });

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.load
        }
      },
      component: {
        'nx-coreui-support-sysinfo': {
          afterrender: me.load
        },
        'nx-coreui-support-sysinfo button[action=download]': {
          'click': me.download
        },
        'nx-coreui-support-sysinfo button[action=print]': {
          'click': me.print
        }
      }
    });
  },

  /**
   * Load system information panel.
   *
   * @private
   */
  load: function () {
    var me = this,
        panel = me.getSysInfo();

    if (panel) {
      me.logDebug('Refreshing sysinfo');

      panel.getEl().mask('Loading...');
      NX.direct.atlas_SystemInformation.read(function (response) {
        panel.getEl().unmask();
        if (Ext.isObject(response) && response.success) {
          panel.setInfo(response.data);
        }
      });
    }
  },

  /**
   * @private
   * Download system information report.
   */
  download: function () {
    NX.util.DownloadHelper.downloadUrl(NX.util.Url.urlOf('service/siesta/atlas/system-information'));
  },

  /**
   * @private
   * Print system information panel contents.
   */
  print: function () {
    var me = this,
        panel = me.getSysInfo(),
        win;

    win = NX.Windows.open('', '', 'width=640,height=480');
    if (win !== null) {
      win.document.write('<html><head>');
      win.document.write('<title>System Information</title>');

      // FIXME: Ideally want some of the style in here
      // FIXME: ... but unsure how to resolve that URL (since it could change for debug, etc)
      // FIXME: See for more details http://stackoverflow.com/questions/5939456/how-to-print-extjs-component

      win.document.write('</head><body>');
      win.document.write(panel.body.dom.innerHTML);
      win.document.write('</body></html>');
      win.print();
    }
  }
});