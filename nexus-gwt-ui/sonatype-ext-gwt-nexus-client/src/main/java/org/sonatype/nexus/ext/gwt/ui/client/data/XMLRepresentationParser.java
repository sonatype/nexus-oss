package org.sonatype.nexus.ext.gwt.ui.client.data;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class XMLRepresentationParser implements RepresentationParser {
    
    public Entity parseEntity(String representation, EntityFactory factory) {
        return parseEntity(representation, factory.create());
    }
    
    public Entity parseEntity(String representation, Entity entity) {
        Document doc = XMLParser.parse(representation);
        return parse(doc.getElementsByTagName("data").item(0), entity);
    }
    
    public List<Entity> parseEntityList(String representation, EntityFactory factory) {
        List<Entity> result = new ArrayList<Entity>();
        
        Document doc = XMLParser.parse(representation);
        
        NodeList nodes = doc.getElementsByTagName(factory.create().getType());
        
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(parse(nodes.item(i), factory.create()));
        }
        
        return result;
    }

    private Entity parse(Node node, Entity entity) {
        NodeList nodes = node.getChildNodes();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            String nodeName = n.getNodeName();
            
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Class fieldType = entity.getFieldType(nodeName);
                Object value = null;
                
                //TODO: handle all primitive wrapper type
                if (String.class.equals(fieldType)) {
                    value = n.getFirstChild().getNodeValue();
                } else if (Integer.class.equals(fieldType)) {
                    value = Integer.valueOf(n.getFirstChild().getNodeValue());
                } else if (fieldType != null) {
                    Entity childEntity = entity.createEntity(nodeName);
                    if (childEntity != null) {
                        value = parse(n, childEntity);
                    }
                }
                
                if (value != null) {
                    entity.set(nodeName, value);
                }
            }
        }
        
        if (entity instanceof Initializable) {
            ((Initializable) entity).initialize();
        }
        
        return entity;
    }
    
    public String serializeEntity(String root, Entity entity) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<" + root + ">");
        sb.append("<data>");
        
        serialize(entity, sb);
        
        sb.append("</data>");
        sb.append("</" + root + ">");
        
        return sb.toString();
    }
    
    public void serialize(Entity entity, StringBuilder sb) {
        //TODO: don't serialize properties not belong to the resource
        for (String propertyName : entity.getPropertyNames()) {
            Object value = entity.get(propertyName);
            if (value != null) {
                sb.append("<" + propertyName + ">");
                if (value instanceof Entity) {
                    serialize((Entity) value, sb);
                } else {
                    sb.append(value.toString());
                }
                sb.append("</" + propertyName + ">");
            }
        }
    }
    
}
