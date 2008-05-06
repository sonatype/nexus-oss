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
package org.sonatype.nexus.proxy.access;

/**
 * Repository permissions. Thanks Andy!
 * 
 * @author cstamas
 */
public class RepositoryPermission
{

    /** The Constant RETRIEVE. */
    public static final RepositoryPermission RETRIEVE = new RepositoryPermission( "proxy.retrieve" );

    /** The Constant DELETE. */
    public static final RepositoryPermission DELETE = new RepositoryPermission( "proxy.delete" );

    /** The Constant STORE. */
    public static final RepositoryPermission STORE = new RepositoryPermission( "proxy.store" );

    /** The Constant LIST. */
    public static final RepositoryPermission LIST = new RepositoryPermission( "proxy.list" );

    /** The perm. */
    private String perm;

    /**
     * Instantiates a new repository permission.
     * 
     * @param perm the perm
     */
    public RepositoryPermission( String perm )
    {
        super();
        
        this.perm = perm;
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId()
    {
        return perm;
    }

    public String toString()
    {
        return perm;
    }

    public boolean equals( Object compare )
    {
        if ( !( compare instanceof RepositoryPermission ) )
        {
            return false;
        }

        return perm == null ? false : perm.equals( ( (RepositoryPermission) compare ).getId() );
    }

}
