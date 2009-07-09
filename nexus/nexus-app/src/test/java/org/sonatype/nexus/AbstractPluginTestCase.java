package org.sonatype.nexus;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleaner;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerRequest;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerResponse;

public abstract class AbstractPluginTestCase
    extends AbstractNexusTestCase
{

    private String[] sourceDirectories = { "target/classes", "target/test-classes" };

    protected void setupContainer()
    {
        super.setupContainer();

        try
        {
            // look through all the main and test classes, and create component descriptors if needed
            PlexusComponentGleaner plexusComponentGleaner = new PlexusComponentGleaner();
            List<String> projectClassNames = this.buildProjectClassList();

            

            for ( String className : projectClassNames )
            {
                PlexusComponentGleanerRequest request = new PlexusComponentGleanerRequest( className, ClassLoader
                    .getSystemClassLoader() );

                PlexusComponentGleanerResponse gleanerResponse = plexusComponentGleaner.glean( request );

                if ( gleanerResponse != null  && gleanerResponse.getComponentDescriptor() != null )
                {
                    ComponentDescriptor<?> componentDescriptor = gleanerResponse.getComponentDescriptor();
//                    System.out.println( "... ... adding component role=\"" + componentDescriptor.getRole()
//                        + "\", hint=\"" + componentDescriptor.getRoleHint() + "\"" );
//                    System.out.println( new XStream().toXML( componentDescriptor ) );

                    this.getContainer().addComponentDescriptor( componentDescriptor );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Failed to glean classes" );
        }
    }

    private List<String> buildProjectClassList()
    {
        List<String> classNames = new ArrayList<String>();

        for ( String sourceDir : this.sourceDirectories )
        {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( new File( sourceDir ) );
            scanner.addDefaultExcludes();
            scanner.setIncludes( new String[] { "**/*.class" } );

            scanner.scan();

            for ( String fileNameOfClass : scanner.getIncludedFiles() )
            {
                String className = fileNameOfClass.substring( 0, fileNameOfClass.lastIndexOf( ".class" ) ).replaceAll( "\\\\|/", "." );
                classNames.add( className );
            }
        }
        return classNames;
    }
}
