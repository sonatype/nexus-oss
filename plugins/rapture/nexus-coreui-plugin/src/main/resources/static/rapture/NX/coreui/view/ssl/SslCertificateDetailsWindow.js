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
 * Ssl Certificate detail window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ssl.SslCertificateDetailsWindow', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-sslcertificate-details-window',
  requires: [
    'NX.Conditions'
  ],

  title: 'Certificate Details',

  items: {
    xtype: 'nx-coreui-sslcertificate-details',
    buttons: [
      { text: 'Cancel',
        handler: function () {
          this.up('window').close();
        }
      }
    ]
  },

  initComponent: function () {
    var me = this,
        form;

    me.callParent(arguments);

    Ext.override(me.down('form'), {
      loadRecord: function (model) {
        var me = this,
            tbar = me.getDockedItems('toolbar[dock="bottom"]')[0],
            button;

        if (model) {
          if (model.get('inNexusSSLTrustStore')) {
            tbar.insert(0, {
              text: 'Remove Certificate from TrustStore',
              action: 'remove',
              formBind: true,
              disabled: true,
              ui: 'primary',
              glyph: 'xf056@FontAwesome' /* fa-minus-circle */
            });
            button = tbar.down('button[action=remove]');
            me.mon(
                NX.Conditions.isPermitted('nexus:ssl:truststore', 'delete'),
                {
                  satisfied: button.enable,
                  unsatisfied: button.disable,
                  scope: button
                }
            );
          }
          else {
            tbar.insert(0, {
              text: 'Add Certificate to TrustStore',
              action: 'add',
              formBind: true,
              disabled: true,
              ui: 'primary',
              glyph: 'xf055@FontAwesome' /* fa-plus-circle */
            });
            button = tbar.down('button[action=add]');
            me.mon(
                NX.Conditions.isPermitted('nexus:ssl:truststore', 'create'),
                {
                  satisfied: button.enable,
                  unsatisfied: button.disable,
                  scope: button
                }
            );
          }
        }
        me.callParent(arguments);
      }
    });
  }



});
