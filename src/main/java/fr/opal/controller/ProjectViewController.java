package fr.opal.controller;

import fr.opal.exception.ProjectException;
import fr.opal.facade.ProjectFacade;
import fr.opal.type.Project;
import fr.opal.type.User;
import java.util.List;

/**
 * Controller for project view operations
 */
public class ProjectViewController {
    private ProjectFacade projectFacade;

    public ProjectViewController() {
        this.projectFacade = ProjectFacade.getInstance();
    }

    /**
     * Create a new project
     */
    public void createProject(String name, String description, User owner) {
        try {
            Project project = projectFacade.createProject(name, description, owner);
            System.out.println("Project created: " + name + " (ID: " + project.getProjectId() + ")");
        } catch (ProjectException e) {
            System.err.println("Error creating project: " + e.getMessage());
        }
    }

    /**
     * Delete a project
     */
    public int deleteProject(int projectId) {
        try {
            projectFacade.deleteProject(projectId);
            System.out.println("Project deleted with ID: " + projectId);
            return projectId;
        } catch (ProjectException e) {
            System.err.println("Error deleting project: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Search projects by keyword
     */
    public List<Project> searchProjects(String keyword) {
        return projectFacade.searchProjects(keyword);
    }

    /**
     * Get all loaded projects
     */
    public List<Project> getLoadedProjects() {
        return projectFacade.getLoadedProjects();
    }

    /**
     * Load projects from database
     */
    public void loadProjectsFromDatabase() {
        projectFacade.loadProjectsFromDatabase();
    }

    /**
     * Get projects by owner
     */
    public List<Project> getProjectsByOwner(String ownerUsername) {
        return projectFacade.getProjectsByOwner(ownerUsername);
    }
}
