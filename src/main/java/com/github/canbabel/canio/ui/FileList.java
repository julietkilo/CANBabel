
package com.github.canbabel.canio.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class FileList implements ListModel{

    private List<File> files = new ArrayList<File>();
    private Set<ListDataListener> listeners = new HashSet<ListDataListener>();

    public void addFile(File f) {
        files.add(f);
        int i = files.size();

        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, i, i);
        for(ListDataListener l : listeners) {
            l.intervalRemoved(e);
        }
    }

    public void remove(int i) {
        files.remove(i);

        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, i, i);
        for(ListDataListener l : listeners) {
            l.intervalRemoved(e);
        }
    }

    public List<File> getFiles() {
        return Collections.unmodifiableList(files);
    }

    @Override
    public int getSize() {
        return files.size();
    }

    @Override
    public Object getElementAt(int index) {
        return files.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    public void clear() {
        int oldsize = files.size();
        files.clear();

        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, oldsize-1);
        for(ListDataListener l : listeners) {
            l.intervalRemoved(e);
        }
    }

}
