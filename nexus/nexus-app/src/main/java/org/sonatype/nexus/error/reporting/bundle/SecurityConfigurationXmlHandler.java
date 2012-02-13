/**
 */
package org.sonatype.nexus.error.reporting.bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;
import org.sonatype.sisu.pr.bundle.ManagedBundle;
import org.sonatype.sisu.pr.bundle.StorageManager;

@Component(role = BundleAssembler.class, hint = "security-configuration.xml")
public class SecurityConfigurationXmlHandler
    extends AbstractXmlHandler
    implements BundleAssembler
{
    @Requirement
    SecurityConfigurationSource source;
    
    @Requirement
    NexusConfiguration nexusConfig;
    
    @Requirement
    StorageManager storageManager;
    
    @Override
    public boolean isParticipating( IssueSubmissionRequest request )
    {
        return source.getConfiguration() != null;
    }

    @Override
    public Bundle assemble( IssueSubmissionRequest request )
        throws IssueSubmissionException
    {
        SecurityConfiguration configuration = ( SecurityConfiguration )cloneViaXml( source.getConfiguration() );
        
        configuration.setAnonymousPassword( PASSWORD_MASK );
        
        SecurityConfigurationXpp3Writer xppWriter = new SecurityConfigurationXpp3Writer();
        
        Writer writer = null;
        
        try
        {
            ManagedBundle bundle = storageManager.createBundle( "security-configuration.xml", "application/xml" );
            OutputStream out = bundle.getOutputStream();
            writer = new OutputStreamWriter( out );
            xppWriter.write( writer, configuration );
            writer.close();
            return bundle;
        }
        catch (IOException e)
        {
            throw new IssueSubmissionException( "Could not assemble security-configuration.xml-bundle", e );
        }
        finally
        {
            IOUtil.close(writer);
        }
    }
}
