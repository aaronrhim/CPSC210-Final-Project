package persistence;

import model.*;

import java.io.*;
import org.json.*;
import java.util.*;

/**
 * Utilities for writing and reading {@link Simulation} data to and from JSON files on disk.
 */
public class SimulationReadWriter {
    public static final String SAVE_PATH = "./data/";
    public static final int TAB_SPACES = 4;
    public static final String FILE_SUFFIX = ".json";

    private SimulationReadWriter() {
        // not allowed to be instantiated
    }

    // REQUIRES: fileTitle is non-null/non-empty
    // EFFECTS: given a file title, produces the java.io File object which
    // represents the save file in the save directory
    public static File fileFromFileTitle(String fileTitle) {
        File file = new File(SAVE_PATH + fileTitle + FILE_SUFFIX);
        file.getParentFile().mkdirs();
        return file;
    }

    // REQUIRES: simulation non-null; fileTitle non-null/non-empty
    // MODIFIES: filesystem
    // EFFECTS: writes a given simulation with a given file title to the disk
    public static void writeSimulation(Simulation simulation, String fileTitle)
            throws IOException, FileNotFoundException {
        JSONObject jsonSimulation = JsonConverter.simulationToJsonObject(simulation);
        File writeFile = fileFromFileTitle(fileTitle);
        if (!writeFile.isFile()) {
            writeFile.createNewFile();
        }

        PrintWriter writeStream = new PrintWriter(writeFile);
        writeStream.print(jsonSimulation.toString(TAB_SPACES));
        writeStream.flush();
        writeStream.close();
    }

    // REQUIRES: fileTitle non-null/non-empty
    // MODIFIES: none
    // EFFECTS: reads a given simulation with the given file title from the disk;
    // throws FileNotFoundException if the fileTitle doesn't reference any existing saved file
    public static Simulation readSimulation(String fileTitle) throws FileNotFoundException {
        File readFile = fileFromFileTitle(fileTitle);
        if (!readFile.isFile()) {
            throw new FileNotFoundException();
        }

        String jsonStringBuffer = "";
        Scanner readScanner = new Scanner(readFile);
        while (readScanner.hasNextLine()) {
            jsonStringBuffer += readScanner.nextLine();
        }
        readScanner.close();

        // NOTE: whole-file read keeps formatting simple before delegating to JsonConverter
        JSONObject jsonSimObject = new JSONObject(jsonStringBuffer);
        return JsonConverter.jsonObjectToSimulation(jsonSimObject);
    }
}
