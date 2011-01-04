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
package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.text.ParseException;
import java.util.Date;

/**
 * @author Oleg Gusakov
 * @version $Id: TimeUtil.java 762963 2009-04-07 21:01:07Z ogusakov $
 */
public class TimeUtil
{
    public static final java.util.TimeZone TS_TZ = java.util.TimeZone.getTimeZone( "UTC" );

    public static final java.text.DateFormat TS_FORMAT = new java.text.SimpleDateFormat( "yyyyMMddHHmmss" );

    static
    {
        TS_FORMAT.setTimeZone( TS_TZ );
    }

    /**
     * @return current UTC timestamp by yyyyMMddHHmmss mask
     */
    public static String getUTCTimestamp()
    {
        return getUTCTimestamp( new Date() );
    }

    /**
     * @return current UTC timestamp by yyyyMMddHHmmss mask as a long int
     */
    public static long getUTCTimestampAsLong()
    {
        return Long.parseLong( getUTCTimestamp( new Date() ) );
    }

    /**
     * @return current UTC timestamp by yyyyMMddHHmmss mask as a long int
     */
    public static long getUTCTimestampAsMillis()
    {
        return Long.parseLong( getUTCTimestamp( new Date() ) );
    }

    /**
     * @param date
     * @return current date converted to UTC timestamp by yyyyMMddHHmmss mask
     */
    public static String getUTCTimestamp( Date date )
    {
        return TS_FORMAT.format( date );
    }

    /**
     * convert timestamp to millis
     * 
     * @param ts timestamp to convert. Presumed to be a long of form yyyyMMddHHmmss
     * @return millis, corresponding to the supplied TS
     * @throws ParseException is long does not follow the format
     */
    public static long toMillis( long ts )
        throws ParseException
    {
        return toMillis( "" + ts );
    }

    /**
     * convert timestamp to millis
     * 
     * @param ts timestamp to convert. Presumed to be a string of form yyyyMMddHHmmss
     * @return millis, corresponding to the supplied TS
     * @throws ParseException is long does not follow the format
     */
    public static long toMillis( String ts )
        throws ParseException
    {
        Date dts = toDate( ts );

        return dts.getTime();
    }

    static Date toDate( String ts )
        throws ParseException
    {
        Date dts = TS_FORMAT.parse( ts );
        return dts;
    }

    public static void main( String[] args )
        throws Exception
    {
        if ( args == null || args.length < 0 )
        {
            return;
        }

        if ( "-t".equals( args[0] ) )
        {
            System.out.println( args[1] + " => " + new Date( toMillis( args[1] ) ) );
            return;
        }
    }

    public static int compare( String t1, String t2 )
        throws ParseException
    {
        if ( t1 == t2 )
        {
            return 0;
        }

        if ( t1 == null )
        {
            return -1;
        }

        if ( t2 == null )
        {
            return 1;
        }

        Date d1 = toDate( t1 );
        Date d2 = toDate( t2 );

        return d1.compareTo( d2 );
    }
}
