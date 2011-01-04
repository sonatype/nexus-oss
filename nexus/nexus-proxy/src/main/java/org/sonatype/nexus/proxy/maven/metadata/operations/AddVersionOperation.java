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

import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;

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
  
    public void setOperand( AbstractOperand data )
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
