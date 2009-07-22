package org.sonatype.nexus.mime;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.component.annotations.Component;

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

        // use Extenaion only for now (speed but less accuracy)
        mimeUtil.registerMimeDetector( ExtensionMimeDetector.class.getName() );
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
