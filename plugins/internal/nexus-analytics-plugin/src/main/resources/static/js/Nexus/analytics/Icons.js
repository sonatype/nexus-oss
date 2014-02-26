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
/*global NX, Ext, Nexus*/

/**
 * Container for icons used by analytics plugin.
 *
 * @since 2.8
 */
NX.define('Nexus.analytics.Icons', {
  extend: 'Nexus.util.IconContainer',
  singleton: true,

  /**
   * @constructor
   */
  constructor: function () {
    var me = this;

    // helper to build an icon config with variants, where variants live in directories, foo.png x16 -> x16/foo.png
    function iconConfig(fileName, variants) {
      var config = {};
      if (variants === undefined) {
        variants = ['x32', 'x16'];
      }
      Ext.each(variants, function (variant) {
        config[variant] = variant + '/' + fileName;
      });
      return config;
    }

    me.constructor.superclass.constructor.call(me, {
      stylePrefix: 'nx-analytics-icon-',

      icons: {
        arrow_refresh: 'arrow_refresh.png',
        action_log: iconConfig('action_log.png'),
        _delete: iconConfig('delete.png'), // avoid invalid property id
        download: iconConfig('download.png'),
        external: iconConfig('external.png'),
        transmit: iconConfig('transmit.png'),
        file_extension_zip: iconConfig('file_extension_zip.png'),

        refresh: '@arrow_refresh',
        analytics: '@action_log',
        clear: '@_delete',
        _export: '@download', // avoid invalid property id
        submit: '@external',

        type_REST: '@transmit',
        zip: '@file_extension_zip'
      }
    });
  },

  /**
   * Return icon for given type.
   */
  forType: function(name) {
    // TODO: return generic icon for unknown type
    return this.get('type_' + name);
  }
});