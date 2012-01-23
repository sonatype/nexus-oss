/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
