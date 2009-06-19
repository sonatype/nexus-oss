package org.sonatype.nexus.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceUtil
{
    public static String getStackTraceString( Throwable t )
    {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter( writer );
        
        t.printStackTrace( printWriter );
        
        return writer.toString();
    }
}
