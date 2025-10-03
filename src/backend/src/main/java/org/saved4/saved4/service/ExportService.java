package org.saved4.saved4.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.saved4.saved4.dao.ExportDao;

@RequestScoped
public class ExportService {

    @Inject
    ExportDao exportDao;

    public void exportCompanies() {
//        exportDao.getCompanies();
    }
}
