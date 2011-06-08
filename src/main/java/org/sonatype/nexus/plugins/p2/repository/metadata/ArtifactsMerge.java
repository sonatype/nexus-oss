/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.metadata.Artifacts.Artifact;
import org.sonatype.nexus.plugins.p2.repository.metadata.Content.Unit;


public class ArtifactsMerge
{

    /**
     * Merges artifacts from the other repository. Current implementation requires both repositories to have identical
     * mapping and properties.
     */
    public Artifacts mergeArtifactsMetadata( String name, List<Artifacts> repos )
        throws P2MetadataMergeException
    {
        Artifacts result = new Artifacts( name );

        if ( repos == null || repos.size() <= 0 )
        {
            return result; // nothing to merge
        }

        if ( repos.size() == 1 )
        {
            // TODO do we need/want to handle this specially?
        }

        List<Artifacts.Artifact> mergedArtifacts = new ArrayList<Artifacts.Artifact>();

        LinkedHashMap<String, String> mergedProperties = new LinkedHashMap<String, String>();

        LinkedHashMap<String, String> mergedMappingsMap = new LinkedHashMap<String, String>();
        Set<String> keys = new HashSet<String>();
        for ( Artifacts repo : repos )
        {
            // mergedProperties = mergeProperties( mergedProperties, repo );

            mergeMappings( mergedMappingsMap, repo );

            for ( Artifacts.Artifact artifact : repo.getArtifacts() )
            {
                if ( keys.add( getArtifactKey( artifact ) ) )
                {
                    mergedArtifacts.add( new Artifacts.Artifact( artifact ) );
                }
                // first repo wins
            }
        }

        // handle rule ordering (this potentially creates new map instance, but only if needed)
        mergedMappingsMap = orderMappings( mergedMappingsMap );

        Xpp3Dom mergedMappings = createMappingsDom( mergedMappingsMap );

        mergedProperties.put( P2Constants.PROP_TIMESTAMP, Long.toString( System.currentTimeMillis() ) );
        mergedProperties.put( "publishPackFilesAsSiblings", "true" );

        boolean compressed = P2Constants.ARTIFACTS_PATH.equals( P2Constants.ARTIFACTS_JAR );
        mergedProperties.put( P2Constants.PROP_COMPRESSED, Boolean.toString( compressed ) );

        result.setArtifacts( mergedArtifacts );
        result.setProperties( mergedProperties );
        setMappings( result.getDom(), mergedMappings );

        return result;
    }

    private Xpp3Dom createMappingsDom( Map<String, String> mappingsMap )
    {
        Xpp3Dom mappingsDom = new Xpp3Dom( "mappings" );
        mappingsDom.setAttribute( "size", Integer.toString( mappingsMap.size() ) );

        for ( String filter : mappingsMap.keySet() )
        {
            Xpp3Dom ruleDom = new Xpp3Dom( "rule" );
            ruleDom.setAttribute( "filter", filter );
            ruleDom.setAttribute( "output", mappingsMap.get( filter ) );
            mappingsDom.addChild( ruleDom );
        }

        return mappingsDom;
    }

    private Xpp3Dom createMappingsDom()
    {
        Xpp3Dom mappingsDom = new Xpp3Dom( "mappings" );
        Xpp3Dom ruleDom;

        // <rule filter='(&amp; (classifier=osgi.bundle) (format=packed))'
        // output='${repoUrl}/plugins/${id}_${version}.jar.pack.gz'/>

        // <rule filter='(&amp; (classifier=osgi.bundle))' output='${repoUrl}/plugins/${id}_${version}.jar'/>
        ruleDom = new Xpp3Dom( "rule" );
        ruleDom.setAttribute( "filter", "(& (classifier=osgi.bundle))" );
        ruleDom.setAttribute( "output", "${repoUrl}/plugins/${id}_${version}.jar" );
        mappingsDom.addChild( ruleDom );

        // <rule filter='(&amp; (classifier=binary))' output='${repoUrl}/binary/${id}_${version}'/>
        ruleDom = new Xpp3Dom( "rule" );
        ruleDom.setAttribute( "filter", "(& (classifier=binary))" );
        ruleDom.setAttribute( "output", "${repoUrl}/binary/${id}_${version}" );
        mappingsDom.addChild( ruleDom );

        // <rule filter='(&amp; (classifier=org.eclipse.update.feature))'
        // output='${repoUrl}/features/${id}_${version}.jar'/>
        ruleDom = new Xpp3Dom( "rule" );
        ruleDom.setAttribute( "filter", "(& (classifier=org.eclipse.update.feature))" );
        ruleDom.setAttribute( "output", "${repoUrl}/features/${id}_${version}.jar" );
        mappingsDom.addChild( ruleDom );

        mappingsDom.setAttribute( "size", Integer.toString( mappingsDom.getChildCount() ) );

        return mappingsDom;
    }

