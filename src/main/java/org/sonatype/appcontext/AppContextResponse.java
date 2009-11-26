package org.sonatype.appcontext;

import java.util.Map;

public interface AppContextResponse
{
    String getName();
    
    Map<Object, Object> getContext();

    Map<Object, Object> getRawContext();
}
