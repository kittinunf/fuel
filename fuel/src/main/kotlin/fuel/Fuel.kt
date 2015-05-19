package fuel

import fuel.core.Manager
import fuel.core.Method
import fuel.core.Request

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Fuel {

    public trait PathStringConvertible {
        val path: String
    }

    public trait RequestConvertible {
        val request: Request
    }

    companion object {

        //convenience methods
        //get
        public fun get(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.GET, path, parameters)
        }

        public fun get(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.GET, convertible, parameters)
        }

        public fun get(convertible: RequestConvertible): Request {
            return request(convertible)
        }

        //post

        //put

        //delete

        //request
        public fun request(method: Method, path: String, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.request(method, path, parameters)
        }

        public fun request(method: Method, convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.request(method, convertible, parameters)
        }

        public fun request(convertible: RequestConvertible): Request {
            return Manager.sharedInstance.request(convertible)
        }


    }

}