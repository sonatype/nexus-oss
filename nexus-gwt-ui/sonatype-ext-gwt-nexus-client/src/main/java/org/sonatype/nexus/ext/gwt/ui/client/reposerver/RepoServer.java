package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.ext.gwt.ui.client.AbstractServerType;
import org.sonatype.nexus.ext.gwt.ui.client.ApplicationContext;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunction;
import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionGroup;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;

public class RepoServer extends AbstractServerType {

    public void init() {
        ServerInstance local = new RepoServerInstance(this);
        local.setId("local");
        local.setName("Local");
        addInstance(local);
    }

    public List<ServerFunctionGroup> getFunctionGroups() {
        List<ServerFunctionGroup> groups = new ArrayList<ServerFunctionGroup>();
        if (ApplicationContext.instance().isUserLoggedIn()) {
            ServerFunctionGroup viewsGroup = new ServerFunctionGroup(){
                {
                    setName("Views");
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Artifact Search");
                            setTabName("Search");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Recently Cached Artifacts");
                            setTabName("Cached Artifacts");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Recently Deployed Artifacts");
                            setTabName("Deployed Artifacts");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("System Cahnges");
                            setTabName("System Cahnges");
                            setPanel(new EmptyPage());
                        }
                    });
                }
            };
            
            ServerFunctionGroup maintenance = new ServerFunctionGroup(){
                {
                    setName("Maintenance");
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Repositories");
                            setTabName("Maintenance");
                            setPanel(new RepoMaintenancePage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("View Server Config");
                            setTabName("Config");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("View Server Logs");
                            setTabName("Logs");
                            setPanel(new EmptyPage());
                        }
                    });
                }
            };
            
            ServerFunctionGroup configuration = new ServerFunctionGroup(){
                {
                    setName("Configuration");
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Server");
                            setTabName("Nexus");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Repositories");
                            setTabName("Repositories");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Groups");
                            setTabName("Groups");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Routing");
                            setTabName("Routing");
                            setPanel(new EmptyPage());
                        }
                    });
                }
            };
            
            groups.add(viewsGroup);
            groups.add(maintenance);
            groups.add(configuration);
        } else {
            ServerFunctionGroup viewsGroup = new ServerFunctionGroup(){
                {
                    setName("Views");
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Artifact Search");
                            setTabName("Search");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Browse Repositories");
                            setTabName("Repositories");
                            setPanel(new RepoMaintenancePage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Artifact Search");
                            setTabName("Search");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Recently Cached Artifacts");
                            setTabName("Cached Artifacts");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("Recently Deployed Artifacts");
                            setTabName("Deployed Artifacts");
                            setPanel(new EmptyPage());
                        }
                    });
                    addFunction(new ServerFunction() {
                        {
                            setMenuName("System Cahnges");
                            setTabName("System Cahnges");
                            setPanel(new EmptyPage());
                        }
                    });
                }
            };
            
            groups.add(viewsGroup);
        }
        
        return groups;
    }

    public String getName() {
        return "Nexus";
    }

    public String getPath() {
        return "/nexus/service";
    }
    
}
