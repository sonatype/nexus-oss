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

import java.text.ParseException;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;

/**
 * adds new snapshot to metadata
 * 
 * @author Oleg Gusakov
 * @version $Id: SetSnapshotOperation.java 743040 2009-02-10 18:20:26Z ogusakov $
 */
public class SetSnapshotOperation
    implements MetadataOperation
{

    private SnapshotOperand operand;

    /**
     * @throws MetadataException
     */
    public SetSnapshotOperation( SnapshotOperand data )
        throws MetadataException
    {
        setOperand( data );
    }

    public void setOperand( AbstractOperand data )
        throws MetadataException
    {
        if ( data == null || !( data instanceof SnapshotOperand ) )
        {
            throw new MetadataException( "Operand is not correct: expected SnapshotOperand, but got "
                + ( data == null ? "null" : data.getClass().getName() ) );
        }
        this.operand = (SnapshotOperand) data;
    }

    /**
     * add/replace snapshot to the in-memory metadata instance
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

        Versioning vs = metadata.getVersioning();

        if ( vs == null )
        {
            vs = new Versioning();

            metadata.setVersioning( vs );
        }

        return updateSnapshot( vs );
    }

    private boolean updateSnapshot( Versioning vs )
        throws MetadataException
    {
        if ( operand.getSnapshot() != null )
        {
            vs.setSnapshot( operand.getSnapshot() );
        }

        vs.setLastUpdated( TimeUtil.getUTCTimestamp() );

        List<SnapshotVersion> extras = operand.getSnapshotVersions();
        List<SnapshotVersion> currents = vs.getSnapshotVersions();
        for ( SnapshotVersion extra : extras )
        {
            SnapshotVersion current = MetadataUtil.searchForEquivalent( extra, currents );
            if ( current == null )
            {
                currents.add( extra );
            }
            else
            {
                try
                {
                    if ( TimeUtil.compare( current.getUpdated(), extra.getUpdated() ) < 0 )
                    {
                        currents.remove( current );
                        currents.add( extra );
                    }
                }
                catch ( ParseException e )
                {
                    throw new MetadataException(
                        "Invalid timetamp: " + current.getUpdated() + "-" + extra.getUpdated(), e );
                }
            }
        }

        return true;
    }

}
