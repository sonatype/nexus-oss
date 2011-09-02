package org.sonatype.appcontext.source.keys;

import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * The default normalization, that makes System.env keys (usually set due to OS constraints in form of "MAVEN_OPTS")
 * more system-property keys alike. This is just to follow best practices, but also to be able to have env variables
 * that "projects" themselves to same keys as in System properties, and be able to "override" them if needed. This
 * normalization would normalize "MAVEN_OPTS" into "maven.opts". The applied transformations:
 * <ul>
 * <li>makes all keys lower case</li>
 * <li>replaces all occurrences of character '_' (underscore) to '.' (dot)</li>
 * </ul>
 * 
 * @author cstamas
 */
public class DefaultSystemEnvironmentKeyTransformer
    implements KeyTransformer
{
    public EntrySourceMarker getTransformedEntrySourceMarker( final EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( final EntrySourceMarker wrapped )
            {
                return String.format( "defSysEnvTransformation(%s)", wrapped.getDescription() );
            }
        };
    }

    public String transform( final String key )
    {
        // MAVEN_OPTS => maven.opts
        return key.toLowerCase().replace( '_', '.' );
    }
}
