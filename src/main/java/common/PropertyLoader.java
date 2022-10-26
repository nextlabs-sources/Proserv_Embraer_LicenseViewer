package main.java.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyLoader {
	public static String propertiesPath = System.getProperty("catalina.base") + File.separator + "conf" + File.separator
			+ "LicenseViewer" + File.separator + "LicenseViewer.properties";
	public static String constantsPath = System.getProperty("catalina.base") + File.separator + "conf" + File.separator
			+ "LicenseViewer" + File.separator + "Constants.properties";
	public static String fileStatusPath = System.getProperty("catalina.base") + File.separator + "conf" + File.separator
			+ "LicenseViewer" + File.separator + "last.properties";
	public static Properties properties;
	public static Properties constants;
	public static Properties fileStatus;
	private static final Log LOG = LogFactory.getLog(PropertyLoader.class);

	public static void loadProperties() {
		properties = new Properties();
		try {
			LOG.info("loadProperties(): Loading properties file at " + propertiesPath);
			InputStream in = new FileInputStream(propertiesPath);
			properties.load(in);
			in.close();
		} catch (IOException e) {
			LOG.error("loadProperties(): " + e.getMessage(), e);
		}
	}

	public static void loadConstants() {

		constants = new Properties();
		try {
			LOG.info("loadConstants(): Loading constant file at " + constantsPath);
			InputStream in = new FileInputStream(constantsPath);
			constants.load(in);
			in.close();
		} catch (IOException e) {
			LOG.error("loadConstants(): " + e.getMessage(), e);
		}
	}

	public static void loadFileStatus() {
		fileStatus = new Properties();
		try {
			LOG.info("loadFileStatus(): Loading file status file at " + fileStatusPath);
			InputStream in = new FileInputStream(fileStatusPath);
			fileStatus.load(in);
			in.close();
		} catch (IOException e) {
			LOG.error("loadFileStatus(): " + e.getMessage(), e);
		}
	}

	public static void writeFileStatus(String key, String data) {
		FileOutputStream fileOut = null;
		FileInputStream fileIn = null;
		try {
			Properties configProperty = new Properties();

			File file = new File(fileStatusPath);
			fileIn = new FileInputStream(file);
			configProperty.load(fileIn);
			configProperty.setProperty(key, data);
			fileOut = new FileOutputStream(file);
			configProperty.store(fileOut, "Properties File");

		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		} finally {

			try {
				fileOut.close();
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static String getConstant(String key) {
		return constants.getProperty(key);
	}

}