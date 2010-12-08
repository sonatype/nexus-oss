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

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.mercury.util.TimeUtil;

/**
 * adds new snapshot to metadata
 * 
 * @author Oleg Gusakov
 * @version $Id: SetSnapshotOperation.java 743040 2009-02-10 18:20:26Z ogusakov $
 */
public class SetSnapshotOperation
    implements MetadataOperation
{

    private Snapshot snapshot;
    
    private String snapshotPomName;

    /**
     * @throws MetadataException
     */
    public SetSnapshotOperation( SnapshotOperand data )
        throws MetadataException
    {
        setOperand( data );
    }
    
    public SetSnapshotOperation( StringOperand data )
    	throws MetadataException
    {
    	setOperand( data );
    }

    public void setOperand( Object data )
        throws MetadataException
    {
        if ( data != null && data instanceof SnapshotOperand  )
        {
        	snapshot = ( (SnapshotOperand) data ).getOperand();
        }
        else if ( data != null && data instanceof StringOperand )
        {
        	snapshotPomName = ( (StringOperand) data ).getOperand();
        }
        else
        {
            throw new MetadataException( "Operand is not correct: expected SnapshotOperand, but got "
                + ( data == null ? "null" : data.getClass().getName() ) );
        }

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
        
        if ( snapshotPomName != null )
        {
        	return updateSnapshot( snapshotPomName, metadata );
        }
        else
        {
        	return updateSnapshot( snapshot, vs );
        }
        
    }
    
    private boolean updateSnapshot( String snapshotVersion, Metadata metadata )
    {
    	Snapshot snapshot = buildSnapshot( snapshotVersion, metadata );
    	
    	Snapshot oldSnapshot = metadata.getVersioning().getSnapshot();
    	
    	if ( needUpdateSnapshot( oldSnapshot, snapshot) )
    	{
    		return updateSnapshot( snapshot, metadata.getVersioning() );
    	}
    	
    	return false;
    	
    	
    }
    
    private boolean updateSnapshot( Snapshot snapshot, Versioning vs )
    {
    	vs.setSnapshot( snapshot );
    	
    	vs.setLastUpdated( TimeUtil.getUTCTimestamp() );
    	
    	return true;
    }
    
    private boolean needUpdateSnapshot( Snapshot oldSnapshot, Snapshot newSnapshot )
    {
    	if ( newSnapshot == null )
    	{
    		return false;
    	}
    	
    	if ( oldSnapshot == null )
    	{
    		return true;
    	}
    	
    	if ( oldSnapshot.getBuildNumber() < newSnapshot.getBuildNumber() )
    	{
    		return true;
    	}
    	
    	return false;
    }
    
    private Snapshot buildSnapshot( String pomName, Metadata md )
    {
        // skip files like groupId-artifactId-versionSNAPSHOT.pom
        if ( pomName.endsWith( "SNAPSHOT.pom" ) )
        {
            return null;
        }
        
        Snapshot result = new Snapshot();
        
        int lastHyphenPos = pomName.lastIndexOf( '-' );
        
        try
        {
            int buildNumber = Integer.parseInt( pomName.substring(
                lastHyphenPos + 1,
                pomName.length() - 4 ) );
            
            String timestamp = pomName.substring( ( md.getArtifactId() + '-' + md.getVersion() + '-' )
                    .length()
                    - "-SNAPSHOT".length(), lastHyphenPos );
            
            result.setLocalCopy( false );
            
            result.setBuildNumber( buildNumber );
            
            result.setTimestamp( timestamp );
            
            return result;
        }
        catch ( Exception e )
        {
            // skip any exception because of illegal version numbers
        	return null;
        }        
        
    }

}
