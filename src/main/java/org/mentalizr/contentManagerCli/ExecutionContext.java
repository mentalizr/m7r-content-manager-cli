package org.mentalizr.contentManagerCli;

import de.arthurpicht.cli.CliCall;
import org.mentalizr.contentManager.exceptions.ConsistencyException;
import org.mentalizr.contentManager.helper.Nio2Helper;
import org.mentalizr.contentManager.helper.PathAssertions;
import org.mentalizr.contentManager.helper.PathConsistencyCheck;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mentalizr.contentManagerCli.ContentManagerCli.*;

public class ExecutionContext {

    private final Path contentRootPath;
    private final boolean verbose;
    private final boolean stacktrace;

    public ExecutionContext(CliCall cliCall) throws ConsistencyException {
        this.contentRootPath = this.obtainContentRootPath(cliCall);
        this.verbose = cliCall.getOptionParserResultGlobal().hasOption(OPTION_VERBOSE);
        this.stacktrace = cliCall.getOptionParserResultGlobal().hasOption(OPTION_STACKTRACE);
    }

    private Path obtainContentRootPath(CliCall cliCall) throws ConsistencyException {
        if (cliCall.getOptionParserResultGlobal().hasOption(OPTION_CONTENT_ROOT)) {
            return obtainUserDefinedContentRootPath(cliCall);
        } else {
            return Nio2Helper.getCurrentWorkingDir();
        }
    }

    private Path obtainUserDefinedContentRootPath(CliCall cliCall) throws ConsistencyException {
        String contentRoot = cliCall.getOptionParserResultGlobal().getValue(OPTION_CONTENT_ROOT);
        Path contentRootPath =  Paths.get(contentRoot).toAbsolutePath();
        PathConsistencyCheck.assertIsExistingDirectory(contentRootPath);
        return contentRootPath;
    }

    public Path getContentRootPath() {
        return this.contentRootPath;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public boolean isStacktrace() {
        return this.stacktrace;
    }

}
