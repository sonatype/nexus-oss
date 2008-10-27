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
package org.sonatype.nexus.proxy.registry;

/**
 * Thrown when invalid grouping is tried: for example grouping of repositories without same content class.
 * 
 * @author cstamas
 */
public class InvalidGroupingException
    extends Exception
{
    private static final long serialVersionUID = -738329028288324297L;

    public InvalidGroupingException( ContentClass c1, ContentClass c2 )
    {
        super( "The content classes are not groupable! '" + c1.getId() + "' and '" + c2.getId()
            + "' are not compatible!" );
    }
}
