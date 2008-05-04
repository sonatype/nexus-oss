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
package org.sonatype.nexus.proxy;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * Thrown when a request is denied for security reasons.
 * 
 * @author cstamas
 */
public class AccessDeniedException
    extends Exception
{

    private static final long serialVersionUID = 8341250956517740603L;

    private RepositoryItemUid itemUid;

    public AccessDeniedException( RepositoryItemUid request, String msg )
    {
        super( msg );
        this.itemUid = request;
    }

    /**
     * The RepositoryItemUid that is forbidden to access.
     * 
     * @return
     */
    public RepositoryItemUid getRepositoryItemUid()
    {
        return this.itemUid;
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append( "Access " );
        str.append( "to resource " );
        str.append( itemUid.toString() );
        str.append( " has been forbidden because:" );
        str.append( super.getMessage() );
        return str.toString();
    }

}
