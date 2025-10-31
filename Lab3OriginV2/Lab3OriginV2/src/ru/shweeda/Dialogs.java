package ru.shweeda;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.io.File;

public class Dialogs {
    public static File showSaveFileDialog(JFileChooser chooser) {
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            FileFilter filter = chooser.getFileFilter();

            // Автодобавление расширения
            if (filter.getDescription().contains("jpeg") && !file.getName().toLowerCase().endsWith(".jpg")) {
                file = new File(file.getAbsolutePath() + ".jpg");
            } else if (filter.getDescription().contains("png") && !file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            } else if (filter.getDescription().contains("frac") && !file.getName().toLowerCase().endsWith(".frac")) {
                file = new File(file.getAbsolutePath() + ".frac");
            }

            return file;
        }
        return null;
    }

}
