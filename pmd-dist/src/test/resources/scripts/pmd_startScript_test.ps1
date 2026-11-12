# BSD-style license; for more info see http://pmd.sourceforge.net/license.html

#
# Simple test script for the windows start script "scripts/pmd.bat"
# - the real start script (src/main/resources/scripts/pmd.bat) is copied into a temporary
#   PMD_HOME, whose path contains spaces and parentheses
# - a fake java.exe is compiled at test time, which mocks "java -version" and
#   "java -XshowSettings:properties" and otherwise just records the arguments
#   it has been called with. A fake java.bat doesn't work, as pmd.bat calls java without CALL.
# - requires Windows PowerShell 5.1 (powershell.exe), as only this version can compile executables
# - just run it with "powershell -ExecutionPolicy Bypass -File pmd_startScript_test.ps1" and look at the output
#   optionally the path to the start script can be given as first argument
# - test cases are at the end of this script
# - this file must only contain ASCII characters, as Windows PowerShell 5.1
#   reads scripts without BOM in the ANSI codepage
#

param([string]$PmdScript)

$ErrorActionPreference = 'Stop'

if ($PSVersionTable.PSEdition -eq 'Core') {
    Write-Host 'This test requires Windows PowerShell 5.1 (powershell.exe), it does not work with PowerShell 7 (pwsh.exe)'
    exit 1
}

if (-not $PmdScript) {
    $PmdScript = Join-Path $PSScriptRoot '..\..\..\main\resources\scripts\pmd.bat'
}
if (-not [IO.File]::Exists($PmdScript)) {
    Write-Host "Start script [$PmdScript] not found"
    exit 1
}
$PmdScript = (Resolve-Path -LiteralPath $PmdScript).ProviderPath

$FakeJavaSource = @'
using System;
using System.IO;
using System.Text;

public static class FakeJava {
    public static int Main(string[] args) {
        Encoding utf8 = new UTF8Encoding(false);
        string version = Environment.GetEnvironmentVariable("FAKE_JAVA_VERSION");
        if (args.Length == 1 && args[0] == "-version") {
            using (StreamWriter err = new StreamWriter(Console.OpenStandardError(), utf8)) {
                err.WriteLine("fake java version \"" + version + "\"");
            }
            return 0;
        }
        if (args.Length == 1 && args[0] == "-XshowSettings:properties") {
            using (StreamWriter err = new StreamWriter(Console.OpenStandardError(), utf8)) {
                err.WriteLine("Property settings:");
                err.WriteLine("    java.home = " + Environment.GetEnvironmentVariable("FAKE_JAVA_HOME"));
                err.WriteLine("    java.version = " + version);
                err.WriteLine("    java.version.date = 2025-01-01");
                err.WriteLine("    some.other.property = value");
                err.WriteLine();
            }
            return 0;
        }
        string argsFile = Environment.GetEnvironmentVariable("FAKE_JAVA_ARGS_FILE");
        File.WriteAllLines(argsFile, args, utf8);
        // the raw command line helps when debugging quoting problems
        File.WriteAllText(argsFile + ".raw", Environment.CommandLine, utf8);
        string exitCode = Environment.GetEnvironmentVariable("FAKE_JAVA_EXIT_CODE");
        return string.IsNullOrEmpty(exitCode) ? 0 : int.Parse(exitCode);
    }
}
'@

