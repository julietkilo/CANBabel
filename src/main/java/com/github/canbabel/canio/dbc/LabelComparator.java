/**
 *  CANBabel - Translator for Controller Area Network description formats
 *  Copyright (C) 2011-2013 julietkilo and Jan-Niklas Meier
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

package com.github.canbabel.canio.dbc;

import com.github.canbabel.canio.kcd.Label;
import java.util.Comparator;

/**
 *
 * @author julietkilo
 */
class LabelComparator implements Comparator<Label> {

    @Override
    public int compare( Label l1, Label l2){
        return l1.getValue().compareTo(l2.getValue());        
    }
    
}
