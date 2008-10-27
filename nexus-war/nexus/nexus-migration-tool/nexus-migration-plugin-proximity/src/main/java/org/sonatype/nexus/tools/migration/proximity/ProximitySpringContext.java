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
package org.sonatype.nexus.tools.migration.proximity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ProximitySpringContext
{

    private Xpp3Dom proximityBean;

    private Map<String, Xpp3Dom> repositoryBeans;

    private Map<String, Xpp3Dom> localStorageBeans;

    private Map<String, Xpp3Dom> remoteStorageBeans;

    private Map<String, Xpp3Dom> repositoryLogicBeans;

    private Xpp3Dom groupRequestMapperBean;

    private Map<String, List<String>> repositoryGroups;;

    public Xpp3Dom getProximityBean()
    {
        return proximityBean;
    }

    public void setProximityBean( Xpp3Dom proximityBean )
    {
        this.proximityBean = proximityBean;
    }

    public Map<String, Xpp3Dom> getRepositoryBeans()
    {
        if (repositoryBeans == null) {
            repositoryBeans = new HashMap<String, Xpp3Dom>();
        }
        return repositoryBeans;
    }

    public void setRepositoryBeans( Map<String, Xpp3Dom> repositoryBeans )
    {
        this.repositoryBeans = repositoryBeans;
    }

    public Map<String, Xpp3Dom> getLocalStorageBeans()
    {
        if (localStorageBeans == null) {
            localStorageBeans = new HashMap<String, Xpp3Dom>();
        }
        return localStorageBeans;
    }

    public void setLocalStorageBeans( Map<String, Xpp3Dom> localStorageBeans )
    {
        this.localStorageBeans = localStorageBeans;
    }

    public Map<String, Xpp3Dom> getRemoteStorageBeans()
    {
        if (remoteStorageBeans == null) {
            remoteStorageBeans = new HashMap<String, Xpp3Dom>();
        }
        return remoteStorageBeans;
    }

    public void setRemoteStorageBeans( Map<String, Xpp3Dom> remoteStorageBeans )
    {
        this.remoteStorageBeans = remoteStorageBeans;
    }

    public Map<String, Xpp3Dom> getRepositoryLogicBeans()
    {
        if (repositoryLogicBeans == null) {
            repositoryLogicBeans = new HashMap<String, Xpp3Dom>();
        }
        return repositoryLogicBeans;
    }

    public void setRepositoryLogicBeans( Map<String, Xpp3Dom> repositoryLogicBeans )
    {
        this.repositoryLogicBeans = repositoryLogicBeans;
    }

    public Xpp3Dom getGroupRequestMapperBean()
    {
        return groupRequestMapperBean;
    }

    public void setGroupRequestMapperBean( Xpp3Dom groupRequestMapperBean )
    {
        this.groupRequestMapperBean = groupRequestMapperBean;
    }

    public Map<String, List<String>> getRepositoryGroups()
    {
        if (repositoryGroups == null) {
            repositoryGroups = new HashMap<String, List<String>>();
        }
        return repositoryGroups;
    }

    public void setRepositoryGroups( Map<String, List<String>> repositoryGroups )
    {
        this.repositoryGroups = repositoryGroups;
    }

}
