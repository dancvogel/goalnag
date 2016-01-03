package com.allegedvino.goalnag.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by dancvogel on 10/27/15.
 *
 * Represents a single Goal and all it's attributes.
 */
public class Goal implements Serializable {
    private static final long serialVersionUID = 1999L;
    private String _text;
    private boolean _completed;
    private UUID  _uuid;
    private ArrayList<Nag> _nags;

    public Goal(){
        _completed = false;
        _uuid = UUID.randomUUID();
        _nags = new ArrayList<>();
    }

    public String getText() { return _text; }
    public void setText(String value) { _text = value; }

    public boolean getCompleted() { return _completed; }
    public void setCompleted(boolean value) { _completed = value; }

    public UUID getId() { return _uuid; }

    public Nag getNagAt(int index) { return _nags.get(index); }
    public Nag getNag(UUID id) {
        for(int i = 0; i < _nags.size(); i++) {
            if(_nags.get(i).getId().equals(id)){
                return _nags.get(i);
            }
        }

        return null;
    }

    public void addNag(Nag n) {
        n.setGoalId(_uuid);
        _nags.add(n);
    }

    public void removeNagAt(int index) {
        _nags.remove(index);
    }
    public void removeNag(UUID id) {
        for(int i = 0; i < _nags.size(); i++) {
            if(_nags.get(i).getId().equals(id)){
                _nags.remove(i);
                break;
            }
        }
    }

    public int getNagCount() {
        return _nags.size();
    }

    public String toString() {
        return _text;
    }
}
