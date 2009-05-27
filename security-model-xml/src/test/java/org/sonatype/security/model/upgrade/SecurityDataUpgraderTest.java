package org.sonatype.security.model.upgrade;

import java.io.File;
import java.io.StringWriter;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.security.model.AbstractSecurityConfigTest;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Writer;

public class SecurityDataUpgraderTest
    extends AbstractSecurityConfigTest
{

    protected SecurityConfigurationUpgrader configurationUpgrader;

    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.cleanDirectory( new File( getSecurityConfiguration() ).getParentFile() );

        this.configurationUpgrader = (SecurityConfigurationUpgrader) lookup( SecurityConfigurationUpgrader.class );
    }

    protected void resultIsFine( String path, Configuration configuration )
        throws Exception
    {
        SecurityConfigurationXpp3Writer w = new SecurityConfigurationXpp3Writer();

        StringWriter sw = new StringWriter();

        w.write( sw, configuration );

        String shouldBe = IOUtil.toString( getClass().getResourceAsStream( path + ".result" ) );

        assertEquals( shouldBe, sw.toString() );
    }

    public void testFrom100()
        throws Exception
    {
        copyFromClasspathToFile( "/org/sonatype/security/model/upgrade/data-upgrade/security.xml", getSecurityConfiguration() );

        Configuration configuration = configurationUpgrader
            .loadOldConfiguration( new File( getSecurityConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/security/model/upgrade/data-upgrade/security.xml", configuration );
    }

}
