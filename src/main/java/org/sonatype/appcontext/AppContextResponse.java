package org.sonatype.appcontext;

import java.io.File;
import java.util.Map;

public interface AppContextResponse
{
    String getName();
    
    File getBasedir();
    
    Map<Object, Object> getContext();

    Map<Object, Object> getRawContext();
}
