/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.text.ParseException;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.StringUtils;

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
            SnapshotVersion current = getCurrent( extra, currents );
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

    private SnapshotVersion getCurrent( SnapshotVersion exVersion, List<SnapshotVersion> current )
    {
        for ( SnapshotVersion curVersion : current )
        {
            if ( StringUtils.equals( exVersion.getClassifier(), curVersion.getClassifier() )
                && StringUtils.equals( exVersion.getExtension(), curVersion.getExtension() ) )
            {
                return curVersion;
            }
        }
        return null;
    }

}
