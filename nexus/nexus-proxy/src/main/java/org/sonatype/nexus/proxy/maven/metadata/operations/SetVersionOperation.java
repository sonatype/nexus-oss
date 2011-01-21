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
package org.sonatype.nexus.proxy.maven.metadata.operations;

import org.apache.maven.artifact.repository.metadata.Metadata;

/**
 * adds version to metadata
 * 
 * @author Oleg Gusakov
 * @version $Id: SetVersionOperation.java 726701 2008-12-15 14:31:34Z hboutemy $
 */
public class SetVersionOperation
    implements MetadataOperation
{
    private String version;

    /**
     * @throws MetadataException
     */
    public SetVersionOperation( StringOperand data )
        throws MetadataException
    {
        setOperand( data );
    }

    public void setOperand( AbstractOperand data )
        throws MetadataException
    {
        if ( data == null || !( data instanceof StringOperand ) )
        {
            throw new MetadataException( "Operand is not correct: expected SnapshotOperand, but got "
                + ( data == null ? "null" : data.getClass().getName() ) );
        }

        version = ( (StringOperand) data ).getOperand();
    }

    /**
     * add version to the in-memory metadata instance
     * 
     * @param metadata
     * @return
     * @throws MetadataException
     */
    public boolean perform( Metadata metadata )
        throws MetadataException
    {
        if ( metadata == null )
        {
            return false;
        }

        String vs = metadata.getVersion();

        if ( vs == null )
        {
            if ( version == null )
            {
                return false;
            }
        }
        else if ( vs.equals( version ) )
        {
            return false;
        }

        metadata.setVersion( version );

        return true;
    }

}
