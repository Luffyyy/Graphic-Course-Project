package your_code;

import org.joml.Vector3f;

import app_interface.ModelSphere;

//this class serve as the container of the return value of the intersection method
class IntersectionResults {
	/** The point of intersection on the ray */
	Vector3f intersectionPoint;
	/** The normal vector at the point of intersection */
	Vector3f normal;
	/** Flag indicating if the ray originated from outside of the sphere */
	boolean rayFromOutsideOfSphere;
	/** The ModelSphere object that was intersected */
	ModelSphere intersectedSphere;

	/** Constructor that sets all fields of the IntersectionResults object
	 * @param intersected            true if an intersection occurred, false otherwise
	 * @param intersectionPoint      the point of intersection on the ray
	 * @param normal                 the normal vector at the point of intersection
	 * @param rayFromOutsideOfSphere flag indicating if the ray originated from outside of the sphere
	 * @param intersectedSphere      the ModelSphere object that was intersected */
	IntersectionResults(boolean intersected, Vector3f intersectionPoint, Vector3f normal,
			boolean rayFromOutsideOfSphere, ModelSphere intersectedSphere) {
		this.intersectionPoint = intersectionPoint;
		this.normal = normal;
		this.rayFromOutsideOfSphere = rayFromOutsideOfSphere;
		this.intersectedSphere = intersectedSphere;
	}
}