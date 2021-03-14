/**
 * This class is generated by jOOQ
 */
package jooq.sqlite.gen.tables.records;


import javax.annotation.Generated;

import jooq.sqlite.gen.tables.Fitevent;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
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
public class FiteventRecord extends UpdatableRecordImpl<FiteventRecord> implements Record8<Integer, Integer, Integer, Integer, String, String, Integer, Integer> {

    private static final long serialVersionUID = -1189302286;

    /**
     * Setter for <code>FitEvent.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>FitEvent.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>FitEvent.estimatorSpec</code>.
     */
    public void setEstimatorspec(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>FitEvent.estimatorSpec</code>.
     */
    public Integer getEstimatorspec() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>FitEvent.model</code>.
     */
    public void setModel(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>FitEvent.model</code>.
     */
    public Integer getModel() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>FitEvent.df</code>.
     */
    public void setDf(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>FitEvent.df</code>.
     */
    public Integer getDf() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>FitEvent.inputColumns</code>.
     */
    public void setInputcolumns(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>FitEvent.inputColumns</code>.
     */
    public String getInputcolumns() {
        return (String) get(4);
    }

    /**
     * Setter for <code>FitEvent.labelColumns</code>.
     */
    public void setLabelcolumns(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>FitEvent.labelColumns</code>.
     */
    public String getLabelcolumns() {
        return (String) get(5);
    }

    /**
     * Setter for <code>FitEvent.experimentRun</code>.
     */
    public void setExperimentrun(Integer value) {
        set(6, value);
    }

    /**
     * Getter for <code>FitEvent.experimentRun</code>.
     */
    public Integer getExperimentrun() {
        return (Integer) get(6);
    }

    /**
     * Setter for <code>FitEvent.stageNumber</code>.
     */
    public void setStagenumber(Integer value) {
        set(7, value);
    }

    /**
     * Getter for <code>FitEvent.stageNumber</code>.
     */
    public Integer getStagenumber() {
        return (Integer) get(7);
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
    // Record8 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Integer, Integer, Integer, Integer, String, String, Integer, Integer> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Integer, Integer, Integer, Integer, String, String, Integer, Integer> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Fitevent.FITEVENT.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return Fitevent.FITEVENT.ESTIMATORSPEC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return Fitevent.FITEVENT.MODEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field4() {
        return Fitevent.FITEVENT.DF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return Fitevent.FITEVENT.INPUTCOLUMNS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return Fitevent.FITEVENT.LABELCOLUMNS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field7() {
        return Fitevent.FITEVENT.EXPERIMENTRUN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field8() {
        return Fitevent.FITEVENT.STAGENUMBER;
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
        return getModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value4() {
        return getDf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getInputcolumns();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getLabelcolumns();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value7() {
        return getExperimentrun();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value8() {
        return getStagenumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FiteventRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FiteventRecord value2(Integer value) {
        setEstimatorspec(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FiteventRecord value3(Integer value) {
        setModel(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FiteventRecord value4(Integer value) {
        setDf(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FiteventRecord value5(String value) {
        setInputcolumns(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FiteventRecord value6(String value) {
        setLabelcolumns(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FiteventRecord value7(Integer value) {
        setExperimentrun(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FiteventRecord value8(Integer value) {
        setStagenumber(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FiteventRecord values(Integer value1, Integer value2, Integer value3, Integer value4, String value5, String value6, Integer value7, Integer value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached FiteventRecord
     */
    public FiteventRecord() {
        super(Fitevent.FITEVENT);
    }

    /**
     * Create a detached, initialised FiteventRecord
     */
    public FiteventRecord(Integer id, Integer estimatorspec, Integer model, Integer df, String inputcolumns, String labelcolumns, Integer experimentrun, Integer stagenumber) {
        super(Fitevent.FITEVENT);

        set(0, id);
        set(1, estimatorspec);
        set(2, model);
        set(3, df);
        set(4, inputcolumns);
        set(5, labelcolumns);
        set(6, experimentrun);
        set(7, stagenumber);
    }
}
