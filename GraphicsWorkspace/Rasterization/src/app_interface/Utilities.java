package app_interface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.IntBuffer;

import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class Utilities {
    public static String openFileChooser(Stage stage, String fileExtension, String initialDirectory) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        
        // Set initial directory (optional)
        File initialDirFile = new File(initialDirectory);
        if (initialDirFile.exists() && initialDirFile.isDirectory()) 
        	fileChooser.setInitialDirectory(initialDirFile);
        else
        	fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        // Set file extension filters (optional)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Files", "*." + fileExtension)
        );
        
        // Open the file chooser dialog
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        } else {
            return null;
        }
    }	
    
    public static String saveFileChooser(Stage stage, String fileExtension, String initialDirectory) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        
        // Set initial directory (optional)
        File initialDirFile = new File(initialDirectory);
        if (initialDirFile.exists() && initialDirFile.isDirectory()) 
        	fileChooser.setInitialDirectory(initialDirFile);
        else
        	fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        // Set file extension filter
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Files", "*." + fileExtension)
        );
        
        // Set initial file name (optional)
        fileChooser.setInitialFileName("untitled." + fileExtension);
        
        // Open the file chooser dialog
        File selectedFile = fileChooser.showSaveDialog(stage);
        
        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        } else {
            return null;
        }
    }   
   
	// Method to compute the relative path of a file
	public static String getRelativePath(File file) {
		// Get the current working directory
		File currentDir = new File(System.getProperty("user.dir"));

		String currentDirPath = currentDir.getAbsolutePath();
		String filePath = file.getAbsolutePath();

		// Remove the current directory path from the selected file path to get the
		// relative path
		if (filePath.startsWith(currentDirPath)) {
			return ".\\" + filePath.substring(currentDirPath.length() + 1); // +1 to remove the separator
		} else {
			return filePath; // If the file is outside the current directory
		}
	}
    public static void saveIntBufferAsBMP(IntBuffer buffer, int width, int height, String filePath) {
        final int BYTES_PER_PIXEL = 3; // BMP uses 24-bit color (3 bytes per pixel)
        final int rowPadding = (4 - (width * BYTES_PER_PIXEL) % 4) % 4; // Padding to align rows to 4-byte boundaries
        final int imageSize = (width * BYTES_PER_PIXEL + rowPadding) * height; // Total image data size
        final int fileSize = 54 + imageSize; // File size (header + image data)

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            // Write BMP Header (14 bytes)
            fos.write(new byte[] { 'B', 'M' }); // Signature
            fos.write(intToBytes(fileSize)); // File size
            fos.write(new byte[] { 0, 0, 0, 0 }); // Reserved
            fos.write(intToBytes(54)); // Data offset (header size)

            // Write DIB Header (40 bytes)
            fos.write(intToBytes(40)); // Header size
            fos.write(intToBytes(width)); // Image width
            fos.write(intToBytes(height)); // Image height
            fos.write(new byte[] { 1, 0 }); // Planes
            fos.write(new byte[] { 24, 0 }); // Bits per pixel
            fos.write(new byte[] { 0, 0, 0, 0 }); // Compression (none)
            fos.write(intToBytes(imageSize)); // Image size
            fos.write(new byte[] { 0, 0, 0, 0 }); // X pixels per meter (not specified)
            fos.write(new byte[] { 0, 0, 0, 0 }); // Y pixels per meter (not specified)
            fos.write(new byte[] { 0, 0, 0, 0 }); // Colors in color table (none)
            fos.write(new byte[] { 0, 0, 0, 0 }); // Important colors (all)

            // Write Pixel Data
            byte[] rowData = new byte[width * BYTES_PER_PIXEL + rowPadding];
            for (int y = height - 1; y >= 0; y--) { // BMP stores pixels bottom-to-top
                int rowIndex = 0;
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    int argb = buffer.get(index);
                    rowData[rowIndex++] = (byte) (argb & 0xFF); // Blue
                    rowData[rowIndex++] = (byte) ((argb >> 8) & 0xFF); // Green
                    rowData[rowIndex++] = (byte) ((argb >> 16) & 0xFF); // Red
                }
                // Add row padding
                for (int p = 0; p < rowPadding; p++) {
                    rowData[rowIndex++] = 0;
                }
                fos.write(rowData);
            }

            System.out.println("Image successfully written to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException occurred while writing the image: " + e.getMessage());
        }
    }
 
    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
        };
    }
    
    public static void saveIntBufferAsCSV(IntBuffer buffer, int width, int height, String filePath) {
        File file = new File(filePath);

        try (FileWriter writer = new FileWriter(file)) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    int argb = buffer.get(index);

                    // Extract the RGB components
                    int blue = argb & 0xFF;
                    int green = (argb >> 8) & 0xFF;
                    int red = (argb >> 16) & 0xFF;

                    // Write the R-G-B value as "red-green-blue"
                    writer.write(String.format("%03d-%03d-%03d", red, green, blue));

                    // Add a comma between columns, but not after the last column in a row
                    if (x < width - 1) {
                        writer.write(",");
                    }
                }

                // Add a new line after each row
                writer.write("\n");
            }

            System.out.println("CSV file successfully written to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException occurred while writing the CSV file: " + e.getMessage());
        }
    }
    
}
