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
package org.sonatype.appcontext.internal;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextEntry;

public class ContextStringDumper
{
    public static final String dumpToString( final AppContext context )
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "Application context \"" + context.getId() + "\" dump:\n" );
        if ( context.getParent() != null )
        {
            sb.append( "Parent context is \"" + context.getParent().getId() + "\"\n" );
        }
        for ( String key : context.keySet() )
        {
            final AppContextEntry entry = context.getAppContextEntry( key );
            sb.append( entry.toString() ).append( "\n" );
        }
        sb.append( String.format( "Total of %s entries.\n", context.size() ) );
        return sb.toString();
    }
}
