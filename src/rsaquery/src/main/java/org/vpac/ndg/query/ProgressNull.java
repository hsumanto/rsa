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
 * Copyright 2014 VPAC Innovations
 */

package org.vpac.ndg.query;

/**
 * A progress object that reports to no one.
 * @author Alex Fraser
 */
public class ProgressNull implements Progress {

	@Override
	public void setNsteps(int nsteps) {}

	@Override
	public void setStep(int step, String message) {}

	@Override
	public void finishedStep() {}

	@Override
	public void setTotalQuanta(long totalQuanta) {}

	@Override
	public void addProcessedQuanta(long volume) {}

	@Override
	public void finished() {}

}
