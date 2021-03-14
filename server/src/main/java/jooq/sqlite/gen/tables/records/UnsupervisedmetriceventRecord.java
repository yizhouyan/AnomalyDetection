/**
 * This class is generated by jOOQ
 */
package jooq.sqlite.gen.tables.records;


import javax.annotation.Generated;

import jooq.sqlite.gen.tables.Unsupervisedmetricevent;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;


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
public class UnsupervisedmetriceventRecord extends UpdatableRecordImpl<UnsupervisedmetriceventRecord> implements Record6<Integer, Integer, Integer, String, Float, Integer> {

    private static final long serialVersionUID = 1573769285;

    /**
     * Setter for <code>UnsupervisedMetricEvent.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>UnsupervisedMetricEvent.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>UnsupervisedMetricEvent.estimatorSpec</code>.
     */
    public void setEstimatorspec(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>UnsupervisedMetricEvent.estimatorSpec</code>.
     */
    public Integer getEstimatorspec() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>UnsupervisedMetricEvent.df</code>.
     */
    public void setDf(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>UnsupervisedMetricEvent.df</code>.
     */
    public Integer getDf() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>UnsupervisedMetricEvent.metricType</code>.
     */
    public void setMetrictype(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>UnsupervisedMetricEvent.metricType</code>.
     */
    public String getMetrictype() {
        return (String) get(3);
    }

    /**
     * Setter for <code>UnsupervisedMetricEvent.metricValue</code>.
     */
    public void setMetricvalue(Float value) {
        set(4, value);
    }

    /**
     * Getter for <code>UnsupervisedMetricEvent.metricValue</code>.
     */
    public Float getMetricvalue() {
        return (Float) get(4);
    }

    /**
     * Setter for <code>UnsupervisedMetricEvent.experimentRun</code>.
     */
    public void setExperimentrun(Integer value) {
        set(5, value);
    }

    /**
     * Getter for <code>UnsupervisedMetricEvent.experimentRun</code>.
     */
    public Integer getExperimentrun() {
        return (Integer) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row6<Integer, Integer, Integer, String, Float, Integer> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row6<Integer, Integer, Integer, String, Float, Integer> valuesRow() {
        return (Row6) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Unsupervisedmetricevent.UNSUPERVISEDMETRICEVENT.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return Unsupervisedmetricevent.UNSUPERVISEDMETRICEVENT.ESTIMATORSPEC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return Unsupervisedmetricevent.UNSUPERVISEDMETRICEVENT.DF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Unsupervisedmetricevent.UNSUPERVISEDMETRICEVENT.METRICTYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Float> field5() {
        return Unsupervisedmetricevent.UNSUPERVISEDMETRICEVENT.METRICVALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field6() {
        return Unsupervisedmetricevent.UNSUPERVISEDMETRICEVENT.EXPERIMENTRUN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value2() {
        return getEstimatorspec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getDf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getMetrictype();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float value5() {
        return getMetricvalue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value6() {
        return getExperimentrun();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnsupervisedmetriceventRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnsupervisedmetriceventRecord value2(Integer value) {
        setEstimatorspec(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnsupervisedmetriceventRecord value3(Integer value) {
        setDf(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnsupervisedmetriceventRecord value4(String value) {
        setMetrictype(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnsupervisedmetriceventRecord value5(Float value) {
        setMetricvalue(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnsupervisedmetriceventRecord value6(Integer value) {
        setExperimentrun(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnsupervisedmetriceventRecord values(Integer value1, Integer value2, Integer value3, String value4, Float value5, Integer value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UnsupervisedmetriceventRecord
     */
    public UnsupervisedmetriceventRecord() {
        super(Unsupervisedmetricevent.UNSUPERVISEDMETRICEVENT);
    }

    /**
     * Create a detached, initialised UnsupervisedmetriceventRecord
     */
    public UnsupervisedmetriceventRecord(Integer id, Integer estimatorspec, Integer df, String metrictype, Float metricvalue, Integer experimentrun) {
        super(Unsupervisedmetricevent.UNSUPERVISEDMETRICEVENT);

        set(0, id);
        set(1, estimatorspec);
        set(2, df);
        set(3, metrictype);
        set(4, metricvalue);
        set(5, experimentrun);
    }
}
