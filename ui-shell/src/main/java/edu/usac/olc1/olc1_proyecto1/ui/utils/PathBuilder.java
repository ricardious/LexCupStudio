package edu.usac.olc1.olc1_proyecto1.ui.utils;


public class PathBuilder {

    public static String concatPaths(String root, String sub) {
        if (root == null) {
            root = "";
        }
        if (sub == null) {
            sub = "";
        }
        if (root.endsWith("/") && sub.startsWith("/")) {
            return root + sub.substring(1);
        }
        if (!root.endsWith("/") && !sub.startsWith("/")) {
            return root + "/" + sub;
        }
        return root + sub;
    }

    public static String concatPaths(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        String result = parts[0];
        for (int i = 1; i < parts.length; i++) {
            result = concatPaths(result, parts[i]);
        }
        return result;
    }
}
