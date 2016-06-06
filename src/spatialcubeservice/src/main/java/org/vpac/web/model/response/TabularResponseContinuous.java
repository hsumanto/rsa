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
 * Copyright 2013 CRCSI - Cooperative Research Centre for Spatial Information
 * http://www.crcsi.com.au/
 */

package org.vpac.web.model.response;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Bucket;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.query.stats.Stats;

/**
 * Data suitable for displaying as a histogram. Each row has a lower and
 * upper bound.
 */
@XmlRootElement(name = "Table")
public class TabularResponseContinuous extends TabularResponse<TableRowRanged> {
    public TabularResponseContinuous() {
        setTableType("histogram");

        List<TableColumn> columns = new ArrayList<TableColumn>();
        columns.add(new TableColumn()
                .key(0).name("Lower Bound").type("lowerBound")
                .description("The lower bound of the grouping (value range)."));
        columns.add(new TableColumn()
                .key(1).name("Upper Bound").type("upperBound")
                .description("The upper bound of the grouping (value range)."));
        columns.add(new TableColumn()
                .key(2).name("Area").units("m^2").type("area")
                .portionOf("rawArea")
                .description("The area of land that matches the filters."));
        columns.add(new TableColumn()
                .key(3).name("Unfiltered Area").units("m^2")
                .type("area")
                .description("The area of available land."));
        setColumns(columns);
    }

    public void setRows(Hist hist, Hist unfilteredHist, CellSize resolution) {
        double cellArea = resolution.toDouble() * resolution.toDouble();
        List<TableRowRanged> rows = new ArrayList<TableRowRanged>();
        for (Bucket b : hist.getBuckets()) {
            TableRowRanged row = new TableRowRanged();
            row.setLower(b.getLower());
            row.setUpper(b.getUpper());

            Stats s = b.getStats();
            row.setArea(s.getCount() * cellArea);

            Bucket ub = unfilteredHist.getBucket(b.getLower());
            if (ub != null) {
                s = ub.getStats();
                row.setRawArea(s.getCount() * cellArea);
            }

            rows.add(row);
        }
        this.setRows(rows);

        // Set min and max of value column. These can't be determined from
        // the data in the rows because it is grouped into buckets.
        Stats s = unfilteredHist.summarise();
        getColumns().get(0)
            .min(s.getMin())
            .max(s.getMax());
    }
}
