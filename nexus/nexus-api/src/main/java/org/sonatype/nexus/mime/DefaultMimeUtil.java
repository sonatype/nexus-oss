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

@Component( role = MimeUtil.class )
public class DefaultMimeUtil
    implements MimeUtil
{
    private MimeUtil2 mimeUtil;

    public DefaultMimeUtil()
    {
        mimeUtil = new MimeUtil2();

        // use Extension only for now (speed but less accuracy)
        mimeUtil.registerMimeDetector( ExtensionMimeDetector.class.getName() );
    }

    public MimeUtil2 getMimeUtil2()
    {
        return mimeUtil;
    }

    @Override
    public String getMimeType( String fileName )
    {
        return MimeUtil2.getMostSpecificMimeType( getMimeUtil2().getMimeTypes( fileName ) ).toString();
    }

    @Override
    public String getMimeType( File file )
    {
        return MimeUtil2.getMostSpecificMimeType( getMimeUtil2().getMimeTypes( file ) ).toString();
    }

    @Override
    public String getMimeType( URL url )
    {
        return MimeUtil2.getMostSpecificMimeType( getMimeUtil2().getMimeTypes( url ) ).toString();
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( String fileName )
    {
        return this.toStringSet( getMimeUtil2().getMimeTypes( fileName ) );
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( File file )
    {
        return this.toStringSet( getMimeUtil2().getMimeTypes( file ) );
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( URL url )
    {
        return this.toStringSet( getMimeUtil2().getMimeTypes( url ) );
    }
    

    @Override
    @SuppressWarnings( "unchecked" )
    public Set<String> getMimeTypes( InputStream is )
    {
        return toStringSet( getMimeUtil2().getMimeTypes( is ) );
    }
    
    // ==
    
    private Set<String> toStringSet( Collection<MimeType> mimeTypes)
    {
        Set<String> result = new HashSet<String>();
        for ( MimeType mimeType : mimeTypes )
        {
            result.add( mimeType.toString() );
        }
        return result;
    }
}
