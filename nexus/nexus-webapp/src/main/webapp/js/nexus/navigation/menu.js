/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
Ext.namespace('Sonatype.menu');

Sonatype.menu.Menu = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  Sonatype.menu.Menu.superclass.constructor.call(this);
};

Ext.extend(Sonatype.menu.Menu, Ext.menu.Menu, {
  add : function(c) {
    if (c == null)
      return null;

    var arr = null;
    var a = arguments;
    if (a.length > 1)
    {
      arr = a;
    }
    else if (Ext.isArray(c))
    {
      arr = c;
    }
    if (arr != null)
    {
      for (var i = 0; i < arr.length; i++)
      {
        this.add(arr[i]);
      }
      return;
    }

    var item = Sonatype.menu.Menu.superclass.add.call(this, c);
    var param = c.payload ? c.payload : this.payload;
    if (c.handler && param)
    {
      // create a delegate to pass the payload object to the handler
      item.setHandler(c.handler.createDelegate(c.scope ? c.scope : this.scope, [param], 0));
    }
    return item;
  }
});
