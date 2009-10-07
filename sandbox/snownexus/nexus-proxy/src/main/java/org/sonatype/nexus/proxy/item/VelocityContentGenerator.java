package org.sonatype.nexus.proxy.item;

import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = ContentGenerator.class, hint = "VelocityContentGenerator" )
public class VelocityContentGenerator
    implements ContentGenerator
{
    @Requirement
    private VelocityComponent velocityComponent;

    public ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        InputStreamReader isr = null;

        try
        {
            StringWriter sw = new StringWriter();

            VelocityContext vctx = new VelocityContext( item.getItemContext() );

            isr = new InputStreamReader( item.getInputStream(), "UTF-8" );

            velocityComponent.getEngine().evaluate( vctx, sw, item.getRepositoryItemUid().toString(), isr );

            return new StringContentLocator( sw.toString() );
        }
        catch ( Exception e )
        {
            throw new StorageException( "Could not expand the template: " + item.getRepositoryItemUid().toString(), e );
        }
        finally
        {
            IOUtil.close( isr );
        }
    }
}
