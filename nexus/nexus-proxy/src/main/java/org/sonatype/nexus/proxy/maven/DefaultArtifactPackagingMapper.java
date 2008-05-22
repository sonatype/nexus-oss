package org.sonatype.nexus.proxy.maven;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * A very simple artifact packaging mapper, that has everyting wired in this class.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultArtifactPackagingMapper
    extends AbstractLogEnabled
    implements ArtifactPackagingMapper
{
    // TODO: think about externalizing this map to a file, and make it user extendable

    private final Map<String, String> typeToExtensions;

    {
        typeToExtensions = new HashMap<String, String>();
        typeToExtensions.put( "ear", "jar" );
        typeToExtensions.put( "ejb-client", "jar" );
        typeToExtensions.put( "ejb", "jar" );
        typeToExtensions.put( "rar", "jar" );
        typeToExtensions.put( "par", "jar" );
        typeToExtensions.put( "maven-plugin", "jar" );
        typeToExtensions.put( "maven-archetype", "jar" );
    }

    public String getExtensionForPackaging( String packaging )
    {
        if ( typeToExtensions.containsKey( packaging ) )
        {
            return typeToExtensions.get( packaging );
        }
        else
        {
            // default's to packaging name, ie. "jar", "war", "pom", etc.
            return packaging;
        }
    }

}
