package com.wayapay.thirdpartyintegrationservice.v2.dto.response;

import lombok.Data;

@Data
public class ApiResponse<E> {

    private Boolean status;

    private Integer code;

    private String message;

    private E data;

    public ApiResponse(Boolean status, Integer code, String message, E data) {
        super();
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static class Code{

        public static final Integer SUCCESS = 200; //It indicates that the REST API successfully carried out whatever action the client requested and that no more specific code in the 2xx series is appropriate.Unlike the 204 status code, a 200 response should include a response body.

        public static final Integer CREATED = 201; //A REST API responds with the 201 status code whenever a resource is created inside a collection. There may also be times when a new resource is created as a result of some controller action, in which case 201 would also be an appropriate response.

        public static final Integer ACCEPTED = 202; //A 202 response is typically used for actions that take a long while to process. It indicates that the request has been accepted for processing, but the processing has not been completed. The request might or might not be eventually acted upon, or even maybe disallowed when processing occurs.

        public static final Integer NO_CONTENT = 204; // The 204 status code is usually sent out in response to a PUT, POST, or DELETE request when the REST API declines to send back any status message or representation in the response message’s body.

        public static final Integer BAD_REQUEST = 400; // 400 is the generic client-side error status, used when no other 4xx error code is appropriate. Errors can be like malformed request syntax, invalid request message parameters, or deceptive request routing etc.

        public static final Integer UNAUTHORIZED = 401; // A 401 error response indicates that the client tried to operate on a protected resource without providing the proper authorization. It may have provided the wrong credentials or none at all. The response must include a WWW-Authenticate header field containing a challenge applicable to the requested resource.

        public static final Integer FORBIDDEN = 403; // A 403 error response indicates that the client’s request is formed correctly, but the REST API refuses to honor it, i.e., the user does not have the necessary permissions for the resource. A 403 response is not a case of insufficient client credentials; that would be 401 (“Unauthorized”).

        public static final Integer NOT_FOUND =  404; // The 404 error status code indicates that the REST API can’t map the client’s URI to a resource but may be available in the future. Subsequent requests by the client are permissible.

        public static final Integer METHOD_NOT_ALLOWED = 405; // The API responds with a 405 error to indicate that the client tried to use an HTTP method that the resource does not allow. For instance, a read-only resource could support only GET and HEAD, while a controller resource might allow GET and POST, but not PUT or DELETE.

        public static final Integer NOT_ACCEPTABLE = 406; // The 406 error response indicates that the API is not able to generate any of the client’s preferred media types, as indicated by the Accept request header. For example, a client request for data formatted as application/xml will receive a 406 response if the API is only willing to format data as application/json.

        public static final Integer PRECONDITION_FAILED = 412; // The 412 error response indicates that the client specified one or more preconditions in its request headers, effectively telling the REST API to carry out its request only if certain conditions were met. A 412 response indicates that those conditions were not met, so instead of carrying out the request, the API sends this status code.

        public static final Integer UNSUPPORTED_MEDIA_TYPE = 415; // The 415 error response indicates that the API is not able to process the client’s supplied media type, as indicated by the Content-Type request header. For example, a client request including data formatted as application/xml will receive a 415 response if the API is only willing to process data formatted as application/json.

        public static final Integer INTERNAL_SERVER_ERROR = 500; // 500 is the generic REST API error response. Most web frameworks automatically respond with this response status code whenever they execute some request handler code that raises an exception.

        public static final Integer NOT_IMPLEMENTED = 501; // The server either does not recognize the request method, or it cannot fulfill the request. Usually, this implies future availability (e.g., a new feature of a web-utility API).

    }
}
