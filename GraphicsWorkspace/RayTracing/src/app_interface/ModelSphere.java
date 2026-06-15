package app_interface;

import java.util.Scanner;

import org.joml.Vector3f;

/**
 * This class represents a sphere object in a 3D scene.
 *
 * A ModelSphere object contains information about the sphere's center, radius,
 * material index, and texture index.
 */
public class ModelSphere {
	public Vector3f center;
	public float radius;
	public int materialIndex;
	public int textureIndex; // Index for the texture image of this sphere

	@Override
	public String toString() {
		return String.format(
				"Sphere: center_x: %s center_y: %s center_z: %s radius: %s materialIndex: %d textureIndex: %d",
				center.x, center.y, center.z, radius, materialIndex, textureIndex);
	}

	/**
	 * Constructs a new {@code ModelSphere} with the specified center, radius,
	 * material index, and texture index.
	 *
	 * @param center        the center of the sphere as a {@link Vector3f}
	 * @param radius        the radius of the sphere
	 * @param materialIndex the index referring to the material properties of the
	 *                      sphere
	 * @param textureIndex  the index referring to the texture image of the sphere
	 */
	public ModelSphere(Vector3f center, float radius, int materialIndex, int textureIndex) {
		this.center = center;
		this.radius = radius;
		this.materialIndex = materialIndex;
		this.textureIndex = textureIndex;
	}

	ModelSphere() {
		this(new Vector3f(0.0f, 0.0f, 0.0f), 0.0f, 0, 0);
	}

	// Constructor that takes a string from toString and reconstructs the object
	ModelSphere(String toStringStr) throws FileParsingException {
		try (Scanner scanner = new Scanner(toStringStr)) {
			Utilities.parseTokenWithoutParameter(scanner, "Sphere");
			center = new Vector3f(Utilities.parseTokenFloat(scanner, "center_x"),
					Utilities.parseTokenFloat(scanner, "center_y"), Utilities.parseTokenFloat(scanner, "center_z"));
			radius = Utilities.parseTokenFloat(scanner, "radius");
			materialIndex = Utilities.parseTokenInt(scanner, "materialIndex");
			textureIndex = Utilities.parseTokenInt(scanner, "textureIndex");
		} catch (FileParsingException e) {
			String errorMessage = e.getMessage()
					+ "\nAt Sphere constructor from toStringStr.\n Fail to load parse string:\n" + toStringStr;
			System.err.println(errorMessage);
			throw new FileParsingException(errorMessage);
		}
	}
}
