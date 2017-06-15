package org.strongback.command;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Rothanak So
 */
public class CommandTesterTest {

    @Test
    public void shouldSupportCommandGroups() {
        // Regression test for issue #111: https://github.com/strongback/strongback-java/issues/111
        Command command = Command.pause(100, TimeUnit.MILLISECONDS);
        WatchedCommand watched = WatchedCommand.watch(command); // CommandGroups don't call end(), so watch Command
        CommandTester tester = new CommandTester(CommandGroup.runSequentially(watched));

        tester.step(1);
        assertThat(watched.isEnded()).isFalse();

        tester.step(101);
        assertThat(watched.isEnded()).isTrue();
    }
}
