package app_interface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.joml.Vector3f;

import openGL_code.OpenGlRenderer;

public class Main extends JFrame {
	// Constants for image and camera settings
	public final int IMAGE_WIDTH = InterfaceDefaultParams.IMAGE_WIDTH;
	public final int IMAGE_HEIGHT = InterfaceDefaultParams.IMAGE_HEIGHT;
	public final int INITIAL_CAMERA_DISTANCE_FROM_AXIS_CENTER = InterfaceDefaultParams.INITIAL_CAMERA_DISTANCE_FROM_AXIS_CENTER;
	public final float CAMERA_MAX_VERTICAL_ANGLE = InterfaceDefaultParams.CAMERA_MAX_VERTICAL_ANGLE;

	// Unsaved camera and scene parameters
	private float cameraRadius = InterfaceDefaultParams.cameraRadius;
	private Vector3f cameraPos = InterfaceDefaultParams.cameraPos;
	private Vector3f cameraLookAtCenter = InterfaceDefaultParams.cameraLookAtCenter;
	private Vector3f cameraUp = InterfaceDefaultParams.cameraUp;
	private float cameraAngleHorizontal = InterfaceDefaultParams.cameraAngleHorizontal;
	private float cameraAngleVertical = InterfaceDefaultParams.cameraAngleVertical;
	private float horizontalFOV = InterfaceDefaultParams.horizontalFOV;
	private float modelScale = InterfaceDefaultParams.modelScale;

	// Unsaved material and lighting parameters
	private float materialDiffuse = InterfaceDefaultParams.materialDiffuse;
	private float materialAmbient = InterfaceDefaultParams.materialAmbient;
	private float materialSpecular = InterfaceDefaultParams.materialSpecular;
	private float materialShininess = InterfaceDefaultParams.materialShininess;
	private Vector3f lightPosition = InterfaceDefaultParams.lightPosition;

	// Saved parameters (persisted between sessions)
	private final InterfaceSavedParams savedParams = new InterfaceSavedParams();

	// Error logging
	private final ErrorLogger errorLogger = ErrorLogger.getInstance();
	private final JLabel labelOpenLog = new JLabel(""); // Label to display error count

	// Cursor position for mouse dragging
	private double lastCursorX, lastCursorY;

	// Labels for displaying scene information
	private final JLabel labelInfo1 = new JLabel("starting...");
	private final JLabel labelInfo2 = new JLabel("starting...");
	private final JLabel labelInfo3 = new JLabel("starting...");
	private final JLabel labelInfo4 = new JLabel("starting...");
	private final JLabel labelInfo5 = new JLabel("starting...");
	private long labelLastUpdateTime = 0; // Time of last label update

	// OpenGL renderer
	private OpenGlRenderer renderer;

	public Main() {
		setTitle("OpenGL Rendering App");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 700);
		setLayout(new BorderLayout());

		// Create control panel for buttons
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JButton buttonOpenObjFile = new JButton("Open 3D model ...");
		JButton buttonOpenShaders = new JButton("Open shader folder ...");
		JButton buttonOpenLog = new JButton("Open Log ...");

		controlPanel.add(buttonOpenObjFile);
		controlPanel.add(buttonOpenShaders);
		controlPanel.add(buttonOpenLog);
		controlPanel.add(labelOpenLog); // Add error count label to control panel

