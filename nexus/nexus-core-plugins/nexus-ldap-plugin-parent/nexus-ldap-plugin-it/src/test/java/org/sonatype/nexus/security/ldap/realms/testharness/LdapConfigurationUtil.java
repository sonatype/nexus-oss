/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.testharness;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

import org.sonatype.security.ldap.realms.persist.model.Configuration;
import org.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Reader;

public class LdapConfigurationUtil
{
    public static Configuration getConfiguration() throws IOException, XmlPullParserException
    {
        File ldapConfig =  new File(  AbstractNexusIntegrationTest.WORK_CONF_DIR , "/ldap.xml" );
        return getConfiguration(ldapConfig);
    }

    public static Configuration getConfiguration( File configurationFile ) throws IOException, XmlPullParserException
    {

        Reader fr = null;
        FileInputStream is = null;
        Configuration configuration = null;

        try
        {
            is = new FileInputStream( configurationFile );

            LdapConfigurationXpp3Reader reader = new LdapConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            configuration = reader.read( fr );
        }
        finally
        {
            if ( fr != null )
            {
                try
                {
                    fr.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }

            if ( is != null )
            {
                try
                {
                    is.close();
                }
                catch ( IOException e )
                {
                    // just closing if open
                }
            }
        }

        return configuration;
    }

}
