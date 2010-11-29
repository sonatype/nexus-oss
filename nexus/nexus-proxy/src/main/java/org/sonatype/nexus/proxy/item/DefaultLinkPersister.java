package org.sonatype.nexus.proxy.item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

@Component( role = LinkPersister.class )
public class DefaultLinkPersister
    implements LinkPersister
{
    private static final String LINK_PREFIX = "LINK to ";

    private static final String UTF8_CHARSET = "UTF-8";

    @Requirement
    private RepositoryItemUidFactory repositoryItemUidFactory;

    public boolean isLinkContent( final ContentLocator locator )
        throws IOException
    {
        if ( locator != null )
        {
            InputStream fis = null;

            try
            {
                final byte[] buf = new byte[LINK_PREFIX.length()];

                final byte[] link = LINK_PREFIX.getBytes( UTF8_CHARSET );

                fis = locator.getContent();

                boolean result = fis != null && fis.read( buf ) == LINK_PREFIX.length();

                if ( result )
                {
                    result = Arrays.equals( buf, link );
                }

                return result;
            }
            finally
            {
                IOUtil.close( fis );
            }

        }
        else
        {
            return false;
        }
    }

    public RepositoryItemUid readLinkContent( final ContentLocator locator )
        throws NoSuchRepositoryException, IOException
    {
        if ( locator != null )
        {
            InputStream fis = null;

            try
            {
                fis = locator.getContent();

                final String linkBody = IOUtil.toString( fis, UTF8_CHARSET );

                final String uidStr = linkBody.substring( LINK_PREFIX.length(), linkBody.length() );

                return repositoryItemUidFactory.createUid( uidStr );
            }
            finally
            {
                IOUtil.close( fis );
            }
        }
        else
        {
            return null;
        }
    }

    public void writeLinkContent( final StorageLinkItem link, final OutputStream os )
        throws IOException
    {
        try
        {
            final String linkBody = LINK_PREFIX + link.getTarget().toString();

            IOUtil.copy( new ByteArrayInputStream( linkBody.getBytes( UTF8_CHARSET ) ), os );

            os.flush();
        }
        finally
        {
            IOUtil.close( os );
        }
    }
}
