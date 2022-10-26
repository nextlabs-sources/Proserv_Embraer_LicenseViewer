package main.java.lazy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import main.java.database.NationalityDBHelper;
import main.java.object.Nationality;

public class LazyNationalityDataModel extends LazyDataModel<Nationality> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LazyNationalityDataModel() {
	}

	@Override
	public List<Nationality> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		List<Nationality> data = new ArrayList<Nationality>();

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

		data = NationalityDBHelper.getNationalitiesLazy(first, pageSize, sortField, sortOrderString, filters);
		this.setRowCount(NationalityDBHelper.countNationalities(filters));

		return data;

	}

}
