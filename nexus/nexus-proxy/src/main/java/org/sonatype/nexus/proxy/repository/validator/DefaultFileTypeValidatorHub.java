package org.sonatype.nexus.proxy.repository.validator;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.validator.FileTypeValidator.FileTypeValidity;

@Component( role = FileTypeValidatorHub.class )
public class DefaultFileTypeValidatorHub
    implements FileTypeValidatorHub
{
    @Requirement( role = FileTypeValidator.class )
    private List<FileTypeValidator> fileTypeValidators;

    @Override
    public boolean isExpectedFileType( final StorageItem item )
    {
        if ( item instanceof StorageFileItem )
        {
            StorageFileItem file = (StorageFileItem) item;

            for ( FileTypeValidator fileTypeValidator : fileTypeValidators )
            {
                FileTypeValidity validity = fileTypeValidator.isExpectedFileType( file );

                if ( FileTypeValidity.INVALID.equals( validity ) )
                {
                    // fail fast
                    return false;
                }
            }

            // return true if not failed for now
            // later we might get this better
            return true;
        }
        else
        {
            // we check files only, so say true here
            return true;
        }
    }
}
