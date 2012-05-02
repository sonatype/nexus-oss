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
Sonatype.repoServer.userProfilePanel = function(config) {

  var config = config || {};
  var defaultConfig = {
    autoScroll : true,
    minWidth : 270,
    layout : 'absolute'
  };
  Ext.apply(this, config, defaultConfig);

  var views = [];

  Sonatype.Events.fireEvent('userProfileInit', views);

  this.content = new Sonatype.repoServer.userProfilePanel.contentClass({
    cls : 'user-profile-dynamic-content',
    border : false,
    x : 20,
    y : 20,
    anchor : '-20 -20'
  });

  this.selector = new Ext.form.ComboBox({
    id: 'user-profile-selector',
    x : 30,
    y : 11,
    editable : false,
    triggerAction : 'all',
    listeners : {
      'select' : {
        fn : function(combo, record, index) {
          this.content.display(record.get('value'));
        },
        scope : this
      },
      'render' : {
        fn : function(combo) {
          var rec = combo.store.getAt(0);
          combo.setValue(rec.get('text'));
          this.content.display(rec.get('value'));
        },
        scope : this
      }
    },
    store : (function() {
      // [ (v.item,v.name) for v in views]
      var viewArray = [];
      Ext.each(views, function(v) {
        viewArray.push([new v.item({username:Sonatype.user.curr.username, border : false,frame : false}), v.name])
      });
      return viewArray;
    })()
  });

  Sonatype.repoServer.userProfilePanel.superclass.constructor.call(this, {
    title : 'Profile',
    items : [
      this.content,
      this.selector
    ]
  });

}
Ext.extend(Sonatype.repoServer.userProfilePanel, Ext.Panel);

Sonatype.repoServer.userProfilePanel.contentClass = function(config)
{
  Ext.apply(this, config || {}, {
    plain : true,
    autoScroll : true,
    border : true,
    listeners : {
      'tabchange' : function() {
        // hide tabStrip
        this.header.hide();
      },
      scope : this
    }
  });

  Sonatype.repoServer.userProfilePanel.contentClass.superclass.constructor.call(this);

  this.display = function(panel)
  {
    this.add(panel);
    this.setActiveTab(panel);
  }
}
Ext.extend(Sonatype.repoServer.userProfilePanel.contentClass, Ext.TabPanel);

