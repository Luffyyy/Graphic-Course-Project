package app_interface;

import org.joml.Vector3f;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The {@code SphereTexture} class represents a texture mapped onto a sphere and
 * provides methods to sample pixel colors based on direction vectors. The
 * texture is loaded from an image file, and pixel colors are retrieved through
 * bilinear interpolation or nearest-neighbor sampling.
 */
public class SphereTexture {
	private int width = -1;
	private int height = -1;
	private byte[] imageData = null;
	private String filename;

	/**
	 * Constructs a {@code SphereTexture} object by loading the image from the
	 * specified file path.
	 *
	 * @param filepath the path to the image file to be used as the texture
	 * @throws IOException if the image fails to load from the specified file path
	 */
	public SphereTexture(String filepath) throws IOException {
		loadImage(filepath);
	}

	/**
	 * Samples the color from the texture based on a given direction vector. The
	 * direction is normalized, and the corresponding texture coordinates are
	 * calculated to retrieve the pixel color. Bilinear interpolation is used to
	 * produce smooth transitions between pixels.
	 *
	 * @param direction the direction vector used to determine the texture
	 *                  coordinates
	 * @return the interpolated color at the specified direction as a
	 *         {@code Vector3f} (RGB values)
	 */
	public Vector3f sampleDirectionFromMiddle(Vector3f direction) {
		boolean interpolate = true;

		float directionLength = direction.length();
		Vector3f normalizedDirection = new Vector3f(direction);
		if (directionLength != 0) {
			normalizedDirection.div(directionLength);
		}

		// Calculate texture coordinates from the direction vector
		float u = 0.5f + (float) (Math.atan2(normalizedDirection.z, normalizedDirection.x) / (2.0 * Math.PI));
		float v = 0.5f - (float) (Math.asin(normalizedDirection.y) / Math.PI);

		// Perform bilinear interpolation if needed
		if (interpolate) {
			// Get pixel coordinates
			int x0 = (int) (u * (width - 1));
			int y0 = (int) (v * (height - 1));
			int x1 = Math.min(x0 + 1, width - 1);
			int y1 = Math.min(y0 + 1, height - 1);

			// Weights for interpolation
			float wx0 = (x1 - u * (width - 1)) * (y1 - v * (height - 1));
			float wx1 = (u * (width - 1) - x0) * (y1 - v * (height - 1));
			float wx2 = (x1 - u * (width - 1)) * (v * (height - 1) - y0);
			float wx3 = (u * (width - 1) - x0) * (v * (height - 1) - y0);

			// Sample colors
			Vector3f c0 = getImagePixel(x0, y0);
			Vector3f c1 = getImagePixel(x1, y0);
			Vector3f c2 = getImagePixel(x0, y1);
			Vector3f c3 = getImagePixel(x1, y1);

			// Perform bilinear interpolation
			Vector3f pixelColor = new Vector3f(c0).mul(wx0).add(new Vector3f(c1).mul(wx1))
					.add(new Vector3f(c2).mul(wx2)).add(new Vector3f(c3).mul(wx3));

			return pixelColor;
		} else {
			// No interpolation, sample the nearest pixel
			int x = (int) (u * (width - 1));
			int y = (int) (v * (height - 1));
			return getImagePixel(x, y);
		}
	}

	// Get pixel color as a Vector3f at a specific (x, y) position
	private Vector3f getImagePixel(int x, int y) {
		int index = (y * width + x) * 3;
		return new Vector3f((float) (imageData[index] & 0xFF) / 255, // Red
				(float) (imageData[index + 1] & 0xFF) / 255, // Green
				(float) (imageData[index + 2] & 0xFF) / 255 // Blue
		);
	}

	// Load image from a file
	private void loadImage(String filepath) throws IOException {
		BufferedImage image = ImageIO.read(new File(filepath));
		if (image == null) {
			throw new IOException("Failed to load image: " + filepath);
		}
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.filename = filepath;

		// Store image data in byte array (RGB format)
		this.imageData = new byte[width * height * 3]; // 3 bytes per pixel (RGB)
		int index = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = image.getRGB(x, y);
				imageData[index++] = (byte) ((pixel >> 16) & 0xFF); // Red
				imageData[index++] = (byte) ((pixel >> 8) & 0xFF); // Green
				imageData[index++] = (byte) (pixel & 0xFF); // Blue
			}
		}
	}

	// Main method for testing
	public static void main(String[] args) {
		try {
			SphereTexture texture = new SphereTexture(
					"./Models/panoramic-view-field-covered-grass-trees-sunlight-cloudy-sky.jpg");
			Vector3f direction = new Vector3f(1, 1, 1);
			Vector3f color = texture.sampleDirectionFromMiddle(direction);
			System.out.println("Sampled color: " + color);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
