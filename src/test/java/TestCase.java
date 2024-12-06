/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import vavi.apps.registryViewer.RegistryViewer;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.serdes.CachingDIContainer;
import vavi.util.win32.registry.Registry;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-12-04 nsano initial version <br>
 */
public class TestCase {

    @Property
    String file = "src/test/resources/user.dat";

    @Test
    void test1() throws Exception {
        Registry registry = new Registry(Files.newByteChannel(Paths.get(file)));
        Registry.TreeRecord root = registry.getRoot();
Debug.println(root);
        assertEquals("HKEY_root", root.toString());
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        RegistryViewer.main(new String[] {file});

        CountDownLatch cdl = new CountDownLatch(1);
        cdl.await();
    }

    @AfterAll
    static void teardown() throws Exception {
        if (Boolean.parseBoolean(System.getProperty("vavi.util.serdes.cache.statistics", "false")))
            CachingDIContainer.printCacheStatistics();
    }
}
