package com.allegedvino.goalnag.data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by dancvogel on 11/7/15.
 *
 * Represents a single nag instance.
 */
public class Nag implements Serializable {
    public static final String EXTRA_NAG = "com.allegedvino.goalnag.EXTRA_NAG";
    public static final String EXTRA_NAG_ID = "com.allegedvino.goalnag.EXTRA_NAG_ID";

    private static final long serialVersionUID = 2000L;
    private Calendar _time;
    private UUID _uuid;
    private UUID _goalId;

    public Nag() {
        _uuid = UUID.randomUUID();
    }

    public void setTime(Calendar value) { _time = value; }
    public Calendar getTime() { return _time; }

    public void setId(UUID value) { _uuid = value; }
    public UUID getId() { return _uuid; }

    public void setGoalId(UUID value) { _goalId = value; }
    public UUID getGoalId() {return _goalId; }
}
