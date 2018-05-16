package com.xin.test.proxy.https;

import static com.xin.test.proxy.https.Main.hexStringToBytes;

/**
 * @author linxixin@cvte.com
 * @since 1.0
 */
public class Decode {

    public static final int TEST = 4;

    public static void main(String[] args) {
        byte[] key = hexStringToBytes("f05375bd61b773ff1911611578c26910");
        System.out.println("asdf");
        String content = "00000000000000012add27f56a262f4a478336c39d34c1e04fcf0eccd0b2868f3c8c76bacc65dcf185a0628540e1346d05a9fe5745a95b9260d1139447958cf2bf210af3c87585b27cc463a2223804a6b0881c3517535c160dad2260d51ea030c314";
        byte[] content1 = hexStringToBytes(content);
//        for(int i=0;i<10;i++) {
        try {
            byte[] bytes = new byte[content1.length - 8];
            System.arraycopy(content1, 8, bytes, 0, content1.length - 8);
            String trueContent = AESUtil.decrypt(bytes, key);
            if (trueContent != null) {
                System.out.println(trueContent);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
        System.out.println("end");

    }

}
