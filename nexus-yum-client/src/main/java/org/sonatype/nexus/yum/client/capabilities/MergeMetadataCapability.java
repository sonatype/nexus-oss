/*
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.client.capabilities;

import org.sonatype.nexus.capabilities.client.Capability;
import org.sonatype.nexus.capabilities.client.spi.CapabilityProperty;
import org.sonatype.nexus.capabilities.client.spi.CapabilityType;

/**
 * Merge Metadata capability.
 *
 * @since 3.0
 */
@CapabilityType( MergeMetadataCapability.TYPE_ID )
public interface MergeMetadataCapability
    extends Capability<MergeMetadataCapability>
{

    String TYPE_ID = "yum.merge";

    @CapabilityProperty( "repository" )
    String repository();

    @CapabilityProperty( "repository" )
    MergeMetadataCapability withRepository( String repository );

}
