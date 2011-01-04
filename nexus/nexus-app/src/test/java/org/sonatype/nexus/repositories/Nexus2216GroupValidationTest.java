/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repositories;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

public class Nexus2216GroupValidationTest
    extends AbstractNexusTestCase
{
    // we need some stuff to prepare
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    // ==

    public void testInvertedOrdering()
        throws Exception
    {
        // mangle config
        mangleConfiguration();

        try
        {
            // lookup nexus, this will do all sort of things, amongst them validate the config
            lookup( Nexus.class );

            RepositoryRegistry repositoryRegistry = lookup( RepositoryRegistry.class );

            MavenGroupRepository publicGroup =
                repositoryRegistry.getRepositoryWithFacet( "public", MavenGroupRepository.class );
            
            List<String> memberIds = new ArrayList<String>();
            for ( Repository repo : publicGroup.getMemberRepositories() )
            {
                memberIds.add(  repo.getId() );
            }
            assertEquals( "Repo object list returned a different set of repos", publicGroup.getMemberRepositoryIds(), memberIds );
            
            assertEquals( "The config should be correct, ids found are: "+ publicGroup.getMemberRepositoryIds(), 9, publicGroup.getMemberRepositories().size() );
        }
        catch ( Exception e )
        {
            fail( "Should succeed!" );
        }
    }

    // ==

    protected void mangleConfiguration()
        throws IOException, XmlPullParserException
    {
        // copy the defaults
        copyDefaultConfigToPlace();

        File configFile = new File( getNexusConfiguration() );

        // raw load the config file in place
        FileReader fileReader = new FileReader( configFile );

        NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

        Configuration config = reader.read( fileReader );

        fileReader.close();

        CRepository publicGroup = null;

        // simple put the "public" group (that reference other reposes) as 1st!
        for ( CRepository repository : config.getRepositories() )
        {
            if ( "public".equals( repository.getId() ) )
            {
                publicGroup = repository;

                break;
            }
        }

        if ( publicGroup == null )
        {
            fail( "Public group not found in default configuration?" );
        }

        config.getRepositories().remove( publicGroup );

        config.getRepositories().add( 0, publicGroup );

        // raw save the modified config
        FileWriter fileWriter = new FileWriter( configFile );

        NexusConfigurationXpp3Writer writer = new NexusConfigurationXpp3Writer();

        writer.write( fileWriter, config );

        fileWriter.flush();

        fileWriter.close();
    }
}
