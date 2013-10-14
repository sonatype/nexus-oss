/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/*global Ext */

Ext.applyIf(Ext.Component.prototype, {
  up: function (selector) {
    var id;

    if (selector.startsWith('#')) {
      id = selector.substring(1);
      return this.findParentBy(function(container){
        return container.id === id;
      });
    }
    return this.findParentByType(selector);
  }
});

Ext.applyIf(Ext.Container.prototype, {
  up: function (selector) {
    var id;

    if (selector.startsWith('#')) {
      id = selector.substring(1);
      return this.findParentBy(function(container){
        return container.id === id;
      });
    }
    return this.findParentByType(selector);
  },
  down: function (selector) {
    var components;

    if (selector.startsWith('#')) {
      components = this.find('id', selector.substring(1));
    }
    else {
      components = this.findByType(selector);
    }

    if (Ext.isDefined(components) && components.length > 0) {
      return components[0];
    }
  }
});
