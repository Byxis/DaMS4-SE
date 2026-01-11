package fr.opal.facade;

import fr.opal.exception.ProjectException;
import fr.opal.service.ProjectManager;
import fr.opal.type.EPermission;
import fr.opal.type.EProjectState;
import fr.opal.type.Project;
import fr.opal.type.ProjectInvitation;
import fr.opal.type.User;

import java.util.List;

/**
 * Facade for project management operations
 * Thin wrapper that delegates all operations to ProjectManager
 */
public class ProjectFacade {
    private static ProjectFacade instance;
    private ProjectManager projectManager;

    /**
     * Get singleton instance
     */
    public static ProjectFacade getInstance() {
        if (instance == null) {
            instance = new ProjectFacade();
        }
        return instance;
    }

    /**
     * Private constructor
     */
    private ProjectFacade() {
        this.projectManager = ProjectManager.getInstance();
    }

    /**
     * Create a new project - delegates to ProjectManager
     */
    public Project createProject(String name, String description, User owner) throws ProjectException {
        return projectManager.createProject(name, description, owner);
    }

    /**
     * Delete a project - delegates to ProjectManager
     */
    public void deleteProject(int projectId) throws ProjectException {
        projectManager.deleteProject(projectId);
    }

    /**
     * Update project name - delegates to ProjectManager
     */
    public void updateProjectName(int projectId, String newName) throws ProjectException {
        projectManager.updateProjectName(projectId, newName);
    }

    /**
     * Invite user to project - delegates to ProjectManager
     */
    public void inviteUser(int projectId, String invitedUsername, String inviterUsername, EPermission permission) throws ProjectException {
        projectManager.inviteUser(projectId, invitedUsername, inviterUsername, permission);
    }

    /**
     * Accept project invitation - delegates to ProjectManager
     */
    public void acceptInvitation(int invitationId) throws ProjectException {
        projectManager.acceptInvitation(invitationId);
    }

    /**
     * Decline project invitation - delegates to ProjectManager
     */
    public void declineInvitation(int invitationId) throws ProjectException {
        projectManager.declineInvitation(invitationId);
    }

    /**
     * Change project state - delegates to ProjectManager
     */
    public void changeState(int projectId, EProjectState state) throws ProjectException {
        projectManager.changeState(projectId, state);
    }

    /**
     * Search projects - delegates to ProjectManager
     */
    public List<Project> searchProjects(String keyword) {
        return projectManager.searchProjects(keyword);
    }

    /**
     * Get projects by owner - delegates to ProjectManager
     */
    public List<Project> getProjectsByOwner(String ownerUsername) {
        return projectManager.getProjectsByOwner(ownerUsername);
    }

    /**
     * Get projects for collaborator - delegates to ProjectManager
     */
    public List<Project> getProjectsForCollaborator(String username) {
        return projectManager.getProjectsForCollaborator(username);
    }

    /**
     * Get projects by tag - delegates to ProjectManager
     */
    public List<Project> getProjectsByTag(String tag) {
        return projectManager.getProjectsByTag(tag);
    }

    /**
     * Get pending invitations - delegates to ProjectManager
     */
    public List<ProjectInvitation> getPendingInvitations(String username) {
        return projectManager.getPendingInvitations(username);
    }

    /**
     * Find project by ID - delegates to ProjectManager
     */
    public Project findProject(int projectId) {
        return projectManager.findProject(projectId);
    }

    /**
     * Get all loaded projects - delegates to ProjectManager
     */
    public List<Project> getLoadedProjects() {
        return projectManager.getLoadedProjects();
    }

    /**
     * Load projects from database - delegates to ProjectManager
     */
    public void loadProjectsFromDatabase() {
        projectManager.loadProjectsFromDatabase();
    }
}
