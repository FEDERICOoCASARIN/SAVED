package org.saved4.saved4.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.saved4.saved4.dto.Vehicle;
import org.saved4.saved4.service.VehicleService;

import java.io.IOException;
import java.util.List;

@Path("/vehicle")
public class VehicleResource {

    @Inject
    private VehicleService vehicleService;


    /**
     * Retrieves a specific vehicle by its ID.
     *
     * @param vehicleId The unique integer ID of the vehicle to retrieve.
     * @return The vehicle object in JSON format.
     * @throws WebApplicationException if an error occurs during retrieval (HTTP 500).
     */
    @Path("/{vehicle_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Vehicle getVehicle(@PathParam("vehicle_id") int vehicleId) {
        try {

            Vehicle result = vehicleService.getVehicleByID(vehicleId);
            System.out.println("Successfully");
            return result;
        } catch (Exception e) {
            System.err.println("Error calling VehicleService.getVehicle() from VehicleResource:");
            e.printStackTrace();

            throw new WebApplicationException("Failed to retrieve vehicles: " + e.getMessage(),
                    jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Retrieves a list of vehicles.
     * Can filter vehicles by their status if a vehicleStatus query parameter is provided.
     *
     * @return A list of vehicle objects in JSON format.
     * @throws WebApplicationException if an error occurs during retrieval (HTTP 500).
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Vehicle> getVehicles(@QueryParam("status") String vehicleStatus) {
        try {
            if (vehicleStatus != null) {
                return vehicleService.getFilteredVehicles(vehicleStatus);
            } else {
                return vehicleService.getAllVehicles();
            }
        } catch (Exception e) {
            System.err.println(
                    "Error calling VehicleService.getAllVehicles() from VehicleResource:");
            e.printStackTrace();

            throw new WebApplicationException("Failed to retrieve vehicles: " + e.getMessage(),
                    jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Adds a new vehicle to the system.
     *
     * @param vehicle The vehicle object to be added, provided in JSON format in the request body.
     * @return The newly added vehicle object, including any generated ID.
     * @throws WebApplicationException if an error occurs during addition (HTTP 500).
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Vehicle addVehicle(Vehicle vehicle) {
        try {
            Vehicle result = vehicleService.addNewVehicle(vehicle);
            System.out.println("Successfully added");
            return result;
        } catch (Exception e) {
            System.err.println(
                    "Error calling VehicleService.updateVehicle() from VehicleResource:");
            e.printStackTrace();
            throw new WebApplicationException("Failed to retrieve vehicles: " + e.getMessage(),
                    jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing vehicle partially using a PATCH request.
     * Only the fields provided in the request body will be updated.
     *
     * @param vehicleId The unique integer ID of the vehicle to update.
     * @param vehicle   The vehicle object containing the fields to be updated, provided in JSON format.
     * @return The updated vehicle object in JSON format.
     * @throws WebApplicationException if an error occurs during the update (HTTP 500).
     */

    @Path("/{vehicle_id}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Vehicle updateVehicle(@PathParam("vehicle_id") int vehicleId, Vehicle vehicle) {
        try {
            Vehicle result = vehicleService.updateVehicle(vehicleId, vehicle);
            System.out.println("[RESOURCE] Successfully update vehicle id: " + vehicleId);
            return result;
        } catch (Exception e) {
            System.err.println(
                    "Error calling VehicleService.updateVehicle() from VehicleResource:");
            e.printStackTrace();

            throw new WebApplicationException("Failed to retrieve vehicles: " + e.getMessage(),
                    jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a vehicle from the system by its ID.
     *
     * @param vehicleId The unique integer ID of the vehicle to delete.
     * @return true if the vehicle was successfully deleted, false otherwise.
     * @throws WebApplicationException if an error occurs during deletion (HTTP 500).
     */
    @Path("/{vehicle_id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public boolean deleteVehicle(@PathParam("vehicle_id") int vehicleId) {
        try {
            Boolean result = vehicleService.deleteVehicle(vehicleId);
            return result;
        } catch (Exception e) {
            System.err.println(
                    "Error calling VehicleService.deleteVehicle() from VehicleResource:");
            e.printStackTrace();

            throw new WebApplicationException("Failed to retrieve vehicles: " + e.getMessage(),
                    jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Exports all vehicle data to an Excel (XLSX) file.
     *
     * @return A response containing the Excel file as a stream, with appropriate headers for download.
     * @throws WebApplicationException if an error occurs during Excel generation (HTTP 500).
     */
    @GET
    @Path("/export/xlsx")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportVehiclesExcel() {
        List<Vehicle> vehicles = vehicleService.exportVehicles();

        StreamingOutput stream = output -> {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Vehicles Data");

                String[] headers = {"ID", "Latitude", "Longitude", "Battery Level", "Status", "Milage (km)"};

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    Font headerFont = workbook.createFont();
                    headerFont.setBold(true);
                    CellStyle headerCellStyle = workbook.createCellStyle();
                    headerCellStyle.setFont(headerFont);
                    cell.setCellStyle(headerCellStyle);
                }

                int rowNum = 1;
                for (Vehicle vehicle : vehicles) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue("VEH_" + vehicle.getVehicleId());
                    String formattedLatitude = String.format("%.5f", vehicle.getPosition().x);
                    String formattedLongitude = String.format("%.5f", vehicle.getPosition().y);
                    row.createCell(1).setCellValue(formattedLatitude);
                    row.createCell(2).setCellValue(formattedLongitude);
                    row.createCell(3).setCellValue(vehicle.getBattery_level());
                    row.createCell(4).setCellValue(vehicle.getStatus().name());
                    row.createCell(5).setCellValue(vehicle.getDistance());
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(output);
                output.flush();
            } catch (IOException e) {
                System.err.println("Error while writing excel" + e.getMessage());
                throw new WebApplicationException("Excel not generated" + e.getMessage(), e);
            }
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"vehicles_export.xlsx\"")
                .build();
    }

    /**
     * Exports all vehicle data to an Excel (XLSX) file.
     *
     * @return A response containing the Excel file as a stream, with appropriate headers for download.
     * @throws WebApplicationException if an error occurs during Excel generation (HTTP 500).
     */
    @GET
    @Path("/export/csv")
    @Produces("text/csv")
    public Response exportVehiclesCSV() {
        List<Vehicle> vehicles = vehicleService.exportVehicles();

        StreamingOutput stream = output -> {
            try {
                String[] headers = {"ID", "Latitude", "Longitude", "Battery Level", "Status", "Milage (km)"};
                StringBuilder csvBuilder = new StringBuilder();

                for (String header : headers) {
                    csvBuilder.append(header).append(",");
                }
                csvBuilder.setLength(csvBuilder.length() - 1);
                csvBuilder.append("\n");


                for (Vehicle vehicle : vehicles) {
                    csvBuilder.append("VEH_").append(vehicle.getVehicleId()).append(",");
                    String formattedLatitude = String.format("%.5f", vehicle.getPosition().x);
                    String formattedLongitude = String.format("%.5f", vehicle.getPosition().y);
                    csvBuilder.append(formattedLatitude).append(",");
                    csvBuilder.append(formattedLongitude).append(",");
                    csvBuilder.append(vehicle.getBattery_level()).append(",");
                    csvBuilder.append(vehicle.getStatus().name()).append(",");
                    csvBuilder.append(vehicle.getDistance()).append("\n");
                }

                output.write(csvBuilder.toString().getBytes());
                output.flush();
            } catch (IOException e) {
                System.err.println("Error while writing CSV: " + e.getMessage());
                throw new WebApplicationException("CSV not generated: " + e.getMessage(), e);
            }
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"vehicles_export.csv\"")
                .build();
    }


}
