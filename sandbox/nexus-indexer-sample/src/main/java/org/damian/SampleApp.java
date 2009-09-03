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
