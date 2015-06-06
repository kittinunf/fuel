package fuel

import fuel.core.Manager
import fuel.core.Method
import fuel.core.Request

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Fuel {

    public interface PathStringConvertible {
        val path: String
    }

    public interface RequestConvertible {
        val request: Request
    }

    companion object {

        //convenience methods
        //get
        jvmOverloads public fun get(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.GET, path, parameters)
        }

        jvmOverloads public fun get(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.GET, convertible, parameters)
        }

        //post
        jvmOverloads public fun post(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.POST, path, parameters)
        }

        jvmOverloads public fun post(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.POST, convertible, parameters)
        }

        //put
        jvmOverloads public fun put(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.PUT, path, parameters)
        }

        jvmOverloads public fun put(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.PUT, convertible, parameters)
        }

        //delete
        jvmOverloads public fun delete(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.DELETE, path, parameters)
        }

        jvmOverloads public fun delete(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.DELETE, convertible, parameters)
        }

        //download
        jvmOverloads public fun download(path: String, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.download(path, parameters)
        }

        //request
        private fun request(method: Method, path: String, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.request(method, path, parameters)
        }

        private fun request(method: Method, convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.request(method, convertible, parameters)
        }

        public fun request(convertible: RequestConvertible): Request {
            return Manager.sharedInstance.request(convertible)
        }

    }

}