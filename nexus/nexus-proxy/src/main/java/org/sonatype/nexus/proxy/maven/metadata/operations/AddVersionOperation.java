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

import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.mercury.util.TimeUtil;

/**
 * adds new version to metadata
 *
 * @author Oleg Gusakov
 * @version $Id: AddVersionOperation.java 743040 2009-02-10 18:20:26Z ogusakov $
 *
 */
public class AddVersionOperation
    implements MetadataOperation
{
  
    private String version;
  
    /**
     * @throws MetadataException
     */
    public AddVersionOperation( StringOperand data )
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
     * add version to the in-memory metadata instance
     * 
     * @param metadata
     * @param version
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

        if ( vs.getVersions() != null && vs.getVersions().size() > 0 )
        {
            List<String> vl = vs.getVersions();

            if ( vl.contains( version ) )
            {
                return false;
            }
        }

        vs.addVersion( version );
        
        List<String> versions = vs.getVersions();
        
        Collections.sort( versions, new VersionComparator() );
        
        vs.setLatest( getLatestVersion(versions) );
        
        vs.setRelease( getReleaseVersion(versions) );
        
        vs.setLastUpdated( TimeUtil.getUTCTimestamp() );

        return true;
    }
    
    private String getLatestVersion( List<String> orderedVersions )
    {
    	return orderedVersions.get( orderedVersions.size() - 1 );
    }
    
    private String getReleaseVersion( List<String> orderedVersions )
    {
        for (int i = orderedVersions.size() - 1; i >= 0; i--) 
        {
			if (!orderedVersions.get(i).endsWith("SNAPSHOT")) 
			{
				return orderedVersions.get(i);
			}
		}
        
        return "";
    }
}
