/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
var SEARCH_FIELD_CONFIG = {
  xtype : 'trigger',
  triggerClass : 'x-form-search-trigger',
  listeners : {
    'specialkey' : {
      fn : function(f, e) {
        if (e.getKey() == e.ENTER)
        {
          this.onTriggerClick();
        }
      }
    }
  },
  onTriggerClick : function(a, b, c) {
    var v = this.getRawValue();
    if (v.length > 0)
    {
      var panel = Sonatype.view.mainTabPanel.addOrShowTab('nexus-search', Sonatype.repoServer.SearchPanel, {
            title : 'Search'
          });
      panel.startQuickSearch(v);
      // window.location = 'index.html#nexus-search;quick~' + v;
    }
  }
};

Sonatype.Events.addListener('nexusNavigationInit', function(nexusPanel) {
      if (Sonatype.lib.Permissions.checkPermission('nexus:index', Sonatype.lib.Permissions.READ))
      {
        nexusPanel.insert(0,{
              title : 'Artifact Search',
              id : 'st-nexus-search',
              items : [Ext.apply({
                        repoPanel : this,
                        id : 'quick-search--field',
                        width : 140
                      }, SEARCH_FIELD_CONFIG), {
                    title : 'Advanced Search',
                    tabCode : Sonatype.repoServer.SearchPanel,
                    tabId : 'nexus-search',
                    tabTitle : 'Search'
                  }]
            });
      }
    });

Sonatype.Events.addListener('welcomePanelInit', function(repoServer, welcomePanelConfig) {
      if (Sonatype.lib.Permissions.checkPermission('nexus:index', Sonatype.lib.Permissions.READ))
      {
        welcomePanelConfig.items.push({
              layout : 'form',
              border : false,
              frame : false,
              labelWidth : 10,
              items : [{
                    border : false,
                    html : '<div class="little-padding">' + 'Type in the name of a project, class, or artifact into the text box ' + 'below, and click Search. Use "Advanced Search" on the left for more options.' + '</div>'
                  }, Ext.apply({
                        repoPanel : repoServer,
                        id : 'quick-search-welcome-field',
                        anchor : '-10',
                        labelSeparator : ''
                      }, SEARCH_FIELD_CONFIG)]
            });
      }
    });

Sonatype.Events.addListener('welcomeTabRender', function() {
      var c = Ext.getCmp('quick-search-welcome-field');
      if (c)
      {
        c.focus(true, 100);
      }
    });