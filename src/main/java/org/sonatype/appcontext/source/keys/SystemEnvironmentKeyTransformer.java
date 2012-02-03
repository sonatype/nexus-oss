package org.sonatype.appcontext.source.keys;

import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * The default "env var" normalization, that makes System.env keys (usually set due to OS constraints in form of
 * "MAVEN_OPTS") more system-property keys alike. This is just to follow best practices, but also to be able to have env
 * variables that "projects" themselves to same keys as in System properties, and be able to "override" them if needed.
 * This normalization would normalize "MAVEN_OPTS" into "mavenOpts". The applied transformations:
 * <ul>
 * <li>makes all keys lower case</li>
 * <li>removes all occurrences of character '_' (underscore) capitalizing next char if any</li>
 * </ul>
 * 
 * @author cstamas
 */
public class SystemEnvironmentKeyTransformer
    implements KeyTransformer
{
    public EntrySourceMarker getTransformedEntrySourceMarker( final EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( final EntrySourceMarker wrapped )
            {
                return String.format( "sysEnvTransformation(%s)", wrapped.getDescription() );
            }
        };
    }

    public String transform( final String key )
    {
        // MAVEN_OPTS => mavenOpts
        final StringBuilder sb = new StringBuilder();

        boolean capitalize = false;
        for ( int i = 0; i < key.length(); i++ )
        {
            final char ch = key.charAt( i );

            if ( i == 0 )
            {
                sb.append( Character.toLowerCase( ch ) );
            }
            else if ( ch == '_' )
            {
                // just skip it but capitalize next
                capitalize = true;
            }
            else
            {
                if ( capitalize )
                {
                    sb.append( Character.toUpperCase( ch ) );
                    capitalize = false;
                }
                else
                {
                    sb.append( Character.toLowerCase( ch ) );
                }
            }
        }

        return sb.toString();
    }
}
