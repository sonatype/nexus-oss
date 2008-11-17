package org.sonatype.nexus.scheduling;

import java.util.Set;

public interface RepositoryTaskFilter
    extends TaskFilter
{
    boolean allowsRepositoryScanning( String fromPath );

    boolean allowsContentOperations( Set<RepositoryTaskActivityDescriptor.ModificationOperator> ops );

    boolean allowsAttributeOperations( Set<RepositoryTaskActivityDescriptor.AttributesModificationOperator> ops );
}
