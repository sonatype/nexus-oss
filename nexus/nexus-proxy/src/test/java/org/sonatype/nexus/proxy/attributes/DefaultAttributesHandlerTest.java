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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * AttributeStorage implementation driven by XStream.
 * 
 * @author cstamas
 */
public class DefaultAttributesHandlerTest
    extends AbstractAttributesHandlerTest
{

    @Test
    public void testRecreateAttrs()
        throws Exception
    {
        RepositoryItemUid uid =
            getRepositoryItemUidFactory().createUid( repository, "/activemq/activemq-core/1.2/activemq-core-1.2.jar" );

        assertThat( attributesHandler.getAttributeStorage().getAttributes( uid ), nullValue() );

        repository.recreateAttributes( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ), null );

        assertThat( attributesHandler.getAttributeStorage().getAttributes( uid ), notNullValue() );
    }

    @Test
    public void testRecreateAttrsWithCustomAttrs()
        throws Exception
    {
        RepositoryItemUid uid =
            getRepositoryItemUidFactory().createUid( repository, "/activemq/activemq-core/1.2/activemq-core-1.2.jar" );

        assertThat( attributesHandler.getAttributeStorage().getAttributes( uid ), nullValue() );

        Map<String, String> customAttrs = new HashMap<String, String>();
        customAttrs.put( "one", "1" );
        customAttrs.put( "two", "2" );

        repository.recreateAttributes( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ), customAttrs );

        assertThat( attributesHandler.getAttributeStorage().getAttributes( uid ), notNullValue() );

        AbstractStorageItem item = attributesHandler.getAttributeStorage().getAttributes( uid );

        assertThat( "Item should be assignable from StorageFileItem class",
            StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        assertThat( item.getAttributes().get( "one" ), equalTo( "1" ) );
        assertThat( item.getAttributes().get( "two" ), equalTo( "2" ) );
    }
}
