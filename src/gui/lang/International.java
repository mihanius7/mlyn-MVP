package gui.lang;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import static gui.lang.GUIStrings.*;

public class International {
	
	public static final String LANG_FILENAME_BASE = "resources/gui_strings";

	public static boolean prepareStrings(Language lang) {
		Locale locale;
		switch (lang) {
		case ENGLISH: {
			locale = new Locale("", "");
			System.out.println("English");
			break;
		}
		case BELARUSSIAN: {
			locale = new Locale("by", "BY");
			System.out.println("Belarussian");
			break;
		}
		case RUSSIAN: {
			locale = new Locale("ru", "RU");
			System.out.println("Russian");
			break;
		}
		case POLISH: {
			locale = new Locale("pl", "PL");
			System.out.println("Polish");
			break;
		}
		default:
			locale = new Locale("", "");
			System.out.println("DEFAULT LANGUAGE");
		}
		ResourceBundle rb;
		try {
			rb = ResourceBundle.getBundle(LANG_FILENAME_BASE, locale);
			changeStrings(rb);
			return true;
		} catch (MissingResourceException mre) {
			System.err.println("Resource file or phrase is not found!");
		}
		return false;
	}

	private static void changeStrings(ResourceBundle rb) {
		MENU_FILE = rb.getString("MENU_FILE");
		OPEN_SCENE_DIALOG = rb.getString("OPEN_SCENE_DIALOG");
		IMAGE_SAVING_DIALOG = rb.getString("IMAGE_SAVING_DIALOG");
		RECORDER_DIALOG = rb.getString("RECORDER_DIALOG");
		FILE_WRITING_ERROR_MESSAGE = rb.getString("FILE_WRITING_ERROR_MESSAGE");
		FILE_ACESS_DENIED = rb.getString("FILE_ACESS_DENIED");
		CANT_WRITE_FILE_MESSAGE = rb.getString("CANT_WRITE_FILE_MESSAGE");
		NO_VIBRATOR_FOR_AFCH_MESSAGE = rb.getString("NO_VIBRATOR_FOR_AFCH_MESSAGE");
		VIBRATION_DIALOG = rb.getString("VIBRATION_DIALOG");
		EMPTY_VIBRATOR_MESSAGE = rb.getString("EMPTY_VIBRATOR_MESSAGE");
		SELECT_PARTICLE_DIALOG = rb.getString("SELECT_PARTICLE_DIALOG");
		NOTHING_SELECTED_MESSAGE = rb.getString("NOTHING_SELECTED_MESSAGE");
		ABOUT = rb.getString("ABOUT");
		FILETYPE_DESCRIPTION = rb.getString("FILETYPE_DESCRIPTION");
		TIMESTEP_FIXED = rb.getString("TIMESTEP_FIXED");
		TIMESTEP_LABEL = rb.getString("TIMESTEP_LABEL");
		START_PAUSE_BUTTON = rb.getString("START_PAUSE_BUTTON");
		APP_NAME = rb.getString("APP_NAME");
		MENU_ABOUT = rb.getString("MENU_ABOUT");
		MENU_HELP = rb.getString("MENU_HELP");
		MENU_CONTROL_BY_PLACE = rb.getString("MENU_CONTROL_BY_PLACE");
		MENU_CONTROL_BY_FORCE = rb.getString("MENU_CONTROL_BY_FORCE");
		MENU_MOUSE = rb.getString("MENU_MOUSE");
		MENU_CONTROL = rb.getString("MENU_CONTROL");
		MENU_TAKE_SCREENSHOT = rb.getString("MENU_TAKE_SCREENSHOT");
		MENU_PARTICLE_TRACKS = rb.getString("MENU_PARTICLE_TRACKS");
		MENU_GRID = rb.getString("MENU_GRID");
		MENU_TAGS = rb.getString("MENU_TAGS");
		MENU_PRETTY = rb.getString("MENU_PRETTY");
		MENU_FORCES = rb.getString("MENU_FORCES");
		MENU_VELOCITIES = rb.getString("MENU_VELOCITIES");
		MENU_FOLLOW_PARTICLE = rb.getString("MENU_FOLLOW_PARTICLE");
		MENU_ZOOM_TO_BOUNDARIES = rb.getString("MENU_ZOOM_TO_BOUNDARIES");
		MENU_ZOOM_TO_PARTICLES = rb.getString("MENU_ZOOM_TO_PARTICLES");
		MENU_SHOW = rb.getString("MENU_SHOW");
		MENU_SIMULATION_BOUNDS = rb.getString("MENU_SIMULATION_BOUNDS");
		MENU_NULLIFY_VELOCITIES = rb.getString("MENU_NULLIFY_VELOCITIES");
		MENU_FRICTION = rb.getString("MENU_FRICTION");
		MENU_OUTER_FORCES = rb.getString("MENU_OUTER_FORCES");
		MENU_PP_COLLISIONS = rb.getString("MENU_PP_COLLISIONS");
		MENU_CONSIDER = rb.getString("MENU_CONSIDER");
		MENU_FEW_STEPS = rb.getString("MENU_FEW_STEPS");
		MENU_START_PAUSE = rb.getString("MENU_START_PAUSE");
		MENU_SIMULATION = rb.getString("MENU_SIMULATION");
		MENU_ADD_SPRING = rb.getString("MENU_ADD_SPRING");
		MENU_ADD_PARTICLE = rb.getString("MENU_ADD_PARTICLE");
		MENU_ADD = rb.getString("MENU_ADD");
		MENU_SNAP_TO_GRID = rb.getString("MENU_SNAP_TO_GRID");
		MENU_CENTER_OF_MASS = rb.getString("MENU_CENTER_OF_MASS");
		MENU_FIX = rb.getString("MENU_FIX");
		MENU_DELETE = rb.getString("MENU_DELETE");
		MENU_SELECT_ALL_PARTICLES = rb.getString("MENU_SELECT_ALL_PARTICLES");
		MENU_SELECT_SPRINGS = rb.getString("MENU_SELECT_SPRINGS");
		MENU_SELECT_PARTICLES = rb.getString("MENU_SELECT_PARTICLES");
		MENU_EDIT = rb.getString("MENU_EDIT");
		MENU_EXIT = rb.getString("MENU_EXIT");
		MENU_CLEAR_ALL = rb.getString("MENU_CLEAR_ALL");
		MENU_SAVE = rb.getString("MENU_SAVE");
		MENU_OPEN = rb.getString("MENU_OPEN");
		MENU_MUCH_TIME_NEEDED_PLEASE_WAIT = rb.getString("MENU_MUCH_TIME_NEEDED_PLEASE_WAIT");
		IMAGE_SAVED_TO = rb.getString("IMAGE_SAVED_TO");
		SCREENSHOT_NAME = rb.getString("SCREENSHOT_NAME");
		MOUSE_MODE = rb.getString("MOUSE_MODE");
		DRAW_TRACKS = rb.getString("DRAW_TRACKS");
		GRID_SIZE = rb.getString("GRID_SIZE");
		TIME_SCALE = rb.getString("TIME_SCALE");
		TIMESTEP_RESERVE = rb.getString("TIMESTEP_RESERVE");
		INTERRUPTED_THREAD = rb.getString("INTERRUPTED_THREAD");
		RENDERING_THREAD_STARTED = rb.getString("RENDERING_THREAD_STARTED");
		AUTOSCALE = rb.getString("AUTOSCALE");
		EMPTY_SCENE_LOADING = rb.getString("EMPTY_SCENE_LOADING");
		TIMESTEP_CONTROLLER_RESTARTED = rb.getString("TIMESTEP_CONTROLLER_RESTARTED");
		INTERACTION_PROCESSOR_RESTARTED = rb.getString("INTERACTION_PROCESSOR_RESTARTED");
		CLEARED = rb.getString("CLEARED");
		DONE = rb.getString("DONE");
		FORCE_SIMULATION_STOP = rb.getString("FORCE_SIMULATION_STOP");
		TO_SIMULATION_ADDED = rb.getString("TO_SIMULATION_ADDED");
		PARTICLES_ADDED = rb.getString("PARTICLES_ADDED");
		SPRINGS_ADDED = rb.getString("SPRINGS_ADDED");
		SIMULATION_THREAD_CANT_BE_CONTINUED = rb.getString("SIMULATION_THREAD_CANT_BE_CONTINUED");
		TIMESTEP = rb.getString("TIMESTEP");
		SIMULATION_THREAD_ENDED = rb.getString("SIMULATION_THREAD_ENDED");
		SIMULATION_THREAD_STARTED = rb.getString("SIMULATION_THREAD_STARTED");
		NEW_PROJECT_NAME = rb.getString("NEW_PROJECT_NAME");
		MAX_INTERACTION_DEFINING_DISTANCE = rb.getString("MAX_INTERACTION_DEFINING_DISTANCE");
		COLLISIONS_PS_NEEDED = rb.getString("COLLISIONS_PS_NEEDED");
		SPRINGS_NUMBER = rb.getString("SPRINGS_NUMBER");
		COLLISIONS_PP = rb.getString("COLLISIONS_PP");
		EXTERNAL_FORCES = rb.getString("EXTERNAL_FORCES");
		RECOIL_BY_HERTZ = rb.getString("RECOIL_BY_HERTZ");
		BOTTOM_BOUNDARY = rb.getString("BOTTOM_BOUNDARY");
		UPPER_BOUNDARY = rb.getString("UPPER_BOUNDARY");
		RIGHT_BOUNDARY = rb.getString("RIGHT_BOUNDARY");
		LEFT_BOUNDARY = rb.getString("LEFT_BOUNDARY");
		TIMESCALE = rb.getString("TIMESCALE");
		TIMESTEP_CONTROL_MODE = rb.getString("TIMESTEP_CONTROL_MODE");
		TIMESTEP_CORRECTION_DONE = rb.getString("TIMESTEP_CORRECTION_DONE");
		HYPERCRITICAL_DAMPING_FIXED = rb.getString("HYPERCRITICAL_DAMPING_FIXED");
		SPRING_QUALITY_FACTOR = rb.getString("SPRING_QUALITY_FACTOR");
		SPRING_DAMPING = rb.getString("SPRING_DAMPING");
		SPRING_RESONANT_FREQUENCY = rb.getString("SPRING_RESONANT_FREQUENCY");
		SPRING_HARDENING_COEFFICIENT = rb.getString("SPRING_HARDENING_COEFFICIENT");
		SPRING_STIFFNES = rb.getString("SPRING_STIFFNES");
		SPRING_NOMINAL_LENGTH = rb.getString("SPRING_NOMINAL_LENGTH");
		REFERENCE_SPRING_CREATED = rb.getString("REFERENCE_SPRING_CREATED");
		SPRING_DAMPING_RATIO = rb.getString("SPRING_DAMPING_RATIO");
		SPRING_CREATED = rb.getString("SPRING_CREATED");
		CANT_COPY_A_PARTICLE = rb.getString("CANT_COPY_A_PARTICLE");
		PARTICLE_VELOCITIES_RANDOMIZED = rb.getString("PARTICLE_VELOCITIES_RANDOMIZED");
		PARTICLE_COLOURS_CORRESPONDS_TO_CHARGE = rb.getString("PARTICLE_COLOURS_CORRESPONDS_TO_CHARGE");
		VELOCITIES_NULLIFIED = rb.getString("VELOCITIES_NULLIFIED");
		FILE_LOADING_FINISHED = rb.getString("FILE_LOADING_FINISHED");
		FILE_LOADING_STARTET = rb.getString("FILE_LOADING_STARTET");
		NUMBER_FORMAT_EXCEPTION = rb.getString("NUMBER_FORMAT_EXCEPTION");
		NUMBER_PARSE_EXCEPTION = rb.getString("NUMBER_PARSE_EXCEPTION");
		FILE_READING_EXCEPTION = rb.getString("FILE_READING_EXCEPTION");
		FILE_NOT_FOUND_EXCEPTION = rb.getString("FILE_NOT_FOUND_EXCEPTION");
		PARSER_EXCEPTION = rb.getString("PARSER_EXCEPTION");
		BOUNDARY_USE = rb.getString("BOUNDARY_USE");
		APPLY_BUTTON = rb.getString("APPLY_BUTTON");
		EDIT_BOUNDARIES_DIALOG = rb.getString("EDIT_BOUNDARIES_DIALOG");
		
		System.out.println("Last string sample: " + EDIT_BOUNDARIES_DIALOG);
	}

}
