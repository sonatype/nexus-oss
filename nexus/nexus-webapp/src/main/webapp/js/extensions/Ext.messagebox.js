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
// Extned message box, so that we can get ids on the buttons for testing
Sonatype.MessageBox = function() {
  var F = function() {};
  F.prototype = Ext.MessageBox;
  var o = function() {};
  o.prototype = new F();
  o.superclass = F.prototype;

  Ext.override(o, function() {
        return {
          show : function(options) {
            o.superclass.show.call(this, options);
            this.getDialog().getEl().select('button').each(function(el) {
                  el.dom.id = el.dom.innerHTML;
                });
          }
        };
      }());
  return new o();
}();