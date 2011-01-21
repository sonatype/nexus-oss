package org.sonatype.nexus.proxy.maven.metadata.operations;

import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility.Version;

public abstract class AbstractMetadataOperation
    implements MetadataOperation
{
    private final AbstractOperand operand;

    public AbstractMetadataOperation( AbstractOperand operand )
    {
        this.operand = operand;
    }

    public Version getOperandModelVersion()
    {
        return operand.getOriginModelVersion();
    }
}