# The temp directory might be given as 8.3 short name (e.g. C:\Users\RUNNER~1\...)
Add-Type -Namespace PmdStartScriptTest -Name NativeMethods -MemberDefinition @'
[DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
public static extern uint GetLongPathName(string shortPath, System.Text.StringBuilder longPath, uint bufferSize);
'@

function Get-LongPath([string]$Path) {
    $buffer = New-Object System.Text.StringBuilder 1024
    $length = [PmdStartScriptTest.NativeMethods]::GetLongPathName($Path, $buffer, $buffer.Capacity)
    if ($length -eq 0 -or $length -gt $buffer.Capacity) {
        return $Path
    }
    return $buffer.ToString()
}

function Q([string]$Value) {
    return '"' + $Value + '"'
}

# New-PmdHome <path>
# Creates a minimal PMD installation with a copy of the start script.
function New-PmdHome([string]$Path) {
    foreach ($dir in 'bin', 'conf', 'lib\classpath', 'lib\modules') {
        [IO.Directory]::CreateDirectory((Join-Path $Path $dir)) | Out-Null
    }
    [IO.File]::Copy($PmdScript, (Join-Path $Path 'bin\pmd.bat'))
}

$script:Failures = 0
$script:KnownIssues = 0
$script:KnownIssue = $null
$script:ExitCode = $null
$script:Stdout = ''
$script:Stderr = ''

# Start-Test <name> [<known issue>]
# If a known issue is given, failures of this test are reported, but don't fail the test run.
function Start-Test([string]$Name, [string]$KnownIssue) {
    Write-Host "Test: $Name"
    $script:KnownIssue = $KnownIssue
}

function Pass([string]$Message) {
    Write-Host 'OK' -ForegroundColor Green -NoNewline
    Write-Host $Message
}

function Fail([string]$Message) {
    if ($script:KnownIssue) {
        Write-Host 'KNOWN ISSUE' -ForegroundColor Yellow -NoNewline
        Write-Host " $Message - $($script:KnownIssue)"
        $script:KnownIssues++
    } else {
        Write-Host 'FAILED' -ForegroundColor Red -NoNewline
        Write-Host " $Message"
        $script:Failures++
    }
}

function Write-Details {
    if ([IO.File]::Exists("$ArgsFile.raw")) {
        Write-Host "  java command line: $([IO.File]::ReadAllText("$ArgsFile.raw"))"
    }
    foreach ($line in ($script:Stderr -split "`r?`n")) {
        if ($line) {
            Write-Host "  stderr: $line"
        }
    }
}

# Invoke-Pmd -JavaVersion <version> -JavaHome <path> -CommandLine <command line> [-WorkDir <dir>] [-Environment <hashtable>]
# Runs the given command line (which should call the start script) with cmd.exe and the fake java.
function Invoke-Pmd {
    param(
        [string]$JavaVersion,
        [string]$JavaHome,
        [string]$CommandLine,
        [string]$WorkDir = $TmpDir,
        [hashtable]$Environment = @{}
    )
    [IO.File]::Delete($ArgsFile)
    [IO.File]::Delete("$ArgsFile.raw")

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = Join-Path $env:SystemRoot 'System32\cmd.exe'
    # /d: don't run AutoRun commands, /s: only strip the outer quotes of the command line
    $psi.Arguments = '/d /s /c "' + $CommandLine + '"'
    $psi.WorkingDirectory = $WorkDir
    $psi.UseShellExecute = $false
    # a separate (hidden) console for each run, so that "chcp" in pmd.bat doesn't affect this console
    $psi.CreateNoWindow = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.StandardOutputEncoding = [Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [Text.Encoding]::UTF8

    # don't let the environment of the caller influence the tests
    foreach ($name in 'CLASSPATH', 'JAVAFX_HOME', 'PMD_JAVA_OPTS', 'FAKE_JAVA_EXIT_CODE') {
        $psi.EnvironmentVariables.Remove($name)
    }
    $psi.EnvironmentVariables['PATH'] = $BasePath
    $psi.EnvironmentVariables['FAKE_JAVA_VERSION'] = $JavaVersion
    $psi.EnvironmentVariables['FAKE_JAVA_HOME'] = $JavaHome
    $psi.EnvironmentVariables['FAKE_JAVA_ARGS_FILE'] = $ArgsFile
    foreach ($name in $Environment.Keys) {
        $psi.EnvironmentVariables[$name] = $Environment[$name]
    }

    $process = [Diagnostics.Process]::Start($psi)
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $script:Stdout = $process.StandardOutput.ReadToEnd()
    $process.WaitForExit()
    $script:Stderr = $stderrTask.Result
    $script:ExitCode = $process.ExitCode
}

function Get-JavaArgs {
    if ([IO.File]::Exists($ArgsFile)) {
        return [IO.File]::ReadAllLines($ArgsFile, [Text.Encoding]::UTF8)
    }
    return @()
}

# Expect-Args <args array>
# Verifies that java has been called with exactly the given arguments.
function Expect-Args([string[]]$Expected) {
    $actual = @(Get-JavaArgs)
    if ($script:ExitCode -eq 0 -and (($Expected -join "`n") -ceq ($actual -join "`n"))) {
        Pass
    } else {
        Fail "(exit code: $($script:ExitCode))"
        Write-Host '  expected:'
        foreach ($arg in $Expected) { Write-Host "    [$arg]" }
        Write-Host '  actual:'
        foreach ($arg in $actual) { Write-Host "    [$arg]" }
        Write-Details
    }
}

# Expect-ArgValue <option> <value>
# Verifies that java has been called with the option directly followed by the value.
function Expect-ArgValue([string]$Option, [string]$Value) {
    $actual = [string[]]@(Get-JavaArgs)
    $index = [Array]::IndexOf($actual, $Option)
    $actualValue = $null
    if ($index -ge 0 -and $index + 1 -lt $actual.Count) {
        $actualValue = $actual[$index + 1]
    }
    if ($script:ExitCode -eq 0 -and $Value -ceq $actualValue) {
        Pass " $Option [$Value]"
    } else {
        Fail "${Option}: expected [$Value], actual [$actualValue] (exit code: $($script:ExitCode))"
        Write-Details
    }
}

function Expect-Arg([string]$Arg) {
    if ($script:ExitCode -eq 0 -and @(Get-JavaArgs) -ccontains $Arg) {
        Pass " [$Arg]"
    } else {
        Fail "missing argument [$Arg] (exit code: $($script:ExitCode))"
        Write-Details
    }
}

function Expect-NoArg([string]$Arg) {
    if ($script:ExitCode -eq 0 -and [IO.File]::Exists($ArgsFile) -and -not (@(Get-JavaArgs) -ccontains $Arg)) {
        Pass " no [$Arg]"
    } else {
        Fail "unexpected argument [$Arg] (exit code: $($script:ExitCode))"
        Write-Details
    }
}

# Expect-Error <exit code> <message part>
# Verifies that java has not been called and the message has been printed to stderr.
function Expect-Error([int]$Code, [string]$Message) {
    if ($script:ExitCode -eq $Code -and -not [IO.File]::Exists($ArgsFile) -and $script:Stderr.Contains($Message)) {
        Pass " exit code ${Code}: $Message"
    } else {
        Fail "expected exit code $Code and message [$Message], actual exit code: $($script:ExitCode)"
        Write-Details
    }
}

function Expect-ExitCode([int]$Code) {
    if ($script:ExitCode -eq $Code -and [IO.File]::Exists($ArgsFile)) {
        Pass " exit code $Code"
    } else {
        Fail "expected exit code $Code, actual exit code: $($script:ExitCode)"
        Write-Details
    }
}

function Expect-Output([string]$Text) {
    if ($script:Stdout.Contains($Text)) {
        Pass " output contains [$Text]"
    } else {
        Fail "output doesn't contain [$Text]"
        Write-Host "  stdout: $($script:Stdout)"
        Write-Details
    }
}

function Expect-NoOutput([string]$Pattern) {
    if ($script:Stdout -cnotmatch $Pattern) {
        Pass " output doesn't match [$Pattern]"
    } else {
        Fail "output matches [$Pattern]"
        Write-Host "  stdout: $($script:Stdout)"
    }
}

# Test-JavaVersion <java version> <expected feature version>
# Verifies the effects of the determined java version: classpath vs. module path,
# --illegal-access (java 9-16) and --enable-native-access for the designer (java 24+).
function Test-JavaVersion([string]$Version, [int]$Feature) {
    Start-Test "java version '$Version' is detected as java $Feature"
    Invoke-Pmd -JavaVersion $Version -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check"
    if ($Feature -eq 8) {
        Expect-Arg 'net.sourceforge.pmd.cli.PmdCli'
        Expect-NoArg '--module-path'
    } else {
        Expect-ArgValue '--module' $PmdCliModule
    }
    if ($Feature -ge 9 -and $Feature -le 16) {
        Expect-Arg '--illegal-access=warn'
    } else {
        Expect-NoArg '--illegal-access=warn'
    }

    if ($Feature -ge 10) {
        Invoke-Pmd -JavaVersion $Version -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat designer" `
            -Environment @{ JAVAFX_HOME = $OpenJfxHome }
        if ($Feature -ge 24) {
            Expect-Arg '--enable-native-access=javafx.graphics'
        } else {
            Expect-NoArg '--enable-native-access=javafx.graphics'
        }
    }
}

# Test-SpecialPmdHome <directory name> [<known issue>]
# Verifies that a PMD_HOME with special characters works.
function Test-SpecialPmdHome([string]$DirName, [string]$KnownIssue) {
    $pmdHome = Join-Path $TmpDir $DirName
    New-PmdHome $pmdHome
    Start-Test "PMD_HOME with special characters: $DirName" $KnownIssue
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$(Q "$pmdHome\bin\pmd.bat") check"
    Expect-Args @('-cp', "$pmdHome/conf;$pmdHome/lib/classpath/*",
        '--module-path', "$pmdHome/lib/modules;$pmdHome/lib/controlsfx-java11", '--add-modules', 'kotlin.stdlib',
        '--module', $PmdCliModule, 'check')
}


$TmpDir = Join-Path ([IO.Path]::GetTempPath()) ('pmd-start-script-test-' + [Guid]::NewGuid().ToString('N').Substring(0, 8))
[IO.Directory]::CreateDirectory($TmpDir) | Out-Null
$TmpDir = Get-LongPath $TmpDir
$testRunExitCode = 0

try {
    # a PMD installation with spaces and parentheses in the path
    $H = Join-Path $TmpDir 'pmd home (x86)'
    New-PmdHome $H
    $PmdBat = Q "$H\bin\pmd.bat"

    # java homes with and without bundled JavaFX. We only check for the existence of a javafx.properties file
    # at lib/ or jre/lib. java.home sometimes points already to jre (oracle), but openjdk
    # builds don't have a jre folder anymore. Anyway, both should work.
    $JavaHomeWithFx = Join-Path $TmpDir 'java home with fx'
    [IO.Directory]::CreateDirectory("$JavaHomeWithFx\jre\lib") | Out-Null
    [IO.File]::WriteAllText("$JavaHomeWithFx\jre\lib\javafx.properties", '')
    $JavaHomeWithFx2 = "$JavaHomeWithFx\jre"
    $JavaHomeWithoutFx = Join-Path $TmpDir 'java home without fx'
    [IO.Directory]::CreateDirectory("$JavaHomeWithoutFx\lib") | Out-Null

    # a separate OpenJFX installation
    $OpenJfxHome = Join-Path $TmpDir 'open jfx'
    [IO.Directory]::CreateDirectory("$OpenJfxHome\lib") | Out-Null

    # the fake java executable
    $FakeJavaDir = Join-Path $TmpDir 'fake java bin'
    [IO.Directory]::CreateDirectory($FakeJavaDir) | Out-Null
    Add-Type -TypeDefinition $FakeJavaSource -Language CSharp `
        -OutputType ConsoleApplication -OutputAssembly (Join-Path $FakeJavaDir 'java.exe')
    $BasePath = "$FakeJavaDir;$env:PATH"

    $ArgsFile = Join-Path $TmpDir 'java-args.txt'

    $PmdCliModule = 'net.sourceforge.pmd.cli/net.sourceforge.pmd.cli.PmdCli'
    $PmdDesignerModule = 'net.sourceforge.pmd.util.fxdesigner'
    # classpath and module path for java 9+
    $CP = "$H/conf;$H/lib/classpath/*"
    $MP = "$H/lib/modules;$H/lib/controlsfx-java11"
    # classpath for java 8
    $CP8 = "$H/conf;$H/lib/classpath/*;$H/lib/modules/*;$H/lib/controlsfx-java8/*"
    # some non-ASCII characters: u-umlaut and two CJK characters
    $NonAscii = 'pr' + [char]0x00FC + 'fung ' + [char]0x65E5 + [char]0x672C

    Write-Host "Testing start script: $PmdScript"
    Write-Host "PMD_HOME: $H"
    Write-Host ''


    Start-Test 'java 21, absolute path, arguments with spaces'
    Invoke-Pmd -JavaVersion '21.0.2' -JavaHome $JavaHomeWithoutFx `
        -CommandLine "$PmdBat check -d `"src dir\with spaces`" -R `"my rules.xml`""
    Expect-Args @('-cp', $CP, '--module-path', $MP, '--add-modules', 'kotlin.stdlib',
        '--module', $PmdCliModule, 'check', '-d', 'src dir\with spaces', '-R', 'my rules.xml')

    Start-Test 'java 8, only classpath'
    Invoke-Pmd -JavaVersion '1.8.0_442' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check -d `"src dir`""
    Expect-Args @('-cp', $CP8, 'net.sourceforge.pmd.cli.PmdCli', 'check', '-d', 'src dir')

    Start-Test 'java 11, --illegal-access=warn'
    Invoke-Pmd -JavaVersion '11.0.20' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check"
    Expect-Args @('--illegal-access=warn', '-cp', $CP, '--module-path', $MP, '--add-modules', 'kotlin.stdlib',
        '--module', $PmdCliModule, 'check')

    Start-Test 'CLASSPATH with spaces is prepended'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check" `
        -Environment @{ CLASSPATH = 'C:\some dir\a.jar;C:\other dir\classes' }
    Expect-ArgValue '-cp' "C:\some dir\a.jar;C:\other dir\classes;$CP"

    Start-Test 'CLASSPATH with special characters'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check" `
        -Environment @{ CLASSPATH = 'C:\lib (x86)\a&b!c^d%e.jar' }
    Expect-ArgValue '-cp' "C:\lib (x86)\a&b!c^d%e.jar;$CP"

    Start-Test 'PMD_JAVA_OPTS are split into words'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check" `
        -Environment @{ PMD_JAVA_OPTS = '-Xmx1g -Dfoo=bar' }
    Expect-Args @('-Xmx1g', '-Dfoo=bar', '-cp', $CP, '--module-path', $MP, '--add-modules', 'kotlin.stdlib',
        '--module', $PmdCliModule, 'check')

    Start-Test 'arguments with special characters'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx `
        -CommandLine "$PmdBat check -R `"rules & more (1).xml`" -d `"100% src!dir^x`" --report-file `"$NonAscii.txt`""
    Expect-Args @('-cp', $CP, '--module-path', $MP, '--add-modules', 'kotlin.stdlib',
        '--module', $PmdCliModule, 'check', '-R', 'rules & more (1).xml', '-d', '100% src!dir^x',
        '--report-file', "$NonAscii.txt")

    # See also
    # https://openjdk.org/jeps/223 JEP 223: New Version-String Scheme (Since Java 9)
    # https://openjdk.org/jeps/322 JEP 322: Time-Based Release Versioning (Since Java 10)
    Test-JavaVersion '1.8.0_271' 8
    Test-JavaVersion '1.8.0_345' 8
    Test-JavaVersion '9.0.4'     9
    Test-JavaVersion '10.0.2'   10
    Test-JavaVersion '11.0.6'   11
    Test-JavaVersion '11.0.6.1' 11
    Test-JavaVersion '11.0.13'  11
    Test-JavaVersion '16.0.2'   16
    Test-JavaVersion '17.0.4'   17
    Test-JavaVersion '17.0.4.1' 17
    Test-JavaVersion '18.0.2.1' 18
    Test-JavaVersion '19-ea'    19
    Test-JavaVersion '23.0.2'   23
    Test-JavaVersion '24'       24
    Test-JavaVersion '25'       25
    Test-JavaVersion '25.0.1'   25
    Test-JavaVersion '26-ea'    26

    Test-SpecialPmdHome 'pmd home & co'
    Test-SpecialPmdHome 'pmd 100% home'
    Test-SpecialPmdHome 'pmd^home'
    Test-SpecialPmdHome "pmd $NonAscii"
    Test-SpecialPmdHome 'pmd!home' 'pmd.bat uses delayed expansion, which removes "!" from PMD_HOME'

    Start-Test 'relative path, called from within PMD_HOME'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine 'bin\pmd.bat check' -WorkDir $H
    Expect-ArgValue '--module-path' $MP

    Start-Test 'called as "pmd" from within bin'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine 'pmd check' -WorkDir "$H\bin"
    Expect-ArgValue '--module-path' $MP

    Start-Test 'called as "pmd" with pmd.bat on the PATH'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine 'pmd check' `
        -Environment @{ PATH = "$H\bin;$BasePath" }
    Expect-ArgValue '--module-path' $MP

    Start-Test 'absolute path with forward slashes'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$(Q "$($H.Replace('\', '/'))/bin/pmd.bat") check"
    Expect-ArgValue '--module-path' $MP

    Start-Test 'PMD_HOME via directory junction'
    $junction = Join-Path $TmpDir 'pmd home junction'
    New-Item -ItemType Junction -Path $junction -Value $H | Out-Null
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$(Q "$junction\bin\pmd.bat") check"
    Expect-ArgValue '--module-path' "$junction/lib/modules;$junction/lib/controlsfx-java11"
    # only removes the junction, not the target
    [IO.Directory]::Delete($junction)

    Start-Test 'UNC path'
    $uncHome = '\\localhost\' + $H.Substring(0, 1) + '$' + $H.Substring(2)
    if ([IO.File]::Exists("$uncHome\bin\pmd.bat")) {
        Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$(Q "$uncHome\bin\pmd.bat") check"
        Expect-ArgValue '--module-path' "$uncHome/lib/modules;$uncHome/lib/controlsfx-java11"
    } else {
        Write-Host "Skipped - administrative share $uncHome is not accessible"
    }

    Start-Test 'exit code of java is passed through'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check" `
        -Environment @{ FAKE_JAVA_EXIT_CODE = '4' }
    Expect-ExitCode 4

    Start-Test 'called with CALL from another batch file'
    $callerBat = Join-Path $TmpDir 'caller.bat'
    [IO.File]::WriteAllText($callerBat, ((
        '@echo off',
        'call %*',
        'echo EXIT_CODE=%ERRORLEVEL%',
        'set PMD_HOME 2>nul',
        'set PMD_CLASSPATH 2>nul',
        'set APPNAME 2>nul',
        'set java_version_feature 2>nul',
        'echo DONE',
        '') -join "`r`n"))
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$(Q $callerBat) $PmdBat check" `
        -Environment @{ FAKE_JAVA_EXIT_CODE = '4' }
    Expect-Output 'EXIT_CODE=4'
    Expect-Output 'DONE'
    # SETLOCAL in pmd.bat: no variables leak into the caller
    Expect-NoOutput '(?m)^(PMD_HOME|PMD_CLASSPATH|APPNAME|java_version_feature)='

    Start-Test 'designer, java 21 with OpenJFX (JAVAFX_HOME with spaces)'
    Invoke-Pmd -JavaVersion '21.0.2' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat designer" `
        -Environment @{ JAVAFX_HOME = $OpenJfxHome }
    Expect-ArgValue '-cp' $CP
    Expect-ArgValue '--module-path' "$MP;$OpenJfxHome/lib"
    Expect-ArgValue '--add-modules' "kotlin.stdlib,$PmdDesignerModule,javafx.controls,javafx.fxml"
    Expect-ArgValue '--module' $PmdCliModule
    Expect-Arg 'javafx.controls/javafx.scene.control.skin=org.apache.commons.lang3'
    Expect-NoArg '--enable-native-access=javafx.graphics'
    Expect-NoArg '--illegal-access=warn'

    Start-Test 'designer, java 21 with OpenJFX (JAVAFX_HOME with special characters)'
    $specialOpenJfxHome = Join-Path $TmpDir 'open jfx (x86) & co!'
    [IO.Directory]::CreateDirectory("$specialOpenJfxHome\lib") | Out-Null
    Invoke-Pmd -JavaVersion '21.0.2' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat designer" `
        -Environment @{ JAVAFX_HOME = $specialOpenJfxHome }
    Expect-ArgValue '--module-path' "$MP;$specialOpenJfxHome/lib"

    Start-Test 'designer, java 25 with OpenJFX'
    Invoke-Pmd -JavaVersion '25' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat designer" `
        -Environment @{ JAVAFX_HOME = $OpenJfxHome }
    Expect-ArgValue '--module-path' "$MP;$OpenJfxHome/lib"
    Expect-Arg '--enable-native-access=javafx.graphics'
    Expect-Arg '--sun-misc-unsafe-memory-access=allow'

    Start-Test 'designer, java 17 with bundled JavaFX (java.home with spaces)'
    Invoke-Pmd -JavaVersion '17.0.10' -JavaHome $JavaHomeWithFx -CommandLine "$PmdBat designer" `
        -Environment @{ JAVAFX_HOME = $OpenJfxHome }
    Expect-ArgValue '--module-path' $MP
    Expect-ArgValue '--add-modules' "kotlin.stdlib,$PmdDesignerModule"
    Expect-Arg 'javafx.controls/javafx.scene.control.skin=org.apache.commons.lang3'

    Start-Test 'designer, java 17 with bundled JavaFX (java.home with special characters)'
    $specialJavaHome = Join-Path $TmpDir "java home (x86) & co $NonAscii"
    [IO.Directory]::CreateDirectory("$specialJavaHome\lib") | Out-Null
    [IO.File]::WriteAllText("$specialJavaHome\lib\javafx.properties", '')
    Invoke-Pmd -JavaVersion '17.0.10' -JavaHome $specialJavaHome -CommandLine "$PmdBat designer" `
        -Environment @{ JAVAFX_HOME = $OpenJfxHome }
    Expect-ArgValue '--module-path' $MP
    Expect-ArgValue '--add-modules' "kotlin.stdlib,$PmdDesignerModule"

    Start-Test 'designer, java 8 with bundled JavaFX'
    Invoke-Pmd -JavaVersion '1.8.0_441' -JavaHome $JavaHomeWithFx -CommandLine "$PmdBat designer"
    Expect-Args @('-cp', $CP8, 'net.sourceforge.pmd.cli.PmdCli', 'designer')

    Start-Test 'designer, java 8 with bundled JavaFX, java.home pointing to jre'
    Invoke-Pmd -JavaVersion '1.8.0_471' -JavaHome $JavaHomeWithFx2 -CommandLine "$PmdBat designer"
    Expect-Args @('-cp', $CP8, 'net.sourceforge.pmd.cli.PmdCli', 'designer')

    Start-Test 'designer, java 21 with bundled JavaFX, java.home pointing to jre'
    Invoke-Pmd -JavaVersion '21.0.5' -JavaHome $JavaHomeWithFx2 -CommandLine "$PmdBat designer"
    Expect-ArgValue '--module-path' $MP
    Expect-ArgValue '--add-modules' "kotlin.stdlib,$PmdDesignerModule"

    Start-Test 'designer, java 8u451 without JavaFX'
    Invoke-Pmd -JavaVersion '1.8.0_451' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat designer"
    Expect-Error 2 'JavaFX has been removed from Oracle Java 8 since Java 8u451'

    Start-Test 'designer, java 8u450 without JavaFX'
    Invoke-Pmd -JavaVersion '1.8.0_450' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat designer"
    Expect-Error 2 'For OpenJFX at least Java 10 is required'

    Start-Test 'designer, java 9 without JavaFX'
    Invoke-Pmd -JavaVersion '9.0.4' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat designer" `
        -Environment @{ JAVAFX_HOME = $OpenJfxHome }
    Expect-Error 2 'For OpenJFX at least Java 10 is required'

    Start-Test 'designer, java 21 without JavaFX and without JAVAFX_HOME'
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat designer"
    Expect-Error 2 'The environment variable JAVAFX_HOME is missing'

    Start-Test 'no java on the PATH'
    $emptyDir = Join-Path $TmpDir 'empty'
    [IO.Directory]::CreateDirectory($emptyDir) | Out-Null
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check" `
        -Environment @{ PATH = $emptyDir }
    Expect-Error 2 'No java executable found in PATH'

    # the error messages are printed within IF blocks: the ")" in PMD_HOME must not end the block
    Start-Test 'missing lib/classpath directory'
    [IO.Directory]::Move("$H\lib\classpath", "$H\lib\classpath.bak")
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check"
    Expect-Error 2 "The classpath directory [$H/lib/classpath] does not exist"
    [IO.Directory]::Move("$H\lib\classpath.bak", "$H\lib\classpath")

    Start-Test 'missing lib/modules directory'
    [IO.Directory]::Move("$H\lib\modules", "$H\lib\modules.bak")
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check"
    Expect-Error 2 "The module path directory [$H/lib/modules] does not exist"
    [IO.Directory]::Move("$H\lib\modules.bak", "$H\lib\modules")

    Start-Test 'missing conf directory'
    [IO.Directory]::Move("$H\conf", "$H\conf.bak")
    Invoke-Pmd -JavaVersion '21' -JavaHome $JavaHomeWithoutFx -CommandLine "$PmdBat check"
    Expect-Error 2 "The configuration directory [$H/conf] does not exist"
    [IO.Directory]::Move("$H\conf.bak", "$H\conf")

    Write-Host ''
    $script:KnownIssue = $null
    if ($script:KnownIssues -gt 0) {
        Write-Host "$($script:KnownIssues) check(s) failed because of known issues, see above."
    }
    if ($script:Failures -eq 0) {
        Pass ' All tests passed.'
    } else {
        $failures = $script:Failures
        Fail "$failures check(s) failed."
        $testRunExitCode = 1
    }
} finally {
    Remove-Item -LiteralPath $TmpDir -Recurse -Force -ErrorAction SilentlyContinue
}

exit $testRunExitCode
