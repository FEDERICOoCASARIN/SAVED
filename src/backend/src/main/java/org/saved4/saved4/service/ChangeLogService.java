package org.saved4.saved4.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.saved4.saved4.dao.ChangeLogDao;
import org.saved4.saved4.dto.ChangeLog;

/**
 * Service layer for managing ChangeLog operations.
 * This class orchestrates business logic and interacts with the ChangeLogDao.
 */
@ApplicationScoped
public class ChangeLogService {
    @Inject
    private ChangeLogDao changeLogDao;

    /**
     * Retrieves all change logs from the database.
     * This method is typically used for an administrative view.
     *
     * @return A list of {@link ChangeLog} objects representing all change log entries.
     */
    public List<ChangeLog> getAllChangeLogs() {
        System.out.println("Service: Getting all changelogs.");
        return changeLogDao.getAllChangeLogs();
    }

    /**
     * Retrieves change logs for a specific order owner.
     * This method enforces user-specific visibility based on the order owner.
     *
     * @param orderOwner The ID of the order owner (e.g., company name or user ID) whose change logs are to be retrieved.
     * @return A list of {@link ChangeLog} entries associated with the given order owner.
     */
    public List<ChangeLog> getChangeLogsByOrderOwner(String orderOwner) {
        System.out.println("Service: Getting changelogs for order owner: " + orderOwner);
        return changeLogDao.getChangeLogsByOrderOwner(orderOwner);
    }

    /**
     * Creates a new change log entry in the database.
     *
     * @param log The {@link ChangeLog} object to be inserted.
     */
    public void createChangeLog(ChangeLog log) {
        System.out.println("Service: Creating new changelog.");
        changeLogDao.insertChangeLog(log);
    }

    /**
     * Marks specified change logs as read by a given user.
     *
     * @param changeIds A list of change log IDs to mark as read.
     * @param username  The username of the user who is marking the logs as read.
     */
    public void markLogsAsRead(List<Integer> changeIds, String username) {
        System.out.println("Service: Marking changelogs as read for user: " + username);
        changeLogDao.markLogsAsRead(changeIds, username);
    }
}
