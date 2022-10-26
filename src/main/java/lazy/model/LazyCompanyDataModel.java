package main.java.lazy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import main.java.database.CompanyDBHelper;
import main.java.object.Company;

public class LazyCompanyDataModel extends LazyDataModel<Company> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LazyCompanyDataModel() {
	}

	@Override
	public List<Company> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		List<Company> data = new ArrayList<Company>();

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

		data = CompanyDBHelper.getCompaniesLazy(first, pageSize, sortField, sortOrderString, filters);
		this.setRowCount(CompanyDBHelper.countCompanies(filters));

		return data;

	}

}
