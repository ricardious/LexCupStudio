package edu.usac.olc1.olc1_proyecto1.ui.components;

import java.io.File;

@FunctionalInterface
public interface ActiveFileAccessor {
    File getActiveFile();
    default String getActiveFileText() { return null; }
}
