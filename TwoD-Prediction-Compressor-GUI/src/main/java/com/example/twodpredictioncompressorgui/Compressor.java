package com.example.twodpredictioncompressorgui;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import static java.lang.Math.*;

public class Compressor {

    private static final int NUM_CLASSES = 16;
    private static final int CLASS_RANGE = 32;
    private static final int MID_VALUE = CLASS_RANGE / 2;
    public void compress(String inputImagePath, String outputBinaryPath){
        // Convert image to 2D array
        int[][] pixel = ImageUtil.convertImageTo2DArray(inputImagePath);
        ArrayList<ArrayList<Integer>> pixels = new ArrayList<>();
        for (int i = 0; i < pixel.length; i++) {
            ArrayList<Integer> rowList = new ArrayList<>();
            for (int j = 0; j < pixel[i].length; j++) {
                rowList.addLast(pixel[i][j]);
            }
            pixels.add(rowList);
        }

        ArrayList<ArrayList<Integer>> difference = new ArrayList<>(getDifference(pixels));

        ArrayList<ArrayList<Integer>> quantizedDifference = new ArrayList<>(quantize(difference));

        writeArrayListToFile(quantizedDifference,outputBinaryPath);
        System.out.println("Image Compressed Successfully!");
    }

    public void decompress (ArrayList<ArrayList<Integer>> quantizedDifference){
        ArrayList<ArrayList<Integer>> deQuantizedDifference = new ArrayList<>(deQuantize(quantizedDifference));

        ArrayList<ArrayList<Integer>> decoded = new ArrayList<>(getDecoded(deQuantizedDifference));

        String outputImagePath = "/run/media/phantom/New Volume/University/Data Compression/Assignments/Assignment 5/2D-Predictive-Compressor/TwoD-Prediction-Compressor-GUI/src/main/resources/com/example/twodpredictioncompressorgui/DeCompressed.jpg";
        try {
            BufferedImage compressedImage = generateCompressedImage(decoded);
            saveCompressedImage(compressedImage, outputImagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("File DeCompressed Successfully!");
    }

    public ArrayList<ArrayList<Integer>> quantize(ArrayList<ArrayList<Integer>> difference) {
        for (int i = 1; i < difference.size(); i++) {
            for (int j = 1; j < difference.get(i).size(); j++) {
                int value = difference.get(i).get(j);

                // Quantize the value
                int quantizedValue = quantizeValue(value);

                // Update the difference array with quantized value
                difference.get(i).set(j, quantizedValue);
            }
        }

        return difference;
    }

    private int quantizeValue(int value) {
        // Ensure that the value is within the range [-255, 255]
        value = Math.max(-255, Math.min(255, value));

        // Map the value to the corresponding quantization level
        int quantizationLevel = (value + 255) / CLASS_RANGE;

        // Ensure that the quantization level is within the range [0, NUM_CLASSES - 1]
        quantizationLevel = Math.max(0, Math.min(NUM_CLASSES - 1, quantizationLevel));

        // Map the quantization level back to the quantized value
        //int quantizedValue = quantizationLevel * CLASS_RANGE - 255;

        return quantizationLevel;
    }

    public ArrayList<ArrayList<Integer>> deQuantize(ArrayList<ArrayList<Integer>> quantizedDifference) {
        for (int i = 1; i < quantizedDifference.size(); i++) {
            for (int j = 1; j < quantizedDifference.get(i).size(); j++) {
                int quantizedValue = quantizedDifference.get(i).get(j);

                // Dequantize the value
                int deQuantizedValue = deQuantizeValue(quantizedValue);

                // Update the dequantized difference array with dequantized value
                quantizedDifference.get(i).set(j, deQuantizedValue);
            }
        }

        return quantizedDifference;
    }


    private int deQuantizeValue(int quantizationLevel) {
        // Map the quantized value back to the original range
        int deQuantizedValue = quantizationLevel * CLASS_RANGE + MID_VALUE - 255;

        return deQuantizedValue;
    }

    public ArrayList<ArrayList<Integer>> getDecoded(ArrayList<ArrayList<Integer>> deQuantized){
        ArrayList<ArrayList<Integer>> predicted = new ArrayList<>(getPredicted(deQuantized));
        ArrayList<ArrayList<Integer>> decoded = new ArrayList<>();
        int[][]d = new int[predicted.size()][predicted.get(0).size()];
        for (int i = 1; i < predicted.size(); i++) {
            for (int j = 1; j < predicted.get(i).size(); j++) {
                d[i][j] = predicted.get(i).get(j)+deQuantized.get(i).get(j);
            }
        }
        for (int i = 0; i < predicted.size(); i++) {
            ArrayList<Integer> rowList = new ArrayList<>();
            for (int j = 1; j < predicted.get(i).size(); j++) {
                rowList.addLast(d[i][j]);
            }
            decoded.add(rowList);
        }
        return decoded;
    }

    public ArrayList<ArrayList<Integer>> getDifference(ArrayList<ArrayList<Integer>> pixels) {
        ArrayList<ArrayList<Integer>> difference = new ArrayList<>();
        for (int i = 0; i < pixels.size(); i++) {
            difference.add(new ArrayList<>(pixels.get(i)));
        }
        ArrayList<ArrayList<Integer>> predicted = new ArrayList<>(getPredicted(pixels));

        for (int i = 1; i < predicted.size(); i++) {
            for (int j = 1; j < predicted.get(i).size(); j++) {
                int predictedValue = predicted.get(i).get(j);
                int differenceValue = pixels.get(i).get(j) - predictedValue;
//                System.out.print(differenceValue + " ");
                predicted.get(i).set(j, predictedValue);

                difference.get(i).set(j, differenceValue);
            }
//            System.out.println();
        }

        return difference;
    }

    public ArrayList<ArrayList<Integer>> getPredicted(ArrayList<ArrayList<Integer>> pixels){
        ArrayList<ArrayList<Integer>> predicted = new ArrayList<>();
        for (int i = 0; i < pixels.size(); i++) {
            predicted.add(new ArrayList<>(pixels.get(i)));
        }
        for (int i = 1; i < predicted.size(); i++) {
            for (int j = 1; j < predicted.get(i).size(); j++) {
                predicted.get(i).set(j, predict(predicted.get(i).get(j-1), predicted.get(i-1).get(j), predicted.get(i-1).get(j-1)));
            }
        }

        return predicted;
    }

    public int predict(int j1, int i1, int j1i1){
        int predicted;
        if(j1i1 <= min(j1, i1)){
            predicted = max(j1, i1);
        } else if(j1i1 >= max(j1, i1)){
            predicted = min(j1, i1);
        } else{
            predicted = j1 + i1 - j1i1;
        }
        return predicted;
    }

    private BufferedImage generateCompressedImage(ArrayList<ArrayList<Integer>> decoded) {
        int height = decoded.size();
        int width = decoded.get(0).size();

        BufferedImage compressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int intensity = decoded.get(i).get(j);
                compressedImage.setRGB(j, i, intensity << 16 | intensity << 8 | intensity);
            }
        }

        return compressedImage;
    }


    private void saveCompressedImage(BufferedImage compressedImage, String outputImagePath) throws IOException {
        File outputImageFile = new File(outputImagePath);
        ImageIO.write(compressedImage, "jpg", outputImageFile);
    }

    public void writeArrayListToFile(ArrayList<ArrayList<Integer>> data, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (ArrayList<Integer> row : data) {
                StringBuilder rowString = new StringBuilder();
                for (int value : row) {
                    rowString.append(value).append(" ");
                }
                // Trim the trailing space and write the row to the file
                writer.write(rowString.toString().trim());
                // Move to the next line
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



