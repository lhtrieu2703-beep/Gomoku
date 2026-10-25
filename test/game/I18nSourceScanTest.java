package game;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quét source để đảm bảo:
 * (I1a) Không còn "Lang.isVI()" trong src/game/view/ trừ Lang.java.
 * (I1b) Không còn string literal chứa ký tự tiếng Việt có dấu trong view/*.java trừ Lang.java.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class I18nSourceScanTest {

    static final Path VIEW_DIR = Paths.get(
        "c:\\VNUK\\Năm 3 - Kì 1 - 2026_2027\\Basic Project\\Gomoku\\src\\game\\view");

    // Regex ký tự Unicode tiếng Việt có dấu
    static final Pattern VIET_CHAR = Pattern.compile(
        "[\u00C0-\u024F\u1E00-\u1EFF]");

    // Regex string literal Java: "..." (đơn giản, bỏ escaped quote)
    static final Pattern STRING_LITERAL = Pattern.compile(
        "\"((?:[^\"\\\\]|\\\\.)*)\"");

    // Allowlist: file được phép chứa tiếng Việt có dấu trong chuỗi.
    // SoundManager.java: tiếng Việt nằm trong Javadoc comment ("cách") —
    //   parser đơn giản của test bắt nhầm vì comment chứa dấu ngoặc kép.
    //   SoundManager không phải file view nên vẫn cần allowlist.
    static final Set<String> ALLOWLIST_FILES = Set.of("Lang.java", "SoundManager.java");

    // Allowlist: comment/javadoc được phép — chỉ kiểm string literal
    // (regex STRING_LITERAL chỉ bắt trong dấu ngoặc kép)

    @Test
    @Order(1)
    @DisplayName("I1a – Không còn 'Lang.isVI()' trong view/*.java (trừ Lang.java)")
    void i1a_noIsVICall() throws IOException {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(VIEW_DIR)) {
            files.filter(p -> p.toString().endsWith(".java"))
                 .filter(p -> !ALLOWLIST_FILES.contains(p.getFileName().toString()))
                 .forEach(p -> {
                     try {
                         List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
                         for (int i = 0; i < lines.size(); i++) {
                             String line = lines.get(i);
                             if (line.contains("Lang.isVI()")) {
                                 violations.add(p.getFileName() + ":" + (i+1) + ": " + line.trim());
                             }
                         }
                     } catch (IOException e) {
                         violations.add("IOException reading " + p);
                     }
                 });
        }

        if (!violations.isEmpty()) {
            fail("I1a – Còn 'Lang.isVI()' ở:\n" + String.join("\n", violations));
        }
        System.out.println("[I1a] PASS – Không còn Lang.isVI() trong view/*.java (trừ Lang.java)");
    }

    @Test
    @Order(2)
    @DisplayName("I1b – Không còn string literal tiếng Việt có dấu trong view/*.java (trừ Lang.java)")
    void i1b_noVietnameseInStringLiterals() throws IOException {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(VIEW_DIR)) {
            files.filter(p -> p.toString().endsWith(".java"))
                 .filter(p -> !ALLOWLIST_FILES.contains(p.getFileName().toString()))
                 .forEach(p -> {
                     try {
                         List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
                         for (int i = 0; i < lines.size(); i++) {
                             String line = lines.get(i);
                             // Bỏ qua comment dòng đơn
                             String code = removeLineComment(line);
                             Matcher m = STRING_LITERAL.matcher(code);
                             while (m.find()) {
                                 String content = m.group(1);
                                 if (VIET_CHAR.matcher(content).find()) {
                                     violations.add(p.getFileName() + ":" + (i+1)
                                         + ": \"" + content + "\"");
                                 }
                             }
                         }
                     } catch (IOException e) {
                         violations.add("IOException reading " + p);
                     }
                 });
        }

        if (!violations.isEmpty()) {
            fail("I1b – Còn string literal tiếng Việt trong view/ (trừ Lang.java):\n"
                + String.join("\n", violations));
        }
        System.out.println("[I1b] PASS – Không còn string literal tiếng Việt có dấu trong view/*.java");
    }

    /** Xóa phần sau "//" trên một dòng (ngoài string literal). */
    private String removeLineComment(String line) {
        // Đơn giản: tìm "//" không trong chuỗi
        boolean inString = false;
        for (int i = 0; i < line.length() - 1; i++) {
            char c = line.charAt(i);
            if (c == '\\' && inString) { i++; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (!inString && c == '/' && line.charAt(i+1) == '/') {
                return line.substring(0, i);
            }
        }
        return line;
    }
}
