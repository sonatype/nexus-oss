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

// ********* Set ExtJS options *************************************************
  
Ext.Ajax.defaultHeaders = {'accept' : 'application/json'};

//Set default HTTP headers //@todo: move this to some other common init section
Ext.lib.Ajax.defaultPostHeader = 'application/json';

//set Sonatype defaults for Ext widgets
Ext.form.Field.prototype.msgTarget = 'under';

Ext.Msg.minWidth = 200;

Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
Ext.QuickTips.init();
Ext.apply(Ext.QuickTips.getQuickTip(), {
  showDelay: 250,
  hideDelay: 300,
  dismissDelay: 0 //don't automatically hide quicktip
});
  

Sonatype.config = function() {
  var host = window.location.protocol + '//' + window.location.host;
  var contextPath = window.location.pathname;
  contextPath = contextPath.substr(0, contextPath.lastIndexOf('/'));
  var servicePath = contextPath + '/service/local';
  var resourcePath = contextPath;
  var contentPath = contextPath + '/content';
  var browsePathSnippet = '/content';
  
  return {
    isDebug : false, //set to true to enable Firebug console output (getfirebug.com)
    host : host,
    servicePath : servicePath,
    resourcePath : resourcePath,
    contentPath : contentPath,
    cssPath : '/styles',
    jsPath : '/js',
    browsePathSnippet : browsePathSnippet,
  
    installedServers : {repoServer:true},
  
    repos : {
      urls : {
        login : servicePath + '/authentication/login',
        logout : servicePath + '/authentication/logout',
        globalSettings : servicePath + '/global_settings',
        globalSettingsState : servicePath + '/global_settings/current',
        repositories : servicePath + '/repositories',
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
        attributes : servicePath + '/attributes',
        cache : servicePath + '/data_cache',
        groups : servicePath + '/repo_groups',
        routes : servicePath + '/repo_routes',
        configs : servicePath + '/configs',
        configCurrent : servicePath + '/configs/current',
        logs : servicePath + '/logs',
        feeds : servicePath + '/feeds',
        recentlyChangedArtifactsRss: servicePath + '/feeds/recentChanges',
        recentlyCachedArtifactsRss: servicePath + '/feeds/recentlyCached',
        recentlyDeployedArtifactsRss: servicePath + '/feeds/recentlyDeployed',
        systemChangesRss: servicePath + '/feeds/systemChanges',
        status: servicePath + '/status',
        identify: servicePath + '/identify/sha1',
        schedules: servicePath + '/schedules',
        scheduleTypes: servicePath + '/schedules/types',
        upload: servicePath + '/artifact/maven/content'
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
  repoServer : {
    viewSearch : 1,
    viewUpdatedArtifacts : 0,
    viewCachedArtifacts : 1,
    viewDeployedArtifacts : 1,
    viewSystemChanges : 1,
    maintRepos : 1,
    maintLogs : 0,
    maintConfig : 0,
    configServer : 0,
    configGroups : 0,
    configRules : 0,
    configRepos : 0,
    configSchedules : 0
  }
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