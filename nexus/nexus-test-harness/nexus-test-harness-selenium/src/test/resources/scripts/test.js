/*
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
selenium.browserbot.getCurrentWindow().Ext.lib.Ajax.request = function(method, uri, cb, data, options) 
{
   
    LOG.info('Overriden request' );
    
    
            var responseObject = {};
            var headerObj = {};

			var responseText = "\"{\"data\":[{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/admin\",\"id\":\"admin\",\"name\":\"Nexus Administrator Role\",\"description\":\"Administration role for Nexus\",\"sessionTimeout\":30,\"privileges\":[\"1000\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/deployment\",\"id\":\"deployment\",\"name\":\"Nexus Deployment Role\",\"description\":\"Deployment role for Nexus\",\"sessionTimeout\":60,\"roles\":[\"anonymous\"],\"privileges\":[\"T1\",\"T2\",\"T3\",\"T4\",\"T5\",\"T6\",\"T7\",\"T8\",\"64\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/anonymous\",\"id\":\"anonymous\",\"name\":\"Nexus Anonymous Role\",\"description\":\"Anonymous role for Nexus\",\"sessionTimeout\":60,\"privileges\":[\"1\",\"6\",\"14\",\"17\",\"19\",\"44\",\"54\",\"55\",\"56\",\"57\",\"58\",\"64\",\"T1\",\"T2\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/developer\",\"id\":\"developer\",\"name\":\"Nexus Developer Role\",\"description\":\"Developer role for Nexus\",\"sessionTimeout\":30,\"roles\":[\"anonymous\",\"deployment\"],\"privileges\":[\"2\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-search\",\"id\":\"ui-search\",\"name\":\"UI: Search\",\"description\":\"Gives access to the Search screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"17\",\"19\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-repo-browser\",\"id\":\"ui-repo-browser\",\"name\":\"UI: Repository Browser\",\"description\":\"Gives access to the Repository Browser screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"6\",\"14\",\"55\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-system-feeds\",\"id\":\"ui-system-feeds\",\"name\":\"UI: System Feeds\",\"description\":\"Gives access to the System Feeds screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"44\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-logs-config-files\",\"id\":\"ui-logs-config-files\",\"name\":\"UI: Logs and Config Files\",\"description\":\"Gives access to the Logs and Config Files screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"42\",\"43\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-server-admin\",\"id\":\"ui-server-admin\",\"name\":\"UI: Server Administration\",\"description\":\"Gives access to the Server Administration screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"3\",\"4\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-repository-admin\",\"id\":\"ui-repository-admin\",\"name\":\"UI: Repository Administration\",\"description\":\"Gives access to the Repository Administration screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"5\",\"6\",\"7\",\"8\",\"10\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-group-admin\",\"id\":\"ui-group-admin\",\"name\":\"UI: Group Administration\",\"description\":\"Gives access to the Group Administration screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"6\",\"13\",\"14\",\"15\",\"16\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-routing-admin\",\"id\":\"ui-routing-admin\",\"name\":\"UI: Routing Administration\",\"description\":\"Gives access to the Routing Administration screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"6\",\"14\",\"22\",\"23\",\"24\",\"25\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-scheduled-tasks-admin\",\"id\":\"ui-scheduled-tasks-admin\",\"name\":\"UI: Scheduled Task Administration\",\"description\":\"Gives access to the Scheduled Task Administration screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"6\",\"14\",\"26\",\"27\",\"28\",\"29\",\"69\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-repository-targets-admin\",\"id\":\"ui-repository-targets-admin\",\"name\":\"UI: Repository Target Administration\",\"description\":\"Gives access to the Repository Target Administration screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"45\",\"46\",\"47\",\"48\",\"56\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-users-admin\",\"id\":\"ui-users-admin\",\"name\":\"UI: User Administration\",\"description\":\"Gives access to the User Administration screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"35\",\"38\",\"39\",\"40\",\"41\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-roles-admin\",\"id\":\"ui-roles-admin\",\"name\":\"UI: Role Administration\",\"description\":\"Gives access to the Role Administration screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"31\",\"34\",\"35\",\"36\",\"37\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-privileges-admin\",\"id\":\"ui-privileges-admin\",\"name\":\"UI: Privilege Administration\",\"description\":\"Gives access to the Privilege Administration screen in Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"6\",\"14\",\"30\",\"31\",\"32\",\"33\",\"46\"]},{\"resourceURI\":\"http://localhost:8081/nexus/service/local/roles/ui-basic\",\"id\":\"ui-basic\",\"name\":\"UI: Base UI Privileges\",\"description\":\"Generic privileges for users in the Nexus UI\",\"sessionTimeout\":60,\"privileges\":[\"1\",\"2\",\"64\"]}]}\"";
            try
            {
                var headerStr = "Content-Type: application/json; charset=UTF-8\nContent-Length: 4806\nLast-Modified: Wed, 27 Aug 2008 17:07:44 GMT\nDate: Thu, 28 Aug 2008 17:18:53 GMT\nServer: Noelios-Restlet-Engine/1.0..10\nVary: Accept-Charset, Accept-Encoding, Accept-Language, Accept\n";
                var header = headerStr.split('\n');
                for (var i = 0; i < header.length; i++) {
                    var delimitPos = header[i].indexOf(':');
                    if (delimitPos != -1) {
                        headerObj[header[i].substring(0, delimitPos)] = header[i].substring(delimitPos + 2);
                    }
                }
            }
            catch(e) {
            }

LOG.info('line 25' );
            responseObject.tId = 3;
            responseObject.status = 200;
            responseObject.statusText = "OK";
            responseObject.getResponseHeader = headerObj;
            responseObject.getAllResponseHeaders = headerStr;
            responseObject.responseText = responseText;
            responseObject.responseXML = null;
            
            
LOG.info('line 35' );

            //if (typeof callbackArg !== undefined) 
            //{
            //    responseObject.argument = callbackArg;
            //}
LOG.info('line 41: '+  cb.success);            
            if (cb.success) 
            {
            LOG.info('line 44' );
                    if (!cb.scope) 
                    {
                    LOG.info('line 47' );
                        cb.success(responseObject);
                    }
                    else 
                    {
                    LOG.info('line 52: '+ cb.success.apply );
                    try{
                        cb.success.apply(cb.scope, [responseObject]);
                        }
                        catch(e)
                        {
                        LOG.error('error: '+ e.description);
                        }
                    }
            }
  LOG.info('end ' );
  

}