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
package magic.system.hyperion.reader;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import magic.system.hyperion.tools.MessagesCollector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing class {@link DocumentReader}.
 *
 * @author Thomas Lehmann
 */
@DisplayName("Testing DocumentReader class")
public class DocumentReaderTest {

    /**
     * Intention to have a quite complete document.
     *
     * @throws URISyntaxException when loading of the document has failed.
     */
    @Test
    public void testReader() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/document-is-valid.yml").toURI());
        final var reader = new DocumentReader(path);
        final var document = reader.read();
        //CHECKSTYLE.OFF: MultipleStringLiterals - ok here
        assertNotNull(document, "Document shouldn't be null");
        //CHECKSTYLE.ON: MultipleStringLiterals
        assertEquals(1, document.getListOfTaskGroups().size());
        assertEquals(2, document.getListOfTaskGroups().get(0).getListOfTasks().size());
    }

    /**
     * Testing a document with Groovy.
     *
     * @throws URISyntaxException when loading of the document has failed.
     */
    @Test
    public void testGroovy() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/document-with-groovy.yml").toURI());
        final var reader = new DocumentReader(path);
        final var document = reader.read();
        //CHECKSTYLE.OFF: MultipleStringLiterals - ok here
        assertNotNull(document, "Document shouldn't be null");
        //CHECKSTYLE.ON: MultipleStringLiterals
        assertEquals(1, document.getListOfTaskGroups().size());
        assertEquals(2, document.getListOfTaskGroups().get(0).getListOfTasks().size());

        MessagesCollector.clear();
        document.run();
        assertTrue(MessagesCollector.getMessages().contains("set variable default=hello world!"));
        assertTrue(MessagesCollector.getMessages().contains("set variable test2= this is a demo "));
    }
}
