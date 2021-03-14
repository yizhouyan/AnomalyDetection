/**
 * This class is generated by jOOQ
 */
package jooq.sqlite.gen.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import jooq.sqlite.gen.DefaultSchema;
import jooq.sqlite.gen.Keys;
import jooq.sqlite.gen.tables.records.EstimatorspecRecord;

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
public class Estimatorspec extends TableImpl<EstimatorspecRecord> {

    private static final long serialVersionUID = 1298838935;

    /**
     * The reference instance of <code>EstimatorSpec</code>
     */
    public static final Estimatorspec ESTIMATORSPEC = new Estimatorspec();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EstimatorspecRecord> getRecordType() {
        return EstimatorspecRecord.class;
    }

    /**
     * The column <code>EstimatorSpec.id</code>.
     */
    public final TableField<EstimatorspecRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>EstimatorSpec.estimatorType</code>.
     */
    public final TableField<EstimatorspecRecord, String> ESTIMATORTYPE = createField("estimatorType", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>EstimatorSpec.tag</code>.
     */
    public final TableField<EstimatorspecRecord, String> TAG = createField("tag", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>EstimatorSpec.experimentRun</code>.
     */
    public final TableField<EstimatorspecRecord, Integer> EXPERIMENTRUN = createField("experimentRun", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>EstimatorSpec</code> table reference
     */
    public Estimatorspec() {
        this("EstimatorSpec", null);
    }

    /**
     * Create an aliased <code>EstimatorSpec</code> table reference
     */
    public Estimatorspec(String alias) {
        this(alias, ESTIMATORSPEC);
    }

    private Estimatorspec(String alias, Table<EstimatorspecRecord> aliased) {
        this(alias, aliased, null);
    }

    private Estimatorspec(String alias, Table<EstimatorspecRecord> aliased, Field<?>[] parameters) {
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
    public UniqueKey<EstimatorspecRecord> getPrimaryKey() {
        return Keys.PK_ESTIMATORSPEC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<EstimatorspecRecord>> getKeys() {
        return Arrays.<UniqueKey<EstimatorspecRecord>>asList(Keys.PK_ESTIMATORSPEC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<EstimatorspecRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<EstimatorspecRecord, ?>>asList(Keys.FK_ESTIMATORSPEC_EXPERIMENTRUN_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Estimatorspec as(String alias) {
        return new Estimatorspec(alias, this);
    }

    /**
     * Rename this table
     */
    public Estimatorspec rename(String name) {
        return new Estimatorspec(name, null);
    }
}
