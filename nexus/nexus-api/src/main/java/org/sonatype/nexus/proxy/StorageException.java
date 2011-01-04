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

import java.io.IOException;

/**
 * Generic storage exception thrown by given storage implementation (like IOExceptions), and so. Denotes a (probably)
 * unrecoverable, serious system and/or IO error. <b>This class is deprecated, and will be removed in future
 * releases!</b> The StorageException was used in more then half of cases to "wrap" an IOException and that did not make
 * any sense. IOException will replace the StorageException usage, but internally, two descendants of IOExceptions,
 * LocalStorageException and RemoteStorageException should be used to "fine tune" Nexus Core behavior.
 * 
 * @author cstamas
 * @deprecated Use {@link LocalStorageException} or {@link RemoteStorageException} respectively.
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
