package org.sonatype.appcontext;

import java.util.Map;

/**
 * A ContextFiller that uses System.getenv() as source, filters it for environment variable names starting with
 * configured prefix + "_". For example if prefix equals "PLEXUS", "PLEXUS_FOO=1" will be put into appcontext as
 * "foo=1".
 * 
 * @author cstamas
 */
public class SystemEnvironmentContextFiller
    implements ContextFiller
{
    private String keyPrefix = null;

    public void fillContext( AppContextFactory factory, AppContextRequest request, Map<Object, Object> context )
    {
        /*
         * Iterate through environment variables, insert all items into a map (making sure to do translation needed,
         * remove "PLEXUS_" , change all _ to - and convert to lower case)
         */
        Map<String, String> envMap = System.getenv();

        String envVarPrefix = produceKeyPrefix( request );

        for ( String key : envMap.keySet() )
        {
            if ( key.toUpperCase().startsWith( envVarPrefix ) && key.length() > envVarPrefix.length() )
            {
                String plexusKey = key.toLowerCase().substring( envVarPrefix.length() ).replace( '_', '-' );

                context.put( plexusKey, envMap.get( key ) );
            }
        }
    }

    protected String produceKeyPrefix( AppContextRequest request )
    {
        if ( getKeyPrefix() == null )
        {
            return request.getName().toUpperCase() + "_";
        }
        else
        {
            return getKeyPrefix();
        }
    }

    public String getKeyPrefix()
    {
        return keyPrefix;
    }

    public void setKeyPrefix( String keyPrefix )
    {
        this.keyPrefix = keyPrefix;
    }
}
