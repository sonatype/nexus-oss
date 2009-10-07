package org.sonatype.nexus.mock.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ContainerUtil
{

    public static Map<Object, Object> createContainerContext()
    {
        Map<Object, Object> containerContext = new HashMap<Object, Object>();

        containerContext.put( "basedir", new File( "" ).getAbsolutePath() );

        containerContext.put( "nexus-work", new File( "target/nexus-work" ).getAbsolutePath() );
 
        containerContext.put( "application-conf", new File( "target/nexus-work/conf/" ).getAbsolutePath() );
        containerContext.put( "security-xml-file", new File( "target/nexus-work/conf/security.xml" ).getAbsolutePath() );

        containerContext.put( "index.template.file", "templates/index-debug.vm" );

        // for EHCache component
        System.setProperty( "nexus.home", new File( "target/nexus-work" ).getAbsolutePath() );
        System.setProperty( "plexus.log4j-prop-file",
                            new File( "target/test-classes/log4j.properties" ).getAbsolutePath() );

        return containerContext;
    }
}
