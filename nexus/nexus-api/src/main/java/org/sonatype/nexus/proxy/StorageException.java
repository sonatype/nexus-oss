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

import java.io.IOException;

/**
 * Generic storage exception thrown by given storage implementation (like IOExceptions), and so. Denotes a (probably)
 * unrecoverable, serious system and/or IO error.
 * 
 * @author cstamas
 */
public class StorageException
    extends IOException
{
    private static final long serialVersionUID = -7119754988039787918L;

    public StorageException( String msg )
    {
        super( msg );
    }

    public StorageException( String msg, Throwable cause )
    {
        super( msg );

        initCause( cause );
    }

    public StorageException( Throwable cause )
    {
        super( "A storage exception occured!" );

        initCause( cause );
    }
}
