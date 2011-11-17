/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.mime;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A simple handy implementation of MimeRulesSource that uses an ordered map of regexp Patterns to match path again, and
 * in case of match the mapping value is returned. The order how Regexp/type is registered is important, since first
 * matched "wins". Meaning, you'd need to register the most specific ones first, and then the "usua" ones (if needed, or
 * just leave them to "global MIME type handling" if enough).
 * 
 * @author cstamas
 * @since 1.10.0
 */
public class RegexpMimeRulesSource
    implements MimeRulesSource
{
    private final LinkedHashMap<Pattern, String> rules;

    public RegexpMimeRulesSource()
    {
        this.rules = new LinkedHashMap<Pattern, String>();
    }

    public void addRule( final String regexpString, final String mimeType )
    {
        addRule( Pattern.compile( regexpString ), mimeType );
    }

    public void addRule( final Pattern pattern, final String mimeType )
    {
        rules.put( checkNotNull( pattern ), checkNotNull( mimeType ) );
    }

    public void clear()
    {
        rules.clear();
    }

    @Override
    public String getRuleForPath( final String path )
    {
        for ( Map.Entry<Pattern, String> entry : rules.entrySet() )
        {
            if ( entry.getKey().matcher( path ).matches() )
            {
                return entry.getValue();
            }
        }

        return null;
    }

}
