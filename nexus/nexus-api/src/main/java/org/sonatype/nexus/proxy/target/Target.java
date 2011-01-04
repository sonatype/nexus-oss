/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
        // make sure to check the inverse of the isCompatible too !!
        if ( StringUtils.equals( getContentClass().getId(), contentClass.getId() )
            || getContentClass().isCompatible( contentClass ) 
            || contentClass.isCompatible( getContentClass() ) )
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
