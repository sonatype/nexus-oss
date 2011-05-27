/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.osgi.impl.bundle.obr.resource;

import java.util.*;

public class StringSet extends HashSet {
	static final long	serialVersionUID	= 1L;

	public StringSet(String set) {
		StringTokenizer st = new StringTokenizer(set, ",");
		while (st.hasMoreTokens())
			add(st.nextToken().trim());
	}
}
