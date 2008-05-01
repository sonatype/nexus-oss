/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.creator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/** @author Jason van Zyl */
public abstract class AbstractIndexCreator
    extends AbstractLogEnabled
    implements IndexCreator
{

    /** Record separator */
    public static final String RS = "@";

    /** Field separator */
    public static final String FS = "|";

    public static final Pattern FS_PATTERN = Pattern.compile( Pattern.quote( "|" ) );

    /** Non available value */
    public static final String NA = "NA";

    public AbstractIndexCreator()
    {
        super();
    }

    public static String bos( boolean b )
    {
        return b ? "1" : "0";
    }

    public static boolean sob( String b )
    {
        return b.equals( "1" );
    }

    public static String nvl( String v )
    {
        return v == null ? NA : v;
    }

    public static String renvl( String v )
    {
        return NA.equals( v ) ? null : v;
    }

    public static String lst2str( Collection<String> list )
    {
        StringBuilder sb = new StringBuilder();
        for ( String s : list )
        {
            sb.append( s ).append( FS );
        }
        return sb.length()==0 ? sb.toString() : sb.substring( 0, sb.length() - 1 );
    }

    public static List<String> str2lst( String str )
    {
        return Arrays.asList( FS_PATTERN.split( str ) );
    }

    public static String getGAV( String groupId, String artifactId, String version, String classifier )
    {
        return new StringBuilder()
            .append( groupId ).append( FS ).append( artifactId ).append( FS ).append( version ).append( FS ).append(
                nvl( classifier ) ).toString();
    }

}
