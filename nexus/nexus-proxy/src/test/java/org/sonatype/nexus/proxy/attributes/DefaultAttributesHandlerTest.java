/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

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

        repository = (Repository) lookup( Repository.class, "maven2" );

        repository.setId( "dummy" );

        repository.setLocalUrl( new File( getBasedir(), "target/test-reposes/repo1" ).toURI().toURL().toString() );

        DefaultFSLocalRepositoryStorage ls = (DefaultFSLocalRepositoryStorage) lookup(
            LocalRepositoryStorage.class,
            "file" );

        repository.setLocalStorage( ls );
    }

    public void testRecreateAttrs()
        throws Exception
    {
        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid(
            repository,
            "/activemq/activemq-core/1.2/activemq-core-1.2.jar" );

        assertFalse( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid ).exists() );

        repository.recreateAttributes( null, null );

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

        repository.recreateAttributes( null, customAttrs );

        assertTrue( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid ).exists() );

        AbstractStorageItem item = attributesHandler.getAttributeStorage().getAttributes( uid );

        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        assertEquals( "1", item.getAttributes().get( "one" ) );

        assertEquals( "2", item.getAttributes().get( "two" ) );
    }
}
