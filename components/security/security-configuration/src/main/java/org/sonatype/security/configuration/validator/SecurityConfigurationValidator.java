/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.configuration.validator;

import java.util.List;

import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.configuration.model.SecurityConfiguration;

public interface SecurityConfigurationValidator
{
    ValidationResponse validateModel( SecurityValidationContext context,
                                      ValidationRequest<SecurityConfiguration> request );

    ValidationResponse validateAnonymousUsername( SecurityValidationContext context, String anonymousUsername );

    ValidationResponse validateAnonymousPassword( SecurityValidationContext context, String anonymousPassword );

    ValidationResponse validateRealms( SecurityValidationContext context, List<String> realms );
}
