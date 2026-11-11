package game;

import game.view.BoardPanel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.File;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Real display/DPI in isolated JVMs; do not replace native validation with doLayout. */
class BoardVisibilityTest {
    @ParameterizedTest @ValueSource(strings={"1.0","1.5"})
    void realModalGameFlowPaintsGridAndStoneInChildJvm(String scale) throws Exception {
        Path workingDirectory=Path.of("").toAbsolutePath().normalize();
        Path folder=Path.of("bin_test","board-visibility", "scale-"+scale);
        Files.createDirectories(folder);Path log=folder.resolve("probe.txt");
        // The Windows launcher can decode argv using Cp1252; keep Unicode parent paths out of argv.
        String classpath=workingDirectory.relativize(Path.of(BoardDisplayProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI()).normalize())
                +File.pathSeparator+workingDirectory.relativize(Path.of(BoardPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI()).normalize());
        Process child=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java.exe").toString(),
                "-Dsun.java2d.uiScale="+scale,"-Dstdout.encoding=UTF-8","-cp",classpath,
                "game.BoardDisplayProbe",folder.toString()).directory(workingDirectory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        boolean finished=child.waitFor(45,TimeUnit.SECONDS);
        if(!finished) { child.destroyForcibly(); child.waitFor(5,TimeUnit.SECONDS); }
        String evidence=Files.readString(log);
        assertTrue(finished,"Child JVM timeout at scale "+scale+"\n"+evidence);
        assertEquals(0,child.exitValue(),"Missing board pixels at scale "+scale+"\n"+evidence);
        assertEquals(8,evidence.lines().filter(s->s.startsWith("PIXELS ")&&s.contains("grid=true")).count(),evidence);
        assertEquals(4,evidence.lines().filter(s->s.startsWith("PIXELS ")&&s.contains("blackStone=true expectedStone=true")).count(),evidence);
        assertTrue(evidence.contains("errors=0"),evidence);
    }
}
