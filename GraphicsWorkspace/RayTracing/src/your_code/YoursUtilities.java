package your_code;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Your utility class that provides common methods.
 */
public class YoursUtilities {

	
	
    /**
     * Calculates the direction of a transmission ray as it passes through
     * a surface defined by a normal vector, using Snell's Law to account
     * for refraction.
     *
     * @param incidentRay_                        The direction of the incoming ray represented as a {@link Vector3f}.
     * @param normal_                             The normal vector at the surface point, represented as a {@link Vector3f}.
     * @param refractiveIndexIntersectedSphere  The refractive index of the medium that the ray is entering or leaving.
     * @param rayFromOutside                     A boolean indicating whether the ray is originating from outside the medium (true) 
     *                                            or from inside the medium (false).
     * @return                                   A normalized {@link Vector3f} representing the direction of the transmitted ray.
     *                                           Returns a zero vector if total internal reflection occurs.
     */	
	static Vector3f calcTransmissionRay(Vector3f incidentRay, Vector3f normal,
			float refractiveIndexIntersectedSphere, boolean rayFromOutside) {
		float refractiveIndexAir = 1.000293f;
		float n1, n2;

		Vector3f incidentRay_ = new Vector3f(incidentRay).normalize();
		Vector3f normal_ = new Vector3f(normal).normalize();
		
		// Determine indices of refraction based on the ray direction
		if (rayFromOutside) {
			n1 = refractiveIndexAir;
			n2 = refractiveIndexIntersectedSphere;
		} else {
			n1 = refractiveIndexIntersectedSphere;
			n2 = refractiveIndexAir;
		}

		// Calculate dot product of incident ray and normal
		float cosTheta1 = incidentRay_.dot(normal_);

		// Invert the normal if the ray is inside the object
		if (cosTheta1 < 0) {
			cosTheta1 = -cosTheta1;
		} else {
			normal_.negate();
		}

		// Calculate the ratio of refractive indices and squared terms for Snell's law
		float ratio = n1 / n2;
		float sinTheta2Sq = ratio * ratio * (1.0f - cosTheta1 * cosTheta1);

		// Check for total internal reflection (if sin^2(theta2) > 1.0)
		if (sinTheta2Sq > 1.0f) {
			return new Vector3f(0.0f); // Total internal reflection
		}

		// Calculate cosTheta2 using trigonometric identity
		float cosTheta2 = (float) Math.sqrt(1.0f - sinTheta2Sq);

		// Compute the transmission direction using Snell's law
		Vector3f transmittedDir = new Vector3f(incidentRay_).mul(ratio)
				.add(new Vector3f(normal_).mul(ratio * cosTheta1 - cosTheta2));

		return transmittedDir.normalize(); // Return the normalized direction
	}
}
	
