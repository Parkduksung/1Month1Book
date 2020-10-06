package utils

import com.rsupport.mobile.agent.service.OnRspermBindListener
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rsperm.IRSPerm
import kotlinx.coroutines.CompletableDeferred
import org.koin.java.KoinJavaComponent

suspend fun bindRperm(): Boolean {
    val rspermService by KoinJavaComponent.inject(RSPermService::class.java)
    val rspermBindDeferred = CompletableDeferred<Result<IRSPerm>>()
    rspermService.bindRsperm(object : OnRspermBindListener {
        override fun onResult(result: Result<IRSPerm>) {
            rspermBindDeferred.complete(result)
        }
    })
    return rspermBindDeferred.await() is Result.Success
}