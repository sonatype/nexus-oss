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
package org.sonatype.nexus.proxy.item.uid;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * Attribute yielding "true" for paths (and it's subpaths) belonging -- or referred as -- "not subject of group merge"
 * or simply "group local" content. Used by Group repository implementations, to decide should process member
 * repositories too, or just group's locol storage. Logically equivalent to
 * {@link ResourceStoreRequest#isRequestGroupLocalOnly()}, but while that allows per-request control, this one as
 * attribute allows programmatical control over it too.
 * 
 * @author cstamas
 */
public class IsGroupLocalOnlyAttribute
    implements Attribute<Boolean>
{
    public Boolean getValueFor( RepositoryItemUid subject )
    {
        // stuff being group-local
        // /.meta
        // /.index
        // /.nexus
        // we are specific about these for a good reason (see future)

        if ( subject.getPath() != null )
        {
            return subject.getPath().startsWith( "/.meta" ) || subject.getPath().startsWith( "/.index" )
                || subject.getPath().startsWith( "/.nexus" );
        }
        else
        {
            return false;
        }
    }
}
