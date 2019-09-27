package server.storage;

import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.records.ProjectRecord;
import anomalydetection.*;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class contains logic for reading and storing projects.
 */
public class ProjectDao {
  /**
   * Store a project in the database.
   * @param pr - The project.
   * @param ctx - The database context.
   * @return A response indicating that the project has been stored.
   */
  public static ProjectEventResponse store(ProjectEvent pr, DSLContext ctx) {
    // TODO: Do we really need to distinguish between ProjectEvent and Project? A ProjectEvent doesn't do anything else
    // besides just storing a Project.
    Project p = pr.project;

    // Check if there already exists a project with the given name and author.
    // If so, then just return that project.
    ProjectRecord pRec = ctx.selectFrom(Tables.PROJECT).where(
      Tables.PROJECT.NAME.eq(p.name).and(
        Tables.PROJECT.AUTHOR.eq(p.author))
    ).fetchOne();
    if (pRec != null) {
      return new ProjectEventResponse(pRec.getId());
    }

    // Otherwise, create a new project and store it in the Project table.
    pRec = ctx.newRecord(Tables.PROJECT);
    pRec.setId(p.id < 0 ? null : p.id);
    pRec.setName(p.name);
    pRec.setAuthor(p.author);
    pRec.setDescription(p.description);
    pRec.setCreated(new Timestamp((new Date()).getTime()));
    pRec.store();

    return new ProjectEventResponse(pRec.getId());
  }


  /**
   * Read the project with the given ID.
   * @param projId - The ID of the project.
   * @param ctx - The database context.
   * @return The project with ID projId.
   * @throws ResourceNotFoundException - Thrown if there's no project with ID projId.
   */
  public static Project read(int projId, DSLContext ctx) throws ResourceNotFoundException {
    // Query for the project with the given ID.
    ProjectRecord rec = ctx
      .selectFrom(Tables.PROJECT)
      .where(Tables.PROJECT.ID.eq(projId))
      .fetchOne();

    // Thrown an exception if there's no project with the given ID, otherwise, return the project.
    if (rec == null) {
      throw new ResourceNotFoundException(String.format(
        "Can't find Project with ID %d",
        projId
      ));
    }

    return new Project(rec.getId(), rec.getName(), rec.getAuthor(), rec.getDescription());
  }

  /**
   * Get the IDs of all the projects that match the specified key-value pairs.
   * @param  keyValuePairs - The map containing key-value pairs to match.
   * @param  ctx - The database context.
   * @return A list of all project IDs that match the given attributes.
   */
  public static List<Integer> getProjectIds(Map<String, String> keyValuePairs, DSLContext ctx) {
    return ctx
          .select(Tables.PROJECT.ID)
          .from(Tables.PROJECT)
          .where(DSL.and(keyValuePairs.keySet()
                                      .stream()
                                      .map(
                                        key -> DSL.field(DSL.name("PROJECT", key))
                                              .eq(keyValuePairs.get(key)))
                                      .collect(Collectors.toList())))
          .fetch()
          .stream()
          .map(rec -> rec.value1())
          .collect(Collectors.toList());
  }

  /**
   * Update the given field of the project of the given ID with the given value.
   * The field must be an existing field of the project.
   * @param  projectId - The ID of the project.
   * @param  key - The key to update.
   * @param  value - The value for the key.
   * @param  ctx - The database context.
   * @return whether field was successfully updated or not
   */
  public static boolean updateProject(int projectId, String key, String value, DSLContext ctx) {
    // TODO: throw some kind of exception when key doesn't exist
    int recordsUpdated = ctx
      .update(Tables.PROJECT)
      .set(DSL.field(DSL.name("PROJECT", key)), value)
      .where(Tables.PROJECT.ID.eq(projectId))
      .execute();
    return recordsUpdated > 0;
  }
}
