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
package org.sonatype.nexus.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.model.CProps;

/**
 * A simple CProps to Map converter, to ease handling of CProps.
 * 
 * @author cstamas
 */
public class ModelloUtils
{
    @SuppressWarnings( "unchecked" )
    public static Map<String, String> getMapFromConfigList( List list )
    {
        Map<String, String> result = new HashMap<String, String>( list.size() );

        for ( Object obj : list )
        {
            CProps props = (CProps) obj;
            result.put( props.getKey(), props.getValue() );
        }

        return result;
    }
}
