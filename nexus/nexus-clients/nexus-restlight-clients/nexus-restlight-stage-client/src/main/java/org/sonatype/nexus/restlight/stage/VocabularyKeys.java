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
package org.sonatype.nexus.restlight.stage;

/**
 * Constant library that contains all of the vocabulary elements that can vary for the staging client.
 */
public final class VocabularyKeys
{

    /**
     * This is the root element of the promote, drop, and finish staging actions. In Nexus Professional 1.3.1, it was
     * <code>com.sonatype.nexus.staging.api.dto.StagingPromoteRequestDTO</code>, but starting in Nexus Professional
     * 1.3.2, the element has been simplified to <code>promoteRequest</code>.
     */
    public static final String PROMOTE_STAGE_REPO_ROOT_ELEMENT = "promoteRepository.rootElement";
    
    /**
     * This is the root element of the bulk staging actions. In Nexus Professional 1.7.1, it was
     * <code>com.sonatype.nexus.staging.api.dto.StagingActionRequestDTO</code>, but starting in Nexus Professional
     * 1.7.2, the element has been simplified to <code>stagingActionRequest</code>.
     */
    public static final String BULK_ACTION_REQUEST_ROOT_ELEMENT = "bulkActionRequest.rootElement";
    
    /**
     * This is the description element name to use when finishing a staged repository. In Nexus Professional 1.3.1, it
     * wasn't used at all, so the value is {@link VocabularyKeys#SUPPRESS_ELEMENT_VALUE}. In Nexus Professional 1.3.2+,
     * the element is <code>description</code>.
     */
    public static final String PROMOTE_STAGE_REPO_DESCRIPTION_ELEMENT = "promoteRepository.descriptionElement";

    /**
     * Flag value that tells the REST client to suppress that element.
     */
    public static final String SUPPRESS_ELEMENT_VALUE = "NONE";

    private VocabularyKeys()
    {
    }

}
