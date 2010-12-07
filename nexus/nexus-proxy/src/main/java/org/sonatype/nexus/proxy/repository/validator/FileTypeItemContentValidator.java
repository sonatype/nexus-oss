package org.sonatype.nexus.proxy.repository.validator;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

@Component( role = ItemContentValidator.class, hint = "FileTypeItemContentValidator" )
public class FileTypeItemContentValidator
    implements ItemContentValidator
{
    @Requirement
    private FileTypeValidatorHub validatorHub;

    public boolean isRemoteItemContentValid( final ProxyRepository proxy, final ResourceStoreRequest request,
                                             final String baseUrl, final AbstractStorageItem item,
                                             final List<NexusArtifactEvent> events )
    {
        if ( !proxy.isFileTypeValidation() )
        {
            // make sure this is enabled before we check.
            return true;
        }

        return validatorHub.isExpectedFileType( item );
    }
}
