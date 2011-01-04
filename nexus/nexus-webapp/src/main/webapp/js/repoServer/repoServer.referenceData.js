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
/*
 * /js/repoServer/repoServer.referenceData.js Reference service data objects
 * used for reading form data and compiling resource state objects to send back
 * to the service.
 */

(function() {

  Sonatype.repoServer.referenceData = {
    globalSettingsState : {
      securityAnonymousUsername : "",
      securityAnonymousPassword : "",
      securityEnabled : false,
      securityAnonymousAccessEnabled : false,
      securityRealms : [],
      smtpSettings : {
        host : "",
        port : 25,
        username : "",
        password : "",
        systemEmailAddress : "",
        sslEnabled : "",
        tlsEnabled : ""
      },
      errorReportingSettings : {
        jiraUsername : "",
        jiraPassword : "",
        reportErrorsAutomatically : false
      },
      globalConnectionSettings : {
        connectionTimeout : 0,
        retrievalRetryCount : 0,
        queryString : "",
        userAgentString : ""
      },
      globalHttpProxySettings : {
        proxyHostname : "",
        proxyPort : 0,
        nonProxyHosts : [],
        authentication : {
          username : "",
          password : "",
          ntlmHost : "",
          ntlmDomain : ""
        }
      },
      globalRestApiSettings : {
        baseUrl : "",
        forceBaseUrl : false,
        uiTimeout : 30000
      },
      systemNotificationSettings : {
        enabled : false,
        emailAddresses : "",
        roles : []
      }
    },

    repositoryState : {
      virtual : {
        repoType : "",
        id : "",
        name : "",
        shadowOf : "",
        provider : "",
        providerRole : "",
        syncAtStartup : false,
        exposed : true
      },

      hosted : {
        repoType : "",
        id : "",
        name : "",
        writePolicy : "ALLOW_WRITE_ONCE",
        browseable : true,
        indexable : true,
        exposed : true,
        notFoundCacheTTL : 0,
        repoPolicy : "",
        provider : "",
        providerRole : "",
        overrideLocalStorageUrl : "",
        defaultLocalStorageUrl : "",
        downloadRemoteIndexes : true,
        checksumPolicy : ""
      },

      proxy : {
        repoType : "",
        id : "",
        name : "",
        browseable : true,
        indexable : true,
        notFoundCacheTTL : 0,
        artifactMaxAge : 0,
        metadataMaxAge : 0,
        repoPolicy : "",
        provider : "",
        providerRole : "",
        overrideLocalStorageUrl : "",
        defaultLocalStorageUrl : "",
        downloadRemoteIndexes : true,
        autoBlockActive : true,
        fileTypeValidation : false,
        exposed : true,
        checksumPolicy : "",
        remoteStorage : {
          remoteStorageUrl : "",
          authentication : {
            username : "",
            password : "",
            ntlmHost : "",
            ntlmDomain : ""
          },
          connectionSettings : {
            connectionTimeout : 0,
            retrievalRetryCount : 0,
            queryString : "",
            userAgentString : ""
          },
          httpProxySettings : {
            proxyHostname : "",
            proxyPort : 0,
            authentication : {
              username : "",
              password : "",
              ntlmHost : "",
              ntlmDomain : ""
            }
          }
        }
      } // end repositoryProxyState
    },

    group : {
      id : "",
      name : "",
      format : "",
      exposed : "",
      provider : "",
      repositories : []
      // note: internal record structure is the responsibility of data modifier
      // func
      // {
      // id:"central",
      // name:"Maven Central",
      // resourceURI:".../repositories/repoId" // added URI to be able to reach
      // repo
      // }
    },

    route : {
      id : "",
      ruleType : "",
      groupId : "",
      pattern : "",
      repositories : []
      // @todo: there's a discrepancy between routes list and state
      // representation of
      // the repo data inside routes data
    },

    schedule : {
      manual : {
        id : "",
        name : "",
        enabled : "",
        typeId : "",
        alertEmail : "",
        schedule : "",
        properties : [{
              id : "",
              value : ""
            }]
      },
      once : {
        id : "",
        name : "",
        enabled : "",
        typeId : "",
        alertEmail : "",
        schedule : "",
        properties : [{
              id : "",
              value : ""
            }],
        startDate : "",
        startTime : ""
      },
      hourly : {
        id : "",
        name : "",
        enabled : "",
        typeId : "",
        alertEmail : "",
        schedule : "",
        properties : [{
              id : "",
              value : ""
            }],
        startDate : "",
        startTime : ""
      },
      daily : {
        id : "",
        name : "",
        enabled : "",
        typeId : "",
        alertEmail : "",
        schedule : "",
        properties : [{
              id : "",
              value : ""
            }],
        startDate : "",
        recurringTime : ""
      },
      weekly : {
        id : "",
        name : "",
        enabled : "",
        typeId : "",
        alertEmail : "",
        schedule : "",
        properties : [{
              id : "",
              value : ""
            }],
        startDate : "",
        recurringTime : "",
        recurringDay : []
      },
      monthly : {
        id : "",
        name : "",
        enabled : "",
        typeId : "",
        alertEmail : "",
        schedule : "",
        properties : [{
              id : "",
              value : ""
            }],
        startDate : "",
        recurringTime : "",
        recurringDay : []
      },
      advanced : {
        id : "",
        name : "",
        enabled : "",
        typeId : "",
        alertEmail : "",
        schedule : "",
        properties : [{
              id : "",
              value : ""
            }],
        cronCommand : ""
      }
    },

    upload : {
      r : "",
      g : "",
      a : "",
      v : "",
      p : "",
      c : "",
      e : ""
    },

    users : {
      userId : "",
      firstName : "",
      lastName : "",
      email : "",
      status : "",
      roles : []
    },

    userNew : {
      userId : "",
      firstName : "",
      lastName : "",
      email : "",
      status : "",
      password : "",
      roles : []
    },

    roles : {
      id : "",
      name : "",
      description : "",
      sessionTimeout : 0,
      roles : [],
      privileges : []
    },

    privileges : {
      target : {
        name : "",
        description : "",
        type : "",
        repositoryTargetId : "",
        repositoryId : "",
        repositoryGroupId : "",
        method : []
      }
    },

    repoTargets : {
      id : "",
      name : "",
      contentClass : "",
      patterns : []
    },

    contentClasses : {
      contentClass : "",
      name : ""
    },

    logConfig : {
      rootLoggerLevel : "",
      rootLoggerAppenders : "",
      fileAppenderLocation : "",
      fileAppenderPattern : ""
    },

    repoMirrors : [{
          id : "",
          url : ""
        }]

  };

})();
