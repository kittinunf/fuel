package fuel

import fuel.core.Manager
import fuel.core.Method
import fuel.core.Request

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Fuel {

    public trait StringConvertible {
        val path: String
    }

    public trait RequestConvertible {
        val request: Request
    }

    companion object {

        //convenience methods
        //get
        public fun get(path: String): Request {
            return request(Method.GET, path)
        }

        public fun get(convertible: StringConvertible): Request {
            return request(Method.GET, convertible)
        }

        public fun get(convertible: RequestConvertible): Request {
            return request(convertible)
        }

        //post

        //put

        //delete

        //request
        public fun request(method: Method, path: String): Request {
            return Manager.sharedInstance.request(method, path)
        }

        public fun request(method: Method, convertible: StringConvertible): Request {
            return Manager.sharedInstance.request(method, convertible)
        }

        public fun request(convertible: RequestConvertible): Request {
            return Manager.sharedInstance.request(convertible)
        }


    }

}