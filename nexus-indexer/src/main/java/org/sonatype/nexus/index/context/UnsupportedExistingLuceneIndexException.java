/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.index.context;

/**
 * Thrown when a user tries to create a NexusInder IndexingContext over and existing Lucene index. The reason for
 * throwing this exception may be multiple: non-NexusIndexer Lucene index, index version is wrong, repositoryId does not
 * matches the context repositoryId, etc.
 * 
 * @author cstamas
 */
public class UnsupportedExistingLuceneIndexException
    extends Exception
{
    private static final long serialVersionUID = -3206758653346308322L;

    public UnsupportedExistingLuceneIndexException( String message )
    {
        super( message );
    }

}
