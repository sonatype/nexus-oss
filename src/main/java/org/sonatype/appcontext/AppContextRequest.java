package org.sonatype.appcontext;

import java.util.List;

public interface AppContextRequest
{
    String getName();

    void setName( String name );

    BasedirDiscoverer getBasedirDiscoverer();

    void setBasedirDiscoverer( BasedirDiscoverer discoverer );

    List<ContextFiller> getContextFillers();

    void setContextFillers( List<ContextFiller> fillers );

    List<ContextPublisher> getContextPublishers();

    void setContextPublishers( List<ContextPublisher> publishers );
}
