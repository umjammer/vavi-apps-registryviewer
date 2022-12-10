/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import vavi.util.Debug;
import vavi.util.win32.registry.Registry;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-12-04 nsano initial version <br>
 */
public class Test1 {

    @Test
    void test1() throws Exception {
        Registry registry = new Registry(Files.newByteChannel(Paths.get("src/test/resources/user.dat")));
        Registry.TreeRecord root = registry.getRoot();
Debug.println(root);
    }
}
