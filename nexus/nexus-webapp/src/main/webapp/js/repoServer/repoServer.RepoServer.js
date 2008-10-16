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
(function(){

// Repository main Controller(conglomerate) Singleton
Sonatype.repoServer.RepoServer = function(){
  var cfg = Sonatype.config.repos;
  var sp = Sonatype.lib.Permissions;

// ************************************  
  return {
    pomDepTmpl : new Ext.XTemplate('<dependency><groupId>{groupId}</groupId><artifactId>{artifactId}</artifactId><version>{version}</version></dependency>'),
    
    loginFormConfig : { 
      labelAlign: 'right',
      labelWidth:60,
      frame:true,  

      defaultType:'textfield',
      monitorValid:true,

      items:[
        { 
          id:'usernamefield',
          fieldLabel:'Username', 
          name:'username',
          tabIndex: 1,
          width: 200,
          allowBlank:false 
        },
        { 
          id:'passwordfield',
          fieldLabel:'Password', 
          name:'password',
          tabIndex: 2, 
          inputType:'password', 
          width: 200,
          allowBlank:false 
        }
      ]
      //buttons added later to provide scope to handler
       
    },
    
    buildRecoveryText : function(){
        var htmlString = null;
        
        if(sp.checkPermission(Sonatype.user.curr.repoServer.actionForgotUserid, sp.CREATE)){
          htmlString = 'Forgot your <a id="recover-username" href="#">username</a>'
        }
        if(sp.checkPermission(Sonatype.user.curr.repoServer.actionForgotPassword, sp.CREATE)){
          if (htmlString != null){
            htmlString += ' or ';
          }
          else{
            htmlString = 'Forgot your ';
          }
          htmlString += '<a id="recover-password" href="#">password</a>';
        }
        if (htmlString != null){
          htmlString += '?';
        }
        
        return htmlString;
    },
    
    statusComplete : function( statusResponse ){        
        this.resetMainTabPanel();
        
        this.createSubComponents(); //update left panel
        
        var htmlString = this.buildRecoveryText();
        
        var recoveryPanel = this.loginForm.findById('recovery-panel');
        if ( recoveryPanel ) {
          this.loginForm.remove( recoveryPanel );
          recoveryPanel.destroy();
        }
        this.loginForm.add({
            xtype: 'panel',
            id: 'recovery-panel',
            style: 'padding-left: 70px',
            html: htmlString
          });
    },
    
    // Each Sonatype server will need one of these 
    initServerTab : function() {

      Sonatype.Events.addListener( 'nexusNavigationInit', this.addNexusNavigationItems, this );
      
      // Left Panel
      this.nexusPanel = new Sonatype.navigation.NavigationPanel({
        id: 'st-nexus-tab',
        title: 'Nexus'
      });

      this.createSubComponents();
      
      Sonatype.view.serverTabPanel.add(this.nexusPanel);
      
      var htmlString = this.buildRecoveryText();
      
      if (htmlString != null){
    	this.loginFormConfig.items[2] = 
    	  {
    		xtype: 'panel',
    		id: 'recovery-panel',
    		style: 'padding-left: 70px',
    		html: htmlString
    	  };
      }
      
      this.loginFormConfig.buttons = [{ 
        id:'loginbutton',
        text:'Log In',
        tabIndex: 3, 
        formBind: true,
        scope: this,
        handler:function(){
          Sonatype.utils.doLogin( this.loginWindow,
            this.loginForm.find('name', 'username')[0].getValue(),
            this.loginForm.find('name', 'password')[0].getValue()); 
        } 
      }];
      
      this.loginFormConfig.keys = {
        key: Ext.EventObject.ENTER,
        fn: this.loginFormConfig.buttons[0].handler,
        scope: this
      };
      
      this.loginForm = new Ext.form.FormPanel(this.loginFormConfig);
      this.loginWindow = new Ext.Window({
        title:'Nexus Log In',
        animateTarget: 'login-link',
        closable: true,
        closeAction: 'hide',
        autoWidth: false,
        width: 300,
        autoHeight: true,
        modal:true,
        constrain: true,
        resizable: false,
        draggable: false,
        items: [this.loginForm]
      });
      
      this.loginWindow.on('show', function(){
        var panel = this.loginWindow.findById( 'recovery-panel' );
        if (panel && !panel.clickListenerAdded) {
          // these listeners only work if added after the window is created
          panel.body.on('click', Ext.emptyFn, null, {delegate:'a', preventDefault:true});
          panel.body.on('mousedown', this.recoverLogin, this, {delegate:'a'});
          panel.clickListenerAdded = true;
        }

        var field = this.loginForm.find('name', 'username')[0];
        if ( field.getRawValue() ) {
          field = this.loginForm.find('name', 'password')[0]
        }
        field.focus(true, 100);
      }, this);
      
      this.loginWindow.on('close', function(){
        this.loginForm.getForm().reset();
      }, this);
    },
    
    //Add/Replace Nexus left hand components
    createSubComponents : function() {
      var wasRendered = this.nexusPanel.rendered;
      
      if (wasRendered) {
        this.nexusPanel.getEl().mask('Updating...', 'loading-indicator');
        this.nexusPanel.items.each(function(item, i, len){
          this.remove(item, true);
        }, this.nexusPanel);
      }

      Sonatype.Events.fireEvent( 'nexusNavigationInit', this.nexusPanel );

      if (wasRendered) {
        this.nexusPanel.doLayout();
        this.nexusPanel.getEl().unmask();
        //this.nexusPanel.enable();
      }
      
    },
    
    addNexusNavigationItems: function( nexusPanel ) {
      var userPerms = Sonatype.user.curr.repoServer;
      
      if(sp.checkPermission(userPerms.viewSearch, sp.READ)){
        nexusPanel.add(
          {
            title: 'Artifact Search',
            id: 'st-nexus-search',
            items: [
              {
                xtype: 'trigger',
                id: 'quick-search--field',
                triggerClass: 'x-form-search-trigger',
                repoPanel: this,
                width: 150,
                listeners: {
                  'specialkey': {
                    fn: function(f, e){
                      if(e.getKey() == e.ENTER){
                        this.onTriggerClick();
                      }
                    }
                  }
                },
                onTriggerClick: function(a,b,c){
                  var v = this.getRawValue();
                  if ( v.length > 0 ) {
                    var panel = Sonatype.view.mainTabPanel.addOrShowTab(
                      'st-nexus-search-panel', Sonatype.repoServer.SearchPanel, { title: 'Search' } );
                    var searchType = 'quick';
                    if ( v.search(/^[0-9a-f]{40}$/) == 0 ) {
                      searchType = 'checksum';
                    }
                    else if ( v.search(/^[A-Z]/) == 0 ) {
                      searchType = 'classname';
                    }
                    panel.setSearchType( panel, searchType );
                    panel.searchField.setRawValue( this.getRawValue() );
                    panel.startSearch( panel );
                  }
                }
              },
              {
                title: 'Advanced Search',
                tabCode: Sonatype.repoServer.SearchPanel,
                tabId: 'st-nexus-search-panel',
                tabTitle: 'Search'
              }
            ]
          }
        );
      }
      
      //Views Group **************************************************
      nexusPanel.add( {
        title: 'Views',
        id: 'st-nexus-views',
        items: [
          {
            enabled: sp.checkPermission( userPerms.maintRepos, sp.READ ),
            title: 'Browse Repositories',
            tabId: 'repos-maint',
            tabCode: Sonatype.repoServer.RepoMaintPanel,
            tabTitle: 'Repositories'
          },
          {
            enabled: sp.checkPermission( userPerms.viewSystemChanges, sp.READ ),
            title: 'System Feeds',
            tabId: 'feed-view-system_changes',
            tabCode: Sonatype.repoServer.FeedViewPanel
          },
          {
            enabled: sp.checkPermission( userPerms.maintLogs, sp.READ ) ||
              sp.checkPermission( userPerms.maintConfig, sp.READ ),
            title: 'Logs and Config Files',
            tabId: 'view-logs',
            tabCode: Sonatype.repoServer.LogsViewPanel,
            tabTitle: 'Logs and Configs'
          }
        ]
      } );

      //Config Group **************************************************
      nexusPanel.add( {
        title: 'Administration',
        id: 'st-nexus-config',
        items: [
          {
            enabled: sp.checkPermission(userPerms.configServer, sp.READ) &&
              ( sp.checkPermission(userPerms.configServer, sp.CREATE) ||
                sp.checkPermission(userPerms.configServer, sp.DELETE)
              |  sp.checkPermission(userPerms.configServer, sp.EDIT)),
            title: 'Server',
            tabId: 'nexus-config',
            tabCode: Sonatype.repoServer.ServerEditPanel,
            tabTitle: 'Nexus'
          },
          {
            enabled: sp.checkPermission(userPerms.configRepos, sp.READ) &&
              ( sp.checkPermission(userPerms.configRepos, sp.CREATE) ||
                sp.checkPermission(userPerms.configRepos, sp.DELETE) ||
                sp.checkPermission(userPerms.configRepos, sp.EDIT)),
            title: 'Repositories',
            tabId: 'repos-config',
            tabCode: Sonatype.repoServer.RepoEditPanel,
            tabTitle: 'Repository Config'
          },
          {
            enabled: sp.checkPermission(userPerms.configGroups, sp.READ) &&
              ( sp.checkPermission(userPerms.configGroups, sp.CREATE) ||
                sp.checkPermission(userPerms.configGroups, sp.DELETE) ||
                sp.checkPermission(userPerms.configGroups, sp.EDIT)),
            title: 'Groups',
            tabId: 'groups-config',
            tabCode: Sonatype.repoServer.GroupsEditPanel
          },
          {
            enabled: sp.checkPermission(userPerms.configRules, sp.READ) &&
              ( sp.checkPermission(userPerms.configRules, sp.CREATE) ||
                sp.checkPermission(userPerms.configRules, sp.DELETE) ||
                sp.checkPermission(userPerms.configRules, sp.EDIT)),
            title: 'Routing',
            tabId: 'routes-config',
            tabCode: Sonatype.repoServer.RoutesEditPanel
          },
          {
            enabled: sp.checkPermission(userPerms.configSchedules, sp.READ) &&
              ( sp.checkPermission(userPerms.configSchedules, sp.CREATE) ||
                sp.checkPermission(userPerms.configSchedules, sp.DELETE) ||
                sp.checkPermission(userPerms.configSchedules, sp.EDIT)),
            title: 'Scheduled Tasks',
            tabId: 'schedules-config',
            tabCode: Sonatype.repoServer.SchedulesEditPanel
          },
          {
            enabled: sp.checkPermission(userPerms.configRepoTargets, sp.READ) &&
              ( sp.checkPermission(userPerms.configRepoTargets, sp.CREATE) ||
                sp.checkPermission(userPerms.configRepoTargets, sp.DELETE) ||
                sp.checkPermission(userPerms.configRepoTargets, sp.EDIT)),
            title: 'Repository Targets',
            tabId: 'config-repoTargets',
            tabCode: Sonatype.repoServer.RepoTargetEditPanel
          }
        ]
      } );

      //Security Group **************************************************
      nexusPanel.add({
        title: 'Security',
        id: 'st-nexus-security',
        items: [
          {
            enabled: Sonatype.user.curr.isLoggedIn && 
              sp.checkPermission( userPerms.actionChangePassword, sp.CREATE ),
            title: 'Change Password',
            handler: Sonatype.utils.changePassword
          },
          {
            enabled: sp.checkPermission( userPerms.configUsers, sp.READ ) && 
              ( sp.checkPermission(userPerms.configUsers, sp.CREATE) &&
                sp.checkPermission(userPerms.configUsers, sp.DELETE) &&
                sp.checkPermission(userPerms.configUsers, sp.EDIT)),
            title: 'Users',
            tabId: 'security-users',
            tabCode: Sonatype.repoServer.UserEditPanel
          },
          {
            enabled: sp.checkPermission( userPerms.configRoles, sp.READ ) && 
              ( sp.checkPermission(userPerms.configRoles, sp.CREATE) ||
                sp.checkPermission(userPerms.configRoles, sp.DELETE) ||
                sp.checkPermission(userPerms.configRoles, sp.EDIT)),
            title: 'Roles',
            tabId: 'security-roles',
            tabCode: Sonatype.repoServer.RoleEditPanel
          },
          {
            enabled: sp.checkPermission( userPerms.configPrivileges, sp.READ ) && 
              ( sp.checkPermission(userPerms.configPrivileges, sp.CREATE) ||
                sp.checkPermission(userPerms.configPrivileges, sp.DELETE) ||
                sp.checkPermission(userPerms.configPrivileges, sp.EDIT)),
            title: 'Privileges',
            tabId: 'security-privileges',
            tabCode: Sonatype.repoServer.PrivilegeEditPanel
          }
        ]
      } );

      nexusPanel.add( {
        title: 'Help',
        id: 'st-nexus-docs',
        collapsible: true,
        collapsed: true,
        items: [
          { title: 'Nexus Home',
            href: 'http://nexus.sonatype.org/' },
          { title: 'Getting Started',
            href: 'http://www.sonatype.com/book/reference/repository-manager.html' },
          { title: 'Nexus Wiki',
            href: 'https://docs.sonatype.com/display/NX/Home' },
          { title: 'Maven Book',
            href: 'http://www.sonatype.com/book/reference/public-book.html' },
          { title: 'Release Notes',
            href: 'http://nexus.sonatype.org/changes.html' },
          { title: 'Report Issue',
            href: 'http://issues.sonatype.org/secure/CreateIssue.jspa?pid=10001&issuetype=1' }
        ]
      } );
    },
    
    loginHandler : function(){
      if (Sonatype.user.curr.isLoggedIn) {
        //do logout
        Ext.Ajax.request({
          scope: this,
          method: 'GET',
          url: Sonatype.config.repos.urls.logout,
          callback: function(options, success, response){
            Sonatype.utils.clearCookie('JSESSIONID');
            Sonatype.utils.clearCookie('nxRememberMe');
            Sonatype.utils.loadNexusStatus();
          }
        });
        
      }
      else {
        this.loginForm.getForm().clearInvalid();
        var cp = Sonatype.state.CookieProvider;
        var username = cp.get('username', null);
        if ( username ) {
          this.loginForm.find( 'name', 'username' )[0].setValue( username );
        }
        this.loginWindow.show();
      }
    },
    
    resetMainTabPanel: function() {
      Sonatype.view.mainTabPanel.items.each(function(item, i, len){
        this.remove( item, true );
      }, Sonatype.view.mainTabPanel);
      Sonatype.view.mainTabPanel.activeTab = null;
      
      Sonatype.view.welcomeTab = new Ext.Panel({
        title: 'Welcome',
        html: '<div class="little-padding">' +
          '<p>Welcome to the <a href="http://nexus.sonatype.org" target="new">Sonatype Nexus Maven Repository Manager</a>.</p>' +
          '<br/>' +
          '<p>You may browse and search the repositories using the options on the left. Administrators may login via the link on the top right.<p>' +
          '</div>'
      });
      Sonatype.view.mainTabPanel.add(Sonatype.view.welcomeTab);
      Sonatype.view.mainTabPanel.setActiveTab(Sonatype.view.welcomeTab);
    },

    recoverLogin : function(e, target){
      e.stopEvent();
      if (this.loginWindow.isVisible()) {
    	this.loginWindow.hide();
      }
      
      var action = target.id;
      if (action == 'recover-username') {
    	  Sonatype.utils.recoverUsername();
      }
      else if (action == 'recover-password') {
      	Sonatype.utils.recoverPassword();
      }
    }
     
  };
}();


})();