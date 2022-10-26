package main.java.sync;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;

import main.java.common.PropertyLoader;
import main.java.sync.common.LicenseConstant;
import main.java.sync.excel.ExcelParser;
import main.java.sync.object.License;
import main.java.sync.object.NDAUser;

public class SynchronizationTask implements Runnable {
	private Boolean error;
	private int numberOfSuccess;
	private int numberOfFailure;
	private DestinationDBHandler dbHandler;
	private Boolean toExit;
	private Boolean forceSync;
	private Boolean isPause;
	private Date lastSync;

	private List<String> messages;

	private static final Log LOG = LogFactory.getLog(SynchronizationTask.class);
	private Boolean isSynching;
	private Boolean allowNewSync;

	public SynchronizationTask() {
		isSynching = false;
		messages = new ArrayList<String>();
		toExit = false;
		forceSync = false;
		allowNewSync = true;
		isPause = false;
	}

	@Override
	public void run() {
		System.setProperty("file.encoding", "UTF-8");

		// reload properties file
		PropertyLoader.loadProperties();

		long iReloadInterval = Integer
				.parseInt(PropertyLoader.properties.getProperty(LicenseConstant.SYNC_INTERVAL_KEY)) * 60 * 1000;

		try {
			while (!toExit) {
				recordMessage(2, "Sleep interval ended. Prepare to start new synchronization.");
				messages.clear();

				allowNewSync = false;

				if (forceSync) {
					String sLicenseFile = PropertyLoader.properties.getProperty(LicenseConstant.LICENSE_FILE_LOCATION_KEY);
					String sNDAFile =PropertyLoader.properties.getProperty(LicenseConstant.NDA_FILE_LOCATION_KEY);
					isSynching = true; startProcessExcel(sLicenseFile, sNDAFile);
					isSynching = false; forceSync = false;
			
				} else {
					isSynching = true;
					startProcess();
					isSynching = false;
				}

				lastSync = new Date();

				recordMessage(2,
						"Process goes into sleep mode until next interval which is " + iReloadInterval + " ms.");

				allowNewSync = true;

				synchronized (this) {
					try {
						this.wait(iReloadInterval);
					} catch (InterruptedException e) {
						LOG.warn("Sleep interrupted. Thread continues to run.");
					}
				}

				if (isPause) {
					synchronized (this) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							LOG.warn("Pause interrupted. Thread continues to run.");
						}
					}
				}
			}

