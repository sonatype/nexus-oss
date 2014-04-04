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
 * An read only **{@link Ext.form.field.Text}** that shows a timestamp as a formatted date.
 *
 * @since 3.0
 */
Ext.define('NX.ext.form.field.Timestamp', {
  extend: 'Ext.form.field.Text',
  alias: 'widget.nx-timestamp',
  requires: [
    'NX.util.DateFormat'
  ],

  readOnly: true,

  /**
   * @cfg {String} [format=NX.util.DateFormat.forName('datetime')['long']]
   * A formatting string as used by {@link Ext.Date#format} to format a Date.
   */

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    if (!me.format) {
      me.format = NX.util.DateFormat.forName('datetime')['long'];
    }

    me.callParent(arguments);
  },

  valueToRaw: function(value) {
    return NX.util.DateFormat.timestamp(value, this.format);
  }

});
