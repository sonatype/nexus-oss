/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Processes requests for storing, listing, accessing and deleting raw binaries.
 *
 * PUT operations (to unused URIs) store the request body as a binary file.  GET operations return the binary previously
 * uploaded to that URI. If none exists, then a list of binaries that start with that path is returned.  DELETE requests
 * delete binaries.
 *
 * @since 3.0
 */
package org.sonatype.nexus.views.rawbinaries;
