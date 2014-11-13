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
 * Font styles.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.styles.Fonts', {
  extend: 'NX.view.dev.styles.StyleSection',
  requires: [
    'Ext.XTemplate'
  ],

  title: 'Fonts',
  layout: {
    type: 'hbox',
    defaultMargins: {top: 0, right: 4, bottom: 0, left: 0}
  },

  /**
   * @protected
   */
  initComponent: function () {
    var me = this;

    var template = Ext.create('Ext.XTemplate',
        '<div>',
        '<span class="section-header">{text}</span>',
        '<p class="{clz}">',
        'Trusted applications at the speed of deployment<br/>',
        'abcdefghijklmnopqrstuvwxyz<br/>',
        'ABCDEFGHIJKLMNOPQRSTUVWXYZ<br/>',
        ',1234567890?¿¡;.:*@#£$%&/()=[]+',
        '</p>',
        '</div>'
    );

    function font(name, clz) {
      return me.html(template.apply({
          text: name,
          clz: clz
        })
      );
    }

    me.items = [
      font('Proxima Nova Regular', 'proxima-nova-regular'),
      font('Proxima Nova Bold', 'proxima-nova-bold'),
      font('Courier New', 'courier-new-regular')
    ];

    me.callParent();
  }
});