package fuel

import cocoapods.AFNetworking.AFHTTPSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.Foundation.NSURLSessionDataTask

public class Fuel(private val manager: AFHTTPSessionManager = AFHTTPSessionManager.manager()) : HttpLoader {
    private val error = { _: NSURLSessionDataTask?, error: NSError? ->
        NSLog(error?.localizedDescription!!)
    }

    override suspend fun get(request: Request): Flow<Any?> = flow {
        val success = { _: NSURLSessionDataTask?, responseObject: Any? ->
            suspend {
                emit(responseObject)
            }
            Unit
        }
        manager.GET(request.url, null, null, null, success, error)
    }

    override suspend fun post(request: Request): Flow<Any?> = flow {
        val success = { _: NSURLSessionDataTask?, responseObject: Any? ->
            suspend {
                emit(responseObject)
            }
            Unit
        }
        manager.POST(request.url, null, null, null, success, error)
    }

    override suspend fun put(request: Request): Flow<Any?> = flow {
        val success = { _: NSURLSessionDataTask?, responseObject: Any? ->
            suspend {
                emit(responseObject)
            }
            Unit
        }
        manager.PUT(request.url, null, null, success, error)
    }

    override suspend fun patch(request: Request): Flow<Any?> = flow {
        val success = { _: NSURLSessionDataTask?, responseObject: Any? ->
            suspend {
                emit(responseObject)
            }
            Unit
        }
        manager.PATCH(request.url, null, null, success, error)
    }

    override suspend fun delete(request: Request): Flow<Any?> = flow {
        val success = { _: NSURLSessionDataTask?, responseObject: Any? ->
            suspend {
                emit(responseObject)
            }
            Unit
        }
        manager.DELETE(request.url, null, null, success, error)
    }

    override suspend fun head(request: Request): Flow<NSURLSessionDataTask?> = flow {
        val success = { dataTask: NSURLSessionDataTask? ->
            suspend {
                emit(dataTask)
            }
            Unit
        }
        manager.HEAD(request.url, null, null, success, error)
    }

    override suspend fun method(request: Request): Flow<Any?> = flow {
        val method = requireNotNull(request.method) { "method should be not null" }
        val success = { _: NSURLSessionDataTask?, responseObject: Any? ->
            suspend {
                emit(responseObject)
            }
            Unit
        }
        manager.dataTaskWithHTTPMethod(method, request.url, null, null, null, null, success, error)
    }
}
