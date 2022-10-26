package main.java.bean;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import main.java.common.PropertyLoader;
import main.java.database.DestinationDBHelper;
import main.java.sync.SourceDBParser;
import main.java.sync.SynchronizationTask;

@ManagedBean(name = "synchronizationBean", eager = true)
@ApplicationScoped
public class SynchronizationBean {
	private static final Log LOG = LogFactory.getLog(SynchronizationBean.class);

	private ExecutorService executorForSpawningTask;
	private SynchronizationTask syncTask;

	@PostConstruct
	public void init() {
		// setup properties of the application
		try {
			setupProperties();
		} catch (Exception e) {
			LOG.error("init() Cannot setup properties. Abort synchronization");
			return;
		}
		
		SourceDBParser.setUpSourceDBParser(PropertyLoader.properties);

		executorForSpawningTask = Executors.newSingleThreadExecutor();
		
		if (!DestinationDBHelper.checkTables()) {
			DestinationDBHelper.initializeDatabase();
		}
		
		DestinationDBHelper.initializeNationalities();

		syncTask = new SynchronizationTask();
		executeSpawningTask(syncTask);

		executorForSpawningTask.shutdown();
		LOG.info("init(): Finished initializing Executor service. The synchronization process is listening.");
	}

	public void executeSpawningTask(Runnable r) {
		executorForSpawningTask.execute(r);
	}

	public void manualSync() {

		if (syncTask == null) {
			syncTask = new SynchronizationTask();
			syncTask.setAllowNewSync(false);
			executeSpawningTask(syncTask);
			return;
		}

		if (syncTask.getIsSynching()) {
			return;
		} else {
			syncTask.setAllowNewSync(false);
			synchronized (syncTask) {
				syncTask.notify();
			}
		}
	}

	public void forceSync() {
		if (syncTask == null) {
			syncTask = new SynchronizationTask();
			syncTask.setAllowNewSync(false);
			syncTask.setForceSync(true);
			executeSpawningTask(syncTask);
			return;
		}

		if (syncTask.getIsSynching()) {
			return;
		} else {
			syncTask.setForceSync(true);
			syncTask.setAllowNewSync(false);
			synchronized (syncTask) {
				syncTask.notify();
			}
		}
	}

	public void pauseSync() {
		if (syncTask == null) {
			return;
		}

		if (syncTask.getIsSynching()) {
			return;
		} else {
			syncTask.setIsPause(true);
			synchronized (syncTask) {
				syncTask.notify();
			}
		}
	}

	public void resume() {
		if (syncTask == null) {
			return;
		}

		if (syncTask.getIsSynching()) {
			return;
		} else {
			syncTask.setIsPause(false);
			synchronized (syncTask) {
				syncTask.notify();
			}
		}
	}

	public void updateProcess() {

	}

	public void setupProperties() {
		// get properties file
		PropertyLoader.loadProperties();
		PropertyLoader.loadConstants();
		//PropertyLoader.loadFileStatus();
	}

	@PreDestroy
	public void destroy() {
		LOG.info("destroy(): Destroying Executor service ...");
		syncTask.setToExit(true);
		synchronized (syncTask) {
			syncTask.notify();
		}
		if (executorForSpawningTask != null) {
			executorForSpawningTask.shutdown();
			try {
				if (!executorForSpawningTask.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
					LOG.debug("Executor did not terminate in the specified time."); // optional
																					// *
					List<Runnable> droppedTasks = executorForSpawningTask.shutdownNow(); // optional
																							// **
					LOG.debug(
							"Executor was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed."); // optional
																															// **
				}
			} catch (InterruptedException e) {
				LOG.debug(
						"Unable to check for running thread. Server might not have been shut down cleanly. Please check for any unwanted running process to prevent memory leak");
				LOG.debug(e.getMessage());
			}
		}
		LOG.info("destroy(): Finished destroying Executor service");
	}

	public SynchronizationTask getSyncTask() {
		return syncTask;
	}

	public void setSyncTask(SynchronizationTask syncTask) {
		this.syncTask = syncTask;
	}

	public String getRefreshPeriod() {
		if (syncTask.getAllowNewSync()) {
			return "60";
		} else {
			return "1";
		}
	}

}
