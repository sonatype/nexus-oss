package org.sonatype.nexus.plugins.rrb;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.rrb.MavenRepositoryReader.Data;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;
import com.sdicons.json.model.JSONValue;

/**
 * A REST resource for retrieving directories from a remote repository. By
 * default, this will automatically be mounted at:
 * http://host:port/nexus/service/local/remotebrowser .
 */
//@Component( role = PlexusResource.class, hint = "protected" )
public class RemoteBrowserResource extends AbstractNexusPlexusResource implements PlexusResource {

	private final Logger logger = LoggerFactory.getLogger(RemoteBrowserResource.class);
	
    @Override
    public Object getPayloadInstance() {
        // if you allow PUT or POST you would need to return your object.
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection() {
        // Allow anonymous access
        return new PathProtectionDescriptor(this.getResourceUri(), "anon");
    }

    @Override
    public String getResourceUri() {
        return "/remotebrowser";
    }

    @Override
    public Object get(Context context, Request request, Response response, Variant variant) throws ResourceException{
    	
    	String query = request.getResourceRef().getQuery();
    	String id = getId(query);
        String remoteUrl = getRemoteUrl(query);
        ProxyRepository proxyRepository=null;
        try {
//			proxyRepository = getRepositoryRegistry().getRepositoryWithFacet( id, ProxyRepository.class );
        	proxyRepository = getUnprotectedRepositoryRegistry().getRepositoryWithFacet( id, ProxyRepository.class );
		} catch (NoSuchRepositoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        MavenRepositoryReader mr = new MavenRepositoryReader();
        Data data = mr.new Data();
        data.setData(mr.extract(remoteUrl, request.getResourceRef().toString(false, false), proxyRepository,id));
        String returnValue;
        try {
            JSONValue value = JSONMapper.toJSON(data);
            returnValue = value.render(true);
        } catch (MapperException e) {
            // TODO Auto-generated catch block
        	logger.error(e.getMessage(), e);
            returnValue = "fail";
        }
        logger.debug("return value is {}", returnValue);
        return returnValue;
    }

    private String getId(String query) {
    	 String result = "";
         int start = query.indexOf("id=");
         if (start != -1) {
             int end = query.indexOf('&', start);
             if (end > start) {
                 result = query.substring(start + 3, end);
             } else {
                 result = query.substring(start + 3);
             }
         }
         int islocal = result.indexOf("?isLocal");
         if (islocal > 0) {
             result = result.substring(0, islocal);
         }
		return result;
	}

	private String getRemoteUrl(String query) {
        String result = "";
        String prefix = getPrefix(query);
        int start = query.indexOf("remoteurl=");
        if (start != -1) {
            int end = query.indexOf('&', start);
            if (end > start) {
                result = query.substring(start + 10, end);
            } else {
                result = query.substring(start + 10);
            }
        }
       
        int islocal = result.indexOf("?");
        if (islocal > 0) {
            result = result.substring(0, islocal);
        }
//        if (!result.endsWith("/")) {
//            result += "/";
//        }
        if(prefix!=""){
        	result=result+"?prefix="+prefix;
        }
        logger.debug("remoter url is {}", result);
        return result;
    }

	private String getPrefix(String query) {
		String result="";
		int start =query.indexOf("prefix="); 
		if(start!=-1){
	        	result=query.substring(start+7, query.indexOf("?",start));
	    }
		return result;
	}
}
