package org.mentalizr.contentManagerCli;

import de.arthurpicht.cli.*;
import de.arthurpicht.cli.command.CommandSequenceBuilder;
import de.arthurpicht.cli.command.Commands;
import de.arthurpicht.cli.command.InfoDefaultCommand;
import de.arthurpicht.cli.common.UnrecognizedArgumentException;
import de.arthurpicht.cli.option.ManOption;
import de.arthurpicht.cli.option.OptionBuilder;
import de.arthurpicht.cli.option.Options;
import de.arthurpicht.cli.option.VersionOption;
import de.arthurpicht.cli.parameter.ParametersMin;
import de.arthurpicht.cli.parameter.ParametersOne;
import de.arthurpicht.utils.core.strings.Strings;
import org.mentalizr.contentManager.exceptions.InconsistencyException;
import org.mentalizr.contentManagerCli.console.Console;
import org.mentalizr.contentManagerCli.console.ConsoleConfig;
import org.mentalizr.contentManagerCli.console.ConsoleConfigCreator;
import org.mentalizr.contentManagerCli.executors.build.BuildExecutor;
import org.mentalizr.contentManagerCli.executors.check.CheckExecutor;
import org.mentalizr.contentManagerCli.executors.clean.CleanExecutor;
import org.mentalizr.contentManagerCli.executors.media.prune.MediaPruneExecutor;
import org.mentalizr.contentManagerCli.executors.show.ShowStructureExecutor;
import org.mentalizr.contentManagerCli.executors.media.ls.MediaListExecutor;
import org.mentalizr.mdpCompiler.Const;

public class ContentManagerCli {

    public static final String OPTION_VERBOSE = "verbose";
    public static final String OPTION_STACKTRACE = "stacktrace";
    public static final String OPTION_CONTENT_ROOT = "content_root";
    public static final String OPTION_SILENT = "silent";
    public static final String OPTION_LOGGER = "logger";
    public static final String OPTION_LOGGER_NAME = "logger_name";
    public static final String OPTION_NO_COLOR = "no_color";
    public static final String OPTION_NO_SUMMARY = "no_summary";

    public static final String OPTION_MEDIA_ABSOLUTE = "absolute";
    public static final String OPTION_MEDIA_ORPHANED = "orphaned";

