/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.configuration.upgrade;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.model.io.xpp3.SecurityConfigurationXpp3Writer;

public class DefaultSecurityConfigurationUpgraderTest extends InjectedTestCase
{
    private final String UPGRADE_HOME = new String("/org/sonatype/security/configuration/upgrade");

    protected final File PLEXUS_HOME = new File(getBasedir(), "target/plexus-home");

    protected final File CONF_HOME = new File(PLEXUS_HOME, "conf");

    private SecurityConfigurationUpgrader configurationUpgrader;

    @Override
    public void configure(Properties properties)
    {
        properties.put("application-conf", CONF_HOME.getAbsolutePath());
        super.configure(properties);
    }

    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory(PLEXUS_HOME);
        CONF_HOME.mkdirs();

        this.configurationUpgrader = (SecurityConfigurationUpgrader) lookup(SecurityConfigurationUpgrader.class);
    }

    @Test
    public void testFrom203()
        throws Exception
    {
        testUpgrade("security-configuration-203.xml");
    }

    private void testUpgrade(String filename)
        throws Exception
    {
        copyFromClasspathToFile(UPGRADE_HOME + "/" + filename, getSecurityConfiguration());

        SecurityConfiguration configuration = configurationUpgrader.loadOldConfiguration(new File(getSecurityConfiguration()));

        assertThat(configuration.getVersion(), is(SecurityConfiguration.MODEL_VERSION));

        resultIsFine(UPGRADE_HOME + "/" + filename, configuration);
    }

    private void resultIsFine(String path, SecurityConfiguration configuration)
        throws Exception
    {
        SecurityConfigurationXpp3Writer w = new SecurityConfigurationXpp3Writer();

        StringWriter sw = new StringWriter();

        w.write(sw, configuration);

        String actual = sw.toString();
        actual = actual.replace("\r\n", "\n");

        String shouldBe = IOUtil.toString(getClass().getResourceAsStream(path + ".result"));
        shouldBe = shouldBe.replace("\r\n", "\n");

        assertThat(actual, is(shouldBe));
    }

    private void copyFromClasspathToFile(String path, String outputFilename)
        throws IOException
    {
        copyFromClasspathToFile(path, new File(outputFilename));
    }

    private void copyFromClasspathToFile(String path, File output)
        throws IOException
    {
        copyFromStreamToFile(getClass().getResourceAsStream(path), output);
    }

    private void copyFromStreamToFile(InputStream is, File output)
        throws IOException
    {
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(output);

            IOUtil.copy(is, fos);
        } finally
        {
            IOUtil.close(is);

            IOUtil.close(fos);
        }
    }

    protected String getSecurityConfiguration()
    {
        return CONF_HOME + "/security-configuration.xml";
    }
}
