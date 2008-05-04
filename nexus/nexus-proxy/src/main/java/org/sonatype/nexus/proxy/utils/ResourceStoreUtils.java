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
package org.sonatype.nexus.proxy.utils;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ResourceStore;

/**
 * Simple utils regarding stores.
 * 
 * @author cstamas
 */
public class ResourceStoreUtils
{

    /**
     * A simple utility to "format" a list of stores for logging or other output.
     * 
     * @param stores
     * @return
     */
    public static String getResourceStoreListAsString( List<? extends ResourceStore> stores )
    {
        if ( stores == null )
        {
            return "[]";
        }

        ArrayList<String> repoIdList = new ArrayList<String>( stores.size() );

        for ( ResourceStore store : stores )
        {
            repoIdList.add( store.getId() );
        }

        return StringUtils.join( repoIdList.iterator(), ", " );
    }

}
