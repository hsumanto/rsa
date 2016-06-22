/*
 * This file is part of the Raster Storage Archive (RSA).
 *
 * The RSA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * The RSA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the RSA.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2016 VPAC Innovations
 * http://vpac-innovations.com.au/
 */

package org.vpac.ndg.task;

/**
 * defines an interface that allows a child object to communicate progress without
 * being too aware of parent.
 */
public interface IProgressCallback {
    /** value between 0 and 100 indicating current progress being reported */
    public void progressUpdated(double progress);
}
