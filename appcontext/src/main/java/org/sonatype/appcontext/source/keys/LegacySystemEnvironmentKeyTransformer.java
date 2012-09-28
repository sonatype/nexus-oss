package org.sonatype.appcontext.source.keys;

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * The "env var" normalization that is configurable, that makes System.env keys (usually set due to OS constraints in
 * form of "MAVEN_OPTS") more system-property keys alike. This is just to follow best practices, but also to be able to
 * have env variables that "projects" themselves to same keys as in System properties, and be able to "override" them if
 * needed. This normalization would normalize "MAVEN_OPTS" into "maven-opts" -- if {@code underscoreReplacement} is set
 * to '-' (dash). The applied transformations:
 * <ul>
 * <li>makes all keys lower case</li>
 * <li>replaces all occurrences of character '_' (underscore) to {@code underscoreReplacement}</li>
 * </ul>
 * 
 * @author cstamas
 */
public class LegacySystemEnvironmentKeyTransformer
    implements KeyTransformer
{
    private final char underscoreReplacement;

    /**
     * Constructs default instance that uses '-' (dash).
     */
    public LegacySystemEnvironmentKeyTransformer()
    {
        this( '-' );
    }

    /**
     * Constructs an instance that uses passed in char. Recommended characters are '-' (dash) and '.' (dot), but
     * naturally, it's matter of taste.
     * 
     * @param underscoreReplacement
     */
    public LegacySystemEnvironmentKeyTransformer( char underscoreReplacement )
    {
        this.underscoreReplacement = Preconditions.checkNotNull( underscoreReplacement );
    }

    public char getUnderscoreReplacement()
    {
        return underscoreReplacement;
    }

    public EntrySourceMarker getTransformedEntrySourceMarker( final EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( final EntrySourceMarker wrapped )
            {
                return String.format( "legacySysEnvTransformation(%s)", wrapped.getDescription() );
            }
        };
    }

    public String transform( final String key )
    {
        // MAVEN_OPTS => maven-opts
        return key.toLowerCase().replace( '_', underscoreReplacement );
    }
}
