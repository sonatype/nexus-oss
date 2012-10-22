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
Ext.namespace('Sonatype.navigation');

Sonatype.navigation.NavigationPanel = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  this.delayedItems = {};

  Sonatype.navigation.NavigationPanel.superclass.constructor.call(this, {
    cls : 'st-server-panel',
    autoScroll : true,
    border : false,
    items : []
  });
};

Ext.extend(Sonatype.navigation.NavigationPanel, Ext.Panel, {
  insert : function(sectionIndex, container) {
    if (sectionIndex == null || sectionIndex == undefined)
      sectionIndex = 0;
    if (container == null)
      return;

    // check if this is an attempt to add a navigation item to an existing
    // section
    if (container.sectionId)
    {
      var panel = this.findById(container.sectionId);
      if (panel)
      {
        return panel.insert(sectionIndex, container);
      }
      else
      {
        if (this.delayedItems[container.sectionId] == null)
        {
          this.delayedItems[container.sectionId] = [];
        }
        this.delayedItems[container.sectionId].push(container);
        return null;
      }
    }

    var panel = new Sonatype.navigation.Section(container);
    panel = Sonatype.navigation.NavigationPanel.superclass.insert.call(this, sectionIndex, panel);
    if (panel.id && this.delayedItems[panel.id])
    {
      panel.add(this.delayedItems[panel.id]);
      this.delayedItems[panel.id] = null;
    }
    return panel;
  },
  add : function(c) {
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

    if (c == null)
      return;

    // check if this is an attempt to add a navigation item to an existing
    // section
    if (c.sectionId)
    {
      var panel = this.findById(c.sectionId);
      if (panel)
      {
        panel.add(c);
        panel.sort();
      }
      else
      {
        if (this.delayedItems[c.sectionId] == null)
        {
          this.delayedItems[c.sectionId] = [];
        }
        this.delayedItems[c.sectionId].push(c);
        return null;
      }
    }

    var panel = new Sonatype.navigation.Section(c);
    panel = Sonatype.navigation.NavigationPanel.superclass.add.call(this, panel);
    if (panel.id && this.delayedItems[panel.id])
    {
      panel.add(this.delayedItems[panel.id]);
      this.delayedItems[panel.id] = null;
      panel.sort();
    }
    return panel;
  }
});
