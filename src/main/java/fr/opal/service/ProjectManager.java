package fr.opal.service;

import fr.opal.dao.ProjectDAO;
import fr.opal.exception.ProjectException;
import fr.opal.factory.AbstractDAOFactory;
import fr.opal.manager.NotificationManager;
import fr.opal.type.ENotifType;
import fr.opal.type.EPermission;
import fr.opal.type.EProjectState;
import fr.opal.type.Project;
import fr.opal.type.ProjectInvitation;
import fr.opal.type.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager for project operations
 * Contains project-related business logic and persistence coordination
 * Follows the singleton pattern
 */
public class ProjectManager {
    private static ProjectManager instance;
    private List<Project> loadedProjects;
    private ProjectDAO projectDAO;

    /**
     * Private constructor for singleton
     */
    private ProjectManager() {
        this.loadedProjects = new ArrayList<>();
        this.projectDAO = AbstractDAOFactory.getFactory().createProjectDAO();
    }

    /**
     * Get singleton instance
     */
    public static ProjectManager getInstance() {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }

    /**
     * Create a new project
     */
    public Project createProject(String name, String description, User owner) throws ProjectException {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new ProjectException("Error creating project: Project name cannot be empty");
        }
        if (owner == null) {
            throw new ProjectException("Error creating project: Project must have an owner");
        }

        Project project = new Project(name, description, owner);
        int projectId = projectDAO.createProject(project);
        project.setProjectId(projectId);
        
        // Send notification to owner about project creation
        NotificationManager.getInstance().sendNotification(
            owner, 
            ENotifType.PROJECT, 
            "Project '" + name + "' has been created successfully!"
        );
        
        this.loadedProjects.add(project);
        return project;
    }

    /**
     * Update project name
     */
    public void updateProjectName(int projectId, String newName) throws ProjectException {
        if (newName == null || newName.trim().isEmpty()) {
            throw new ProjectException("Error updating project: Project name cannot be empty");
        }

        Project project = findProject(projectId);
        if (project == null) {
            throw new ProjectException("Error updating project: Project not found");
        }

        project.updateProjectName(newName);
        projectDAO.saveProject(project);
    }

    /**
     * Invite user to project
     */
    public void inviteUser(int projectId, String invitedUsername, String inviterUsername, EPermission permission) throws ProjectException {
        Project project = findProject(projectId);
        if (project == null) {
            throw new ProjectException("Error inviting user: Project not found");
        }

        if (project.isCollaborator(invitedUsername)) {
            throw new ProjectException("Error inviting user: User is already a collaborator: " + invitedUsername);
        }

        ProjectInvitation invitation = new ProjectInvitation(projectId, invitedUsername, inviterUsername, permission);
        int invitationId = projectDAO.createInvitation(invitation);
        invitation.setInvitationId(invitationId);

        // TODO: Send notification to invited user
    }

    /**
     * Accept project invitation
     */
    public void acceptInvitation(int invitationId) throws ProjectException {
        ProjectInvitation invitation = projectDAO.getInvitation(invitationId);
        if (invitation == null) {
            throw new ProjectException("Error accepting invitation: Invitation not found");
        }

        invitation.acceptInvitation();
        projectDAO.updateInvitationStatus(invitationId, ProjectInvitation.InvitationStatus.ACCEPTED);

        // Add user to project collaborators
        Project project = findProject(invitation.getProjectId());
        if (project != null) {
            project.addCollaborator(invitation.getInvitedUsername(), invitation.getSuggestedPermission());
            projectDAO.saveProjectCollaborators(project.getProjectId(), project.getCollaborators());
            
            // Send notification to project owner that invitation was accepted
            User projectOwner = project.getOwner();
            if (projectOwner != null) {
                NotificationManager.getInstance().sendNotification(
                    projectOwner,
                    ENotifType.PROJECT,
                    "User '" + invitation.getInvitedUsername() + "' has accepted the invitation to join project '" + project.getName() + "'!"
                );
            }
        }
    }

    /**
     * Decline project invitation
     */
    public void declineInvitation(int invitationId) throws ProjectException {
        ProjectInvitation invitation = projectDAO.getInvitation(invitationId);
        if (invitation == null) {
            throw new ProjectException("Error declining invitation: Invitation not found");
        }

        invitation.declineInvitation();
        projectDAO.updateInvitationStatus(invitationId, ProjectInvitation.InvitationStatus.DECLINED);
    }

    /**
     * Change project state
     */
    public void changeState(int projectId, EProjectState state) throws ProjectException {
        Project project = findProject(projectId);
        if (project == null) {
            throw new ProjectException("Error changing state: Project not found");
        }

        project.changeState(state);
        projectDAO.saveProject(project);
    }

    /**
     * Delete project
     */
    public void deleteProject(int projectId) throws ProjectException {
        Project project = findProject(projectId);
        if (project == null) {
            throw new ProjectException("Error deleting project: Project not found");
        }

        loadedProjects.removeIf(p -> p.getProjectId() == projectId);
        projectDAO.deleteProject(projectId);
    }

    /**
     * Search projects by keyword
     */
    public List<Project> searchProjects(String keyword) {
        return projectDAO.searchProjects(keyword);
    }

    /**
     * Get projects by owner
     */
    public List<Project> getProjectsByOwner(String ownerUsername) {
        return projectDAO.getProjectsByOwner(ownerUsername);
    }

    /**
     * Get projects for collaborator
     */
    public List<Project> getProjectsForCollaborator(String username) {
        return projectDAO.getProjectsForCollaborator(username);
    }

    /**
     * Get projects by tag
     */
    public List<Project> getProjectsByTag(String tag) {
        return projectDAO.getProjectsByTag(tag);
    }

    /**
     * Get pending invitations for user
     */
    public List<ProjectInvitation> getPendingInvitations(String username) {
        return projectDAO.getPendingInvitations(username);
    }

    /**
     * Get all loaded projects
     */
    public List<Project> getLoadedProjects() {
        return loadedProjects;
    }

    /**
     * Find project by ID in loaded projects
     */
    public Project findProject(int projectId) {
        return loadedProjects.stream()
                .filter(p -> p.getProjectId() == projectId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Load projects from database
     */
    public void loadProjectsFromDatabase() {
        loadedProjects = projectDAO.getAllProjects();
    }
}