		// Create info panel for displaying text
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(5, 1));
		infoPanel.add(labelInfo1);
		infoPanel.add(labelInfo2);
		infoPanel.add(labelInfo3);
		infoPanel.add(labelInfo4);
		infoPanel.add(labelInfo5);
		infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Create bottom panel to hold info panel
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(infoPanel, BorderLayout.NORTH);

		// Add panels to the frame
		add(controlPanel, BorderLayout.NORTH);
		add(bottomPanel, BorderLayout.SOUTH);

		// Initialize and add the OpenGL renderer
		try {
			renderer = new OpenGlRenderer(savedParams.getModelFileName(), savedParams.getShadersFolder(), cameraPos,
					cameraLookAtCenter, cameraUp, horizontalFOV, modelScale, materialDiffuse, materialAmbient,
					materialSpecular, materialShininess, lightPosition);
			add(renderer.getOpenGlPanel(), BorderLayout.CENTER);
		} catch (Exception e) {
			String errorMessage = "Failed to initialize the OpenGL renderer in Main constructor: " + e.getMessage();
			errorLogger.report(errorMessage);
			JOptionPane.showMessageDialog(this, errorMessage, "Initialization Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1); // Close the application
		}

		// Add action listeners to buttons
		buttonOpenObjFile.addActionListener(this::handleOpenFileAction);
		buttonOpenShaders.addActionListener(this::handleOpenShadersAction);
		buttonOpenLog.addActionListener(this::handleOpenLog);

		// Add key listener to the frame
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				handleKeyPressed(event);
			}
		});
		setFocusable(true);
		requestFocusInWindow(); // Ensure frame has focus for key events

		// Add mouse listeners to the frame
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				handleMousePress(event);
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				handleMouseRelease(event);
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent event) {
				handleMouseDragged(event);
			}
		});

		addMouseWheelListener(this::handleMouseWheelScrolling);

		// Initial update of labels and visibility
		updateWindow();
		setVisible(true);
		requestFocusInWindow(); // Ensure frame has focus after becoming visible
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(Main::new); // Use invokeLater for thread safety
	}

	private void updateWindow() {
		// Update labels at a fixed interval
		if (System.nanoTime() - labelLastUpdateTime > InterfaceDefaultParams.LABELS_UPDATE_INTERVAL_IN_MS * 1_000_000) {
			labelInfo1.setText(descriptionString1());
			labelInfo2.setText(descriptionString2());
			labelInfo3.setText(descriptionString3());
			labelInfo4.setText(descriptionString4());
			labelInfo5.setText(descriptionString5());

			// Display error count if there are errors
			if (errorLogger.getTotalCount() > 0) {
				labelOpenLog.setText(" Errors count: " + errorLogger.getTotalCount());
				labelOpenLog.setForeground(Color.RED);
			} else
				labelOpenLog.setText(""); // clear the label

			labelLastUpdateTime = System.nanoTime();
		}

		// Handle model loading failure
		if (!renderer.getImageLoaded()) {
			JOptionPane.showMessageDialog(this, "Model failed to load. Please select a valid file.", "Load Error",
					JOptionPane.ERROR_MESSAGE);
			handleOpenFile(); // Prompt user to select a file
		}
		if (!renderer.isShaderFilesExist()) {
			JOptionPane.showMessageDialog(this, "Shader files does not exist. Please select valid shader files.", "Load Error",
					JOptionPane.ERROR_MESSAGE);
			handleOpenShaders(); // Prompt user to select a file
		}
		renderer.getOpenGlPanel().repaint(); // Trigger OpenGL rendering
	}

	// Methods to generate descriptive strings for labels
	private String descriptionString1() {
		String str = savedParams.getModelFileName();
		str = (str.length() > 60)
				? Paths.get(savedParams.getModelFileName()).getRoot() + " ... " + str.substring(str.length() - 60)
				: str;
		if (!renderer.getImageLoaded())
			str += " - loading failed !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
		return String.format("3D model: %s", str);
	}

	private String descriptionString2() {
		String str = savedParams.getShadersFolder();
		str = (str.length() > 60)
				? Paths.get(savedParams.getModelFileName()).getRoot() + " ... " + str.substring(str.length() - 60)
				: str;
		if (!renderer.isShaderFilesExist())
			str += " - files does not exist !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
		return String.format("Shaders folder: %s", str);
	}

	private String descriptionString3() {
		return "";
	}

	private String descriptionString4() {
		return String.format(
				"Camera angles:(%.1f,%.1f) position:(%.1f,%.1f,%.1f) cameraLookAtCenter:(%.1f,%.1f,%.1f) Scale: %.1f",
				cameraAngleHorizontal, cameraAngleVertical, cameraPos.x, cameraPos.y, cameraPos.z, cameraLookAtCenter.x,
				cameraLookAtCenter.y, cameraLookAtCenter.z, modelScale);
	}

	private String descriptionString5() {
		return String.format("Lighting reflection - Ambient:%1.2f, Specular:%1.2f, Diffuse:%1.2f, sHininess:%1.2f",
				materialAmbient, materialSpecular, materialDiffuse, materialShininess);
	}

	// Key event handling
	private void handleKeyPressed(KeyEvent event) {
		int keyCode = event.getKeyCode();

		// Handle camera movement with arrow keys
		if (keyCode == KeyEvent.VK_UP) {
			moveForwardOrBackward(true, true);
			renderer.setCameraLocation(cameraPos, cameraLookAtCenter, cameraUp, horizontalFOV);
			updateWindow();
		} else if (keyCode == KeyEvent.VK_DOWN) {
			moveForwardOrBackward(false, true);
			renderer.setCameraLocation(cameraPos, cameraLookAtCenter, cameraUp, horizontalFOV);
			updateWindow();
		} else if (keyCode == KeyEvent.VK_LEFT) {
			turnRightOrLeft(false);
			renderer.setCameraLocation(cameraPos, cameraLookAtCenter, cameraUp, horizontalFOV);
			updateWindow();
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			turnRightOrLeft(true);
			renderer.setCameraLocation(cameraPos, cameraLookAtCenter, cameraUp, horizontalFOV);
			updateWindow();
		}
		// Handle model scaling with '+' and '-' keys
		else if (keyCode == KeyEvent.VK_ADD || keyCode == KeyEvent.VK_PLUS) {
			modelScale += 0.1;
			renderer.setModelTransformation(modelScale);
			updateWindow();
		} else if (keyCode == KeyEvent.VK_SUBTRACT || keyCode == KeyEvent.VK_MINUS) {
			modelScale -= 0.1;
			renderer.setModelTransformation(modelScale);
			updateWindow();
		}
		// Handle material property changes
		else if (keyCode == KeyEvent.VK_D) {
			materialDiffuse += 0.05;
			updateLighting();
			updateWindow();
		} else if (keyCode == KeyEvent.VK_C) {
			materialDiffuse -= 0.05;
			updateLighting();
			updateWindow();
		} else if (keyCode == KeyEvent.VK_S) {
			materialSpecular += 0.05;
			updateLighting();
			updateWindow();
		} else if (keyCode == KeyEvent.VK_X) {
			materialSpecular -= 0.05;
			updateLighting();
			updateWindow();
		} else if (keyCode == KeyEvent.VK_A) {
			materialAmbient += 0.05;
			updateLighting();
			updateWindow();
		} else if (keyCode == KeyEvent.VK_Z) {
			materialAmbient -= 0.05;
			updateLighting();
			updateWindow();
		} else if (keyCode == KeyEvent.VK_H) {
			materialShininess += 1;
			updateLighting();
			updateWindow();
		} else if (keyCode == KeyEvent.VK_N) {
			materialShininess -= 1;
			updateLighting();
			updateWindow();
		}
	}

	private void updateLighting() {
		renderer.setLightingParams(materialDiffuse, materialAmbient, materialSpecular, materialShininess,
				lightPosition);
	}

	// Mouse event handling
	private void handleMousePress(MouseEvent event) {
		lastCursorX = event.getX();
		lastCursorY = event.getY();
	}

	private void handleMouseRelease(MouseEvent event) {
		updateWindow();
	}

	private void handleMouseDragged(MouseEvent event) {
		double curCursorX = event.getX();
		double curCursorY = event.getY();
		double deltaX = curCursorX - lastCursorX;
		double deltaY = curCursorY - lastCursorY;

		final float CURSER_MOVEMENT_DIVISION_CONSTANT = 5; // Corrected variable name
		cameraAngleHorizontal += deltaX / CURSER_MOVEMENT_DIVISION_CONSTANT;
		cameraAngleVertical -= deltaY / CURSER_MOVEMENT_DIVISION_CONSTANT;
		calcCameraPos();
		renderer.setCameraLocation(cameraPos, cameraLookAtCenter, cameraUp, horizontalFOV);

		lastCursorX = curCursorX;
		lastCursorY = curCursorY;

		updateWindow();
	}

	private void handleMouseWheelScrolling(MouseWheelEvent event) {
		int notches = event.getWheelRotation();

		if (notches < 0) {
//			moveForwardOrBackward(true, false); // Scroll up
			modelScale += 0.1;
		} else {
//			moveForwardOrBackward(false, false); // Scroll down
			modelScale -= 0.1;
		}
		renderer.setModelTransformation(modelScale);
		updateWindow();
		event.consume(); // Prevent further scrolling events
	}

	// Helper methods for camera movement and position calculation
	private void calcCameraPos() {
		Vector3f cameraDirection = new Vector3f(cameraLookAtCenter).sub(cameraPos);
		cameraRadius = cameraDirection.length();

		// Clamp vertical camera angle
		if (cameraAngleVertical > CAMERA_MAX_VERTICAL_ANGLE)
			cameraAngleVertical = CAMERA_MAX_VERTICAL_ANGLE;
		if (cameraAngleVertical < -CAMERA_MAX_VERTICAL_ANGLE)
			cameraAngleVertical = -CAMERA_MAX_VERTICAL_ANGLE;

		cameraAngleHorizontal = (cameraAngleHorizontal + 360) % 360; // Ensure angle is within 0-360 range

		// Calculate camera position based on angles and radius
		cameraPos.x = (float) (cameraRadius * Math.cos((cameraAngleVertical / 180 * Math.PI))
				* Math.cos(cameraAngleHorizontal / 180 * Math.PI));
		cameraPos.z = -(float) (cameraRadius * Math.cos((cameraAngleVertical / 180 * Math.PI))
				* Math.sin(cameraAngleHorizontal / 180 * Math.PI));
		cameraPos.y = (float) (cameraRadius * Math.sin((cameraAngleVertical / 180 * Math.PI)));
	}

	private void moveForwardOrBackward(boolean forward, boolean moveAlsoCameraLookAtCenter) {
		Vector3f cameraDirection = new Vector3f(cameraLookAtCenter).sub(cameraPos);
		cameraDirection.mul(1f / 50); // Move by a fraction of the direction vector
		if (!forward)
			cameraDirection.mul(-1); // Reverse direction if moving backward
		cameraPos.add(cameraDirection); // Update camera position
		if (moveAlsoCameraLookAtCenter)
			cameraLookAtCenter.add(cameraDirection); // Update look-at point if requested
	}

	private void turnRightOrLeft(boolean right) {
		Vector3f cameraDirection = new Vector3f(cameraLookAtCenter).sub(cameraPos);
		Vector3f cameraMovingDirection = new Vector3f(cameraDirection).cross(cameraUp); // Use cross product for
																						// perpendicular vector
		cameraMovingDirection.mul(1f / 50);
		if (!right)
			cameraMovingDirection.mul(-1);
		cameraLookAtCenter.add(cameraMovingDirection);
	}

	// Button event handlers
	private void handleOpenFileAction(ActionEvent event) {
		handleOpenFile(); // Call the file handling logic
		requestFocusInWindow(); // Keep focus in the main frame
	}

	private void handleOpenShadersAction(ActionEvent event) {
		handleOpenShaders(); // Call the shader handling logic
		requestFocusInWindow(); // Keep focus in the main frame
	}

	private void handleOpenFile() {
		JFileChooser fileChooser = new JFileChooser(Paths.get(savedParams.getModelFileName()).getParent().toString());
		javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
				"3D model files", "obj");
		fileChooser.setFileFilter(filter);
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				if (file.exists() && Files.isReadable(file.toPath())) {
					savedParams.setModelFileName(file.getAbsolutePath());
					try {
						renderer.changeModel(savedParams.getModelFileName()); // Notify renderer of new model file
						if (!renderer.getImageLoaded()) {
							errorLogger.report("Failed to load 3D model: " + savedParams.getModelFileName());
							JOptionPane.showMessageDialog(this, "Failed to load the selected 3D model file.",
									"Load Error", JOptionPane.ERROR_MESSAGE);
							// Optionally revert to a default model or handle the error differently
						}
					} catch (Exception e) {
						errorLogger.report("Error changing model: " + e.getMessage());
						JOptionPane.showMessageDialog(this, "An error occurred while loading the 3D model.",
								"Load Error", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					errorLogger.report("Invalid or unreadable 3D model file: " + file.getAbsolutePath());
					JOptionPane.showMessageDialog(this, "The selected 3D model file is not valid or readable.",
							"File Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (SecurityException e) {
				errorLogger.report(
						"Security error accessing model file: " + file.getAbsolutePath() + " - " + e.getMessage());
				JOptionPane.showMessageDialog(this, "Security error accessing the model file.", "Security Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		updateWindow(); // Update display after file selection
	}

    private void handleOpenShaders() {
        JFileChooser fileChooser = new JFileChooser(savedParams.getShadersFolder().toString());
        javax.swing.filechooser.FileNameExtensionFilter filter = 
        		new javax.swing.filechooser.FileNameExtensionFilter("Shader files", "glsl");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                if (selectedFile.exists() && selectedFile.isFile() && Files.isReadable(selectedFile.toPath())) {
                    savedParams.setShadersFolder(selectedFile.getParent());
                    renderer.changeShaders(savedParams.getShadersFolder());
                } else {
                    errorLogger.report("Invalid or unreadable shader file: " + selectedFile.getAbsolutePath());
                    JOptionPane.showMessageDialog(this,
                            "The selected shader file is not valid or readable.",
                            "Invalid Shader File", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SecurityException e) {
                errorLogger.report("Security error accessing shader file: " + selectedFile.getAbsolutePath() + " - " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Security error accessing the shader file.", "Security Error", JOptionPane.ERROR_MESSAGE);
            }
        } 
        else if (returnVal == JFileChooser.CANCEL_OPTION) 
        	System.exit(0);
        updateWindow();
    }

	private void handleOpenLog(ActionEvent event) {
		errorLogger.showErrorWindowSwing(); // Show the error log window
	}
}
