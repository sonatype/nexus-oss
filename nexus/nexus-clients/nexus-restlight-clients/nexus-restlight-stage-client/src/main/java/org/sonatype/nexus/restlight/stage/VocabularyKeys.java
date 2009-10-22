/*
 * Nexus: RESTLight Client
 * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
