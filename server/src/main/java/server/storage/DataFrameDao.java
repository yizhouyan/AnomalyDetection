package server.storage;

import jooq.sqlite.gen.Tables;
import jooq.sqlite.gen.tables.records.DataframeRecord;
import jooq.sqlite.gen.tables.records.DataframecolumnRecord;
import anomalydetection.DataFrame;
import anomalydetection.DataFrameColumn;

import anomalydetection.ResourceNotFoundException;
import org.jooq.DSLContext;

import java.util.List;

import static jooq.sqlite.gen.Tables.*;

/**
 * This class contains logic for storing and reading DataFrames.
 */
public class DataFrameDao {
  /**
   * Store the given DataFrame in the database.
   * @param df - The DataFrame.
   * @param experimentRunId - The ID of the experiment run that contains the DataFrame.
   * @param ctx - The database context.
   * @return The row in the DataFrame table.
   */
  public static DataframeRecord store(DataFrame df, int experimentRunId, DSLContext ctx) {
    // Check if a DataFrame with the given ID already exists. If so, then just return it.
    DataframeRecord rec = ctx.selectFrom(Tables.DATAFRAME).where(Tables.DATAFRAME.ID.eq(df.id)).fetchOne();
    if (rec != null) {
      return rec;
    }

    // Store an entry in the DataFrame table.
    final DataframeRecord dfRec = ctx.newRecord(DATAFRAME);
    dfRec.setId(null);
    dfRec.setExperimentrun(experimentRunId);
    dfRec.setTag(df.tag);
    dfRec.setFilepath(df.getFilepath());
    dfRec.store();

    // Store an entry in DataFrame column for each column of the DataFrame.
    df.getSchema().forEach(col -> {
      DataframecolumnRecord colRec = ctx.newRecord(DATAFRAMECOLUMN);
      colRec.setId(null);
      colRec.setDfid(dfRec.getId());
      colRec.setName(col.name);
      colRec.setType(col.type);
      colRec.store();
      colRec.getId();
    });

    // Return the row that was stored in the DataFrame table.
    return dfRec;
  }

  /**
   * Read the schema for the DataFrame with the given ID.
   * @param dfId - The ID of a DataFrame. It MUST exist in the database.
   * @param ctx - The database context.
   * @return The list of columns in the DataFrame with ID dfId.
   */
  public static List<DataFrameColumn> readSchema(int dfId, DSLContext ctx) {
    return ctx
      .select(Tables.DATAFRAMECOLUMN.NAME, Tables.DATAFRAMECOLUMN.TYPE)
      .from(Tables.DATAFRAMECOLUMN)
      .where(Tables.DATAFRAMECOLUMN.DFID.eq(dfId))
      .fetch()
      .map(r -> new DataFrameColumn(r.value1(), r.value2()));
  }

  /**
   * Read the DataFrame with the given ID.
   * @param dfId - The ID of the DataFrame.
   * @param ctx - The database context.
   * @return The DataFrame with the given ID.
   * @throws ResourceNotFoundException - Thrown if there is no row in the DataFrame table that has a primary key of
   * dfId.
   */
  public static DataFrame read(int dfId, DSLContext ctx) throws ResourceNotFoundException {
    // Attempt to read the row with the given ID. Throw an exception if it cannot be found.
    DataframeRecord rec = ctx.selectFrom(Tables.DATAFRAME).where(Tables.DATAFRAME.ID.eq(dfId)).fetchOne();
    if (rec == null) {
      throw new ResourceNotFoundException(String.format(
        "Could not read DataFrame %d, it doesn't exist",
        dfId
      ));
    }

    // Turn the row into a modeldb.DataFrame object.
    DataFrame df = new DataFrame(rec.getId(), readSchema(dfId, ctx), rec.getTag(),rec.getFilepath());
    df.setFilepath(rec.getFilepath());

    return df;
  }

  /**
   * Check if the DataFrame table contains a row whose primary key is equal to id.
   * @param id - The primary key value we want to find.
   * @param ctx - The database context.
   * @return Whether there exists a row in the DataFrame table that has a primary key equal to id.
   */
  public static boolean exists(int id, DSLContext ctx) {
    return ctx.selectFrom(Tables.DATAFRAME).where(Tables.DATAFRAME.ID.eq(id)).fetchOne() != null;
  }
}
