/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
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
package org.sonatype.nexus.proxy.storage.remote.apachehttp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;

/**
 * The Class ApacheHttpClientInputStream.
 */
public class ApacheHttpClientInputStream extends InputStream {

	/** The entity. */
	private HttpEntity entity;

	/** The is. */
	private InputStream is;

	/**
     * Instantiates a new http client input stream.
     * 
     * @param entity the entity
     * @param is the is
     */
	public ApacheHttpClientInputStream(HttpEntity entity, InputStream is) {
		super();
		this.is = is;
		this.entity= entity;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		return is.read();
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		try {
			super.close();
			is.close();
		} finally {
		    entity.consumeContent();
		}
	}

}
