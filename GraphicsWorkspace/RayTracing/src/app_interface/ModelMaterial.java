package app_interface;

import java.util.Scanner;

import org.joml.Vector3f;

/**
 * This class represents a material in a 3D scene.
 *
 * A ModelMaterial object contains information about the material's color,
 * lighting properties, reflection, refraction, texture mapping, and an optional
 * comment.
 */
public class ModelMaterial {
	// color component
	public float kColor; // Coefficient for overall color
	public Vector3f color; // RGB color values

	// direct light component
	public float kDirect; // Coefficient for direct lighting
	public Vector3f ka; // Ambient reflection coefficients for red, green, and blue channels
	public Vector3f kd; // Diffuse reflection coefficients for red, green, and blue channels
	public Vector3f ks; // Specular reflection coefficients for red, green, and blue channels
	public float shininess; // Exponent that controls the scattering/radius of the specular highlight

	// reflection component
	public float kReflection; // Coefficient for reflection

	// transmission component
	public float kTransmission; // Coefficient for transmission
	public float refractiveIndex; // Refractive index

	// texture component
	public float kTexture; // Coefficient for texture mapping

	public String comment; // Additional comment or name of the material

	@Override
	public String toString() {
		return String.format(
				"Material: kColor: %s color_R: %s color_G: %s color_B: %s kDirect: %s ka_R: %s ka_G: %s ka_B: %s kd_R: %s kd_G: %s kd_B: %s ks_R: %s ks_G: %s ks_B: %s shininess: %s kReflection: %s kTransmission: %s refractiveIndex: %s kTexture: %s comment: %s",
				kColor, color.get(0), color.get(1), color.get(2), kDirect, ka.get(0), ka.get(1), ka.get(2), kd.get(0),
				kd.get(1), kd.get(2), ks.get(0), ks.get(1), ks.get(2), shininess, kReflection, kTransmission,
				refractiveIndex, kTexture, comment);
	}

	ModelMaterial(float kColor, Vector3f color, float kDirect, Vector3f ka, Vector3f kd, Vector3f ks, float shininess,
			float kReflection, float kTransmission, float refractiveIndex, float kTexture, String comment) {
		this.kColor = kColor;
		this.color = color;
		this.kDirect = kDirect;
		this.ka = ka;
		this.kd = kd;
		this.ks = ks;
		this.shininess = shininess;
		this.kReflection = kReflection;
		this.kTransmission = kTransmission;
		this.refractiveIndex = refractiveIndex;
		this.kTexture = kTexture;
		this.comment = comment;
	}

	ModelMaterial() {
		this(0.0f, new Vector3f(), // float kColor, Vector3f color,
				0.0f, new Vector3f(), new Vector3f(), new Vector3f(), 1f, // float kDirect, Vector3f ka, Vector3f kd,
																			// Vector3f ks, float shininess,
				0.0f, // float kReflection,
				0.0f, 1.0f, // float kTransmission, float refractiveIndex,
				0.0f, // float kTexture,
				""); // String comment) {
	}

	ModelMaterial(String toStringStr) throws FileParsingException {
		try (Scanner scanner = new Scanner(toStringStr)) {
			Utilities.parseTokenWithoutParameter(scanner, "Material");
			kColor = Utilities.parseTokenFloat(scanner, "kColor");
			color = new Vector3f(Utilities.parseTokenFloat(scanner, "color_R"),
					Utilities.parseTokenFloat(scanner, "color_G"), Utilities.parseTokenFloat(scanner, "color_B"));
			kDirect = Utilities.parseTokenFloat(scanner, "kDirect");
			ka = new Vector3f(Utilities.parseTokenFloat(scanner, "ka_R"), Utilities.parseTokenFloat(scanner, "ka_G"),
					Utilities.parseTokenFloat(scanner, "ka_B"));
			kd = new Vector3f(Utilities.parseTokenFloat(scanner, "kd_R"), Utilities.parseTokenFloat(scanner, "kd_G"),
					Utilities.parseTokenFloat(scanner, "kd_B"));
			ks = new Vector3f(Utilities.parseTokenFloat(scanner, "ks_R"), Utilities.parseTokenFloat(scanner, "ks_G"),
					Utilities.parseTokenFloat(scanner, "ks_B"));
			shininess = Utilities.parseTokenFloat(scanner, "shininess");
			kReflection = Utilities.parseTokenFloat(scanner, "kReflection");
			kTransmission = Utilities.parseTokenFloat(scanner, "kTransmission");
			refractiveIndex = Utilities.parseTokenFloat(scanner, "refractiveIndex");
			kTexture = Utilities.parseTokenFloat(scanner, "kTexture");
			comment = Utilities.parseTokenRestOfString(scanner, "comment");
		} catch (FileParsingException e) {
			String errorMessage = e.getMessage()
					+ "\nAt Material constructor from toStringStr.\n Fail to load parse string:\n" + toStringStr;
			System.err.println(errorMessage);
			throw new FileParsingException(errorMessage);
		}
	}
}
