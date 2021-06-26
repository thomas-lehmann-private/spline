/*
 * The MIT License
 *
 * Copyright 2021 Thomas Lehmann.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package magic.system.hyperion;

import magic.system.hyperion.cli.CliCommand;
import magic.system.hyperion.cli.CliException;
import magic.system.hyperion.cli.CliHelpPrinter;
import magic.system.hyperion.cli.CliOptionList;
import magic.system.hyperion.cli.CliParser;
import magic.system.hyperion.command.RunCommandProcessor;
import magic.system.hyperion.command.ThirdPartyCommandProcessor;
import magic.system.hyperion.tools.Capabilities;
import magic.system.hyperion.tools.CapabilitiesPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Hyperion - the special task processing pipeline - the application.
 *
 * @author Thomas Lehmann
 */
public final class Application {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    /**
     * Logger name without timestamps.
     */
    private static final String NO_TIMESTAMP = "NO-TIMESTAMP";

    /**
     * The key for final name of the jar (without extension).
     */
    private static final String PROPERTY_FINAL_NAME = "finalName";

    /**
     * The key for the product version (see pom.xml).
     */
    private static final String PROPERTY_PRODUCT_VERSION = "productVersion";

    /**
     * They key for the build timestamp.
     */
    private static final String PROPERTY_BUILD_TIMESTAMP = "buildTimestamp";

    /**
     * They key for the architect and developer of the project.
     */
    private static final String PROPERTY_AUTHOR = "author";

    /**
     * They key for the git commit id.
     */
    private static final String PROPERTY_GIT_COMMIT_ID = "gitCommitId";

    /**
     * The key for the groovy version embedded into Hyperion tool.
     */
    private static final String PROPERTY_GROOVY_VERSION = "groovyVersion";

    /**
     * Application properties.
     */
    private Properties properties;

    /**
     * Defined global options for application.
     */
    private CliOptionList globalOptions;

    /**
     * Defined list of commands for application.
     */
    private List<CliCommand> commands;

    /**
     * Initialize application.
     */
    private Application() {
        // Nothing to do for the moment
    }

    /**
     * Running the application.
     *
     * @param args provided command line arguments.
     * @throws CliException when validation or the process has failed.
     */
    private void run(final String[] args) throws CliException {
        this.properties = getApplicationProperties();
        this.globalOptions = ApplicationOptionsFunctions.defineGlobalOptions();
        this.commands = ApplicationOptionsFunctions.defineCommands();

        final var parser = CliParser.builder()
                .setGlobalOptions(this.globalOptions).setCommands(this.commands).build();
        final var result = parser.parse(args);

        if (result.getGlobalOptions().containsKey(ApplicationOptions.HELP.getLongName())) {
            printHelp();
        } else if (result.getCommandName().equals(ApplicationCommands.THIRD_PARTY.getCommand())) {
            new ThirdPartyCommandProcessor(
                    this.globalOptions, this.commands, result).processCommand();
        } else if (result.getCommandName().equals(ApplicationCommands.RUN.getCommand())) {
            logEnvironment();
            new RunCommandProcessor(this.globalOptions, this.commands, result).processCommand();
        } else if (result.getCommandName().equals(ApplicationCommands.CAPABILITIES.getCommand())) {
            final var printer = new CapabilitiesPrinter();
            printer.setGroovyVersion(this.properties.getProperty(PROPERTY_GROOVY_VERSION));
            printer.print(LoggerFactory.getLogger(NO_TIMESTAMP)::info);
        }
    }

    /**
     * Logging of environment.
     */
    private void logEnvironment() {
        final String strFormat = "{}: {} ({})";
        LOGGER.info(strFormat, "Hyperion Version",
                this.properties.getProperty(PROPERTY_PRODUCT_VERSION),
                "git commit=" + this.properties.getProperty(PROPERTY_GIT_COMMIT_ID));
        LOGGER.info(strFormat, "Operating System", Capabilities.getOperatingSystemName(),
                "arch=" + Capabilities.getOperatingSystemArchitecture());
        LOGGER.info(strFormat, "Host Name", Capabilities.getHostName(),
                "address=" + Capabilities.getHostAddress());
        LOGGER.info(strFormat, "Java", Capabilities.getJavaVersion(),
                "java class=" + Capabilities.getJavaClassVersion());
    }

    /**
     * Print the help.
     *
     * @throws CliException when validation has failed.
     */
    private void printHelp() throws CliException {
        final var helpPrinter = CliHelpPrinter.builder()
                .setExecution("java -jar "
                        + this.properties.getProperty(PROPERTY_FINAL_NAME) + ".jar")
                .setProductVersion(this.properties.getProperty(PROPERTY_PRODUCT_VERSION))
                .setBuildTimestamp(this.properties.getProperty(PROPERTY_BUILD_TIMESTAMP))
                .setAuthor(this.properties.getProperty(PROPERTY_AUTHOR))
                .setGlobalOptions(this.globalOptions)
                .setCommands(this.commands)
                .build();
        helpPrinter.print(LoggerFactory.getLogger(NO_TIMESTAMP)::info);
    }

    /**
     * Load and provide the application properties.
     *
     * @return application properties.
     */
    private static Properties getApplicationProperties() {
        final var properties = new Properties();

        try (var stream = Application.class.getResourceAsStream("/application.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            // should never happen (the file should always be in the jar too)
            LOGGER.error(e.getMessage(), e);
        }

        return properties;
    }

    /**
     * Initialize the application.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        final var application = new Application();
        try {
            application.run(args);
        } catch (CliException e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
    }
}
