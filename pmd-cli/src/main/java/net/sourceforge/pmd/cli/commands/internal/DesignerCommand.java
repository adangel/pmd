/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cli.commands.internal;

import net.sourceforge.pmd.cli.internal.CliExitCode;
import net.sourceforge.pmd.util.fxdesigner.DesignerStarter;
import net.sourceforge.pmd.util.fxdesigner.DesignerStarter.ExitStatus;
import net.sourceforge.pmd.util.fxdesigner.DesignerVersion;

import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

@Command(name = "designer", showDefaultValues = true,
    versionProvider = DesignerVersionProvider.class,
    description = "The PMD visual rule designer")
public class DesignerCommand extends AbstractPmdSubcommand {

    @SuppressWarnings("unused")
    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version information and exit.")
    private boolean versionRequested;

    @Override
    protected CliExitCode execute() {
        final String[] rawArgs = spec.commandLine().getParseResult().expandedArgs().toArray(new String[0]);
        final ExitStatus status;
        try {
            status = DesignerStarter.launchGui(rawArgs);
        } catch (NoClassDefFoundError e) {
            // The designer module is optional at runtime (requires static), so that the other
            // commands work without JavaFX. It is only resolved if added explicitly.
            spec.commandLine().getErr().println("The PMD Designer could not be started: " + e
                + System.lineSeparator()
                + "Make sure to use the start script (\"pmd designer\") or add the designer module"
                + " and JavaFX to the module path, e.g. with \"--add-modules net.sourceforge.pmd.util.fxdesigner\".");
            return CliExitCode.ERROR;
        }

        return status == ExitStatus.OK ? CliExitCode.OK : CliExitCode.ERROR;
    }
}

class DesignerVersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
        return new String[] { "PMD Rule Designer " + DesignerVersion.getCurrentVersion() };
    }
    
}
