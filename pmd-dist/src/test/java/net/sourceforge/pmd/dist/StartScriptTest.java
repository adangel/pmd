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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Runs the script based tests for the start scripts:
 * <ul>
 *     <li>unix start script "pmd", see src/test/resources/scripts/pmd_startScript_test</li>
 *     <li>windows start script "pmd.bat", see src/test/resources/scripts/pmd_startScript_test.ps1</li>
 * </ul>
 */
class StartScriptTest {

    @ParameterizedTest
    @ValueSource(strings = {"sh", "dash", "bash"})
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void unixStartScript(String shell) throws Exception {
        assumeTrue(isAvailable(shell), shell + " is not available");

        runTestScript(shell, shell,
                "src/test/resources/scripts/pmd_startScript_test",
                "src/main/resources/scripts/pmd");
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windowsStartScript() throws Exception {
        // needs Windows PowerShell 5.1 (powershell.exe), which can compile the fake java.exe
        runTestScript("powershell", "powershell.exe", "-NoProfile", "-NonInteractive",
                "-ExecutionPolicy", "Bypass",
                "-File", "src\\test\\resources\\scripts\\pmd_startScript_test.ps1",
                "src\\main\\resources\\scripts\\pmd.bat");
    }

    private static void runTestScript(String shell, String... command) throws IOException, InterruptedException {
        // the working directory is the module directory (pmd-dist)
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        assertTrue(process.waitFor(5, TimeUnit.MINUTES), "Timeout");

        assertEquals(0, process.exitValue(), () -> "Start script test failed with " + shell + ":\n" + output);
    }

    private static boolean isAvailable(String shell) throws IOException, InterruptedException {
        return new ProcessBuilder("sh", "-c", "command -v " + shell).start().waitFor() == 0;
    }
}
