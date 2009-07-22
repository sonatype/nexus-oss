package org.sonatype.nexus.mime;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.component.annotations.Component;

import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

@Component( role = MimeUtil.class )
public class DefaultMimeUtil
    implements MimeUtil
{
    private MimeUtil2 mimeUtil;

    public DefaultMimeUtil()
    {
        mimeUtil = new MimeUtil2();

        mimeUtil.registerMimeDetector( ExtensionMimeDetector.class.getName() );

        // magic will make MIME types OS dependant
        // mimeUtil.registerMimeDetector( MagicMimeMimeDetector.class.getName() );
    }

    public MimeUtil2 getMimeUtil2()
    {
        return mimeUtil;
    }

    public String getMimeType( File file )
    {
        return MimeUtil2.getMostSpecificMimeType( getMimeUtil2().getMimeTypes( file ) ).toString();
    }

    public String getMimeType( URL url )
    {
        return MimeUtil2.getMostSpecificMimeType( getMimeUtil2().getMimeTypes( url ) ).toString();
    }
}
