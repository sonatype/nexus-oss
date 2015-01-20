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
 * Plugin console controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.PluginConsole', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.util.Url',
    'NX.Permissions',
    'NX.I18n'
  ],

  masters: 'nx-coreui-system-pluginlist',

  stores: [
    'PluginInfo'
  ],
  views: [
    'system.Plugins',
    'system.PluginList'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-system-plugins' },
    { ref: 'list', selector: 'nx-coreui-system-pluginlist' },
    { ref: 'info', selector: 'nx-coreui-system-plugins nx-info-panel' }
  ],

  features: {
    mode: 'admin',
    path: '/System/Plugins',
    text: NX.I18n.get('ADMIN_PLUGINS_TITLE'),
    description: NX.I18n.get('ADMIN_PLUGINS_SUBTITLE'),
    view: 'NX.coreui.view.system.Plugins',
    iconConfig: {
      file: 'plugin.png',
      variants: ['x16', 'x32']
    },
    visible: function () {
      return NX.Permissions.check('nexus:pluginconsoleplugininfos', 'read');
    }
  },

  icons: {
    'plugin-default': {
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
        info;

    if (Ext.isDefined(model)) {
      info = {};
      info[NX.I18n.get('ADMIN_PLUGINS_SUMMARY_NAME')] = model.get('name');
      info[NX.I18n.get('ADMIN_PLUGINS_SUMMARY_VERSION')] = model.get('version');
      info[NX.I18n.get('ADMIN_PLUGINS_SUMMARY_STATUS')] = model.get('status');
      //info[NX.I18n.get('ADMIN_PLUGINS_SUMMARY_DESCRIPTION') = model.get('description');
      //info[NX.I18n.get('ADMIN_PLUGINS_SUMMARY_SCM_VERSION') = model.get('scmVersion');
      //info[NX.I18n.get('ADMIN_PLUGINS_SUMMARY_SCM_TIMESTAMP') = model.get('scmTimestamp');
      //info[NX.I18n.get('ADMIN_PLUGINS_SUMMARY_SITE') = NX.util.Url.asLink(model.get('site'));
      if (Ext.isDefined(model.get('documentation'))) {
        Ext.each(model.get('documentation'), function (doc) {
          if (!Ext.isEmpty(doc.url)) {
            info[NX.I18n.get('ADMIN_PLUGINS_SUMMARY_DOCUMENTATION')] = NX.util.Url.asLink(doc.url, doc.label);
          }
        });
      }
      me.getInfo().showInfo(info);
    }
  }

});
