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
/*
 * Help text resource file for Nexus resource forms
 *
 * Dependencies: Ext.form.Field.AfterRender override
 * Instructions: To render the "?" next to field label give tha associated
 *   value in this config a non-empty string value.  Fields with empty strings
 *   will not have the help "?" rendered next to the field label.
 */

(function(){

//Nexus default help text values for re-use in child forms
var userAgentString = 'This the HTTP \'user-agent\' used in HTTP requests.';
var queryString = 'These are additional parameters sent along with the HTTP request. They are appended to the url along with a \'?\'. So \'foo=bar&foo2=bar2\' becomes \'HTTP://myurl?foo=bar&foo2=bar2\'';
var connectionTimeout = 'Time Nexus will wait for a successful connection before retrying. (seconds)';
var retrievalRetryCount = 'Nexus will make this many connection attempts before giving up.';

var proxyHostname = 'This is the host name of the HTTP proxy used for remote connections. (no HTTP/HTTPs required...just the host or ip)';
var proxyPort = 'This is the port number of the HTTP proxy used for remote connections.';

var username = 'The username used for authentication to the HTTP proxy.';
var password = 'The password used for authentication to the HTTP proxy.';
var privateKey = 'The ssl private key used for authentication to the HTTP proxy.';
var passphrase = 'The passphase for the private key.';
var ntlmHost = 'The Windows NT Lan Manager for authentication.';
var ntlmDomain = 'The Windows NT Lan Manager domain for authentication.';  

Sonatype.repoServer.resources.help = {

  // Server Config help text
  server : {
    adminPassword : 'Optional field to set the new Admin user password.',
    deploymentPassword : 'Optional field to set the new Deployment password.',
    workingDirectory : 'The base folder where Nexus will store all of its data. For easier upgrades, it is recommended that this be in a folder outside your Nexus binary installation. Nexus must be restarted after changing this value.',
    logDirectory : 'This is the location of your log files. It must correspond to the location specified by /etc/log4j.properties.',

    //use default nexus text
    userAgentString : userAgentString,
    queryString : queryString,
    connectionTimeout : connectionTimeout,
    retrievalRetryCount : retrievalRetryCount,

    proxyHostname : proxyHostname,
    proxyPort : proxyPort,
    
    username : username,
    password : password,
    privateKey : privateKey,
    passphrase : passphrase,
    ntlmHost : ntlmHost,
    ntlmDomain : ntlmDomain
  },

  // Groups Config help text
  groups : {
    id : 'The unique id for the group. This id will become part of the url so it should not contain spaces.',
    name : 'The human readable group name used in the UI and logs.'
  },

  // Routes Config help text
  routes : {
    pattern : 'A regular expression used to match the artifact path. The path is everything after /nexus/content/ so it will include the group or repository name. .* is used to specify all paths. \'.*/com/some/company/.*\' will match any artifact with \'com.some.company\' as the group id or artifact id.',
    ruleType: 'There are two types of rules: Inclusive = if the pattern matches, only use the repositories listed below, and Exclusive = exclude the repositories listed below.'
  },

  // Repositories Config help text
  repos : {
    // shared across types of repositories
    id : 'The unique id for the repository. This id will become part of the url so it should not contain spaces.',
    name : 'This is the human readable repository name used in the UI and logs.',
    repoType : 'Nexus supports 3 repository types: Hosted = Normal repository owned by this Nexus instance, Proxy = Retrieve artifacts from the remote repository and store them locally, Virtual = A logical view of another repository configured in Nexus (For example, to provide a Maven 1 view of an existing Maven 2 repository)',
    repoPolicy : 'Repositories can store either all Release artifacts or all Snapshot artifacts.',
    defaultLocalStorageUrl : 'This is the location on the file system used to host the artifacts. It is contained by the Working Directory set in the Server configuration.',
    overrideLocalStorageUrl : 'This is used to override the default local storage. Leave it blank to use the default. Note, only file:// urls are supported.',
    allowWrite : 'This controls if users are allowed to deploy artifacts to this repository. (Hosted repositories only)',
    browseable : 'This controls if users can browse the contents of the repository via their web browser.',
    indexable : 'This controls if the artifacts contained by this repository are indexed and thus searchable.',
    notFoundCacheTTL : 'This controls how long to cache the fact that a file was not found in the repository.',
    artifactMaxAge : 'This controls how long to cache the artifacts in the repository before rechecking the remote repository. In a release repository, this value should be -1 (infinite) as release artifacts shouldn\'t change.',
    metadataMaxAge : 'This controls how long to cache the metadata in the repository before rechecking the remote repository. Unlike artifact max age, this value should not be infinite or Maven won\'t discover new artifact releases.',
    format : 'This is the format of the repository.  Maven1 = A Maven 1.x formatted view of the repository.  Maven2 = A Maven 2.x formatted view of the repository.',
  
    // virtual
    shadowOf : 'This is the id of the physical repository being presented as a logical view by this proxy.',
    syncAtStartup : 'The links are normally updated as changes are made to the repository, if changes may be made while Nexus is offline, the virtual repo should be synchronized at startup.',
  
    //proxy
    remoteStorageUrl : 'This is the location of the remote repository being proxied. Only HTTP/HTTPs urls are currently supported.',
    downloadRemoteIndexes : 'Indicates if the index stored on the remote repository should be downloaded and used for local searches.',
    checksumPolicy : 'The checksum policy for this repository: Ignore: Don\'t check remote checksums. Warn: Log a warning if the checksum is bad but serve the artifact anyway. (Default...there are currently known checksum errors on Central). StrictIfExists: Do not serve the artifact if the checksum exists but is invalid. Strict: Require that a checksum exists on the remote repository and that it is valid.',
    //remote
    remoteUsername : 'The username used for authentication to the HTTP proxy.',
    remotePassword : 'The password used for authentication to the HTTP proxy.',
    remotePrivateKey : 'The ssl private key used for authentication to the HTTP proxy.',
    remotePassphrase : 'The passphase for the private key.',
    remoteNtlmHost : 'The Windows NT Lan Manager for authentication.',
    remoteNtlmDomain : 'The Windows NT Lan Manager domain for authentication.',
  
    //proxy override fields
    userAgentString : userAgentString,
    queryString : queryString,
    connectionTimeout : connectionTimeout,
    retrievalRetryCount : retrievalRetryCount,

    proxyHostname : proxyHostname,
    proxyPort : proxyPort,
    
    username : username,
    password : password,
    privateKey : privateKey,
    passphrase : passphrase,
    ntlmHost : ntlmHost,
    ntlmDomain : ntlmDomain
  }
};

})();