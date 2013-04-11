/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.appcontext.source.keys;

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * A key transformer that removes a given prefix from key.
 * 
 * @author cstamas
 */
public class PrefixRemovingKeyTransformer
    implements KeyTransformer
{
    private final String prefix;

    public PrefixRemovingKeyTransformer( final String prefix )
    {
        this.prefix = Preconditions.checkNotNull( prefix );
    }

    public EntrySourceMarker getTransformedEntrySourceMarker( final EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( final EntrySourceMarker wrapped )
            {
                return String.format( "prefixRemove(prefix:%s, %s)", prefix, wrapped.getDescription() );
            }
        };
    }

    public String transform( final String key )
    {
        if ( key.startsWith( prefix ) )
        {
            // remove prefix, but watch for capitalization
            final String result = key.substring( prefix.length() );
            if ( Character.isUpperCase( result.charAt( 0 ) ) )
            {
                final char[] resultArray = result.toCharArray();
                resultArray[0] = Character.toLowerCase( resultArray[0] );
                return new String( resultArray );
            }
            else
            {
                return result;
            }
        }
        else
        {
            return key;
        }
    }
}
