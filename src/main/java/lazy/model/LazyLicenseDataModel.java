package main.java.lazy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import main.java.database.LicenseDBHelper;
import main.java.object.License;

public class LazyLicenseDataModel extends LazyDataModel<License> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LazyLicenseDataModel() {
	}

	@Override
	public List<License> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		List<License> data = new ArrayList<License>();

		String sortOrderString;
		// translate sort order
		if (sortOrder == null) {
			sortOrderString = "";
		} else if (sortOrder.equals(SortOrder.ASCENDING)) {
			sortOrderString = "ASC";
		} else if (sortOrder.equals(SortOrder.DESCENDING)) {
			sortOrderString = "DESC";
		} else if (sortOrder.equals(SortOrder.UNSORTED)) {
			sortOrderString = "";
		} else {
			sortOrderString = "";
		}

		data = LicenseDBHelper.getLicensesLazy(first, pageSize, sortField, sortOrderString, filters);
		this.setRowCount(LicenseDBHelper.countLicenses(filters));

		return data;

	}

}
