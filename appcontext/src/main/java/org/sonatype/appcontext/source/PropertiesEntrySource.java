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
package org.sonatype.appcontext.source;

import java.util.Properties;

import org.sonatype.appcontext.internal.Preconditions;

/**
 * EntrySource that sources itself from a {@code java.util.Properties} file. It might be set to fail but also to keep
 * silent the fact that file to load is not found.
 * 
 * @author cstamas
 */
public class PropertiesEntrySource
    extends AbstractMapEntrySource
{
    private final Properties source;

    public PropertiesEntrySource( final String name, final Properties source )
    {
        super( name, "props" );
        this.source = Preconditions.checkNotNull( source );
    }

    protected Properties getSource()
    {
        return source;
    }
}
