/*
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
(function(){

// Repository main Controller(conglomerate) Singleton
Sonatype.repoServer.RepoServer = function(){
  var cfg = Sonatype.config.repos;
  var sp = Sonatype.lib.Permissions;
  
  var searchConfig = {
    xtype: 'trigger',
    triggerClass: 'x-form-search-trigger',
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
            'nexus-search', Sonatype.repoServer.SearchPanel, { title: 'Search' } );
        panel.startQuickSearch( v );
      }
    }
  };


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
        
        if(sp.checkPermission('security:usersforgotid', sp.CREATE)){
          htmlString = 'Forgot your <a id="recover-username" href="#">username</a>'
        }
        if(sp.checkPermission('security:usersforgotpw', sp.CREATE)){
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

      Sonatype.Events.addListener( 'nexusNavigationInit',
        this.addNexusNavigationItems, this );
        
      Sonatype.Events.addListener( 'nexusStatus',
        this.nexusStatusEvent, this );
      
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
          var usernameField = this.loginForm.find('name', 'username')[0];
          var passwordField = this.loginForm.find('name', 'password')[0];
          
          if ( usernameField.isValid() && passwordField.isValid() ){
          Sonatype.utils.doLogin( this.loginWindow,
            usernameField.getValue(),
            passwordField.getValue());
          }
        } 
      }];
      
      this.loginFormConfig.keys = {
        key: Ext.EventObject.ENTER,
        fn: this.loginFormConfig.buttons[0].handler,
        scope: this
      };
      
      this.loginForm = new Ext.form.FormPanel(this.loginFormConfig);
      this.loginWindow = new Ext.Window({
        id: 'login-window',
        title:'Nexus Log In',
        animateTarget: 'head-link-r',
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
      
      this.loginWindow.on('hide', function(){
        this.loginForm.getForm().reset();
        Sonatype.view.afterLoginToken = null;
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
      
      Sonatype.Events.fireEvent( 'nexusNavigationInitComplete', this.nexusPanel );
      
    },
    
    nexusStatusEvent: function() {
    	
      // check the user status, if it is not set, then reset the panels
      if( !Sonatype.user.curr.repoServer )
      {
        this.resetMainTabPanel();
      }
    },
    
    addNexusNavigationItems: function( nexusPanel ) {      
      if(sp.checkPermission('nexus:index', sp.READ)){
        nexusPanel.add(
          {
            title: 'Artifact Search',
            id: 'st-nexus-search',
            items: [
              Ext.apply( {
                repoPanel: this,
                id: 'quick-search--field',
                width: 140
              }, searchConfig ),
              {
                title: 'Advanced Search',
                tabCode: Sonatype.repoServer.SearchPanel,
                tabId: 'nexus-search',
                tabTitle: 'Search'
              }
            ]
          }
        );
      }
      
      //Views Group **************************************************
      nexusPanel.add( {
        title: 'Views/Repositories',
        id: 'st-nexus-views',
        items: [
          {
            enabled: sp.checkPermission( 'nexus:repostatus', sp.READ ),
            title: 'Repositories',
            tabId: 'view-repositories',
            tabCode: Sonatype.repoServer.RepositoryPanel,
            tabTitle: 'Repositories'
          },
          {
            enabled: sp.checkPermission( 'nexus:feeds', sp.READ ),
            title: 'System Feeds',
            tabId: 'feed-view-system-changes',
            tabCode: Sonatype.repoServer.FeedViewPanel
          },
          {
            enabled: sp.checkPermission( 'nexus:logs', sp.READ ) ||
              sp.checkPermission( 'nexus:configuration', sp.READ ),
            title: 'System Files',
            tabId: 'view-logs',
            tabCode: Sonatype.repoServer.LogsViewPanel,
            tabTitle: 'System Files'
          }
        ]
      } );

      //Config Group **************************************************
      nexusPanel.add( {
        title: 'Enterprise',
        id: 'st-nexus-enterprise'
      } );

      //Config Group **************************************************
      nexusPanel.add( {
        title: 'Administration',
        id: 'st-nexus-config',
        items: [
          {
            enabled: sp.checkPermission('nexus:settings', sp.READ) &&
              ( sp.checkPermission('nexus:settings', sp.CREATE) ||
                sp.checkPermission('nexus:settings', sp.DELETE) ||
                sp.checkPermission('nexus:settings', sp.EDIT)),
            title: 'Server',
            tabId: 'nexus-config',
            tabCode: Sonatype.repoServer.ServerEditPanel,
            tabTitle: 'Nexus'
          },
          {
            enabled: sp.checkPermission('nexus:routes', sp.READ) &&
              ( sp.checkPermission('nexus:routes', sp.CREATE) ||
                sp.checkPermission('nexus:routes', sp.DELETE) ||
                sp.checkPermission('nexus:routes', sp.EDIT)),
            title: 'Routing',
            tabId: 'routes-config',
            tabCode: Sonatype.repoServer.RoutesEditPanel
          },
          {
            enabled: sp.checkPermission('nexus:tasks', sp.READ) &&
              ( sp.checkPermission('nexus:tasks', sp.CREATE) ||
                sp.checkPermission('nexus:tasks', sp.DELETE) ||
                sp.checkPermission('nexus:tasks', sp.EDIT)),
            title: 'Scheduled Tasks',
            tabId: 'schedules-config',
            tabCode: Sonatype.repoServer.SchedulesEditPanel
          },
          {
            enabled: sp.checkPermission('nexus:targets', sp.READ) &&
              ( sp.checkPermission('nexus:targets', sp.CREATE) ||
                sp.checkPermission('nexus:targets', sp.DELETE) ||
                sp.checkPermission('nexus:targets', sp.EDIT)),
            title: 'Repository Targets',
            tabId: 'targets-config',
            tabCode: Sonatype.repoServer.RepoTargetEditPanel
          },
          {
            enabled: sp.checkPermission('nexus:logconfig', sp.READ) &&
              ( sp.checkPermission('nexus:logconfig', sp.CREATE) ||
                sp.checkPermission('nexus:logconfig', sp.DELETE) ||
                sp.checkPermission('nexus:logconfig', sp.EDIT)),
            title: 'Log Configuration',
            tabId: 'log-config',
            tabCode: Sonatype.repoServer.LogEditPanel
          }          
        ]
      } );

      //Security Group **************************************************
      nexusPanel.add({
        title: 'Security',
        id: 'st-nexus-security',
        items: [
          {
            enabled: Sonatype.user.curr.isLoggedIn && Sonatype.user.curr.loggedInUserSource == 'default' && 
              sp.checkPermission( 'security:userschangepw', sp.CREATE ),
            title: 'Change Password',
            handler: Sonatype.utils.changePassword
          },
          {
            enabled: sp.checkPermission( 'security:users', sp.READ ) && 
              ( sp.checkPermission('security:users', sp.CREATE) &&
                sp.checkPermission('security:users', sp.DELETE) &&
                sp.checkPermission('security:users', sp.EDIT)),
            title: 'Users',
            tabId: 'security-users',
            tabCode: Sonatype.repoServer.UserEditPanel
          },
          {
            enabled: sp.checkPermission('security:roles', sp.READ ) && 
              ( sp.checkPermission('security:roles', sp.CREATE) ||
                sp.checkPermission('security:roles', sp.DELETE) ||
                sp.checkPermission('security:roles', sp.EDIT)),
            title: 'Roles',
            tabId: 'security-roles',
            tabCode: Sonatype.repoServer.RoleEditPanel
          },
          {
            enabled: sp.checkPermission('security:privileges', sp.READ ) && 
              ( sp.checkPermission('security:privileges', sp.CREATE) ||
                sp.checkPermission('security:privileges', sp.DELETE) ||
                sp.checkPermission('security:privileges', sp.EDIT)),
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
          {
            title: 'About Nexus',
            tabId: 'AboutNexus',
            tabCode: Sonatype.repoServer.HelpAboutPanel
          },
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
            Sonatype.utils.authToken = null;
            Sonatype.view.justLoggedOut = true;
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
        Sonatype.view.supportedNexusTabs = {};
        
        var welcomePanelConfig = { 
          layout: 'auto',
          width: 500,
          items: []
        };
        var welcomeTabConfig = {
          title: 'Welcome',
          id: 'welcome',
          layout: 'column',
          defaults: {
            border: false,
            style: 'padding-top: 30px;'
          },
          items: [
            { 
              columnWidth: .5,
              html: '&nbsp;' 
            },
            welcomePanelConfig,
            { 
              columnWidth: .5,
              html: '&nbsp;' 
            }
          ],
          listeners: {
            render: {
              fn: function() {
                var c = Ext.getCmp( 'quick-search-welcome-field' );
                if ( c ) {
                  c.focus( true, 100 );
                }
              },
              single: true,
              delay: 300
            }
          }
        };

        var welcomeMsg = '<p style="text-align:center;"><a href="http://nexus.sonatype.org" target="new">' +
          '<img src="images/nexus.png" border="0" alt="Welcome to the Sonatype Nexus Maven Repository Manager"></a>' +
          '</p>';

        var statusEnabled = sp.checkPermission('nexus:status', sp.READ);
        if ( !statusEnabled ){
        	welcomeMsg += '</br>';
        	welcomeMsg += '<p style="color:red">Warning: Could not retrieve Nexus status, anonymous access might be disabled.</p>';
        }
        
//        if( !Sonatype.user.curr.isLoggedIn ){
//        	welcomeMsg += '</br>';
//        	welcomeMsg += '<p>Administrators may login via the link on the top right.</p>';
//        }
        
        var searchEnabled = sp.checkPermission('nexus:index', sp.READ);
        var browseEnabled = sp.checkPermission('nexus:repostatus', sp.READ );

//        if (searchEnabled || browseEnabled){
//        	welcomeMsg += '</br>';
//        	if (searchEnabled && browseEnabled){
//        		welcomeMsg += '<p>You may browse and search the repositories using the options on the left.</p>'
//        	}
//        	else if (searchEnabled && !browseEnabled){
//        		welcomeMsg += '<p>You may search the repositories using the options on the left.</p>'
//        	}
      	if (!searchEnabled && browseEnabled){
      		welcomeMsg += '<p>You may browse the repositories using the options on the left.</p>'
      	}
//        }

      	welcomePanelConfig.items.push( {
          border: false,
          html: '<div class="little-padding">' + welcomeMsg + '</div>'
        } );
        
        if ( searchEnabled ) {
          welcomePanelConfig.items.push( {
            layout: 'form',
            border: false,
            frame: false,
            labelWidth: 10,
            items: [
              {
                border: false,
                html: '<div class="little-padding">' +
                  'Type in the name of a project, class, or artifact into the text box ' +
                  'below, and click Search. Use "Advanced Search" on the left for more options.' +
                  '</div>'
              },
              Ext.apply( {
                repoPanel: this,
                id: 'quick-search-welcome-field',
                anchor: '-10',
                labelSeparator: ''
              }, searchConfig )
//              {
//                border: false,
//                html: '<div class="little-padding">' +
//                  'Firefox and Internet Explorer users can also install a Maven search plug-in. ' +
//                  'Click the search box in your browser and select "Nexus" from the list of ' +
//                  'available search providers.' +
//                  '</div>'
//              }
            ]
          } );
        }
        
        Sonatype.view.welcomeTab = new Ext.Panel( welcomeTabConfig );
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
