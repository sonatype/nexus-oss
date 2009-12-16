package org.sonatype.appcontext;

import java.util.Map;

public class BasedirContextFiller
    implements ContextFiller
{
    public static final String BASEDIR_CONTEXT_KEY = "basedir";

    public void fillContext( AppContextRequest request, Map<Object, Object> context )
        throws AppContextException
    {
        context.put( BASEDIR_CONTEXT_KEY, request.getBasedirDiscoverer().discoverBasedir().getAbsolutePath() );
    }
}
