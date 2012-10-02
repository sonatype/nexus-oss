package org.sonatype.nexus.plugins.yum;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NameUtil
{
    public static String uniqueName()
    {
        return "repo_" + new SimpleDateFormat( "yyyyMMdd_HHmmss_SSS" ).format( new Date() );
    }
}
