/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * Bundles controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Bundles', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.view.info.Panel',
    'NX.view.info.Entry',
    'NX.util.Url',
    'NX.Permissions',
    'NX.I18n'
  ],

  masters: 'nx-coreui-system-bundlelist',

  stores: [
    'Bundle'
  ],
  views: [
    'system.Bundles',
    'system.BundleList'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-system-bundles' },
    { ref: 'list', selector: 'nx-coreui-system-bundlelist' },
    { ref: 'info', selector: 'nx-coreui-system-bundles nx-info-panel' }
  ],

  features: {
    mode: 'admin',
    path: '/System/Bundles',
    text: NX.I18n.get('ADMIN_BUNDLES_TITLE'),
    description: NX.I18n.get('ADMIN_BUNDLES_SUBTITLE'),
    view: 'NX.coreui.view.system.Bundles',
    iconConfig: {
      file: 'plugin.png',
      variants: ['x16', 'x32']
    },
    visible: function () {
      return NX.Permissions.check('nexus:bundles:read');
    }
  },

  icons: {
    'bundle-default': {
      file: 'plugin.png',
      variants: ['x16', 'x32']
    }
  },

  /**
   * @override
   */
  getDescription: function (model) {
    return model.get('name');
  },

  onSelection: function (list, model) {
    var me = this,
        info,
        headers;

    // TODO: Resolve better presentation

    if (Ext.isDefined(model)) {
      info = {};
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_ID')] = model.get('id');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_NAME')] = model.get('name');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_SYMBOLIC_NAME')] = model.get('symbolicName');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_VERSION')] = model.get('version');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_STATE')] = model.get('state');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_LOCATION')] = model.get('location');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_START_LEVEL')] = model.get('startLevel');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_LAST_MODIFIED')] = model.get('lastModified');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_FRAGMENT')] = model.get('fragment');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_FRAGMENTS')] = model.get('fragments');
      info[NX.I18n.get('ADMIN_BUNDLES_SUMMARY_FRAGMENT_HOSTS')] = model.get('fragmentHosts');

      headers = model.get('headers');
      if (headers) {
        Ext.iterate(headers, function (key, value) {
          info[NX.I18n.format('ADMIN_BUNDLES_SUMMARY_HEADER', key)] = value;
        });
      }

      me.getInfo().showInfo(info);
    }
  }
});
