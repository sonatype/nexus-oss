package org.sonatype.nexus.plugins.rrb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.sonatype.nexus.plugins.rrb.parsers.HtmlRemoteRepositoryParser;
import org.sonatype.nexus.plugins.rrb.parsers.RemoteRepositoryParser;
import org.sonatype.nexus.plugins.rrb.parsers.S3RemoteRepositoryParser;

/**
 * Class for retrieving directory data from remote repository. This class is not
 * thread-safe!
 */
public class MavenRepositoryReader {

	final Logger logger = LoggerFactory.getLogger(MavenRepositoryReader.class);
    private HttpClient client = new HttpClient();
    private String remoteUrl;
    private String localUrl;

    /**
     * 
     * @param remoteUrl url to the remote repository
     * @param localUrl url to the local resource service
     * @return a json array containing the remote data
     */
    public List<RepositoryDirectory> extract(String remoteUrl, String localUrl) {
    	logger.debug("remoteUrl={}",remoteUrl);
    	this.remoteUrl = remoteUrl;
        this.localUrl = localUrl;
        StringBuilder html = getContent();
        logger.debug(html.toString());
        return parseResult(html);
    }

    private ArrayList<RepositoryDirectory> parseResult(StringBuilder indata) {
        RemoteRepositoryParser parser = null;
        if (indata.indexOf("<html>") != -1) {
        	logger.debug("is html repository");
            parser = new HtmlRemoteRepositoryParser(remoteUrl, localUrl);
        }
        if (indata.indexOf("xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\"") != -1 || indata.indexOf("<?xml") != -1) {
        	logger.debug("is S3 repository");
            if (responseContainsError(indata)) {
            	logger.debug("response from S3 repository contains error need to find rootUrl");
                remoteUrl = findRootUrl(indata);
                indata = getContent();
            }
            parser = new S3RemoteRepositoryParser(remoteUrl, localUrl);
        } else {
        	logger.info("Found no matching parser using default html parser");
            parser = new HtmlRemoteRepositoryParser(remoteUrl, localUrl);
        }
        return parser.extractLinks(indata);
    }

    private String findRootUrl(StringBuilder indata) {
    	logger.debug("indata={}", indata.toString());
        String key = "";
        String newUrl = "";
        int start = indata.indexOf("<Key>");
        int end = indata.indexOf("</Key>");
        if (start > 0 && end > start) {
            key = indata.substring(start + 5, end);
            newUrl = remoteUrl.substring(0, remoteUrl.indexOf(key));
            newUrl += "?prefix=" + key;
        }
        logger.debug("newUrl={}", newUrl);
        return newUrl;
    }

    private boolean responseContainsError(StringBuilder indata) {
        if (indata.indexOf("<Error>") != -1 || indata.indexOf("<error>") != -1) {
            return true;
        }
        return false;
    }

    private StringBuilder getContent() {
        GetMethod method = null;
        if (remoteUrl.indexOf("?prefix") != -1) {
            method = new GetMethod(remoteUrl + "&delimiter=/");
        } else {
            method = new GetMethod(remoteUrl + "?delimiter=/");
        }

        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        StringBuilder result = new StringBuilder();

        try {
            int responseCode = client.executeMethod(method);
            logger.debug("responseCode={}",responseCode);
            BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));

            String line = null;
            while ((line = reader.readLine()) != null) {
                result.append(line + "\n");
            }
        } catch (HttpException e) {
        	logger.error(e.getMessage(), e);
        } catch (IOException e) {
        	logger.error(e.getMessage(), e);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

        return result;
    }

    /**
     * Inner class used to imitate the for Nexus expected structure for the json
     * reply.
     */
    public class Data {
        List<RepositoryDirectory> data;

        public Data() {
            super();
        }

        public List<RepositoryDirectory> getData() {
            return data;
        }

        public void setData(List<RepositoryDirectory> data) {
            this.data = data;
        }
    }
}
