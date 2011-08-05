/**
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
package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.AbstractServerType;
import org.sonatype.nexus.ext.gwt.ui.client.ApplicationContext;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunction;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionGroup;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;
import org.sonatype.nexus.ext.gwt.ui.client.reposerver.model.AuthenticationClientPermissions.Permissions;

public class RepoServer extends AbstractServerType {

    public void init() {
        ServerInstance local = new RepoServerInstance(this);
        local.setId("local");
        local.setName("Local");
        addInstance(local);
    }

    public List<ServerFunctionGroup> getFunctionGroups() {
        final ApplicationContext ctx = ApplicationContext.instance();

        List<ServerFunctionGroup> groups = new ArrayList<ServerFunctionGroup>();

        ServerFunctionGroup views = new ServerFunctionGroup(){
            {
                setName("Views");
                if (ctx.checkPermission("viewSearch", Permissions.READ)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Artifact Search");
                            setTabName("Search");
                            setPanel(new EmptyPage());
                        }
                    });
                }
                if (ctx.checkPermission("maintRepos", Permissions.READ) &&
                	!ctx.checkPermission("maintRepos", Permissions.EDIT)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Browse Repositories");
                            setTabName("Repositories");
                            setPanel(new RepoMaintenancePage());
                        }
                    });
                }
                if (ctx.checkPermission("viewUpdatedArtifacts", Permissions.READ)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Recently Updated Artifacts");
                            setTabName("Updated Artifacts");
                            setPanel(new EmptyPage());
                        }
                    });
                }
                if (ctx.checkPermission("viewCachedArtifacts", Permissions.READ)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Recently Cached Artifacts");
                            setTabName("Cached Artifacts");
                            setPanel(new EmptyPage());
                        }
                    });
                }
                if (ctx.checkPermission("viewDeployedArtifacts", Permissions.READ)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Recently Deployed Artifacts");
                            setTabName("Deployed Artifacts");
                            setPanel(new EmptyPage());
                        }
                    });
                }
                if (ctx.checkPermission("viewSystemChanges", Permissions.READ)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("System Changes");
                            setTabName("System Changes");
                            setPanel(new EmptyPage());
                        }
                    });
                }
            }
        };
        if (views.countFunctions() > 0) {
            groups.add(views);
        }

        ServerFunctionGroup maintenance = new ServerFunctionGroup(){
            {
                setName("Maintenance");
                if (ctx.checkPermission("maintRepos", Permissions.EDIT)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Repositories");
                            setTabName("Maintenance");
                            setPanel(new RepoMaintenancePage());
                        }
                    });
                }
                if (ctx.checkPermission("maintConfig", Permissions.READ)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("View Server Config");
                            setTabName("Config");
                            setPanel(new EmptyPage());
                        }
                    });
                }
                if (ctx.checkPermission("maintLogs", Permissions.READ)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("View Server Logs");
                            setTabName("Logs");
                            setPanel(new EmptyPage());
                        }
                    });
                }
            }
        };
        if (maintenance.countFunctions() > 0) {
            groups.add(maintenance);
        }
            
        ServerFunctionGroup configuration = new ServerFunctionGroup(){
            {
                setName("Configuration");
                if (ctx.checkPermission("configServer", Permissions.EDIT)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Server");
                            setTabName("Nexus");
                            setPanel(new EmptyPage());
                        }
                    });
                }
                if (ctx.checkPermission("configRepos", Permissions.EDIT)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Repositories");
                            setTabName("Repositories");
                            setPanel(new EmptyPage());
                        }
                    });
                }
                if (ctx.checkPermission("configGroups", Permissions.EDIT)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Groups");
                            setTabName("Groups");
                            setPanel(new EmptyPage());
                        }
                    });
                }
                if (ctx.checkPermission("configRules", Permissions.EDIT)) {
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Routing");
                            setTabName("Routing");
                            setPanel(new EmptyPage());
                        }
                    });
                }
            }
        };
        if (configuration.countFunctions() > 0) {
            groups.add(configuration);
        }

        return groups;
    }

    public String getName() {
        return "Nexus";
    }

    public String getServicePath() {
        return "/nexus/service";
    }
    
}
