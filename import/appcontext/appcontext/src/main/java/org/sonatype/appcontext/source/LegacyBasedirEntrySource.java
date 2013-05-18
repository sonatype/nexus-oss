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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * This is a "legacy" baseDir EntrySource that should not be used anymore. It was used mostly in Plexus applications,
 * that usually do depend on "baseDir".
 * 
 * @author cstamas
 * @deprecated Do not rely on system properties for stuff like these, use AppContext better.
 */
public class LegacyBasedirEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final String basedirKey;

    private final boolean failIfNotFound;

    /**
     * Constructs the instance using "standard" key used in Plexus Applications. The constructed instance will fail if
     * key is not found!
     */
    public LegacyBasedirEntrySource()
    {
        this( "basedir", true );
    }

    /**
     * Constructs an instance with custom key.
     * 
     * @param basedirKey
     * @param failIfNotFound
     */
    public LegacyBasedirEntrySource( final String basedirKey, final boolean failIfNotFound )
    {
        this.basedirKey = Preconditions.checkNotNull( basedirKey );
        this.failIfNotFound = failIfNotFound;
    }

    public String getDescription()
    {
        return "legacyBasedir(key:\"" + basedirKey + "\")";
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        try
        {
            final File baseDir = discoverBasedir( basedirKey );
            if ( failIfNotFound && !baseDir.isDirectory() )
            {
                throw new AppContextException(
                    "LegacyBasedirEntrySource was not able to find existing basedir! It discovered \""
                        + baseDir.getAbsolutePath() + "\", but it does not exists or is not a directory!" );
            }
            final HashMap<String, Object> result = new HashMap<String, Object>();
            result.put( basedirKey, baseDir );
            return result;
        }
        catch ( IOException e )
        {
            throw new AppContextException( "Could not discover base dir!", e );
        }
    }

    // ==

    /**
     * The essence how old Plexus application was expecting to have "basedir" discovered. Usually using system property
     * that contained a file path, or fall back to current working directory.
     * 
     * @param basedirKey
     * @return
     * @throws IOException
     */
    public File discoverBasedir( final String basedirKey )
        throws IOException
    {
        final String basedirPath = System.getProperty( basedirKey );

        if ( basedirPath == null )
        {
            return new File( "" ).getCanonicalFile();
        }
        else
        {
            return new File( basedirPath ).getCanonicalFile();
        }
    }
}
