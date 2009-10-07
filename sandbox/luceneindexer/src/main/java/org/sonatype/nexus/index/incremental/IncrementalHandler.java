package org.sonatype.nexus.index.incremental;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;


public interface IncrementalHandler
{
    List<Integer> getIncrementalUpdates( IndexPackingRequest request, Properties properties )
        throws IOException;
    
    List<String> loadRemoteIncrementalUpdates( IndexUpdateRequest request, Properties localProperties, Properties remoteProperties )
        throws IOException;
    
    void initializeProperties( Properties properties );
}
