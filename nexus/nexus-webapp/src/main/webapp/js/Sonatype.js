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

window.Sonatype = function(){
  return {    
    init : function() {
      Ext.get('header').hide();
      Ext.get('welcome-tab').hide();
      
      Sonatype.state.CookieProvider = new Sonatype.lib.CookieProvider({
        expires: new Date(new Date().getTime()+(1000*60*60*24*365)) //expires in 1 year
      });
      
      var cp = Sonatype.state.CookieProvider;
      
//      var authToken = cp.get('authToken', null);
      var username = cp.get('username', null);
      
      Sonatype.utils.clearCookie('JSESSIONID');
      
      Sonatype.utils.loadNexusStatus();

      if ( username && false ) {
        Ext.Ajax.request({
          scope: this,
          method: 'GET',
          cbPassThru : {
            username : username
          },
          url: Sonatype.config.repos.urls.login,
          success: function(response, options){
            //get user permissions
            var respObj = Ext.decode(response.responseText);
            var newUserPerms = respObj.data.clientPermissions;
            
            Sonatype.user.curr.username = options.cbPassThru.username;
//            Sonatype.user.curr.authToken = respObj.data.authToken;
            Sonatype.user.curr.repoServer = newUserPerms;
            
//            Sonatype.state.CookieProvider.set('authToken', Sonatype.user.curr.authToken);

//            Ext.lib.Ajax.defaultHeaders.Authorization = 'NexusAuthToken ' + Sonatype.user.curr.authToken;
            
            Sonatype.user.curr.isLoggedIn = true;
            Sonatype.view.init();
          },
          failure: function(response, options){
            delete Ext.lib.Ajax.defaultHeaders.Authorization;
//            Sonatype.state.CookieProvider.clear('authToken');
//            Sonatype.state.CookieProvider.clear('username');
            Sonatype.view.init();
          }

        });
      }
      else {
        Sonatype.user.curr.isLoggedIn = false;
        Sonatype.view.init();
      }
    }
    
    
    
  };
}();

//Define all second level namespaces
Ext.namespace(
  'Sonatype.state',
  'Sonatype.ext',
  'Sonatype.lib',
  'Sonatype.utils',
  'Sonatype.config',
  'Sonatype.user',
  'Sonatype.resources',
  'Sonatype.repoServer',
  'Sonatype.repoServer.resources'
);

})();
