package org.sonatype.nexus.proxy.utils;

import java.io.InputStream;
import java.util.Set;

public interface FileTypeValidator
{
    
    /**
     * Returns a list of strings which represents either a file name or extention.
     * @return a list of file types this validator supports.
     */
    Set<String> getSupportedFileTypesForValidation();
    
    boolean isExpectedFileType( InputStream inputStream, String actualFileName );
}
