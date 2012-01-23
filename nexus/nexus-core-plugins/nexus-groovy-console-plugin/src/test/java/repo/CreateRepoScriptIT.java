/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package repo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import util.GroovyConsoleMessageUtil;

import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptDTO;

public class CreateRepoScriptIT
    extends AbstractNexusIntegrationTest
{

    private RepositoryMessageUtil messageUtil;

    private static File scriptsDir;

    public CreateRepoScriptIT()
        throws Exception
    {
        this.messageUtil = new RepositoryMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        File script = getTestFile( "org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd.groovy" );
        scriptsDir = new File( nexusWorkDir, "scripts" );
        scriptsDir.mkdirs();

        FileUtils.copyFileToDirectory( script, scriptsDir );
    }

    @Test
    public void run()
        throws Exception
    {
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "createTestRepo" );
        resource.setRepoType( "hosted" );
        resource.setName( "Create Test Repo" );
        resource.setProvider( "maven2" );
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        // trigger event expected by groovy script
        this.messageUtil.createRepository( resource );

        String logs = FileUtils.fileRead( new File( nexusLogDir, getTestId() + "/nexus.log" ) );
        Assert.assertTrue( logs.contains( "Groovy console did kicked in!" ) );
    }

    @Test
    public void autoload()
        throws Exception
    {
        List<GroovyScriptDTO> scripts = GroovyConsoleMessageUtil.getScripts();
        Assert.assertNotNull( scripts );
        Assert.assertEquals( 1, scripts.size() );

        FileUtils.fileWrite( new File( scriptsDir, "autoload.groovy" ).getAbsolutePath(), "return 25" );

        Thread.sleep( 5000 );
        scripts = GroovyConsoleMessageUtil.getScripts();
        Assert.assertNotNull( scripts );
        Assert.assertEquals( 2, scripts.size() );
    }
}
