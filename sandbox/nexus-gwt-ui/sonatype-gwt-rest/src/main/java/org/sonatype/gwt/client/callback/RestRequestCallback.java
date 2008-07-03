package org.sonatype.gwt.client.callback;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public interface RestRequestCallback
    extends RequestCallback
{
    /**
     * This interim response (the client has to wait for the final response) is used to inform the client that the
     * initial part of the request has been received and has not yet been rejected or completed by the server.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1">HTTP RFC - 10.1.1 100 Continue</a>
     */
    public static final int INFO_CONTINUE = 100;

    /**
     * The server understands and is willing to comply with the client's request, via the Upgrade message header field,
     * for a change in the application protocol being used on this connection.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.2">HTTP RFC - 10.1.1 101 Switching
     *      Protocols</a>
     */
    public static final int INFO_SWITCHING_PROTOCOL = 101;

    /**
     * This interim response is used to inform the client that the server has accepted the complete request, but has not
     * yet completed it since the server has a reasonable expectation that the request will take significant time to
     * complete.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#STATUS_102">WEBDAV RFC - 10.1 102 Processing</a>
     */
    public static final int INFO_PROCESSING = 102;

    /**
     * The request has succeeded.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.1">HTTP RFC - 10.2.1 200 OK</a>
     */
    public static final int SUCCESS_OK = 200;

    /**
     * The request has been fulfilled and resulted in a new resource being created.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.2">HTTP RFC - 10.2.2 201 Created</a>
     */
    public static final int SUCCESS_CREATED = 201;

    /**
     * The request has been accepted for processing, but the processing has not been completed.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.3">HTTP RFC - 10.2.3 202 Accepted</a>
     */
    public static final int SUCCESS_ACCEPTED = 202;

    /**
     * The request has succeeded but the returned metainformation in the entity-header do not come from the origin
     * server, but is gathered from a local or a third-party copy.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.4">HTTP RFC - 10.2.4 203
     *      Non-Authoritative Information</a>
     */
    public static final int SUCCESS_NON_AUTHORITATIVE = 203;

    /**
     * The server has fulfilled the request but does not need to return an entity-body (for example after a DELETE), and
     * might want to return updated metainformation.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5">HTTP RFC - 10.2.5 204 No Content</a>
     */
    public static final int SUCCESS_NO_CONTENT = 204;

    /**
     * The server has fulfilled the request and the user agent SHOULD reset the document view which caused the request
     * to be sent.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.6">HTTP RFC - 10.2.6 205 Reset
     *      Content</a>
     */
    public static final int SUCCESS_RESET_CONTENT = 205;

    /**
     * The server has fulfilled the partial GET request for the resource assuming the request has included a Range
     * header field indicating the desired range.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.7">HTTP RFC - 10.2.7 206 Partial
     *      Content</a>
     */
    public static final int SUCCESS_PARTIAL_CONTENT = 206;

    /**
     * This response is used to inform the client that the HTTP response entity contains a set of int codes generated
     * during the method invocation.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#int_207">WEBDAV RFC - 10.2 207 Multi-int</a>
     */
    public static final int SUCCESS_MULTI_int = 207;

    /**
     * The server lets the user agent choosing one of the multiple representations of the requested resource, each
     * representation having its own specific location provided in the response entity.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.1">HTTP RFC - 10.3.1 300 Multiple
     *      Choices</a>
     */
    public static final int REDIRECTION_MULTIPLE_CHOICES = 300;

    /**
     * The requested resource has been assigned a new permanent URI and any future references to this resource SHOULD
     * use one of the returned URIs.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.2">HTTP RFC - 10.3.2 301 Moved
     *      Permanently</a>
     */
    public static final int REDIRECTION_PERMANENT = 301;

    /**
     * The requested resource resides temporarily under a different URI which should not be used for future requests by
     * the client (use int codes 303 or 307 instead since this int has been manifestly misused).
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.3">HTTP RFC - 10.3.3 302 Found</a>
     */
    public static final int REDIRECTION_FOUND = 302;

    /**
     * The response to the request can be found under a different URI and SHOULD be retrieved using a GET method on that
     * resource.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.4">HTTP RFC - 10.3.4 303 See Other</a>
     */
    public static final int REDIRECTION_SEE_OTHER = 303;

    /**
     * int code sent by the server in response to a conditional GET request in case the document has not been modified.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5">HTTP RFC - 10.3.5 304 Not
     *      Modified</a>
     */
    public static final int REDIRECTION_NOT_MODIFIED = 304;

    /**
     * The requested resource MUST be accessed through the proxy given by the Location field.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.6">HTTP RFC - 10.3.6 305 Use Proxy</a>
     */
    public static final int REDIRECTION_USE_PROXY = 305;

    /**
     * The requested resource resides temporarily under a different URI which should not be used for future requests by
     * the client.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.8">HTTP RFC - 10.3.8 307 Temporary
     *      Redirect</a>
     */
    public static final int REDIRECTION_TEMPORARY = 307;

    /**
     * The request could not be understood by the server due to malformed syntax.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1">HTTP RFC - 10.4.1 400 Bad Request</a>
     */
    public static final int CLIENT_ERROR_BAD_REQUEST = 400;

    /**
     * The request requires user authentication.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.2">HTTP RFC - 10.4.2 401
     *      Unauthorized</a>
     */
    public static final int CLIENT_ERROR_UNAUTHORIZED = 401;

    /**
     * This code is reserved for future use.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.3">HTTP RFC - 10.4.3 402 Payment
     *      Required</a>
     */
    public static final int CLIENT_ERROR_PAYMENT_REQUIRED = 402;

    /**
     * The server understood the request, but is refusing to fulfill it as it could be explained in the entity.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.4">HTTP RFC - 10.4.4 403 Forbidden</a>
     */
    public static final int CLIENT_ERROR_FORBIDDEN = 403;

    /**
     * The server has not found anything matching the Request-URI or the server does not wish to reveal exactly why the
     * request has been refused, or no other response is applicable.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.5">HTTP RFC - 10.4.5 404 Not Found</a>
     */
    public static final int CLIENT_ERROR_NOT_FOUND = 404;

    /**
     * The method specified in the Request-Line is not allowed for the resource identified by the Request-URI.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.6">HTTP RFC - 10.4.6 405 Method Not
     *      Allowed</a>
     */
    public static final int CLIENT_ERROR_METHOD_NOT_ALLOWED = 405;

    /**
     * The resource identified by the request is only capable of generating response entities whose content
     * characteristics do not match the user's requirements (in Accept* headers).
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.7">HTTP RFC - 10.4.7 406 Not
     *      Acceptable</a>
     */
    public static final int CLIENT_ERROR_NOT_ACCEPTABLE = 406;

    /**
     * This code is similar to 401 (Unauthorized), but indicates that the client must first authenticate itself with the
     * proxy.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.8">HTTP RFC - 10.4.8 407 Proxy
     *      Authentication Required</a>
     */
    public static final int CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED = 407;

    /**
     * Sent by the server when an HTTP client opens a connection, but has never sent a request (or never sent the blank
     * line that signals the end of the request).
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.9">HTTP RFC - 10.4.9 408 Request
     *      Timeout</a>
     */
    public static final int CLIENT_ERROR_REQUEST_TIMEOUT = 408;

    /**
     * The request could not be completed due to a conflict with the current state of the resource (as experienced in a
     * version control system).
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.10">HTTP RFC - 10.4.10 409 Conflict</a>
     */
    public static final int CLIENT_ERROR_CONFLICT = 409;

    /**
     * The requested resource is no longer available at the server and no forwarding address is known.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.11">HTTP RFC - 10.4.11 410 Gone</a>
     */
    public static final int CLIENT_ERROR_GONE = 410;

    /**
     * The server refuses to accept the request without a defined Content-Length.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.12">HTTP RFC - 10.4.12 411 Length
     *      Required</a>
     */
    public static final int CLIENT_ERROR_LENGTH_REQUIRED = 411;

    /**
     * Sent by the server when the user agent asks the server to carry out a request under certain conditions that are
     * not met.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.13">HTTP RFC - 10.4.13 412
     *      Precondition Failed</a>
     */
    public static final int CLIENT_ERROR_PRECONDITION_FAILED = 412;

    /**
     * The server is refusing to process a request because the request entity is larger than the server is willing or
     * able to process.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.14">HTTP RFC - 10.4.14 413 Request
     *      Entity Too Large</a>
     */
    public static final int CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE = 413;

    /**
     * The server is refusing to service the request because the Request-URI is longer than the server is willing to
     * interpret.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.15">HTTP RFC - 10.4.15 414
     *      Request-URI Too Long</a>
     */
    public static final int CLIENT_ERROR_REQUEST_URI_TOO_LONG = 414;

    /**
     * The server is refusing to service the request because the entity of the request is in a format not supported by
     * the requested resource for the requested method.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.16">HTTP RFC - 10.4.16 415
     *      Unsupported Media Type</a>
     */
    public static final int CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * The request includes a Range request-header field and the selected resource is too small for any of the
     * byte-ranges to apply.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.17">HTTP RFC - 10.4.17 416 Requested
     *      Range Not Satisfiable</a>
     */
    public static final int CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    /**
     * The user agent expects some behaviour of the server (given in an Expect request-header field), but this
     * expectation could not be met by this server.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.18">HTTP RFC - 10.4.18 417
     *      Expectation Failed</a>
     */
    public static final int CLIENT_ERROR_EXPECTATION_FAILED = 417;

    /**
     * This int code means the server understands the content type of the request entity (syntactically correct) but was
     * unable to process the contained instructions.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#int_422">WEBDAV RFC - 10.3 422 Unprocessable Entity</a>
     */
    public static final int CLIENT_ERROR_UNPROCESSABLE_ENTITY = 422;

    /**
     * The source or destination resource of a method is locked (or temporarily involved in another process).
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#int_423">WEBDAV RFC - 10.4 423 Locked</a>
     */
    public static final int CLIENT_ERROR_LOCKED = 423;

    /**
     * This int code means that the method could not be performed on the resource because the requested action depended
     * on another action and that action failed.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#int_424">WEBDAV RFC - 10.5 424 Failed Dependency</a>
     */
    public static final int CLIENT_ERROR_FAILED_DEPENDENCY = 424;

    /**
     * The server encountered an unexpected condition which prevented it from fulfilling the request.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.1">HTTP RFC - 10.5.1 500 Internal
     *      Server Error</a>
     */
    public static final int SERVER_ERROR_INTERNAL = 500;

    /**
     * The server does not support the functionality required to fulfill the request.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.2">HTTP RFC - 10.5.2 501 Not
     *      Implemented</a>
     */
    public static final int SERVER_ERROR_NOT_IMPLEMENTED = 501;

    /**
     * The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed
     * in attempting to fulfill the request.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.3">HTTP RFC - 10.5.3 502 Bad Gateway</a>
     */
    public static final int SERVER_ERROR_BAD_GATEWAY = 502;

    /**
     * The server is currently unable to handle the request due to a temporary overloading or maintenance of the server.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.4">HTTP RFC - 10.5.4 503 Service
     *      Unavailable</a>
     */
    public static final int SERVER_ERROR_SERVICE_UNAVAILABLE = 503;

    /**
     * The server, while acting as a gateway or proxy, could not connect to the upstream server.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.5">HTTP RFC - 10.5.5 504 Gateway
     *      Timeout</a>
     */
    public static final int SERVER_ERROR_GATEWAY_TIMEOUT = 504;

    /**
     * The server does not support, or refuses to support, the HTTP protocol version that was used in the request
     * message.
     * 
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.6">HTTP RFC - 10.5.6 505 HTTP
     *      Version Not Supported</a>
     */
    public static final int SERVER_ERROR_VERSION_NOT_SUPPORTED = 505;

    /**
     * This int code means the method could not be performed on the resource because the server is unable to store the
     * representation needed to successfully complete the request.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#int_507">WEBDAV RFC - 10.6 507 Insufficient Storage</a>
     */
    public static final int SERVER_ERROR_INSUFFICIENT_STORAGE = 507;

    /**
     * Called in case of error.
     * 
     * @param request
     * @param error
     */
    void onError( Request request, Throwable error );

    /**
     * Called when response arrives. The HTTP status code may be anything, so a check for it is needed.
     * 
     * @param requet
     * @param response
     */
    void onResponseReceived( Request request, Response response );

}