    private static Cli createCli() {

        Options globalOptions = new Options()
                .add(new VersionOption())
                .add(new ManOption())
                .add(new OptionBuilder().withLongName("verbose").withDescription("verbose output").build(OPTION_VERBOSE))
                .add(new OptionBuilder().withShortName('p').withLongName("content-root").withArgumentName("path").withDescription("Path to content root directory.").build(OPTION_CONTENT_ROOT))
                .add(new OptionBuilder().withShortName('s').withLongName("stacktrace").withDescription("Show stacktrace when running on error.").build(OPTION_STACKTRACE))
                .add(new OptionBuilder().withLongName("silent").withDescription("Make no output to console.").build(OPTION_SILENT))
                .add(new OptionBuilder().withLongName("no-color").withDescription("Omit colorization on console output.").build(OPTION_NO_COLOR))
                .add(new OptionBuilder().withLongName("no-summary").withDescription("Omit summary on output.").build(OPTION_NO_SUMMARY))
                .add(new OptionBuilder().withShortName('l').withLongName("logger").withDescription("Print output to logger.").build(OPTION_LOGGER))
                .add(new OptionBuilder().withLongName("logger-name").withDescription("Name of logger. Default is 'org.mentalizr.contentManagerCli'.").build(OPTION_LOGGER_NAME));

        Commands commands = new Commands();

        commands.setDefaultCommand(new InfoDefaultCommand());

        commands.add(new CommandSequenceBuilder()
                .addCommands("build")
                .withParameters(new ParametersMin(0, "program", "programs to be built"))
                .withCommandExecutor(new BuildExecutor())
                .withDescription("Executes a build on specified programs or on all programs if none is specified.")
                .build()
        );

        commands.add(new CommandSequenceBuilder()
                        .addCommands("clean")
//                .withSpecificOptions(new Options()
//                        .add(new OptionBuilder().withShortName('f').withLongName("force").withDescription("force cleaning program directory").build(OPTION__CLEAN__FORCE)))
                        .withParameters(new ParametersMin(0, "program", "programs to be cleaned"))
                        .withCommandExecutor(new CleanExecutor())
                        .withDescription("Cleans specified programs or all programs if none is specified.")
                        .build()
        );

        commands.add(new CommandSequenceBuilder()
                .addCommands("show", "structure")
                .withParameters(new ParametersMin(0, "program", "programs to be shown"))
                .withCommandExecutor(new ShowStructureExecutor())
                .withDescription("Shows program structure for specified programs or for all programs if none is specified.")
                .build()
        );

        Options mediaResourcesSpecificOptions = new Options()
                .add(new OptionBuilder().withLongName("absolute").withShortName('a').withDescription("as absolute path").build(OPTION_MEDIA_ABSOLUTE))
                .add(new OptionBuilder().withLongName("orphaned").withShortName('o').withDescription("show unreferenced (orphaned) media resources only").build(OPTION_MEDIA_ORPHANED));

        commands.add(new CommandSequenceBuilder()
                .addCommands("media", "ls")
                .withSpecificOptions(mediaResourcesSpecificOptions)
                .withParameters(new ParametersMin(0, "program", "programs to be applied for listing media resources."))
                .withCommandExecutor(new MediaListExecutor())
                .withDescription("Lists all media resources of specified programs. By default referenced ones.")
                .build()
        );

        commands.add(new CommandSequenceBuilder()
                .addCommands("media", "prune")
                .withParameters(new ParametersMin(0, "program", "programs to be applied for pruning media resources."))
                .withCommandExecutor(new MediaPruneExecutor())
                .withDescription("Move all orphaned media resources of specified programs to program directory media-pruned.")
                .build()
        );

        commands.add(new CommandSequenceBuilder()
                .addCommands("check")
                .withParameters(new ParametersOne("content-id", "content id of mdp file to be checked"))
                .withCommandExecutor(new CheckExecutor())
                .withDescription("Checks a single mdp file for syntactically correctness. With no impact for the repo state. For development purposes.")
                .build()
        );

        CliDescription cliDescription = new CliDescriptionBuilder()
                .withDescription("mentalizr content manager CLI\nhttps://github.com/mentalizr/m7r-content-manager-cli")
                .withVersionByTag("0.1.0", "2021-12-21", "mdpc version " + Const.VERSION + " from " + Const.VERSION_DATE)
                .build("m7r-cm");

        return new CliBuilder()
                .withGlobalOptions(globalOptions)
                .withCommands(commands)
                .withAutoHelp()
                .build(cliDescription);
    }

    public static void main(String[] args) {
        Cli cli = createCli();
        CliCall cliCall = null;
        try {
            cliCall = cli.parse(args);
        } catch (UnrecognizedArgumentException e) {
            System.out.println(e.getExecutableName() + " call syntax error. " + e.getMessage());
            System.out.println(e.getCallString());
            System.out.println(e.getCallPointerString());
            System.exit(1);
        }

        boolean showStacktrace = cliCall.getOptionParserResultGlobal().hasOption(OPTION_STACKTRACE);
        boolean isVerbose = cliCall.getOptionParserResultGlobal().hasOption(OPTION_VERBOSE);

        ConsoleConfig consoleConfig = ConsoleConfigCreator.create(cliCall);
        Console.initialize(consoleConfig);

        if (isVerbose) {
            String welcomeString = cliCall.getCliDefinition().getCliDescription().getDescriptionFirstLine()
                    + " - Version " + cliCall.getCliDefinition().getCliDescription().getVersionText();
            Console.out(welcomeString + "\n");
        }

        try {
            cli.execute(cliCall);
        } catch (CommandExecutorException e) {
            if (e.getCause() != null && e.getCause() instanceof InconsistencyException) {
                Console.errorOut(e.getMessage());
            } else {
                if (Strings.isSpecified(e.getMessage())) {
                    Console.errorOut("CommandExecutorException: " + e.getMessage());
                }
            }
            if (showStacktrace) e.printStackTrace(consoleConfig.getErrorOut());
            System.exit(1);
        } catch (RuntimeException | AssertionError e) {
            Console.internalErrorOut(e.getMessage());
            if (showStacktrace) e.printStackTrace(consoleConfig.getErrorOut());
            System.exit(1);
        }
    }

}
