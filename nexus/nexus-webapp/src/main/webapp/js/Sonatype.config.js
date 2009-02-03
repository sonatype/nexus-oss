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

// ********* Set ExtJS options *************************************************
  
Ext.Ajax.defaultHeaders = {'accept' : 'application/json'};
Ext.Ajax.timeout = 60000;

//Set default HTTP headers //@todo: move this to some other common init section
Ext.lib.Ajax.defaultPostHeader = 'application/json';

//set Sonatype defaults for Ext widgets
Ext.form.Field.prototype.msgTarget = 'under';

Sonatype.MessageBox.minWidth = 200;

Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

Sonatype.config = function() {
  var host = window.location.protocol + '//' + window.location.host;
  var contextPath = window.location.pathname;
  contextPath = contextPath.substr(0, contextPath.lastIndexOf('/'));
  var servicePath = contextPath + '/service/local';
  var resourcePath = contextPath;
  var contentPath = contextPath + '/content';
  var browsePathSnippet = '/content';
  var browseIndexPathSnippet = '/index_content';
  
  return {
    isDebug : false, //set to true to enable Firebug console output (getfirebug.com)
    host : host,
    servicePath : servicePath,
    resourcePath : resourcePath,
    extPath : resourcePath + '/ext-2.2',
    contentPath : contentPath,
    cssPath : '/styles',
    jsPath : '/js',
    browsePathSnippet : browsePathSnippet,
    browseIndexPathSnippet : browseIndexPathSnippet,
  
    installedServers : {repoServer:true},
  
    repos : {
      urls : {
        login : servicePath + '/authentication/login',
        logout : servicePath + '/authentication/logout',
        globalSettings : servicePath + '/global_settings',
        globalSettingsState : servicePath + '/global_settings/current',
        repositories : servicePath + '/repositories',
        allRepositories : servicePath + '/all_repositories',
        repositoryStatuses : servicePath + '/repository_statuses',
        repoTemplates : servicePath + '/templates/repositories',
        repoTemplate : {
          virtual : servicePath + '/templates/repositories/default_virtual',
          hosted : servicePath + '/templates/repositories/default_hosted_release', //default
          hosted_release : servicePath + '/templates/repositories/default_hosted_release',
          hosted_snapshot : servicePath + '/templates/repositories/default_hosted_snapshot',
          proxy : servicePath + '/templates/repositories/default_proxy_release', //default
          proxy_release : servicePath + '/templates/repositories/default_proxy_release',
          proxy_snapshot : servicePath + '/templates/repositories/default_proxy_snapshot'
        },
        index : servicePath + '/data_index',
        metadata : servicePath + '/metadata',
        cache : servicePath + '/data_cache',
        groups : servicePath + '/repo_groups',
        routes : servicePath + '/repo_routes',
        configs : servicePath + '/configs',
        configCurrent : servicePath + '/configs/current',
        logs : servicePath + '/logs',
        logConfig : servicePath + '/log/config',
        feeds : servicePath + '/feeds',
        recentlyChangedArtifactsRss: servicePath + '/feeds/recentChanges',
        recentlyCachedArtifactsRss: servicePath + '/feeds/recentlyCached',
        recentlyDeployedArtifactsRss: servicePath + '/feeds/recentlyDeployed',
        systemChangesRss: servicePath + '/feeds/systemChanges',
        status: servicePath + '/status',
        identify: servicePath + '/identify/sha1',
        schedules: servicePath + '/schedules',
        scheduleRun: servicePath + '/schedule_run',
        scheduleTypes: servicePath + '/schedule_types',
        upload: servicePath + '/artifact/maven/content',
        redirect: servicePath + '/artifact/maven/redirect',
        trash: servicePath + '/wastebasket',
        plexusUsersAllConfigured: servicePath + '/plexus_users/allConfigured',
        plexusUsersDefault: servicePath + '/plexus_users/default',
        plexusUsers: servicePath + '/plexus_users',
        userLocators: servicePath + '/components/userLocators',
        searchUsers: servicePath + '/user_search',
        plexusUser: servicePath + '/plexus_user',
        userToRoles: servicePath + '/user_to_roles',
        users: servicePath + '/users',
        usersReset: servicePath + '/users_reset',
        usersForgotId: servicePath + '/users_forgotid',
        usersForgotPassword: servicePath + '/users_forgotpw',
        usersChangePassword: servicePath + '/users_changepw',
        usersSetPassword: servicePath + '/users_setpw',
        roles: servicePath + '/roles',
        plexusRoles: servicePath + '/plexus_roles',
        plexusRolesAll: servicePath + '/plexus_roles/all',
        privileges: servicePath + '/privileges',
        repoTargets: servicePath + '/repo_targets',
        repoContentClasses: servicePath + '/components/repo_content_classes',
        realmComponents: servicePath + '/components/realm_types',
        repoTypes: servicePath + '/components/repo_types',
        shadowRepoTypes: servicePath + '/components/shadow_repo_types',
        groupRepoTypes: servicePath + '/components/group_repo_types',
        repoMirrors: servicePath + '/repository_mirrors',
        repoPredefinedMirrors: servicePath + '/repository_predefined_mirrors',
        repoMirrorStatus: servicePath + '/repository_mirrors_status',
        privilegeTypes: servicePath + '/privilege_types'
      }
    },
    
    content : {
      groups: contentPath + '/groups',
      repositories: contentPath + '/repositories'
    }
  }
}();

// Default anonymous user permissions; 3-bit permissions: delete | edit | read
Sonatype.user.anon = {
  username : '',
  isLoggedIn : false,
  authToken : null,
  repoServer : {}
};

Sonatype.user.curr = Sonatype.utils.cloneObj(Sonatype.user.anon);
//Sonatype.user.curr = {
//  repoServer : {
//    viewSearch : 1,
//    viewUpdatedArtifacts : 1,
//    viewCachedArtifacts : 1,
//    viewDeployedArtifacts : 1,
//    viewSystemChanges : 1,
//    maintRepos : 3,
//    maintLogs : 1,
//    maintConfig : 1,
//    configServer : 3,
//    configGroups : 7,
//    configRules : 7,
//    configRepos : 7
//  }
//};

})();
