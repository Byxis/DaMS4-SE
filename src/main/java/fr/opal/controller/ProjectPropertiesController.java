package fr.opal.controller;

import fr.opal.exception.ProjectException;
import fr.opal.facade.ProjectFacade;
import fr.opal.type.EPermission;
import fr.opal.type.EProjectState;

/**
 * Controller for project properties operations
 */
public class ProjectPropertiesController {
    private ProjectFacade projectFacade;
    private int currentProjectId;

    public ProjectPropertiesController() {
        this.projectFacade = ProjectFacade.getInstance();
    }

    /**
     * Update project name
     */
    public void updateProjectName(int projectId, String newName) {
        try {
            projectFacade.updateProjectName(projectId, newName);
            System.out.println("Project name updated to: " + newName);
        } catch (ProjectException e) {
            System.err.println("Error updating project name: " + e.getMessage());
        }
    }

    /**
     * Invite user to project
     */
    public void inviteUser(int projectId, String invitedUsername, String inviterUsername, EPermission permission) {
        try {
            projectFacade.inviteUser(projectId, invitedUsername, inviterUsername, permission);
            System.out.println("User " + invitedUsername + " invited to project with permission: " + permission);
        } catch (ProjectException e) {
            System.err.println("Error inviting user: " + e.getMessage());
        }
    }

    /**
     * Edit user role in project
     */
    public void editUserRole(int projectId, String username, EPermission permission) {
        try {
            projectFacade.inviteUser(projectId, username, "admin", permission);
            System.out.println("User " + username + " permission changed to: " + permission);
        } catch (ProjectException e) {
            System.err.println("Error editing user role: " + e.getMessage());
        }
    }

    /**
     * Change project state
     */
    public void changeState(int projectId, EProjectState state) {
        try {
            projectFacade.changeState(projectId, state);
            System.out.println("Project state changed to: " + state);
        } catch (ProjectException e) {
            System.err.println("Error changing project state: " + e.getMessage());
        }
    }

    /**
     * Set current project ID
     */
    public void setCurrentProjectId(int projectId) {
        this.currentProjectId = projectId;
    }

    /**
     * Get current project ID
     */
    public int getCurrentProjectId() {
        return currentProjectId;
    }
}
