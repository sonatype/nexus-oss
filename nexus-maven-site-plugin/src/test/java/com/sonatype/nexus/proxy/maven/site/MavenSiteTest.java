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
package com.sonatype.nexus.proxy.maven.site;

import java.io.FileReader;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

public class MavenSiteTest
    extends AbstractNexusTestCase
{

    @Test
    public void testCreateSiteRepoWithTemplate()
        throws Exception
    {
        Nexus nexus = this.lookup( Nexus.class );

        RepositoryTemplate template = nexus.getRepositoryTemplateById( "maven-site" );

        template.getConfigurableRepository().setId( "test-site-repo" );
        template.getConfigurableRepository().setName( "Maven Sites" );

        Repository repo = template.create();

        nexus.getNexusConfiguration().saveConfiguration();

        // now check the config to see what we have
        NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();
        FileReader fileReader = null;

        Configuration config = null;
        try
        {
            fileReader = new FileReader( this.getNexusConfiguration() );
            config = reader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        CRepository siteRepo = null;

        for ( CRepository cRepo : config.getRepositories() )
        {
            if ( cRepo.getId().equals( "test-site-repo" ) )
            {
                siteRepo = cRepo;
            }
        }

        Assert.assertNotNull( "Could not find the site repo in: " + this.getNexusConfiguration(), siteRepo );

        Assert.assertEquals( "Maven Sites", siteRepo.getName() );
//        Assert.assertNull( siteRepo.getExternalConfiguration() );

        // there are too many places that check null, so for now, this cannot be null
        Assert.assertNotNull( siteRepo.getExternalConfiguration() );

    }

}
