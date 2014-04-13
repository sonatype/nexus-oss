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
 * A {@link Ext.grid.column.Column} definition class which renders a passed timestamp according to a configured
 * {@link #format}.
 *
 * @since 3.0
 */
Ext.define('NX.ext.grid.column.Timestamp', {
  extend: 'Ext.grid.column.Column',
  alias: ['widget.nx-timestampcolumn'],
  requires: [
    'Ext.Date',
    'NX.util.DateFormat'
  ],

  /**
   * @cfg {String} [format=NX.util.DateFormat.forName('datetime')['long']]
   * A formatting string as used by {@link Ext.Date#format} to format a Date for this Column.
   *
   * Defaults to NX.util.DateFormat.forName('datetime')['long'].
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

  /**
   * Renders timestamp using {@link NX.util.DateFormat.timestamp} and configured {@link #format}.
   */
  defaultRenderer: function (value) {
    var me = this;
    return NX.util.DateFormat.timestamp(value, me.format);
  }

});