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

import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextEntry;

public class RawAppContextValueSource
    extends AbstractValueSource
{
    private final AppContext context;

    public RawAppContextValueSource( final AppContext context )
    {
        super( false );
        this.context = context;
    }

    public Object getValue( String expression )
    {
        final AppContextEntry entry = context.getAppContextEntry( expression );

        if ( entry != null )
        {
            return entry.getRawValue();
        }
        else
        {
            return null;
        }
    }
}
