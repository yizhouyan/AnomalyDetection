/**
 * This class is generated by jOOQ
 */
package jooq.sqlite.gen.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import jooq.sqlite.gen.DefaultSchema;
import jooq.sqlite.gen.Keys;
import jooq.sqlite.gen.tables.records.UnsupervisedeventRecord;

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
public class Unsupervisedevent extends TableImpl<UnsupervisedeventRecord> {

    private static final long serialVersionUID = 613536753;

    /**
     * The reference instance of <code>UnsupervisedEvent</code>
     */
    public static final Unsupervisedevent UNSUPERVISEDEVENT = new Unsupervisedevent();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<UnsupervisedeventRecord> getRecordType() {
        return UnsupervisedeventRecord.class;
    }

    /**
     * The column <code>UnsupervisedEvent.id</code>.
     */
    public final TableField<UnsupervisedeventRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>UnsupervisedEvent.oldDf</code>.
     */
    public final TableField<UnsupervisedeventRecord, Integer> OLDDF = createField("oldDf", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>UnsupervisedEvent.newDf</code>.
     */
    public final TableField<UnsupervisedeventRecord, Integer> NEWDF = createField("newDf", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>UnsupervisedEvent.estimatorSpec</code>.
     */
    public final TableField<UnsupervisedeventRecord, Integer> ESTIMATORSPEC = createField("estimatorSpec", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>UnsupervisedEvent.inputColumns</code>.
     */
    public final TableField<UnsupervisedeventRecord, String> INPUTCOLUMNS = createField("inputColumns", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>UnsupervisedEvent.outputColumns</code>.
     */
    public final TableField<UnsupervisedeventRecord, String> OUTPUTCOLUMNS = createField("outputColumns", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>UnsupervisedEvent.experimentRun</code>.
     */
    public final TableField<UnsupervisedeventRecord, Integer> EXPERIMENTRUN = createField("experimentRun", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>UnsupervisedEvent.stageNumber</code>.
     */
    public final TableField<UnsupervisedeventRecord, Integer> STAGENUMBER = createField("stageNumber", org.jooq.impl.SQLDataType.INTEGER.defaultValue(org.jooq.impl.DSL.field("-1", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * Create a <code>UnsupervisedEvent</code> table reference
     */
    public Unsupervisedevent() {
        this("UnsupervisedEvent", null);
    }

    /**
     * Create an aliased <code>UnsupervisedEvent</code> table reference
     */
    public Unsupervisedevent(String alias) {
        this(alias, UNSUPERVISEDEVENT);
    }

    private Unsupervisedevent(String alias, Table<UnsupervisedeventRecord> aliased) {
        this(alias, aliased, null);
    }

    private Unsupervisedevent(String alias, Table<UnsupervisedeventRecord> aliased, Field<?>[] parameters) {
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
    public UniqueKey<UnsupervisedeventRecord> getPrimaryKey() {
        return Keys.PK_UNSUPERVISEDEVENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<UnsupervisedeventRecord>> getKeys() {
        return Arrays.<UniqueKey<UnsupervisedeventRecord>>asList(Keys.PK_UNSUPERVISEDEVENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<UnsupervisedeventRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<UnsupervisedeventRecord, ?>>asList(Keys.FK_UNSUPERVISEDEVENT_DATAFRAME_2, Keys.FK_UNSUPERVISEDEVENT_DATAFRAME_1, Keys.FK_UNSUPERVISEDEVENT_ESTIMATORSPEC_1, Keys.FK_UNSUPERVISEDEVENT_EXPERIMENTRUN_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Unsupervisedevent as(String alias) {
        return new Unsupervisedevent(alias, this);
    }

    /**
     * Rename this table
     */
    public Unsupervisedevent rename(String name) {
        return new Unsupervisedevent(name, null);
    }
}