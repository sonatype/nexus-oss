package org.sonatype.nexus.util;

/**
 * Some classpath related utility methods.
 * 
 * @author cstamas
 */
public class ClasspathUtils
{
    /**
     * Converts a supplied "binary name" into "canonical name" if possible. If not possible, returns null.
     * 
     * @param binaryName to convert
     * @return canonical name (in case of classes), null if conversion is not possible.
     */
    public static String convertClassBinaryNameToCanonicalName( String binaryName )
    {
        // sanity check
        if ( binaryName == null || binaryName.trim().length() == 0 )
        {
            return null;
        }

        if ( binaryName.endsWith( ".class" ) )
        {
            int startIdx = 0;

            if ( binaryName.startsWith( "/" ) )
            {
                startIdx = 1;
            }

            // class name without ".class"
            return binaryName.substring( startIdx, binaryName.length() - 6 ).replace( "/", "." ); //.replace( "$", "." );
        }
        else
        {
            return null;
        }
    }
}
