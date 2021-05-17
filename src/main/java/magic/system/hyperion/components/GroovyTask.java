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
package magic.system.hyperion.components;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import magic.system.hyperion.interfaces.IVariable;
import magic.system.hyperion.tools.TemplateEngine;

import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Task for running a Groovy script.
 */
@SuppressWarnings("checkstyle:classdataabstractioncoupling")
public class GroovyTask extends AbstractTask {
    /**
     * Groovy file extension.
     */
    private static final String FILE_EXTENSION = ".groovy";

    /**
     * Initialize task.
     *
     * @param strInitTitle - title of the task.
     * @param strInitCode  - Path and name of file of script or inline script.
     */
    public GroovyTask(String strInitTitle, String strInitCode) {
        super(strInitTitle, strInitCode);
    }

    @Override
    public boolean isRegularFile() {
        return super.isRegularFile() && getCode().endsWith(FILE_EXTENSION);
    }

    @Override
    public TaskResult run(Map<String, IVariable> variables) {
        TaskResult taskResult = null;

        try {
            final var writer = new StringWriter();
            final var binding = new Binding(Map.of("out", new PrintWriter(writer)));

            if (isRegularFile()) {

                final var shell = new GroovyShell(binding);
                shell.evaluate(Files.readString(Paths.get(getCode())));
                getVariable().setValue(writer.toString());
                taskResult = new TaskResult(true, getVariable());
            } else {
                final var engine = new TemplateEngine();
                final var renderedText = engine.render(getCode(),
                        Map.of("variables", variables));

                final var shell = new GroovyShell(binding);
                shell.evaluate(renderedText);
                getVariable().setValue(writer.toString());
                taskResult = new TaskResult(true, getVariable());
            }
        } catch (GroovyRuntimeException | IOException e) {
            taskResult = new TaskResult(false, getVariable());
        }

        return taskResult;
    }
}
