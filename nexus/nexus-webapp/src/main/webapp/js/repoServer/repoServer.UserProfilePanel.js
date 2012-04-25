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
    layout : 'absolute'
  };
  Ext.apply(this, config, defaultConfig);

  var summaryPanel = {
    name : 'Summary',
    item : Sonatype.repoServer.UserProfile
  };

  var views = [
    summaryPanel
  ];

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
    listeners : {
      'select' : {
        fn : function(combo, record, index) {
          var cls = record.get('value');
          // FIXME chache this and reuse instances
          this.content.display(new cls({username:Sonatype.user.curr.username}));
        },
        scope : this
      },
      'show' : {
        fn : function(combo) {
          combo.select(0);
          this.content.display(new summaryPanel({username:Sonatype.user.curr.username}));
        },
        scope : this
      }
    },
    store : (function() {
      // [ (v.item,v.name) for v in views]
      var viewArray = [];
      Ext.each(views, function(v) {
        viewArray.push([v.item, v.name])
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
    layout : 'fit'
  });
  Sonatype.repoServer.userProfilePanel.contentClass.superclass.constructor.call(this);
  this.display = function(panel)
  {
    var cmp = this.getComponent(0);
    if ( panel === cmp ) {
      return;
    }
    this.remove(cmp);
    this.add(panel);
    this.doLayout();
  }
}
Ext.extend(Sonatype.repoServer.userProfilePanel.contentClass, Ext.Panel);

