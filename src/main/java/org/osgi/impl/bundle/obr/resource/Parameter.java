/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.osgi.impl.bundle.obr.resource;

class Parameter {
	final static int	ATTRIBUTE	= 1;
	final static int	DIRECTIVE	= 2;
	final static int	SINGLE		= 0;

	int					type;
	String				key;
	String				value;

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(key);
		switch (type) {
			case ATTRIBUTE :
				sb.append("=");
				break;
			case DIRECTIVE :
				sb.append(":=");
				break;
			case SINGLE :
				return sb.toString();
		}
		sb.append(value);
		return sb.toString();
	}

	boolean is(String s, int type) {
		return this.type == type && key.equalsIgnoreCase(s);
	}
}
