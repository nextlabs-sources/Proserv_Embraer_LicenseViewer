package main.java.lazy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import main.java.database.NDADBHelper;
import main.java.object.NDA;

public class LazyNDADataModel extends LazyDataModel<NDA> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LazyNDADataModel() {
	}

	@Override
	public List<NDA> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
		List<NDA> data = new ArrayList<NDA>();

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

		data = NDADBHelper.getNDALazy(first, pageSize, sortField, sortOrderString, filters);
		this.setRowCount(NDADBHelper.countNDA(filters));

		return data;

	}

}
