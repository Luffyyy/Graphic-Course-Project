package app_interface;


import java.util.Scanner;

import org.joml.Vector3f;

/**
 * This class represents a light source in a 3D scene.
 *
 * A ModelLight object contains information about the light's location, intensity, and an optional comment.
 */
public class ModelLight {
	public Vector3f location; // Light location
	public float intensity; // Light intensity - no need to implement if not using few lights
	public String comment; // Additional comment

	@Override
	public String toString() {
		return String.format("Light: location_x: %s location_y: %s location_z: %s intensity: %s comment: %s",
				location.get(0), location.get(1), location.get(2), intensity, comment);
	}

	ModelLight(Vector3f location, float intensity, String comment) {
		this.location = location;
		this.intensity = intensity;
		this.comment = comment;
	}

	ModelLight() {
		this(new Vector3f(0, 0, 0), 1.0f, "");
	}

	ModelLight(String toStringStr) throws FileParsingException {
		try (Scanner scanner = new Scanner(toStringStr)) {
			Utilities.parseTokenWithoutParameter(scanner, "Light");
			location = new Vector3f(Utilities.parseTokenFloat(scanner, "location_x"),
					Utilities.parseTokenFloat(scanner, "location_y"), Utilities.parseTokenFloat(scanner, "location_z"));
			intensity = Utilities.parseTokenFloat(scanner, "intensity");
			comment = Utilities.parseTokenRestOfString(scanner, "comment");
		} catch (FileParsingException e) {
			String errorMessage = e.getMessage() + "\nAt Light constructor from toStringStr.\n Fail to load parse string:\n" + toStringStr;
			System.err.println(errorMessage);
			throw new FileParsingException(errorMessage);
		}
	}
}

