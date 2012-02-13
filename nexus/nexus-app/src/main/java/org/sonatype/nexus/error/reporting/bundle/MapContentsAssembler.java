package org.sonatype.nexus.error.reporting.bundle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.sonatype.nexus.error.reporting.ErrorReportRequest;
import org.sonatype.sisu.pr.bundle.AbstractBundle;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;

@Component( role = BundleAssembler.class, hint = "context" )
public class MapContentsAssembler
    implements BundleAssembler
{

    @Override
    public boolean isParticipating( final IssueSubmissionRequest request )
    {

        return request.getContext() != null && request.getContext() instanceof ErrorReportRequest;
    }

    @Override
    public Bundle assemble( final IssueSubmissionRequest request )
        throws IssueSubmissionException
    {
        return new MapContentsBundle( ((ErrorReportRequest)request.getContext()).getContext() );
    }

    public static class MapContentsBundle
        extends AbstractBundle
    {

        private static final String LINE_SEPERATOR = System.getProperty( "line.separator" );

        private byte[] content;

        public MapContentsBundle( Map<String, Object> context )
        {
            super( "contextListing.txt", "text/plain" );

            StringBuilder sb = new StringBuilder();

            for ( String key : context.keySet() )
            {
                sb.append( "key: " + key );
                sb.append( LINE_SEPERATOR );

                Object o = context.get( key );
                sb.append( "value: " + o == null ? "null" : o.toString() );
                sb.append( LINE_SEPERATOR );
                sb.append( LINE_SEPERATOR );
            }

            try
            {
                this.content = sb.toString().getBytes( "utf-8" );
            }
            catch ( UnsupportedEncodingException e )
            {
                // use default platform encoding
                this.content = sb.toString().getBytes();
            }
        }

        @Override
        protected InputStream openStream()
            throws IOException
        {
            return new ByteArrayInputStream( content );
        }

        @Override
        public long getContentLength()
        {
            return content.length;
        }

    }
}
