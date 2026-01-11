package fr.opal.dao;

import fr.opal.type.EPermission;
import fr.opal.type.Project;
import fr.opal.type.ProjectInvitation;
import java.util.List;
import java.util.Map;

/**
 * Abstract DAO for Project persistence operations
 */
public abstract class ProjectDAO {

    /**
     * Retrieves a project by its ID
     */
    public abstract Project getProjectById(int projectId);

    /**
     * Saves a project to the database
     */
    public abstract void saveProject(Project project);

    /**
     * Creates a new project in the database and returns its ID
     */
    public abstract int createProject(Project project);

    /**
     * Deletes a project from the database
     */
    public abstract void deleteProject(int projectId);

    /**
     * Retrieves all projects
     */
    public abstract List<Project> getAllProjects();

    /**
     * Retrieves projects by owner
     */
    public abstract List<Project> getProjectsByOwner(String ownerUsername);

    /**
     * Retrieves projects for a collaborator
     */
    public abstract List<Project> getProjectsForCollaborator(String username);

    /**
     * Searches projects by name, description, tags, or owner
     */
    public abstract List<Project> searchProjects(String keyword);

    /**
     * Retrieves projects with a specific tag
     */
    public abstract List<Project> getProjectsByTag(String tag);

    /**
     * Saves project collaborators to database
     */
    public abstract void saveProjectCollaborators(int projectId, Map<String, EPermission> collaborators);

    /**
     * Loads project collaborators from database
     */
    public abstract Map<String, EPermission> loadProjectCollaborators(int projectId);

    /**
     * Updates project relationships in the database
     */
    public abstract void updateProjectRelationships(Project project);

    /**
     * Loads all project data including collaborators
     */
    public abstract Project loadProjectWithDetails(int projectId);

    /**
     * Saves an invitation
     */
    public abstract int createInvitation(ProjectInvitation invitation);

    /**
     * Gets pending invitations for a user
     */
    public abstract List<ProjectInvitation> getPendingInvitations(String username);

    /**
     * Gets invitation by ID
     */
    public abstract ProjectInvitation getInvitation(int invitationId);

    /**
     * Updates invitation status
     */
    public abstract void updateInvitationStatus(int invitationId, ProjectInvitation.InvitationStatus status);
}
