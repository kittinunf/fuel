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

        //post
        public fun post(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.POST, path, parameters)
        }

        public fun post(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.POST, convertible, parameters)
        }

        //put
        public fun put(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.PUT, path, parameters)
        }

        public fun put(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.PUT, convertible, parameters)
        }

        //delete
        public fun delete(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.DELETE, path, parameters)
        }

        public fun delete(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.DELETE, convertible, parameters)
        }

        //request
        private fun request(method: Method, path: String, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.request(method, path, parameters)
        }

        private fun request(method: Method, convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.request(method, convertible, parameters)
        }

        private fun request(convertible: RequestConvertible): Request {
            return Manager.sharedInstance.request(convertible)
        }

    }

}