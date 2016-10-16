package org.tickets.misc;

import org.junit.Assert;
import org.junit.Test;

public class BundleKeyTest {

    @Test
    public void allBundlesMatch() {
        for (BundleKey bundleKey : BundleKey.values()) {
            Assert.assertNotNull(bundleKey.getText());
        }
    }

}