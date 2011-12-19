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
package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

import static org.sonatype.nexus.plugins.capabilities.api.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.p2.repository.internal.capabilities.P2MetadataGeneratorCapability.TYPE_ID;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.AbstractCapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;

@Singleton
@Named( TYPE_ID )
public class P2MetadataGeneratorCapabilityDescriptor
    extends AbstractCapabilityDescriptor
    implements CapabilityDescriptor
{

    private static final CapabilityType TYPE = capabilityType( TYPE_ID );

    public P2MetadataGeneratorCapabilityDescriptor()
    {
        super(
            TYPE,
            "P2 Metadata Generator capability",
            "Automatically generates P2 metadata for OSGi bundles deployed to selected repository",
            new RepoOrGroupComboFormField( P2MetadataGeneratorConfiguration.REPO_OR_GROUP_ID, FormField.MANDATORY )
        );
    }

}
