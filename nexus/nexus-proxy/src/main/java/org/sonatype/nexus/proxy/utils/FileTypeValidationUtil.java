package org.sonatype.nexus.proxy.utils;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role=FileTypeValidationUtil.class )
public class FileTypeValidationUtil
{
    
    @Requirement( role = FileTypeValidator.class )
    private List<FileTypeValidator> fileTypeValidators;
    
    public boolean isExpectedFileType( InputStream inputStream, String actualFileName )
    {
        for ( FileTypeValidator fileTypeValidator : fileTypeValidators )
        {
            for ( String fileType : fileTypeValidator.getSupportedFileTypesForValidation() )
            {
                if( actualFileName.toLowerCase().endsWith( fileType.toLowerCase() ) )
                {
                    boolean result = fileTypeValidator.isExpectedFileType( inputStream, actualFileName );
                    if( !result )
                    {
                        return false;
                    }
                }
            }
        }
        // return true any time a file did NOT fail validation
        return true;
    }
}
