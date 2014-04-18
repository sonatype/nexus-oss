/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Health Check EULA window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.healthcheck.HealthCheckEula', {
  extend: 'Ext.window.Window',
  alias: 'widget.nx-coreui-healthcheck-eula',

  title: 'CLM Terms of Use',

  layout: 'fit',
  autoShow: true,
  modal: true,
  constrain: true,
  width: 640,
  height: 500,

  items: {
    xtype: 'box',
    autoEl: {
      tag: 'iframe',
      src: NX.util.Url.urlOf('/static/healthcheck-tos.html')
    }
  },

  dockedItems: [
    {
      xtype: 'toolbar',
      dock: 'bottom',
      ui: 'footer',
      items: [
        { xtype: 'button', text: 'I Agree', action: 'agree', formBind: true, ui: 'primary' },
        { xtype: 'button', text: 'I Don\'t Agree', handler: function () {
          this.up('window').close();
        }},
        '->',
        { xtype: 'component', html: '<a href="' + NX.util.Url.urlOf('/static/healthcheck-tos.html') +
            '" target="_new">Download a copy of the license.</a>'
        }
      ]
    }
  ]

});
