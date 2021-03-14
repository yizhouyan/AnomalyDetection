/**
 * This class is generated by jOOQ
 */
package jooq.sqlite.gen.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import jooq.sqlite.gen.DefaultSchema;
import jooq.sqlite.gen.Keys;
import jooq.sqlite.gen.tables.records.DataframeRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.8.4"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Dataframe extends TableImpl<DataframeRecord> {

    private static final long serialVersionUID = -2121897909;

    /**
     * The reference instance of <code>DataFrame</code>
     */
    public static final Dataframe DATAFRAME = new Dataframe();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DataframeRecord> getRecordType() {
        return DataframeRecord.class;
    }

    /**
     * The column <code>DataFrame.id</code>.
     */
    public final TableField<DataframeRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>DataFrame.tag</code>.
     */
    public final TableField<DataframeRecord, String> TAG = createField("tag", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>DataFrame.experimentRun</code>.
     */
    public final TableField<DataframeRecord, Integer> EXPERIMENTRUN = createField("experimentRun", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>DataFrame.filepath</code>.
     */
    public final TableField<DataframeRecord, String> FILEPATH = createField("filepath", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>DataFrame</code> table reference
     */
    public Dataframe() {
        this("DataFrame", null);
    }

    /**
     * Create an aliased <code>DataFrame</code> table reference
     */
    public Dataframe(String alias) {
        this(alias, DATAFRAME);
    }

    private Dataframe(String alias, Table<DataframeRecord> aliased) {
        this(alias, aliased, null);
    }

    private Dataframe(String alias, Table<DataframeRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return DefaultSchema.DEFAULT_SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<DataframeRecord> getPrimaryKey() {
        return Keys.PK_DATAFRAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<DataframeRecord>> getKeys() {
        return Arrays.<UniqueKey<DataframeRecord>>asList(Keys.PK_DATAFRAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<DataframeRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<DataframeRecord, ?>>asList(Keys.FK_DATAFRAME_EXPERIMENTRUN_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dataframe as(String alias) {
        return new Dataframe(alias, this);
    }

    /**
     * Rename this table
     */
    public Dataframe rename(String name) {
        return new Dataframe(name, null);
    }
}
