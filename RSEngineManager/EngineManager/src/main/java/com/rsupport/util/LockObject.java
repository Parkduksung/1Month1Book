package com.rsupport.util;

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
 * FileName: LockObject.java<br>
 * Author  : kwcho<br>
 * Date    : 2014. 2. 18.오전 10:30:34<br>
 * Purpose : <p>
 *
 * [History]<p>
 */
public class LockObject {
	private boolean isNotify = false;
	private boolean isLock = false;
	
	public LockObject(){
	}
	
	public void clear(){
		notifyLock();
		isNotify = false;
		isLock = false;
	}
	
	public synchronized void notifyLock(){
		isNotify = true;
		if(isLock == true){
			try {
				isLock = false;
				notifyAll();
			} catch (Exception e) {
			}
		}	
	}
	
	public synchronized void lock(int timeOut){
		if(isNotify == false){
			try {
				isLock = true;
				wait(timeOut);
			} catch (Exception e) {
			}
		}
	}
	
	public synchronized void lock(){
		lock(Integer.MAX_VALUE);
	}

	public synchronized void enforceLock() {
		try {
			isLock = true;
			wait();
		} catch (Exception e) {
		}
	}
}
