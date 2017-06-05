/*
 * Copyright (c) 2017 bin jin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.binave.common.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * 编码工具
 *
 * @author by bin jin on 2017/6/5.
 * @since 1.8
 */
public class CodecUtil {


    /* ******** Hash ******** */

    public enum Hash {
        MD5 {
            /**
             * Copyright (C) 2008 Happy Fish / YuQing
             *
             * FastDFS Java Client may be copied only under the terms of the GNU Lesser
             * General Public License (LGPL).
             * Please visit the FastDFS Home Page http://www.csource.org/ for more detail.
             *
             * md5 function
             *
             * @param src the input buffer
             * @return md5 string
             **/
            @Override
            public String hash(byte[] src) {
                byte digest[] = cloneDigest(md5sum).digest(src);
                char str[] = new char[32];
                int k = 0;
                for (int i = 0; i < 16; i++) {
                    str[k++] = hexDigits[digest[i] >>> 4 & 0xf];
                    str[k++] = hexDigits[digest[i] & 0xf];
                }
                return new String(str);
            }

            @Override
            public String hash(String str) {
                return hash(CharUtil.toBytes(str));
            }
        },

        SHA1 {
            @Override
            public String hash(byte[] src) {
                // todo
                throw new UnsupportedOperationException();
            }

            @Override
            public String hash(String str) {
                return hash(CharUtil.toBytes(str));
            }
        },

        SHA256 {
            @Override
            public String hash(byte[] src) {
                byte[] hash = cloneDigest(sha256sum).digest(src);
                StringBuilder hexString = new StringBuilder();

                for (byte h : hash) {
                    String hex = Integer.toHexString(0xff & h);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }

                return hexString.toString();
            }

            @Override
            public String hash(String str) {
                return hash(CharUtil.toBytes(str));
            }
        };

        /**
         * 计算字节的 hash
         */
        abstract public String hash(byte[] src);

        /**
         * 计算字符的 hash
         */
        abstract public String hash(String str);

        private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        private final static MessageDigest md5sum = getDigest("MD5");
        private final static MessageDigest sha256sum = getDigest("SHA-256");

        private static MessageDigest getDigest(String algorithm) {
            try {
                return MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 支持多线程
         */
        private static MessageDigest cloneDigest(MessageDigest digest) {
            try {
                return (MessageDigest) digest.clone(); // 使用 clone 加快对象复制
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public enum ConsistentHash {
        CRC32 {
            @Override
            public long hash(byte[] src, int offset, int length) {
                Checksum crc32 = new CRC32();
                crc32.update(src, offset, length);
                return crc32.getValue();
            }

        },

        MURMUR3 {
            /**
             * see https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp
             * MurmurHash3_x64_128
             *
             * @author Austin Appleby
             * @author Dimitris Andreou
             */
            @Override
            public long hash(byte[] key, int offset, int len) {

                long h1 = len, h2 = len;

                int nBlocks = offset + (len / 16);

                for (int i = offset; i < nBlocks; i += 16) {

                    long k1 = key[i];
                    long k2 = key[i + 8];

                    k1 *= c1;
                    k1 = Long.rotateLeft(k1, 31);
                    k1 *= c2;
                    h1 ^= k1;

                    h1 = Long.rotateLeft(h1, 27);
                    h1 += h2;
                    h1 = h1 * 5 + 0x52dce729;

                    k2 *= c2;
                    k2 = Long.rotateLeft(k2, 33);
                    k2 *= c1;
                    h2 ^= k2;

                    h2 = Long.rotateLeft(h2, 31);
                    h2 += h1;
                    h2 = h2 * 5 + 0x38495ab5;
                }

                long k1 = 0;
                long k2 = 0;

                switch (len & 15) {
                    case 15:
                        k2 ^= (key[nBlocks + 14] & 0xffL) << 48;
                    case 14:
                        k2 ^= (key[nBlocks + 13] & 0xffL) << 40;
                    case 13:
                        k2 ^= (key[nBlocks + 12] & 0xffL) << 32;
                    case 12:
                        k2 ^= (key[nBlocks + 11] & 0xffL) << 24;
                    case 11:
                        k2 ^= (key[nBlocks + 10] & 0xffL) << 16;
                    case 10:
                        k2 ^= (key[nBlocks + 9] & 0xffL) << 8;
                    case 9:
                        k2 ^= (key[nBlocks + 8] & 0xffL);
                        k2 *= c2;
                        k2 = Long.rotateLeft(k2, 33);
                        k2 *= c1;
                        h2 ^= k2;
                    case 8:
                        k1 ^= ((long) key[nBlocks + 7]) << 56;
                    case 7:
                        k1 ^= (key[nBlocks + 6] & 0xffL) << 48;
                    case 6:
                        k1 ^= (key[nBlocks + 5] & 0xffL) << 40;
                    case 5:
                        k1 ^= (key[nBlocks + 4] & 0xffL) << 32;
                    case 4:
                        k1 ^= (key[nBlocks + 3] & 0xffL) << 24;
                    case 3:
                        k1 ^= (key[nBlocks + 2] & 0xffL) << 16;
                    case 2:
                        k1 ^= (key[nBlocks + 1] & 0xffL) << 8;
                    case 1:
                        k1 ^= (key[nBlocks] & 0xffL);
                        k1 *= c1;
                        k1 = Long.rotateLeft(k1, 31);
                        k1 *= c2;
                        h1 ^= k1;
                }

                h1 ^= len;
                h2 ^= len;

                h1 += h2;
                h2 += h1;

                h1 = fMix64(h1);
                h2 = fMix64(h2);

                h1 += h2;
                h2 += h1;

                return h2;
            }

            private long fMix64(long k) {
                k ^= k >>> 33;
                k *= 0xff51afd7ed558ccdL;
                k ^= k >>> 33;
                k *= 0xc4ceb9fe1a85ec53L;
                k ^= k >>> 33;
                return k;
            }

            private static final long c1 = 0x87c37b91114253d5L;
            private static final long c2 = 0x4cf5ad432745937fL;

        };

        /**
         * 计算 用于一致性 hash 的 key
         */
        abstract public long hash(byte[] src, int offset, int length);

    }

    /* ******** 加密 ******** */

    private static int keyLength = 160;
    private static int iterationCount = 20000;
    private static String secureRandomAlgorithm = "SHA1PRNG";

    // java 8 support SHA-512
    // https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html
    private static String secretKeyAlgorithm = "PBKDF2WithHmacSHA512";

    /**
     * 验证密码
     */
    public static boolean authenticate(byte[] salt, String clearText, byte[] cipherText) {
        return Arrays.equals(
                cipherText,
                getCipherText(clearText, salt)
        );
    }

    /**
     * 使用盐加密明文
     */
    public static byte[] getCipherText(String clearText, byte[] salt) {

        KeySpec spec = new PBEKeySpec(
                clearText.toCharArray(),
                salt,
                iterationCount,
                keyLength
        );

        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance(secretKeyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            return factory.
                    generateSecret(spec).
                    getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得盐
     */
    public static byte[] getSalt() {

        // 使用安全随机数
        SecureRandom random;
        try {
            random = SecureRandom.getInstance(secureRandomAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return salt;
    }

}
