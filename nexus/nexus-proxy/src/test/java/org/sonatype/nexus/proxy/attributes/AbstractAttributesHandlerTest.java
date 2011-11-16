/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Test;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * AttributeStorage implementation driven by XStream.
 *
 * @author cstamas
 */
public class AbstractAttributesHandlerTest
    extends AbstractNexusTestEnvironment
{

    protected DefaultAttributesHandler attributesHandler;

    protected Repository repository;

    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.copyDirectoryStructure( new File( getBasedir(), "target/test-classes/repo1" ), new File(
            getBasedir(), "target/test-reposes/repo1" ) );

        attributesHandler = (DefaultAttributesHandler) lookup( AttributesHandler.class );

        repository = lookup( Repository.class, "maven2" );

        CRepository repoConf = new DefaultCRepository();

        repoConf.setProviderRole( Repository.class.getName() );
        repoConf.setProviderHint( "maven2" );
        repoConf.setId( "dummy" );

        repoConf.setLocalStorage( new CLocalStorage() );
        repoConf.getLocalStorage().setProvider( "file" );
        File localStorageDirectory = new File( getBasedir(), "target/test-reposes/repo1" );
        repoConf.getLocalStorage().setUrl( localStorageDirectory.toURI().toURL().toString() );

        Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        repoConf.setExternalConfiguration( exRepo );
        M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration( exRepo );
        exRepoConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        exRepoConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );

        if ( attributesHandler.getAttributeStorage() instanceof DefaultFSAttributeStorage )
        {
            FileUtils.deleteDirectory( ( (DefaultFSAttributeStorage) attributesHandler.getAttributeStorage() ).getWorkingDirectory() );
        }
        else
        {
            FileUtils.deleteDirectory( new File( localStorageDirectory, ".nexus/attributes" ) );
        }

        repository.configure( repoConf );
    }
}
