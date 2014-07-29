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
 * Health Check Summary window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.healthcheck.HealthCheckSummary', {
  extend: 'Ext.window.Window',
  alias: 'widget.nx-coreui-healthcheck-summary',

  layout: 'fit',
  header: false,
  closable: false,
  autoShow: true,
  modal: false,
  constrain: true,
  resizable: false,
  mouseIsOver: true,

  initComponent: function() {
    var me = this;

    me.items = {
      xtype: 'box',
      border: false,
      autoEl: {
        tag: 'iframe',
        src: me.statusModel.get('summaryUrl')
      }
    };

    me.callParent(arguments);
  },

  listeners: {
    afterrender: function(){
      var me = this;

      me.el.hover(
          function () {
            me.mouseIsOver = true;
          },
          function () {
            me.mouseIsOver = false;
            if (me.closeOnMouseOut) {
              me.closeOnMouseOut = false;
              me.close();
            }
          },
          me
      );

      me.task = new Ext.util.DelayedTask(me.doAutoClose, me);
      me.task.delay(1000);
    }
  },

  doAutoClose: function () {
    var me = this;

    if (!me.mouseIsOver) {
      // Close immediately
      me.close();
    } else {
      // Delayed closing when mouse leaves the component.
      me.closeOnMouseOut = true;
    }
  }

});
