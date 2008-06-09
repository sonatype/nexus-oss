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
package org.sonatype.nexus.timeline;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.NexusService;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;

public interface Timeline
    extends NexusService, ConfigurationChangeListener
{
    String ROLE = Timeline.class.getName();

    void add( String type, String subType, Map<String, String> data );

    void addAll( String type, String subType, Collection<Map<String, String>> data );

    void add( long timestamp, String type, String subType, Map<String, String> data );

    void addAll( long timestamp, String type, String subType, Collection<Map<String, String>> data );

    void purgeAll();

    void purgeAll( Set<String> types );

    void purgeAll( Set<String> types, Set<String> subTypes );

    void purgeOlderThan( long timestamp );

    void purgeOlderThan( long timestamp, Set<String> types );

    void purgeOlderThan( long timestamp, Set<String> types, Set<String> subTypes );

    List<Map<String, String>> retrieveNewest( int count, Set<String> types );

    List<Map<String, String>> retrieveNewest( int count, Set<String> types, Set<String> subtypes );

    List<Map<String, String>> retrieve( long from, int count, Set<String> types );

    List<Map<String, String>> retrieve( long from, int count, Set<String> types, Set<String> subtypes );
}
