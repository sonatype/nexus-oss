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
 * Outreach controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Outreach', {
  extend: 'Ext.app.Controller',

  refs: [
    { ref: 'welcomePage', selector: 'nx-dashboard-welcome' }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.refreshOutreachContent
        },
        '#State': {
          userchanged: me.refreshOutreachContent
        }
      },
      component: {
        'nx-dashboard-welcome': {
          afterrender: me.refreshOutreachContent
        }
      }
    });
  },

  /**
   * @private
   * Add/Remove outreach content to/from welcome page, if outreach content is available.
   */
  refreshOutreachContent: function () {
    var me = this,
        welcomePage = me.getWelcomePage(),
        outreachContent;

    if (welcomePage) {
      outreachContent = welcomePage.down('#outreach');
      if (outreachContent) {
        welcomePage.remove(outreachContent);
      }
      NX.direct.outreach_Outreach.readStatus(function (response) {
        if (Ext.isObject(response) && response.success) {
          welcomePage.add({
            xtype: 'box',
            itemId: 'outreach',
            anchor: '100%',
            height: 700,
            border: false,
            frame: false,
            autoEl: {
              tag: 'iframe',
              src: NX.util.Url.urlOf('service/outreach/')
            }
          });
        }
      });
    }
  }

});