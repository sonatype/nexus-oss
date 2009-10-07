package org.sonatype.nexus.proxy.target;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * This is a repository target.
 * 
 * @author cstamas
 */
public class Target
{
    private final String id;

    private final String name;

    private final ContentClass contentClass;

    private final Set<String> patternTexts;

    private final Set<Pattern> patterns;

    public Target( String id, String name, ContentClass contentClass, Collection<String> patternTexts )
        throws PatternSyntaxException
    {
        super();

        this.id = id;

        this.name = name;

        this.contentClass = contentClass;

        this.patternTexts = new HashSet<String>( patternTexts );

        this.patterns = new HashSet<Pattern>( patternTexts.size() );

        for ( String patternText : patternTexts )
        {
            patterns.add( Pattern.compile( patternText ) );
        }
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public ContentClass getContentClass()
    {
        return contentClass;
    }

    public Set<String> getPatternTexts()
    {
        return Collections.unmodifiableSet( patternTexts );
    }

    public boolean isPathContained( ContentClass contentClass, String path )
    {
        // if is the same or is compatible
        if ( StringUtils.equals( getContentClass().getId(), contentClass.getId() )
            || getContentClass().isCompatible( contentClass ) )
        {
            // look for pattern matching
            for ( Pattern pattern : patterns )
            {
                if ( pattern.matcher( path ).matches() )
                {
                    return true;
                }
            }
        }

        return false;
    }

}
