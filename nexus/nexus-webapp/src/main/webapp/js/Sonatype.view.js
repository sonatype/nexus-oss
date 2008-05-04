/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
Sonatype.view = {
  FIELD_OFFSET_WITH_SCROLL : (3 + 16 + 3 + 18)*(-1) + '', //  (help icon margin) + (help icon) + margin + (scrollbar)
  FIELD_OFFSET : (3 + 16)*(-1) + '', //extra padding on right of icon not needed
  FIELDSET_OFFSET : (3 + 18)*(-1) + '', // (extra room between border and scrollbar) + (scrollbar)
  
  init : function(){
    var dq = Ext.DomQuery;
    
    Ext.get('login-link').on('click', Sonatype.repoServer.RepoServer.loginHandler, Sonatype.repoServer.RepoServer);
    this.updateLoginLinkText();
    
    Ext.get('header').show();
    Ext.get('welcome-tab').show();
    
    //set version in view
    Ext.get('version').update(Sonatype.utils.version);
    
    Sonatype.view.welcomeTab = new Ext.Panel({
      contentEl:'welcome-tab',
      title: 'Welcome'
    });
    
    Ext.Ajax.on( {
      "requestexception" : { 
        fn: function(conn, response, options) {
          Ext.MessageBox.show( {
            title: "Connection Error",
            msg: (
              response.status ?
                "ERROR " + response.status + ": " + response.statusText + "<br />" +
                "<br />" +
                "Nexus returned an error.<br />" +
                "The server is running, but Nexus does not appear to be available."
                :
                "ERROR: " + response.statusText + "<br />" +
                "<br />" +
                "There was an error connecting to Nexus.<br />" +
                "Check your network connection, make sure Nexus is running." ) +
              "<br /><br />" +
              "Click OK to reload the console or CANCEL if you wish to retry the same action in a little while.",
            buttons: Ext.MessageBox.OKCANCEL,
            icon: Ext.MessageBox.ERROR,
            animEl: 'mb3',
            fn: function(button) {
              if(button == "ok") window.location.reload();
            }
          } );
        },
        scope: this } 
    } );
    
    var viewport = new Ext.Viewport({
      layout:'border',
      items:[
        {
          xtype: 'box',
          region:'north',
          el: 'header',
          margins:'5 5 5 5',
          height:'auto'
        },
//      {
//        xtype: 'box',  
//        region:'south',
//        el: 'footer',
//        height: 10,
//        margins: '5 5 5 5'
//      },
        {
          region:'west',
          title: 'Sonatype Servers',
          collapsible: true,
          //collapseMode: 'mini',
          //border: false,
          split:false,
          width: 185,
          minSize: 185,
          maxSize: 185,
          layout:'fit',
          margins:'0 5 5 5',
          items: [
            {
              xtype: 'tabpanel',
              id:'st-server-tab-panel',
              border:false,
              tabPosition:'top',
              layoutOnTabChange: true
              //tabs added by servers
            }
          ]
        },
        new Sonatype.view.MainTabPanel({
          id: 'st-main-tab-panel',
          region:'center',
          margins: '0 5 5 0',
          deferredRender:false,
          resizeTabs:true,
          enableTabScroll: true,
          minTabWidth: 110,
          autoScroll: false, //default
          defaults: {autoScroll:false, closable:true},
          activeTab:0,
          layoutOnTabChange:true,
          //border: false,
          //bodyBorder: true,
          items:[Sonatype.view.welcomeTab]
        })
       ]
    });
    
    //Export useful values to Sonatype.view namespace
    Sonatype.view.viewport = viewport;
    Sonatype.view.serverTabPanel = viewport.findById('st-server-tab-panel');
    Sonatype.view.mainTabPanel = viewport.findById('st-main-tab-panel');
    
    //allow each included sonatype server to setup its tab and events
    var availSvrs = Sonatype.config.installedServers;
    for(var srv in availSvrs) {
      if (availSvrs[srv] && typeof(Sonatype[srv]) != 'undefined') {
        Sonatype[srv][Sonatype.utils.capitalize(srv)].initServerTab();
      }
    }
    
    Sonatype.view.serverTabPanel.setActiveTab('st-nexus-tab');
  },
  
  updateLoginLinkText : function(){
    var loginEl = Ext.get('login-link');
    var usernameEl = Ext.get('username');
    
    if (Sonatype.user.curr.isLoggedIn){ 
      usernameEl.update(Sonatype.user.curr.username + ' | ').show();
      loginEl.update('Log Out');
    }
    else {
      loginEl.update('Log In');
      usernameEl.hide();
    }
  }
};

Sonatype.view.MainTabPanel = Ext.extend(Ext.TabPanel, {
  addOrShowTab : function(id, panelClass, panelClassParams) {
    var panelClassParams = panelClassParams || {};
    var tab = this.getComponent(id);
    if (tab) {
      this.setActiveTab(tab);
    }
    else {
      tab = new panelClass(Ext.apply({id: id}, panelClassParams));
      this.add(tab);
    }
    
    this.setActiveTab(tab);
  }
});
