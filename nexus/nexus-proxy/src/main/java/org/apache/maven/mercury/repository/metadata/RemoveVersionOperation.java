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
package org.apache.maven.mercury.repository.metadata;

import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.mercury.util.TimeUtil;

/**
 * removes a version from Metadata
 * 
 * @author Oleg Gusakov
 * @version $Id: RemoveVersionOperation.java 726701 2008-12-15 14:31:34Z hboutemy $
 */
public class RemoveVersionOperation
    implements MetadataOperation
{

    private String version;

    /**
     * @throws MetadataException
     */
    public RemoveVersionOperation( StringOperand data )
        throws MetadataException
    {
        setOperand( data );
    }

    public void setOperand( Object data )
        throws MetadataException
    {
        if ( data == null || !( data instanceof StringOperand ) )
        {
            throw new MetadataException( "Operand is not correct: expected StringOperand, but got "
                + ( data == null ? "null" : data.getClass().getName() ) );
        }

        version = ( (StringOperand) data ).getOperand();
    }

    /**
     * remove version to the in-memory metadata instance
     * 
     * @param metadata
     * @param version
     * @return
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
            return false;
        }

        if ( vs.getVersions() != null && vs.getVersions().size() > 0 )
        {
            List<String> vl = vs.getVersions();
            if ( !vl.contains( version ) )
            {
                return false;
            }
        }

        vs.removeVersion( version );
        vs.setLastUpdated( TimeUtil.getUTCTimestamp() );

        return true;
    }

}
