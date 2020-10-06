package com.rsupport.mobile.agent.service

import com.rsupport.litecam.binder.Binder
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rsperm.IRSPerm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

/**
 * RSPerm 을 사용하는 서비스.
 * bindRsperm 을 통해 RSPerm 과 Binding 가능하다.
 */
class RSPermService {

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val rspermBinder by inject(Binder::class.java)

    /**
     * 사용 가능한 RSPerm 과 Binding을 시도한다.
     * 비동기로 동작하며 Binding 결과를 [OnRspermBindListener] 으로 받을 수 있다.
     * [OnRspermBindListener.onResult] 는 mainThread 에서 호출된다.
     * @param result rsperm binding callback 이다.
     */
    fun bindRsperm(result: OnRspermBindListener) {
        ioScope.launch {
            try {
                val bindResult = rspermBinder.bind()
                if (bindResult == Binder.RSPERM_BIND_SUCCESS || bindResult == Binder.RSPERM_BINDED) {
                    withContext(Dispatchers.Main) {
                        result.onResult(Result.success(rspermBinder.binder))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        result.onResult(Result.failure(IllegalStateException("not found rsperm.")))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    result.onResult(Result.failure(e))
                }
            }
        }
    }

    /**
     * Rsperm 과 unbinding 한다.
     */
    fun unbindRsperm() {
        uiScope.launch {
            rspermBinder.unbind()
        }
    }

    /**
     * RSPerm 과 binding 되어있는지를 확인한다.
     */
    fun isBind(): Boolean {
        return rspermBinder.isBinderAlive
    }

    /**
     * Binding 되어있는 Rsperm 을 반환한다.
     * @return binding 되어있으면 rsperm, 그렇지 않으면 null
     */
    fun getRsperm(): IRSPerm? {
        if (isBind()) {
            return rspermBinder.binder
        } else {
            return null
        }
    }
}

interface OnRspermBindListener {

    /**
     * Rsperm 과 Binding 결화를 반환한다.
     */
    fun onResult(result: Result<IRSPerm>)
}