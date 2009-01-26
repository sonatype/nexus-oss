/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.packer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.sonatype.nexus.index.ArtifactInfo;

/**
 * Default chunker converts timestamp (e.g. {@link ArtifactInfo#LAST_MODIFIED}) to corresponding index chunk name using
 * day as a base.
 * 
 * @author Tamas Cservenak
 * @plexus.component role-hint="day"
 */
public class DefaultIndexChunker
    implements IndexChunker
{
    private static final String INDEX_TIME_DAY_FORMAT = "yyyyMMdd";

    public String getChunkId( Date d )
    {
        if ( d == null )
        {
            return null;
        }

        DateFormat df = getDateFormat();

        // we are "truncating" the date
        return df.format( d );
    }

    public Date getChunkDate( String id )
    {
        if ( id == null )
        {
            return null;
        }

        try
        {
            return getDateFormat().parse( id );
        }
        catch ( ParseException e )
        {
            // should not happen
            return null;
        }
    }

    private DateFormat getDateFormat()
    {
        DateFormat df = new SimpleDateFormat( INDEX_TIME_DAY_FORMAT );
        df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        return df;
    }
}
