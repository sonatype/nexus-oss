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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.configuration.model.Configuration;

public enum LocalStatus
{

    IN_SERVICE,

    OUT_OF_SERVICE;

    public boolean shouldServiceRequest()
    {
        return IN_SERVICE.equals( this );
    }

    public static LocalStatus fromModel( String string )
    {
        if ( Configuration.LOCAL_STATUS_IN_SERVICE.equals( string ) )
        {
            return IN_SERVICE;
        }
        else if ( Configuration.LOCAL_STATUS_OUT_OF_SERVICE.equals( string ) )
        {
            return OUT_OF_SERVICE;
        }
        else
        {
            return null;
        }
    }

    public static String toModel( LocalStatus localStatus )
    {
        return localStatus.toString();
    }

    public String toString()
    {
        if ( IN_SERVICE.equals( this ) )
        {
            return Configuration.LOCAL_STATUS_IN_SERVICE;
        }
        else if ( OUT_OF_SERVICE.equals( this ) )
        {
            return Configuration.LOCAL_STATUS_OUT_OF_SERVICE;
        }
        else
        {
            return null;
        }
    }

}
