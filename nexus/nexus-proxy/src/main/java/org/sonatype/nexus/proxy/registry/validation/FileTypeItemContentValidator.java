package org.sonatype.nexus.proxy.registry.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.utils.FileTypeValidationUtil;

@Component( role = ItemContentValidator.class, hint = "FileTypeItemContentValidator" )
public class FileTypeItemContentValidator
    implements ItemContentValidator
{

    @Requirement
    private FileTypeValidationUtil validationUtil;

    @Requirement
    private Logger logger;
    
    public boolean isRemoteItemContentValid( ProxyRepository proxy, ResourceStoreRequest request, String baseUrl,
                                             AbstractStorageItem item, List<NexusArtifactEvent> events )
        throws StorageException
    {
        if ( !proxy.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            // we work only with maven proxy reposes, all others are neglected
            return true;
        }

        if ( DefaultStorageFileItem.class.isInstance( item ) )
        {
            DefaultStorageFileItem fileItem = (DefaultStorageFileItem) item;
            InputStream in = null;
            try
            {
                in = fileItem.getInputStream();
                return this.validationUtil.isExpectedFileType( in, request.getRequestPath() );
            }
            catch ( IOException e )
            {
                logger.warn( "Failed to get open file item: "+ item.getPath() );
            }
            finally
            {
                IOUtil.close( in );
            }
        }
        // not a file item, nothing to validate
        return true;
    }
}
