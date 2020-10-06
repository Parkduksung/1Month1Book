/********************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************
 *
 * Copyright (c) 2013 RSUPPORT Co., Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.
 *
 * FileName: IPdu.java
 * Author  : "kyeom@rsupport.com"
 * Date    : 2013. 7. 25.
 * Purpose : Protocol data unit을 구현하기 위한 메소드를 정의함.
 *
 * [History]
 *
 * 2013. 7. 25. -Protocol Data Unit Interface
 *
 */
package com.rsupport.jni;

public interface IPdu {

    /**
     * After read szBuffer, save to Instance.
     *
     * @param buffer
     * @param index
     */
    public void save(byte[] buffer, int index);

    /**
     * Save the Contents of Instance to index
     *
     * @param buffer
     * @param index
     */
    public void push(byte[] buffer, int index);

    /**
     * return size.
     *
     * @return
     */
    public int size();
}
