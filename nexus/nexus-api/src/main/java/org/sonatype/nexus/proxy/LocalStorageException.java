/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy;

/**
 * Generic local storage exception thrown by given storage implementation (more special than generic IOExceptions), and
 * so. Denotes a (probably) unrecoverable, serious system and/or IO error that needs some Core action to manage it.
 * 
 * @author cstamas
 */
public class LocalStorageException
    extends StorageException
{
    private static final long serialVersionUID = -5815995204584266723L;

    public LocalStorageException( String msg )
    {
        super( msg );
    }

    public LocalStorageException( String msg, Throwable cause )
    {
        super( msg, cause );
    }

    public LocalStorageException( Throwable cause )
    {
        super( cause.getMessage(), cause );
    }
}
