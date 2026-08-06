package cli.utils;

/**
 * ANSI Color utility formatter to output premium terminal designs.
 */
public class OutputFormatter {
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    private static final String BG_RED = "\u001B[41m";
    private static final String BG_GREEN = "\u001B[42m";
    private static final String BG_YELLOW = "\u001B[43m";

    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    public static String red(String text) {
        return RED + text + RESET;
    }

    public static String green(String text) {
        return GREEN + text + RESET;
    }

    public static String yellow(String text) {
        return YELLOW + text + RESET;
    }

    public static String blue(String text) {
        return BLUE + text + RESET;
    }

    public static String cyan(String text) {
        return CYAN + text + RESET;
    }

    public static String bgRed(String text) {
        return BOLD + BG_RED + " " + text + " " + RESET;
    }

    public static String bgGreen(String text) {
        return BOLD + BG_GREEN + " " + text + " " + RESET;
    }

    public static String bgYellow(String text) {
        return BOLD + BG_YELLOW + " " + text + " " + RESET;
    }
}
