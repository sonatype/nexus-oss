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

/*global define*/
define(['extjs', 'nexus', 'sonatype'], function(Ext, Nexus, Sonatype) {
// Extend message box, so that we can get ids on the buttons for testing
Nexus.MessageBox = (function() {
  var O, F = function() {};
  F.prototype = Ext.MessageBox;
  O = function() {};
  O.prototype = new F();
  O.superclass = F.prototype;

  Ext.override(O, (function() {
        return {
          show : function(options) {
            O.superclass.show.call(this, options);
            this.getDialog().getEl().select('button').each(function(el) {
                  el.dom.id = el.dom.innerHTML;
                });
          }
        };
      }()));
  return new O();
}());

  // legacy
  Sonatype.MessageBox = Nexus.MessageBox;

  return Nexus.MessageBox;
});


