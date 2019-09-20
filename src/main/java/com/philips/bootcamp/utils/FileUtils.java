package com.philips.bootcamp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import com.philips.bootcamp.domain.Constants;

public class FileUtils {
    public static String getFileContents(File file) {
        if (file != null && file.exists() && Files.isExecutable(file.toPath())) {
            StringBuilder fileContents = new StringBuilder();
            if (file.isDirectory())
                throw new RuntimeException("[ERROR] Cannot read contents of file provided");
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                
                String line = null;
                while ((line  = reader.readLine()) != null) {
                    fileContents.append(line + "\n");
                }
            } catch (IOException ioe) {
                System.out.println("[ERROR] Couldn't read from file");
                return null;
            }
            return fileContents.toString();
        }
        return null;
    }

    public static void writeFileContents(File file, String contents) {
        try {
            String name = file.getName();
            if (!file.exists() && name != null && name.length() > 0) {
                file.createNewFile();
            }
        } catch (IOException ioe) {
            throw new RuntimeException("[ERROR] Something went wrong during file creation!");
        }

        if (file != null && file.isFile()) {
            if (contents == null) contents = "";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(contents);
            } catch (IOException ioe) {
                System.out.println("[ERROR] Couldn't write to file");
            }
        } else {
            throw new RuntimeException("[ERROR] Invalid file provided!");
        }
    }

    public static void deleteDirectoryRecursion(Path path) throws IOException {
        if (path != null && path.toFile().exists()) {
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                    for (Path entry : entries) {
                    deleteDirectoryRecursion(entry);
                    }
                }
            }
            Files.delete(path);
        }
    }

    public static void deleteFolder(File directory) {
        if (directory == null || !directory.exists()) return;
        
        StringBuilder command = new StringBuilder();
        command.append("\"" + new File(Constants.toolsDirectory, "rdir.bat").getAbsolutePath() + "\"");
        command.append(" \"" + directory.getAbsolutePath() + "\"");
        TerminalUtils.run(command.toString());
    }

    public static void renameFile(File file, String newName) {
        StringBuilder command = new StringBuilder();
        command.append("\"" + new File(Constants.toolsDirectory, "rename.bat").getAbsolutePath() + "\"");
        command.append(" \"" + file.getAbsolutePath() + "\"");
        command.append(" \"" + newName + "\"");
        TerminalUtils.run(command.toString());
    }
}