package fuel

import fuel.core.Manager
import fuel.core.Method
import fuel.core.Request
import kotlin.platform.platformStatic

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
        platformStatic jvmOverloads public fun get(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.GET, path, parameters)
        }

        platformStatic jvmOverloads public fun get(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.GET, convertible, parameters)
        }

        //post
        platformStatic jvmOverloads public fun post(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.POST, path, parameters)
        }

        platformStatic jvmOverloads public fun post(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.POST, convertible, parameters)
        }

        //put
        platformStatic jvmOverloads public fun put(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.PUT, path, parameters)
        }

        platformStatic jvmOverloads public fun put(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.PUT, convertible, parameters)
        }

        //delete
        platformStatic jvmOverloads public fun delete(path: String, parameters: Map<String, Any?>? = null): Request {
            return request(Method.DELETE, path, parameters)
        }

        platformStatic jvmOverloads public fun delete(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return request(Method.DELETE, convertible, parameters)
        }

        //download
        platformStatic jvmOverloads public fun download(path: String, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.download(path, parameters)
        }

        platformStatic jvmOverloads public fun download(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.download(convertible, parameters)
        }

        //upload
        platformStatic jvmOverloads public fun upload(path: String, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.upload(path, parameters)
        }

        platformStatic jvmOverloads public fun upload(convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.upload(convertible, parameters)
        }

        //request
        private fun request(method: Method, path: String, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.request(method, path, parameters)
        }

        private fun request(method: Method, convertible: PathStringConvertible, parameters: Map<String, Any?>? = null): Request {
            return Manager.sharedInstance.request(method, convertible, parameters)
        }

        platformStatic public fun request(convertible: RequestConvertible): Request {
            return Manager.sharedInstance.request(convertible)
        }

    }

}

jvmOverloads public fun String.get(parameters: Map<String, Any?>? = null): Request {
    return Fuel.get(this, parameters)
}

jvmOverloads public fun Fuel.PathStringConvertible.get(parameter: Map<String, Any?>? = null): Request {
    return Fuel.get(this, parameter)
}

jvmOverloads public fun String.post(parameters: Map<String, Any?>? = null): Request {
    return Fuel.post(this, parameters)
}

jvmOverloads public fun Fuel.PathStringConvertible.post(parameter: Map<String, Any?>? = null): Request {
    return Fuel.post(this, parameter)
}

jvmOverloads public fun String.put(parameters: Map<String, Any?>? = null): Request {
    return Fuel.put(this, parameters)
}

jvmOverloads public fun Fuel.PathStringConvertible.put(parameter: Map<String, Any?>? = null): Request {
    return Fuel.put(this, parameter)
}

jvmOverloads public fun String.delete(parameters: Map<String, Any?>? = null): Request {
    return Fuel.delete(this, parameters)
}

jvmOverloads public fun Fuel.PathStringConvertible.delete(parameter: Map<String, Any?>? = null): Request {
    return Fuel.delete(this, parameter)
}

jvmOverloads public fun String.download(parameter: Map<String, Any?>? = null): Request {
    return Fuel.download(this, parameter)
}

jvmOverloads public fun Fuel.PathStringConvertible.download(parameter: Map<String, Any?>? = null): Request {
    return Fuel.download(this, parameter)
}

jvmOverloads public fun String.upload(parameter: Map<String, Any?>? = null): Request {
    return Fuel.upload(this, parameter)
}

jvmOverloads public fun Fuel.PathStringConvertible.upload(parameter: Map<String, Any?>? = null): Request {
    return Fuel.upload(this, parameter)
}
