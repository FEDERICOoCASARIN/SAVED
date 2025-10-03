package org.saved4.saved4.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import java.util.List;
import org.saved4.saved4.dao.VehicleDAO;
import org.saved4.saved4.dto.Vehicle;
import org.saved4.saved4.enums.Role;

@RequestScoped
public class VehicleService {
    @Inject
    private VehicleDAO vehicleDAO;

    @Context
    private ContainerRequestContext requestContext;

    /**
     * Retrieves a list of all vehicles from the database.
     *
     * @return A {@link List} of {@link Vehicle} objects.
     */
    public List<Vehicle> getAllVehicles() {
        Role role = (Role) requestContext.getProperty("db_role");
        System.out.println("[VEHICLE SERVICE] : Getting all vehicles.");
        return vehicleDAO.getAllVehiclesDB(role);
    }

    /**
     * Retrieves a list of vehicles filtered by their status.
     *
     * @param vehicleStatus Represents the status to filter vehicles by (e.g., "AVAILABLE", "IN_USE").
     * @return A list of vehicle objects matching the specified status.
     */
    public List<Vehicle> getFilteredVehicles(String vehicleStatus) {
        Role role = (Role) requestContext.getProperty("db_role");
        return vehicleDAO.getFilteredVehicles(role, vehicleStatus);
    }

    /**
     * Retrieves a specific vehicle by its unique identifier.
     *
     * @param id The unique integer ID of the vehicle to retrieve.
     * @return The link object corresponding to the given ID, or null if not found.
     */
    public Vehicle getVehicleByID(int id) {
        Role role = (Role) requestContext.getProperty("db_role");
        System.out.println("[VEHICLE SERVICE] : Getting  vehicle ID: " + id);

        return vehicleDAO.getVehicleDB(role, id);
    }

    /**
     * Updates an existing vehicle's information in the database.
     *
     * @param id      The unique integer ID of the vehicle to update.
     * @param vehicle The vehicle object containing the updated data.
     * @return The updated vehicle object if the update was successful, null otherwise.
     */
    public Vehicle updateVehicle(int id, Vehicle vehicle) {
        Role role = (Role) requestContext.getProperty("db_role");
        System.out.println("[VEHICLE SERVICE] : Update Vehicle with ID: " + id);

        return vehicleDAO.updateVehicleDB(role, id, vehicle);
    }

    /**
     * Deletes a vehicle from the database by its unique identifier.
     *
     * @param id The unique integer ID of the vehicle to delete.
     * @return true if the vehicle was successfully deleted, false otherwise.
     */
    public Boolean deleteVehicle(int id) {
        Role role = (Role) requestContext.getProperty("db_role");
        System.out.println("Service: Delete Vehicle with ID: " + id);

        return vehicleDAO.removeVehicleDB(role, id);
    }

    /**
     * Adds a new vehicle to the database.
     *
     * @param vehicle The vehicle object to be added.
     * @return The newly added vehicle object, including any generated ID from the database,
     * or {@code null} if the addition fails.
     */
    public Vehicle addNewVehicle(Vehicle vehicle) {
        Role role = (Role) requestContext.getProperty("db_role");
        System.out.println("[VEHICLE SERVICE] : Adding new vehicle");

        return vehicleDAO.insertVehicleDB(role, vehicle);
    }


    /**
     * Exports all vehicle data.
     *
     * @return A {@link List} of all {@link Vehicle} objects.
     */
    public List<Vehicle> exportVehicles() {
        Role role = (Role) requestContext.getProperty("db_role");
        System.out.println("[VEHICLE SERVICE] : Exporting the vehicles");

        return vehicleDAO.getAllVehiclesDB(role);
    }

}
