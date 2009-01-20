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

window.Sonatype = function(){
  return {    
    init : function() {
      Ext.QuickTips.init();
      Ext.apply(Ext.QuickTips.getQuickTip(), {
        showDelay: 250,
        hideDelay: 300,
        dismissDelay: 0 //don't automatically hide quicktip
      });

      Ext.get('header').hide();
      Ext.get('welcome-tab').hide();
      
      Sonatype.state.CookieProvider = new Sonatype.lib.CookieProvider({
        expires: new Date(new Date().getTime()+(1000*60*60*24*365)) //expires in 1 year
      });
      
      var cp = Sonatype.state.CookieProvider;
      var username = cp.get('username', null);
//      Sonatype.utils.clearCookie('JSESSIONID');

      Sonatype.view.init();
      
      Sonatype.utils.loadNexusStatus();
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
