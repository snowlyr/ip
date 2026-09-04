package shan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ShanTest {
    @TempDir
    private Path dataDirectory;

    @Test
    void executeCommand_validAddAndList_returnsCommandResponses() {
        Shan shan = new Shan(this.dataDirectory.resolve("shan.txt"));
        shan.initialize();

        CommandResult addResult = shan.executeCommand("todo read book");
        assertTrue(addResult.message().contains("I've added this"));
        assertFalse(addResult.shouldExit());

        CommandResult listResult = shan.executeCommand("list");
        assertTrue(listResult.message().contains("read book"));
        assertFalse(listResult.shouldExit());
    }

    @Test
    void executeCommand_unknownCommand_returnsErrorWithoutExit() {
        Shan shan = new Shan(this.dataDirectory.resolve("shan.txt"));

        CommandResult result = shan.executeCommand("unknown");

        assertEquals("I don't understand bro.", result.message());
        assertFalse(result.shouldExit());
    }

    @Test
    void executeCommand_bye_returnsFarewellAndExitFlag() {
        Shan shan = new Shan(this.dataDirectory.resolve("shan.txt"));

        CommandResult result = shan.executeCommand("bye");
        assertEquals("Bye! See you soon.", result.message());
        assertTrue(result.shouldExit());
    }
}
