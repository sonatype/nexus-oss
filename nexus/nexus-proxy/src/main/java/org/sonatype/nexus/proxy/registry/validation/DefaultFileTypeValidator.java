package org.sonatype.nexus.proxy.registry.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;

@Component( role = FileTypeValidator.class )
public class DefaultFileTypeValidator
    extends AbstractFileTypeValidator
{
    Map<String, String> supportedTypeMap = new HashMap<String, String>();

    @Requirement
    private Logger logger;

    public DefaultFileTypeValidator()
    {
        supportedTypeMap.put( "jar", "application/zip" );
        supportedTypeMap.put( "zip", "application/zip" );
        supportedTypeMap.put( "war", "application/zip" );
        supportedTypeMap.put( "ear", "application/zip" );
        supportedTypeMap.put( "pom", "application/x-maven-pom" );
        supportedTypeMap.put( "xml", "text/xml" );
    }

    protected Map<String, String> getFileTypeToMimeMap()
    {
        return this.supportedTypeMap;
    }

    @Override
    public boolean isExpectedFileType( InputStream inputStream, String actualFileName )
    {
        if ( actualFileName.toLowerCase().endsWith( "pom" ) )
        {
            int lineCount = 0; // only process a few lines
            Scanner scanner = new Scanner( inputStream );
            while ( scanner.hasNextLine() && lineCount < 200 )
            {
                lineCount++;
                String line = scanner.nextLine();
                if ( line.contains( "<project" ) )
                {
                    return true;
                }
            }
            return false;
        }
        else
        {
            return super.isExpectedFileType( inputStream, actualFileName );
        }
    }
}
