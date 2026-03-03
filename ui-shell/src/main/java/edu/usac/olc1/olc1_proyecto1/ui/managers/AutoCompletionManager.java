package edu.usac.olc1.olc1_proyecto1.ui.managers;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;

public class AutoCompletionManager {

    /**
     * Sets up auto-completion for the CodeArea.
     */
    public void setupAutoCompletion(CodeArea codeEditor) {
        codeEditor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.PERIOD) {
                showSuggestions(codeEditor);
            }
        });
    }

    private void showSuggestions(CodeArea codeEditor) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
                createMenuItem("author", codeEditor),
                createMenuItem("args", codeEditor),
                createMenuItem("bot", codeEditor),
                createMenuItem("channel", codeEditor)
        );
        codeEditor.getCaretBounds().ifPresent(bounds -> {
            contextMenu.show(codeEditor, bounds.getMaxX(), bounds.getMaxY());
        });
    }

    private MenuItem createMenuItem(String text, CodeArea codeEditor) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(event -> {
            codeEditor.insertText(codeEditor.getCaretPosition(), text);
        });
        return item;
    }
}