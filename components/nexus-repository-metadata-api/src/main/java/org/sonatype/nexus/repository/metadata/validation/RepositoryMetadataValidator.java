/**
 * Copyright (c) 2007-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License Version 1.0, which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.repository.metadata.validation;

import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

public interface RepositoryMetadataValidator
{
    void validate( RepositoryMetadata metadata )
        throws ValidationException;
}
