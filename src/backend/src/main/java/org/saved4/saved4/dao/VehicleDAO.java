package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.geometric.PGpoint;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Vehicle;
import org.saved4.saved4.enums.Role;
import org.saved4.saved4.enums.VehicleStatus;

@Singleton
public class VehicleDAO implements Serializable {


    /**
     * Retrieves a list of all vehicles from the database.
     *
     * @return A list of vehicle objects representing all vehicles found in the database.
     * Returns an empty list if no vehicles are found or if a SQLException occurs.
     */
    public List<Vehicle> getAllVehiclesDB(Role role) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles";

        try (Connection connection = DBProvider.getConnection(role);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Vehicle vehicle = new Vehicle();
                vehicle.setVehicleId(resultSet.getInt("vehicle_id"));

                String position = resultSet.getString("position");
                position = position.replace("(", "").replace(")", "").trim(); // remove parens
                String[] coords = position.split(",");
                double x = Double.parseDouble(coords[0].trim());
                double y = Double.parseDouble(coords[1].trim());
                vehicle.setPosition(new PGpoint(x, y));

                vehicle.setBattery_level(resultSet.getFloat("battery_level"));
                vehicle.setStatus(VehicleStatus.valueOf(resultSet.getString("status")));
                vehicle.setDistance(resultSet.getFloat("distance"));

                vehicles.add(vehicle);


            }

        } catch (SQLException e) {
            System.err.println("Error retrieving vehicles: " + e.getMessage());
        }
        return vehicles;
    }

    /**
     * Retrieves a specific vehicle object from the database by its ID.
     *
     * @param id The unique integer ID of the vehicle to retrieve.
     * @return The vehicle object if found, or {@code null} if no matching vehicle is found or a SQLException occurs.
     */
    public Vehicle getVehicleDB(Role role,
                                int id) { // Assuming ID in DB is actually INT, but input is String
        String sql = "SELECT * FROM vehicles WHERE vehicle_id = ?";
        Vehicle vehicle = null;

        try (Connection connection = DBProvider.getConnection(role);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    vehicle = new Vehicle(); //
                    vehicle.setVehicleId(resultSet.getInt("vehicle_id"));

                    // Position (POINT)
                    String position = resultSet.getString("position");
                    position = position.replace("(", "").replace(")", "").trim(); // remove parens
                    String[] coords = position.split(",");
                    double x = Double.parseDouble(coords[0].trim());
                    double y = Double.parseDouble(coords[1].trim());
                    vehicle.setPosition(new PGpoint(x, y));


                    vehicle.setBattery_level(resultSet.getFloat("battery_level"));

                    vehicle.setStatus(VehicleStatus.valueOf(resultSet.getString("status")));

                    vehicle.setDistance(resultSet.getFloat("distance"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving vehicle with ID " + id + ": " + e.getMessage());
        }
        return vehicle; // returns the Vehicle object if found, otherwise null
    }

    /**
     * Removes a vehicle from the database based on its ID.
     *
     * @param id The unique integer ID of the vehicle to remove.
     * @return true if the vehicle was successfully deleted, false otherwise.
     */
    public boolean removeVehicleDB(Role role, int id) {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";

        try (Connection connection = DBProvider.getConnection(role);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                System.out.println("success");
                return true;
            } else {
                System.out.println("No vehicle found with id: " + id);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving vehicle with ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a list of vehicles filtered by their status.
     *
     * @param vehicleStatus represents the status to filter vehicles by (e.g., "AVAILABLE", "IN_TRANSIT").
     * @return A list of vehicle objects matching the specified status.
     * Returns an empty list if no vehicles match the criteria or if a SQLException occurs.
     */
    public List<Vehicle> getFilteredVehicles(Role role, String vehicleStatus) {
        StringBuilder sql = new StringBuilder(""" 
                                                      SELECT * FROM vehicles
                                                      """);

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (vehicleStatus != null) {
            conditions.add("status = ?::vehicle_status");
            params.add(vehicleStatus);
            System.out.println("set status");
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        sql.append(" ORDER BY vehicle_id ASC");

        try (Connection conn = DBProvider.getConnection(role);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Vehicle> vehicles = new ArrayList<>();
                while (rs.next()) {
                    Vehicle vehicle = new Vehicle();
                    vehicle.setVehicleId(rs.getInt("vehicle_id"));

                    //                    vehicle location
                    String vehiclePosition = rs.getString("position");
                    vehiclePosition = vehiclePosition.replace("(", "").replace(")", "");
                    String[] coords = vehiclePosition.split(",");
                    double x = Double.parseDouble(coords[0]);
                    double y = Double.parseDouble(coords[1]);
                    vehicle.setPosition(new PGpoint(x, y));

                    vehicle.setBattery_level(rs.getFloat("battery_level"));

                    vehicle.setStatus(VehicleStatus.valueOf(rs.getString("status")));

                    vehicle.setDistance(rs.getFloat("distance"));

                    vehicles.add(vehicle);
                }
                return vehicles;
            }
        } catch (SQLException e) {
            System.err.println("ERROR -- Retrieving filtered vehicles");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates an existing vehicle's information in the database.
     * This method expects all vehicle properties to be provided in the vehicle object.
     *
     * @param id      The unique integer ID of the vehicle to update.
     * @param vehicle The vehicle object containing the updated values
     * @return The updated vehicle object if the update was successful,
     * or null if no vehicle is found with the given ID or exception occurs.
     */
    public Vehicle updateVehicleDB(Role role, int id, Vehicle vehicle) {
        String sql =
                "UPDATE vehicles SET position = CAST(? AS point), battery_level = ?, distance = ?, status = ?::vehicle_status WHERE vehicle_id = ?";


        try (Connection connection = DBProvider.getConnection(role);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(true);
            //vehicle object
            PGpoint newPosition = vehicle.getPosition();
            float newBatteryLevel = vehicle.getBattery_level();
            float newDistance = vehicle.getDistance();
            String newStatus = vehicle.getStatus().name();


            preparedStatement.setInt(5, id);

            String pointString = newPosition.x + "," + newPosition.y;
            preparedStatement.setString(1, pointString);

            preparedStatement.setFloat(2, newBatteryLevel);
            preparedStatement.setFloat(3, newDistance);
            preparedStatement.setString(4, newStatus);

            System.out.println(id);
            System.out.println(vehicle.getStatus().name());

            int result = preparedStatement.executeUpdate();
            System.out.println(result);
            if (result > 0) {
                System.out.println("success");
                vehicle.setVehicleId(id);
                return vehicle;
            } else {
                System.out.println("No vehicle found with id: " + id);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving vehicle with ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Inserts a new vehicle record into the database.
     *
     * @param vehicle The vehicle object containing the data for the new vehicle.
     * @return The vehicle object that was inserted
     * or null if the insertion fails.
     */
    public Vehicle insertVehicleDB(Role role, Vehicle vehicle) {
        String sql =
                "INSERT INTO vehicles (position, battery_level, status, distance) VALUES (CAST(? AS point), ?, ?::vehicle_status, ?)";

        try (Connection connection = DBProvider.getConnection(role);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(true);
            PGpoint vehiclePosition = vehicle.getPosition();
            float vehicleBattLevel = vehicle.getBattery_level();

            String vehicleStatus = vehicle.getStatus().name();
            float vehicleDistance = vehicle.getDistance();


            String pointString = vehiclePosition.x + "," + vehiclePosition.y;
            preparedStatement.setString(1, pointString);

            preparedStatement.setFloat(2, vehicleBattLevel);
            preparedStatement.setString(3, vehicleStatus);

            preparedStatement.setFloat(4, vehicleDistance);

            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                System.out.println("Vehicle Added");
                return vehicle;
            } else {
                System.out.println("Failed");
            }

        } catch (SQLException e) {
            System.err.println("Error inserting into db");
        }
        return null;
    }
}