    private LinkedHashMap<String, String> orderMappings( LinkedHashMap<String, String> mergedMappingsMap )
        throws P2MetadataMergeException
    {
        // detect the presence of format=packed rules having filter attributes as:
        // "(classifier=osgi.bundle) (format=packed)"
        // "(classifier=osgi.bundle)"
        // Note: this is not limited to bundles, features and who knows what else can be packed too
        // IF present, reshuffle the map, otherwise just return the passed in mergedMappingsMap instance

        final String packedFilter = "(format=packed)";
        boolean hasPackedRule = false;

        for ( String filter : mergedMappingsMap.keySet() )
        {
            if ( filter.contains( packedFilter ) )
            {
                hasPackedRule = true;
                break;
            }
        }

        if ( !hasPackedRule )
        {
            return mergedMappingsMap;
        }

        final LinkedHashMap<String, String> ordered = new LinkedHashMap<String, String>( mergedMappingsMap.size() );

        for ( Map.Entry<String, String> entry : mergedMappingsMap.entrySet() )
        {
            // add all with "(format=packed)" first (bundles, features, etc)
            if ( entry.getKey().contains( packedFilter ) )
            {
                ordered.put( entry.getKey(), entry.getValue() );
            }
        }
        for ( Map.Entry<String, String> entry : mergedMappingsMap.entrySet() )
        {
            // add all the rest, without "(format=packed)" after
            if ( !entry.getKey().contains( packedFilter ) )
            {
                // check is non-packed rule already present
                ordered.put( entry.getKey(), entry.getValue() );
            }
        }

        return ordered;
    }

    private void mergeMappings( LinkedHashMap<String, String> mergedMappingsMap, Artifacts repo )
        throws P2MetadataMergeException
    {
        Xpp3Dom repoMappingsDom = repo.getDom().getChild( "mappings" );
        if ( repoMappingsDom == null )
        {
            // Nothing to merge
            return;
        }

        Xpp3Dom[] repoMappingRules = repoMappingsDom.getChildren( "rule" );
        for ( Xpp3Dom repoMappingRule : repoMappingRules )
        {
            String filter = repoMappingRule.getAttribute( "filter" );
            String output = repoMappingRule.getAttribute( "output" );

            if ( mergedMappingsMap.containsKey( filter ) )
            {
                // Known rule
                System.out.println( "mergeMappings found existing rule: filter=" + filter + ", output=" + output );
                if ( !output.equals( mergedMappingsMap.get( filter ) ) )
                {
                    throw new P2MetadataMergeException( "Incompatible artifact repository mapping rules: filter="
                        + filter + ", output1=" + output + ", output2=" + mergedMappingsMap.get( filter ) );
                }
            }
            else
            {
                // New rule
                System.out.println( "mergeMappings found new rule: filter=" + filter + ", output=" + output );
                mergedMappingsMap.put( filter, output );
            }
        }
    }

    private LinkedHashMap<String, String> mergeProperties( LinkedHashMap<String, String> mergedProperties,
                                                           AbstractMetadata repo )
        throws P2MetadataMergeException
    {
        // make sure properties are the same
        LinkedHashMap<String, String> properties = getProperties( repo );
        if ( mergedProperties == null )
        {
            mergedProperties = properties;
        }
        else
        {
            if ( !mergedProperties.equals( properties ) )
            {
                throw new P2MetadataMergeException( "Incompatible repository properties" );
            }
        }
        return mergedProperties;
    }

    private void setMappings( Xpp3Dom dom, Xpp3Dom mergedMappings )
    {
        AbstractMetadata.removeChild( dom, "mappings" );
        dom.addChild( mergedMappings );
    }

    private LinkedHashMap<String, String> getProperties( AbstractMetadata repo )
    {
        LinkedHashMap<String, String> properties = repo.getProperties();
        properties.remove( P2Constants.PROP_TIMESTAMP );
        return properties;
    }

    private String getArtifactKey( Artifact artifact )
    {
        final String format = artifact.getFormat();

        if ( format != null && format.trim().length() > 0 )
        {
            return artifact.getClassifier() + ":" + artifact.getId() + ":" + artifact.getVersion() + ":" + format;
        }
        else
        {
            return artifact.getClassifier() + ":" + artifact.getId() + ":" + artifact.getVersion();
        }
    }

    public Content mergeContentMetadata( String name, ArrayList<Content> repos )
        throws P2MetadataMergeException
    {
        Content result = new Content( name );

        if ( repos == null || repos.size() <= 0 )
        {
            return result; // nothing to merge
        }

        if ( repos.size() == 1 )
        {
            // TODO do we need/want to handle this specially?
        }

        List<Content.Unit> mergedUnits = new ArrayList<Content.Unit>();

        LinkedHashMap<String, String> mergedProperties = new LinkedHashMap<String, String>();

        Set<String> keys = new HashSet<String>();
        for ( Content repo : repos )
        {
            // mergedProperties = mergeProperties( mergedProperties, repo );

            for ( Content.Unit unit : repo.getUnits() )
            {
                if ( keys.add( getUnitKey( unit ) ) )
                {
                    mergedUnits.add( new Content.Unit( unit ) );
                }
                // first repo wins
            }
        }

        mergedProperties.put( P2Constants.PROP_TIMESTAMP, Long.toString( System.currentTimeMillis() ) );

        boolean compressed = P2Constants.ARTIFACTS_PATH.equals( P2Constants.ARTIFACTS_JAR );
        mergedProperties.put( P2Constants.PROP_COMPRESSED, Boolean.toString( compressed ) );

        result.setUnits( mergedUnits );
        result.setProperties( mergedProperties );

        return result;
    }

    private String getUnitKey( Unit unit )
    {
        return unit.getId() + ":" + unit.getVersion();
    }

}
