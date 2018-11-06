/**
 *  CANBabel - Translator for Controller Area Network description formats
 *  Copyright (C) 2011 julietkilo and Jan-Niklas Meier
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

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
public class FileList implements ListModel<File> {

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
    public File getElementAt(int index) {
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
