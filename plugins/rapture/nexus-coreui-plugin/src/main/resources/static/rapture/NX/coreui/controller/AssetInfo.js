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
 * Asset info controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.AssetInfo', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.I18n'
  ],

  views: [
    'component.AssetInfo'
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.listen({
      component: {
        'nx-coreui-component-assetcontainer': {
          updated: me.onUpdated
        }
      }
    });
  },

  /**
   * @private
   * Shows information about selected asset.
   */
  onUpdated: function(container, componentModel, assetModel) {
    var me = this,
        panel = container.down('nx-coreui-component-assetinfo'),
        info = {};

    if (!panel) {
      panel = container.add({ xtype: 'nx-coreui-component-assetinfo', weight: 10 });
    }

    info[NX.I18n.get('BROWSE_ASSET_INFO_NAME')] = assetModel.get('name');
    info[NX.I18n.get('BROWSE_ASSET_INFO_CONTENT_TYPE')] = assetModel.get('contentType');

    panel.showInfo(info);
  },

  /**
   * @private
   */
  toSizeString: function(v) {
    if (typeof v !== 'number') {
      return NX.I18n.get('BROWSE_STANDARD_INFO_SIZE_UNKNOWN');
    }
    if (v < 0) {
      return NX.I18n.get('BROWSE_STANDARD_INFO_SIZE_UNKNOWN');
    }
    if (v < 1024) {
      return NX.I18n.format('BROWSE_STANDARD_INFO_SIZE_BYTES', v);
    }
    if (v < 1048576) {
      return NX.I18n.format('BROWSE_STANDARD_INFO_SIZE_KB', (v / 1024).toFixed(2));
    }
    if (v < 1073741824) {
      return NX.I18n.format('BROWSE_STANDARD_INFO_SIZE_MB', (v / 1048576).toFixed(2));
    }
    return NX.I18n.format('BROWSE_STANDARD_INFO_SIZE_GB', (v / 1073741824).toFixed(2));
  }

});
