package com.example.twodpredictioncompressorgui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {
    public static int[][] convertImageTo2DArray(String imagePath) {
        try {
            // Load the image using ImageIO
            BufferedImage image = ImageIO.read(new File(imagePath));
            if (image == null) {
                System.err.println("Failed to read the image.");
                return null;
            }

            // Get the dimensions of the image
            int width = image.getWidth();
            int height = image.getHeight();

            // Convert the image to a 2D array
            int[][] result = new int[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    result[i][j] = image.getRGB(j, i) & 0xFF; // Get the grayscale value
                }
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
