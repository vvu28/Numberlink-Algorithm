import java.util.Map;

public class Printer {
    // ANSI color codes
    public static final String RESET = "\u001B[0m";

    private static final Map<Character, String> COLOR_MAP = Map.of(
            'a', "\u001B[31m", // red
            'b', "\u001B[32m", // green
            'c', "\u001B[33m", // yellow
            'd', "\u001B[34m", // blue
            'e', "\u001B[35m", // purple
            'f', "\u001B[36m", // cyan
            'g', "\u001B[91m", // bright red
            'h', "\u001B[92m", // bright green
            'i', "\u001B[94m"  // bright blue
    );

        public static void printGrid(Puzzle puzzle) {
        Map<Cell, Point> points = puzzle.getPoints();
        int rows = puzzle.getRows();
        int cols = puzzle.getCols();

        for (int row = 0; row < rows; row++) {

            // Top border
            for (int col = 0; col < cols; col++) {
                System.out.print("+---");
            }
            System.out.println("+");

            // Cell contents
            for (int col = 0; col < cols; col++) {
                char cellChar = ' ';
                String color = RESET;

                for (Cell key : points.keySet()) {
                    if (key.x() == col && key.y() == row) {
                        Point p = points.get(key);
                        cellChar = p.color();
                        if (p.isRoot()) {
                            cellChar = Character.toUpperCase(cellChar);
                        }
                        color = COLOR_MAP.getOrDefault(Character.toLowerCase(p.color()), RESET);
                        break;
                    }
                }

                System.out.print("| " + color + cellChar + RESET + " ");
            }
            System.out.println("|");
        }

        // Bottom border
        for (int col = 0; col < cols; col++) {
            System.out.print("+---");
        }
        System.out.println("+");
    }
}
