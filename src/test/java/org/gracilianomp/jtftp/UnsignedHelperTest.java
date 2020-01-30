package org.gracilianomp.jtftp;

import org.gracilianomp.jtftp.utils.UnsignedHelper;
import org.junit.Assert;
import org.junit.Test;


public class UnsignedHelperTest {

    @Test
    public void testBasic() {

        for (int n = 0; n <= 65535 ; n++) {
            byte[] bs = UnsignedHelper.intTo2UnsignedBytes(n);
            int n2 = UnsignedHelper.twoBytesToInt(bs);
            Assert.assertEquals(n2, n);
        }

    }

    @Test( expected = IllegalArgumentException.class )
    public void testError1() {
        UnsignedHelper.intTo2UnsignedBytes(-100);
    }

    @Test( expected = IllegalArgumentException.class )
    public void testError2() {
        UnsignedHelper.intTo2UnsignedBytes(70000);
    }

}
