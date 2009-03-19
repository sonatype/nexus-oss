/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
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
public class DefaultAttributesHandlerTest
    extends AbstractNexusTestEnvironment
{

    protected DefaultAttributesHandler attributesHandler;

    protected Repository repository;

    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.copyDirectoryStructure( new File( getBasedir(), "target/test-classes/repo1" ), new File(
            getBasedir(),
            "target/test-reposes/repo1" ) );

        attributesHandler = (DefaultAttributesHandler) lookup( AttributesHandler.class );

        FileUtils.deleteDirectory( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getWorkingDirectory() );

        repository = lookup( Repository.class, "maven2" );

        CRepository repoConf = new DefaultCRepository();

        repoConf.setProviderRole( Repository.class.getName() );
        repoConf.setProviderHint( "maven2" );
        repoConf.setId( "dummy" );

        repoConf.setLocalStorage( new CLocalStorage() );
        repoConf.getLocalStorage().setProvider( "file" );
        repoConf.getLocalStorage().setUrl(
            new File( getBasedir(), "target/test-reposes/repo1" ).toURI().toURL().toString() );

        Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        repoConf.setExternalConfiguration( exRepo );
        M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration( exRepo );
        exRepoConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        exRepoConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );

        repository.configure( repoConf );
    }

    public void testRecreateAttrs()
        throws Exception
    {
        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid(
            repository,
            "/activemq/activemq-core/1.2/activemq-core-1.2.jar" );

        assertFalse( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid ).exists() );

        repository.recreateAttributes( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ), null );

        assertTrue( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid ).exists() );
    }

    public void testRecreateAttrsWithCustomAttrs()
        throws Exception
    {
        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid(
            repository,
            "/activemq/activemq-core/1.2/activemq-core-1.2.jar" );

        assertFalse( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid ).exists() );

        Map<String, String> customAttrs = new HashMap<String, String>();
        customAttrs.put( "one", "1" );
        customAttrs.put( "two", "2" );

        repository.recreateAttributes( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ), customAttrs );

        assertTrue( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid ).exists() );

        AbstractStorageItem item = attributesHandler.getAttributeStorage().getAttributes( uid );

        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        assertEquals( "1", item.getAttributes().get( "one" ) );

        assertEquals( "2", item.getAttributes().get( "two" ) );
    }
}
