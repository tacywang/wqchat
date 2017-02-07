/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   qiong.wang (夜色)
 */

package com.wq.wqchat.common.security;

import com.wq.wqchat.api.connection.Cipher;
import com.wq.wqchat.tools.crypto.AESUtils;

import static com.wq.wqchat.tools.crypto.AESUtils.KEY_ALGORITHM;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public final class AesCipher implements Cipher {
    public final byte[] key;
    public final byte[] iv;
    private final IvParameterSpec zeroIv;
    private final SecretKeySpec keySpec;

    public AesCipher(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
        this.zeroIv = new IvParameterSpec(iv);
        this.keySpec = new SecretKeySpec(key, KEY_ALGORITHM);
    }


    @Override
    public byte[] encrypt(byte[] data) {
        return AESUtils.encrypt(data, zeroIv, keySpec);
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return AESUtils.decrypt(data, zeroIv, keySpec);
    }

    @Override
    public String toString() {
        return toString(key) + ',' + toString(iv);
    }

    public String toString(byte[] a) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i != 0) b.append('|');
            b.append(a[i]);
        }
        return b.toString();
    }

    public static byte[] toArray(String str) {
        String[] a = str.split("\\|");
        if (a.length != CipherBox.I.getAesKeyLength()) {
            return null;
        }
        byte[] bytes = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            bytes[i] = Byte.parseByte(a[i]);
        }
        return bytes;
    }
}