			LOG.debug("Synchronization process is exiting");

		} catch (

		Exception e) {
			recordMessage(0, e.getMessage());
			LOG.debug(e.getMessage(), e);
		}
	}

	private boolean isReloadNeeded(String sLicenseFile, String sNDAFile) {

		File licenseFile = new File(sLicenseFile);

		if (!licenseFile.exists()) {
			LOG.warn("License file " + sLicenseFile + " does not exist, skip reload");
			return false;
		}

		File ndaFile = new File(sNDAFile);

		if (!ndaFile.exists()) {
			LOG.warn("NDA file " + sNDAFile + " does not exist, skip reload");
			return false;
		}

		PropertyLoader.loadFileStatus();

		long lLicenseLastModified = Long
				.parseLong(PropertyLoader.fileStatus.getProperty(LicenseConstant.LICENSE_FILE_LAST_MODIFIED_KEY));
		long lNDALastModified = Long
				.parseLong(PropertyLoader.fileStatus.getProperty(LicenseConstant.NDA_FILE_LAST_MODIFIED_KEY));

		if (licenseFile.lastModified() > lLicenseLastModified) {
			recordMessage(2, "Reload needed since license file is updated");
			PropertyLoader.writeFileStatus(LicenseConstant.LICENSE_FILE_LAST_MODIFIED_KEY,
					String.valueOf(licenseFile.lastModified()));
			PropertyLoader.writeFileStatus(LicenseConstant.NDA_FILE_LAST_MODIFIED_KEY,
					String.valueOf(ndaFile.lastModified()));
			return true;
		}

		if (ndaFile.lastModified() > lNDALastModified) {
			recordMessage(2, "Reload needed since nda file is updated");
			PropertyLoader.writeFileStatus(LicenseConstant.LICENSE_FILE_LAST_MODIFIED_KEY,
					String.valueOf(licenseFile.lastModified()));
			PropertyLoader.writeFileStatus(LicenseConstant.NDA_FILE_LAST_MODIFIED_KEY,
					String.valueOf(ndaFile.lastModified()));
			return true;
		}

		recordMessage(2, "All files are up to date");

		return false;
	}

	private void recordMessage(int level, String message) {

		if (message == null) {
			message = "Unknown error. Please check the debug log";
		}

		if (level == 0) {
			LOG.error(message);
			messages.add("<p class = \"red-text\"><i class=\"fa fa-close\"></i>" + message + "</p>");
		} else if (level == 1) {
			LOG.warn(message);
			messages.add("<p class = \"orange-text\"><i class=\"fa fa-warning\"></i>" + message + "</p>");
		} else {
			LOG.info(message);
			messages.add("<p class = \"blue-text\"><i class=\"fa fa-check\"></i>" + message + "</p>");
		}
	}

	public void startProcess() {
		try {
			List<License> licenseList = SourceDBParser.getAllLicenses();
			List<NDAUser> ndaList = SourceDBParser.getAllNDAs();

			dbHandler = new DestinationDBHandler(licenseList, ndaList);
			dbHandler.startProcess(messages);

		} catch (Exception ex) {
			recordMessage(0, ex.getMessage());
			LOG.debug(ex.getMessage(), ex);
		}
	}

	public void startProcessExcel(String sXLSFilePath, String sNDAFilePath) {

		try {
			int minColumns = -1;

			File xlsxFile = new File(sXLSFilePath);
			if (!xlsxFile.exists()) {
				recordMessage(0, "License file Not found or not a file: " + xlsxFile.getPath());
				return;
			}

			File ndaFile = new File(sNDAFilePath);
			if (!ndaFile.exists()) {
				recordMessage(0, "NDA file Not found or not a file: " + ndaFile.getPath());
				return;
			}

			// The package open is instantaneous, as it should be.
			OPCPackage opc = OPCPackage.open(xlsxFile.getPath(), PackageAccess.READ);

			PrintStream printStreamFile = new PrintStream("./license.csv", "UTF-8");
			ExcelParser licenseParser = new ExcelParser(opc, printStreamFile, minColumns);
			licenseParser.process();
			List<License> licenses = licenseParser.readFromLicenseCSV(PropertyLoader.properties);
			opc.close();

			// The package open is instantaneous, as it should be.
			OPCPackage opcNDA = OPCPackage.open(ndaFile.getPath(), PackageAccess.READ);

			PrintStream printStreamFileNDA = new PrintStream("./nda.csv", "UTF-8");
			ExcelParser ndaParser = new ExcelParser(opcNDA, printStreamFileNDA, minColumns);
			ndaParser.process();
			List<NDAUser> nDAUsers = ndaParser.readFromNDACSV(PropertyLoader.properties);
			opc.close();

			dbHandler = new DestinationDBHandler(licenses, nDAUsers);

			dbHandler.startProcess(messages);

		} catch (Exception ex) {
			recordMessage(0, ex.getMessage());
			LOG.debug(ex.getMessage(), ex);
		}
	}

	public int getProcessLicenseCount() {
		if (dbHandler == null)
			return 0;
		return dbHandler.getProcessLicenseCount();
	}

	public int getProcessNDACount() {
		if (dbHandler == null)
			return 0;
		return dbHandler.getProcessNDACount();
	}

	public Boolean getError() {
		return error;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

	public int getNumberOfSuccess() {
		return numberOfSuccess;
	}

	public void setNumberOfSuccess(int numberOfSuccess) {
		this.numberOfSuccess = numberOfSuccess;
	}

	public int getNumberOfFailure() {
		return numberOfFailure;
	}

	public void setNumberOfFailure(int numberOfFailure) {
		this.numberOfFailure = numberOfFailure;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public Boolean getIsSynching() {
		return isSynching;
	}

	public void setIsSynching(Boolean isSynching) {
		this.isSynching = isSynching;
	}

	public Boolean getToExit() {
		return toExit;
	}

	public void setToExit(Boolean toExit) {
		this.toExit = toExit;
	}

	public Boolean getForceSync() {
		return forceSync;
	}

	public void setForceSync(Boolean forceSync) {
		this.forceSync = forceSync;
	}

	public Boolean getAllowNewSync() {
		return allowNewSync;
	}

	public void setAllowNewSync(Boolean allowNewSync) {
		this.allowNewSync = allowNewSync;
	}

	public Boolean getIsPause() {
		return isPause;
	}

	public void setIsPause(Boolean isPause) {
		this.isPause = isPause;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

}
