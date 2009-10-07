/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.maven.mercury.util;

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
        Date dts = TS_FORMAT.parse( ts );

        return dts.getTime();
    }

    public static void main( String[] args ) throws Exception
    {
        if( args == null || args.length < 0 )
            return;
        
        if( "-t".equals( args[0] ) )
        {
            System.out.println( args[1]+" => " + new Date( toMillis( args[1] ) ) ) ;
            return;
        }
    }
}
