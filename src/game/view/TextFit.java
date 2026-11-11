package game.view;

import java.awt.FontMetrics;

/** Explicit ASCII ellipsis, preserving complete Unicode code points. */
final class TextFit {
    private TextFit() {}

    static String ellipsis(String text, FontMetrics metrics, int width) {
        if (metrics.stringWidth(text) <= width) return text;
        while (!text.isEmpty() && metrics.stringWidth(text + "...") > width)
            text = text.substring(0, text.offsetByCodePoints(text.length(), -1));
        return metrics.stringWidth("...") <= width ? text + "..." : "";
    }

    static String lines(String text, FontMetrics metrics, int width, int count) {
        StringBuilder result = new StringBuilder();
        String remaining = text;
        for (int line = 0; line < count && !remaining.isEmpty(); line++) {
            if (line > 0) result.append('\n');
            if (line == count - 1 || metrics.stringWidth(remaining) <= width) {
                result.append(ellipsis(remaining, metrics, width));
                break;
            }
            int end = 0;
            while (end < remaining.length()) {
                int next = remaining.offsetByCodePoints(end, 1);
                if (metrics.stringWidth(remaining.substring(0, next)) > width) break;
                end = next;
            }
            if (end == 0) break;
            int space = remaining.lastIndexOf(' ', end);
            if (space > 0) end = space;
            result.append(remaining, 0, end);
            remaining = remaining.substring(end).stripLeading();
        }
        return result.toString();
    }
}
