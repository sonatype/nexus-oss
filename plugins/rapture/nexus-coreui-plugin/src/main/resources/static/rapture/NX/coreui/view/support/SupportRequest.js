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
 * Support Request panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.support.SupportRequest', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-support-supportrequest',

  items: {
    xtype: 'nx-settingsform',
    items: [
      {
        xtype: 'label',
        html: '<div>' +
            '<p>Submit a support request to Sonatype.</p>' +
            '<p>Please include a complete description of your problem and steps to allow us to reproduce the problem (if available).</p>' +
            '<p>Attaching a <strong>Support ZIP</strong> to your request will help our engineers give you a faster response.</p>' +
            '</div>'
      }
    ],

    buttonAlign: 'left',

    buttons: [
      {
        text: 'Submit Request',
        glyph: 'xf08e@FontAwesome' /* fa-external-link */,
        action: 'makerequest',
        ui: 'primary',
        tooltip: 'Submit a support request in the Sonatype support portal'
      }
    ]
  }

});
