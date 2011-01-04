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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * Thrown when invalid grouping is tried: for example grouping of repositories without same content class.
 * 
 * @author cstamas
 */
public class InvalidGroupingException
    extends ConfigurationException
{
    private static final long serialVersionUID = -738329028288324297L;

    public InvalidGroupingException( ContentClass c1, ContentClass c2 )
    {
        super( "The content classes are not groupable! '" + c1.getId() + "' and '" + c2.getId()
            + "' are not compatible!" );
    }

    public InvalidGroupingException( ContentClass c1 )
    {
        super( "There is no repository group implementation that supports this content class '" + c1.getId() + "'!" );
    }

    public InvalidGroupingException( String id, String path )
    {
        super( "The group '" + id + "' has a cyclic reference! Path to the cyclic reference: '" + path + "'." );
    }
}
