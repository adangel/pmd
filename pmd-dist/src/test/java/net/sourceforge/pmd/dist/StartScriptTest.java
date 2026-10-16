/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.dist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Runs the shell based test for the unix start script "pmd",
 * see src/test/resources/scripts/pmd_startScript_test.
 */
class StartScriptTest {

    @ParameterizedTest
    @ValueSource(strings = {"sh", "dash", "bash"})
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void unixStartScript(String shell) throws Exception {
        assumeTrue(isAvailable(shell), shell + " is not available");

        // the working directory is the module directory (pmd-dist)
        ProcessBuilder pb = new ProcessBuilder(shell,
                "src/test/resources/scripts/pmd_startScript_test",
                "src/main/resources/scripts/pmd");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        assertTrue(process.waitFor(2, TimeUnit.MINUTES), "Timeout");

        assertEquals(0, process.exitValue(), () -> "Start script test failed with " + shell + ":\n" + output);
    }

    private static boolean isAvailable(String shell) throws IOException, InterruptedException {
        return new ProcessBuilder("sh", "-c", "command -v " + shell).start().waitFor() == 0;
    }
}
