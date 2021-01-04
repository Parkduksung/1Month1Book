package com.rsupport.srn30.adjust;

import android.annotation.TargetApi;
import android.os.Build;


/**<pre>*******************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************</pre>
 *
 * <b>Copyright (c) 2012 RSUPPORT Co., Ltd. All Rights Reserved.</b><p>
 *
 * <b>NOTICE</b> :  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.<p>
 *
 * FileName: OnBitRateChangeListener.java<br>
 * Author  : kwcho<br>
 * Date    : 2014. 8. 22.오후 12:40:34<br>
 * Purpose : <p>
 *
 * [History]<p>
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public interface OnBitRateChangeListener {
	
	/**
	 * frame rate 를 올려야 될때 호출된다.
	 */
	public void onUpperEvent();
	
	/**
	 * frame rate 를 내려야 될때 호출된다.
	 */
	public void onLowerEvent();
}
