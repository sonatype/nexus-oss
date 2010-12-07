package org.sonatype.nexus.mime;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

@Component( role = MimeUtil.class )
public class DefaultMimeUtil
    implements MimeUtil
{
    private MimeUtil2 nonTouchingMimeUtil;

    private MimeUtil2 touchingMimeUtil;

    public DefaultMimeUtil()
    {
        // MimeUtil2 by design will start (try to) read the file/stream if some "eager" detector is registered
        // so we follow the "private instance" pattern, and we handle two instances for now
        
        // uses Extension only for now (speed, no IO, but less accuracy)
        nonTouchingMimeUtil = new MimeUtil2();
        nonTouchingMimeUtil.registerMimeDetector( ExtensionMimeDetector.class.getName() );

        // uses magic-mime (IO and lower speed but more accuracy)
        touchingMimeUtil = new MimeUtil2();
        touchingMimeUtil.registerMimeDetector( MagicMimeMimeDetector.class.getName() );
    }

    protected MimeUtil2 getNonTouchingMimeUtil2()
    {
        return nonTouchingMimeUtil;
    }

    protected MimeUtil2 getTouchingMimeUtil2()
    {
        return touchingMimeUtil;
    }

    @Override
    public String getMimeType( String fileName )
    {
        return MimeUtil2.getMostSpecificMimeType( getNonTouchingMimeUtil2().getMimeTypes( fileName ) ).toString();
    }

    @Override
    public String getMimeType( File file )
    {
        return MimeUtil2.getMostSpecificMimeType( getNonTouchingMimeUtil2().getMimeTypes( file ) ).toString();
    }

    @Override
    public String getMimeType( URL url )
    {
        return MimeUtil2.getMostSpecificMimeType( getNonTouchingMimeUtil2().getMimeTypes( url ) ).toString();
    }

    @Override
    public String getMimeType( InputStream is )
    {
        return MimeUtil2.getMostSpecificMimeType( getTouchingMimeUtil2().getMimeTypes( is ) ).toString();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( String fileName )
    {
        return this.toStringSet( getNonTouchingMimeUtil2().getMimeTypes( fileName ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( File file )
    {
        return this.toStringSet( getNonTouchingMimeUtil2().getMimeTypes( file ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( URL url )
    {
        return this.toStringSet( getNonTouchingMimeUtil2().getMimeTypes( url ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( InputStream is )
    {
        return toStringSet( getTouchingMimeUtil2().getMimeTypes( is ) );
    }

    // ==

    private Set<String> toStringSet( Collection<MimeType> mimeTypes )
    {
        Set<String> result = new HashSet<String>();
        for ( MimeType mimeType : mimeTypes )
        {
            result.add( mimeType.toString() );
        }
        return result;
    }
}
