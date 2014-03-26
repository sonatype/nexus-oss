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
 * Refresh controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.Refresh', {
  extend: 'Ext.app.Controller',
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'header.Refresh'
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'refresh': {
        file: 'arrow_refresh.png',
        variants: ['x16', 'x32']
      }
    });

    me.listen({
      component: {
        'nx-header-refresh': {
          click: me.refresh
        }
      }
    });

    me.addEvents(
        /**
         * @event refresh
         * Fires when refresh should be performed.
         */
        'refresh'
    );
  },

  /**
   * @public
   * Fire refresh event.
   */
  refresh: function () {
    var me = this;

    me.fireEvent('refresh');
  }

});