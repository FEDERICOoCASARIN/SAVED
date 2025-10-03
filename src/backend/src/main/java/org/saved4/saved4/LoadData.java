package org.saved4.saved4;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.postgresql.geometric.PGpoint;
import org.saved4.saved4.auth.PasswordHasher;
import org.saved4.saved4.dto.Company;
import org.saved4.saved4.enums.Role;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;

public class LoadData {
    private static final String LOCATION = "initial-data/";
    private static final String BENCHMARK_FILE = LOCATION + "SAVED_Benchmark_Final.xlsx";
    private static final String LOCATIONS_FILE = LOCATION + "SAVED_Locations_Final.xlsx";
    private static final String VEHICLES_FILE = LOCATION + "SAVED_Vehicles_Final.xlsx";

    public static void main(String[] args) {
        System.out.println("Working dir: " + System.getProperty("user.dir"));
        loadCompaniesAndChats();
        System.out.println("done");
    }

    public static void loadCompaniesAndChats() {
        DataFormatter formatter = new DataFormatter();
        try (Connection conn = DBProvider.getConnection(Role.SYSTEM)) {
            PreparedStatement userStmt = conn.prepareStatement("INSERT INTO users (username, password, email, type) VALUES(?, ?, ?, 'company')");
            PreparedStatement companyStmt = conn.prepareStatement("INSERT INTO companies VALUES(?, ?, ?, ?, ?)");
            PreparedStatement chatStmt = conn.prepareStatement("INSERT INTO chats (company) VALUES (?)");

            FileInputStream file = new FileInputStream(LOCATIONS_FILE);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            int i = 0;
            for (Row row : sheet) {
                i++;
                if (i <= 2) continue;
                Company newCompany = new Company();
                for (int j = 0; j < 5; j++) {
                    String cellText = formatter.formatCellValue(row.getCell(j));
                    switch (j) {
                        case 0 -> {
                            newCompany.setUsername(cellText.toLowerCase());
                            newCompany.setName(cellText.replace('_', ' '));
                            newCompany.setEmail("company@" + cellText.toLowerCase().replace("_", "") + ".com");
                        }
                        case 1 ->
                                newCompany.setLocation(new PGpoint(Double.parseDouble(cellText), Double.parseDouble(formatter.formatCellValue(row.getCell(j + 1)))));
                        case 3 -> newCompany.setOpeningTime(Time.valueOf(LocalTime.parse(cellText)));
                        case 4 -> newCompany.setClosingTime(Time.valueOf(LocalTime.parse(cellText)));
                    }
                }
                userStmt.setString(1, newCompany.getUsername());
                userStmt.setString(2, PasswordHasher.hashPassword("password"));
                userStmt.setString(3, newCompany.getEmail());

                userStmt.addBatch();

                companyStmt.setString(1, newCompany.getUsername());
                companyStmt.setString(2, newCompany.getUsername().toLowerCase());
                companyStmt.setObject(3, newCompany.getLocation());
                companyStmt.setTime(4, newCompany.getOpeningTime());
                companyStmt.setTime(5, newCompany.getClosingTime());

                companyStmt.addBatch();

                chatStmt.setString(1, newCompany.getUsername());

                chatStmt.addBatch();
            }

            userStmt.executeBatch();
            companyStmt.executeBatch();
            chatStmt.executeBatch();
            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
