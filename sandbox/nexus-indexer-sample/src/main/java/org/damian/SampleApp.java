/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.damian;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.Grouping;

public interface SampleApp
{
    void index() 
        throws IOException;
    
    Set<ArtifactInfo> searchIndexFlat( String field, String value ) 
        throws IOException;
    
    Set<ArtifactInfo> searchIndexFlat( Query query )
        throws IOException;
    
    Map<String, ArtifactInfoGroup> searchIndexGrouped( String field, String value )
        throws IOException;
    
    Map<String, ArtifactInfoGroup> searchIndexGrouped( String field, String value, Grouping grouping )
        throws IOException;
    
    Map<String, ArtifactInfoGroup> searchIndexGrouped( Query q, Grouping grouping )
        throws IOException;
    
    void publishIndex( File targetDirectory )
        throws IOException;
    
    void updateRemoteIndex()
        throws IOException;
}
