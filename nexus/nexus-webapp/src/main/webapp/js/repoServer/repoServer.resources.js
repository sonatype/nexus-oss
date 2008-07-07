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
    adminPassword : 'Field to set the new Admin user password.',
    deploymentPassword : 'Field to set the new Deployment password.',
    securityConfiguration : 'The Security Model to use.  Options include Simple, Custom and Off.', 
    workingDirectory : 'The base folder where Nexus will store all of its data. For easier upgrades, it is recommended that this be in a folder outside your Nexus binary installation. Nexus must be restarted after changing this value.',
    logDirectory : 'This is the location of your log files. It must correspond to the location specified by - runtime/apps/nexus/conf/log4j.properties.',
    baseUrl : 'This is the Base URL of the Nexus web application.  i.e. http://localhost:8081/nexus',

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
    ruleType: 'There are three types of rules: Inclusive (if the pattern matches, only use the repositories listed below), Exclusive (exclude the repositories listed below) and Blocking (block URLs matching the pattern).'
  },
  
  // Scheduled Services Config help text
  schedules : {
    enabled : 'This flag determines if the service is currently active.  To disable this service for a period of time, de-select this checkbox.',
    name : 'A name for the Scheduled Task.',
    serviceType : 'The Type of service that will be scheduled to run.',
    serviceSchedule : 'The frequency this service will run.  None - this service can only be run manually. Once - run the service once at the specified date/time. Daily - run the service every day at the specified time. Weekly - run the service every week on the specified day at the specified time. Monthly - run the service every month on the specified day(s) and time. Advanced - run the service using the supplied cron string.',
    startDate: 'The Date this service should start running.',
    startTime: 'The Time this service should start running.',
    recurringTime: 'The time this service should start on days it will run.',
    cronCommand: 'A cron expression that will control the running of the service.'
  },
  
  // Users help
  users: {
    userId : 'The ID assigned to this user, will be used as the username.',
    name : 'The name of the user.',
    email : 'Email address, to notify user when necessary.',
    status : 'The current status of the user.',
    roles : 'The roles assigned to this user.',
    password : 'The password required to log the user into the system.',
    reenterPassword : 'Re-enter the password to validate entry.'
  },
  
  // Roles help
  roles: {
    id : 'The ID assigned to this role.',
    name : 'The name of this role.',
    description : 'The description of this role.',
    sessionTimeout : 'The number of minutes to wait before timing out a user session.',
    rolesAndPrivileges : 'Roles and privileges contained in this Role.'
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
  },
  
  // artifact upload help text
  artifact: {
    groupId: 'Group ID',
    artifactId: 'Maven artifact ID',
    version: 'Artifact version',
    packaging: 'Packaging type',
    classifier: 'Classifier (optional)'
  },
  
  cronBigHelp: {
    text: '<br>Provides a parser and evaluator for unix-like cron expressions. Cron expressions provide the ability to specify complex time combinations such as - At 8:00am every Monday through Friday - or - At 1:30am every last Friday of the month.<br><br>Cron expressions are comprised of 6 required fields and one optional field separated by white space. The fields respectively are described as follows: <table cellspacing=&quot;8&quot;><tr><th align=&quot;left&quot;>Field Name</th><th align=&quot;left&quot;>&nbsp;</th><th align=&quot;left&quot;>Allowed Values</th><th align=&quot;left&quot;>&nbsp;</th><th align=&quot;left&quot;>Allowed Special Characters</th></tr><tr><td align=&quot;left&quot;><code>Seconds</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>0-59</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>, - * /</code></td></tr><tr><td align=&quot;left&quot;><code>Minutes</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>0-59</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>, - * /</code></td></tr><tr><td align=&quot;left&quot;><code>Hours</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>0-23</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>, - * /</code></td></tr><tr><td align=&quot;left&quot;><code>Day-of-month</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>1-31</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>, - * ? / L W</code></td></tr><tr><td align=&quot;left&quot;><code>Month</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>1-12 or JAN-DEC</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>, - * /</code></td></tr><tr><td align=&quot;left&quot;><code>Day-of-Week</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>1-7 or SUN-SAT</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>, - * ? / L #</code></td></tr><tr><td align=&quot;left&quot;><code>Year (Optional)</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>empty, 1970-2099</code></td><td align=&quot;left&quot;>&nbsp;</th><td align=&quot;left&quot;><code>, - * /</code></td></tr></table><br><br>The * character is used to specify all values. For example, * in the minute field means every minute.<br><br>The ? character is allowed for the day-of-month and day-of-week fields. It is used to specify no specific value.This is useful when you need to specify something in one of the two fields, but not the other.<br><br>The - character is used to specify ranges For example &quot;10-12&quot; in the hour field means &quot;the hours 10,11 and 12&quot;.<br><br>The , character is used to specify additional values. For example &quot;MON,WED,FRI&quot; in the day-of-week field means &quot;the days Monday, Wednesday, and Friday&quot;.<br><br>The / character is used to specify increments. For example &quot;0/15&quot; in the seconds field means &quot;the seconds 0, 15, 30, and 45&quot;. And &quot;5/15&quot; in the seconds field means &quot;the seconds 5, 20, 35, and 50&quot;. Specifying * before the / is equivalent to specifying 0 is the value to start with. Essentially, for each field in the expression, there is a set of numbers that can be turned on or off. For seconds and minutes, the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to 31, and for months 1 to 12. The &quot;/&quot; character simply helps you turn on every &quot;nth&quot; value in the given set. Thus &quot;7/6&quot; in the month field only turns on month &quot;7&quot;, it does NOT mean every 6th month, please note that subtlety.<br><br>The L character is allowed for the day-of-month and day-of-week fields. This character is short-hand for &quot;last&quot;, but it has different meaning in each of the two fields. For example, the value &quot;L&quot; in the day-of-month field means &quot;the last day of the month&quot; - day 31 for January, day 28 for February on non-leap years. If used in the day-of-week field by itself, it simply means &quot;7&quot; or &quot;SAT&quot;. But if used in the day-of-week field after another value, it means &quot;the last xxx day of the month&quot; - for example &quot;6L&quot; means &quot;the last friday of the month&quot;. When using the L option, it is important not to specify lists, or ranges of values, as you will get confusing results.<br><br>The W character is allowed for the day-of-month field. This character is used to specify the weekday (Monday-Friday) nearest the given day. As an example, if you were to specify &quot;15W&quot; as the value for the day-of-month field, the meaning is: &quot;the nearest weekday to the 15th of the month&quot;. So if the 15th is a Saturday, the trigger will fire on Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th. However if you specify &quot;1W&quot; as the value for day-of-month, and the 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not jump over the boundary of a months days. The W character can only be specified when the day-of-month is a single day, not a range or list of days.<br><br>The L and W characters can also be combined for the day-of-month expression to yield LW, which translates to &quot;last weekday of the month&quot;.<br><br>The # character is allowed for the day-of-week field. This character is used to specify &quot;the nth&quot; XXX day of the month. For example, the value of &quot;6#3&quot; in the day-of-week field means the third Friday of the month (day 6 = Friday and &quot;#3&quot; = the 3rd one in the month). Other examples: &quot;2#1&quot; = the first Monday of the month and &quot;4#5&quot; = the fifth Wednesday of the month. Note that if you specify &quot;#5&quot; and there is not 5 of the given day-of-week in the month, then no firing will occur that month.<br><br>The legal characters and the names of months and days of the week are not case sensitive.<br><br><b>NOTES:</b><ul><li>Support for specifying both a day-of-week and a day-of-month value is not complete (you will need to use the ? character in one of these fields). </li><li>Overflowing ranges is supported - that is, having a larger number on the left hand side than the right. You might do 22-2 to catch 10 o clock at night until 2 o clock in the morning, or you might have NOV-FEB. It is very important to note that overuse of overflowing ranges creates ranges that do not make sense and no effort has been made to determine which interpretation CronExpression chooses. An example would be 0 0 14-6 ? * FRI-MON. </li></ul></p>'
  }
  
};

})();