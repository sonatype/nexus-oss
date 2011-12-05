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
package org.sonatype.nexus.proxy.repository.validator;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEventFailed;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

@Component( role = ItemContentValidator.class, hint = "FileTypeItemContentValidator" )
public class FileTypeItemContentValidator
    implements ItemContentValidator
{
    @Requirement
    private FileTypeValidatorHub validatorHub;

    public boolean isRemoteItemContentValid( final ProxyRepository proxy, final ResourceStoreRequest request,
                                             final String baseUrl, final AbstractStorageItem item,
                                             final List<RepositoryItemValidationEvent> events )
    {
        if ( !proxy.isFileTypeValidation() )
        {
            // make sure this is enabled before we check.
            return true;
        }

        final boolean result = validatorHub.isExpectedFileType( item );

        events.add( new RepositoryItemValidationEventFailed( proxy, item, String.format( "Invalid file type.",
            item.getRepositoryItemUid().toString() ) ) );

        return result;
    }
}
