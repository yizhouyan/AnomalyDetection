/**
 * This class is generated by jOOQ
 */
package jooq.sqlite.gen.tables;


import javax.annotation.Generated;

import jooq.sqlite.gen.DefaultSchema;
import jooq.sqlite.gen.tables.records.SqliteSequenceRecord;

import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
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
public class SqliteSequence extends TableImpl<SqliteSequenceRecord> {

    private static final long serialVersionUID = -618461329;

    /**
     * The reference instance of <code>sqlite_sequence</code>
     */
    public static final SqliteSequence SQLITE_SEQUENCE = new SqliteSequence();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SqliteSequenceRecord> getRecordType() {
        return SqliteSequenceRecord.class;
    }

    /**
     * The column <code>sqlite_sequence.name</code>.
     */
    public final TableField<SqliteSequenceRecord, Object> NAME = createField("name", org.jooq.impl.DefaultDataType.getDefaultDataType(""), this, "");

    /**
     * The column <code>sqlite_sequence.seq</code>.
     */
    public final TableField<SqliteSequenceRecord, Object> SEQ = createField("seq", org.jooq.impl.DefaultDataType.getDefaultDataType(""), this, "");

    /**
     * Create a <code>sqlite_sequence</code> table reference
     */
    public SqliteSequence() {
        this("sqlite_sequence", null);
    }

    /**
     * Create an aliased <code>sqlite_sequence</code> table reference
     */
    public SqliteSequence(String alias) {
        this(alias, SQLITE_SEQUENCE);
    }

    private SqliteSequence(String alias, Table<SqliteSequenceRecord> aliased) {
        this(alias, aliased, null);
    }

    private SqliteSequence(String alias, Table<SqliteSequenceRecord> aliased, Field<?>[] parameters) {
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
    public SqliteSequence as(String alias) {
        return new SqliteSequence(alias, this);
    }

    /**
     * Rename this table
     */
    public SqliteSequence rename(String name) {
        return new SqliteSequence(name, null);
    }
}
