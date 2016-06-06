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
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlRootElement;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Bucket;
import org.vpac.ndg.query.stats.Cats;
import org.vpac.ndg.query.stats.Hist;
import org.vpac.ndg.query.stats.Stats;

/**
 * Data suitable for displaying as a bar chart. Each row has an ID.
 */
@XmlRootElement(name = "Table")
public class TabularResponseCategorical extends TabularResponse<TableRow> {
    public TabularResponseCategorical() {
        setTableType("categories");

        List<TableColumn> columns = new ArrayList<TableColumn>();
        columns.add(new TableColumn()
                .key(0).name("Category").type("category")
                .description("The category of the data."));
        columns.add(new TableColumn()
                .key(1).name("Area").units("m^2").type("area")
                .portionOf("rawArea")
                .description("The area of land that matches the filters."));
        columns.add(new TableColumn()
                .key(2).name("Unfiltered Area").units("m^2").type("area")
                .description("The area of available land."));
        setColumns(columns);
    }

    public void setRows(Cats cats, Cats unfilteredCats, CellSize resolution) {
        double cellArea = resolution.toDouble() * resolution.toDouble();
        List<TableRow> rows = new ArrayList<TableRow>();
        for (Entry<Integer, Hist> entry : cats.getCategories().entrySet()) {
            TableRow row = new TableRow();
            row.setId(entry.getKey());

            Hist hist = entry.getValue();
            Stats s = hist.summarise();
            row.setArea(s.getCount() * cellArea);

            Hist unfilteredHist = unfilteredCats.get(entry.getKey());
            if (unfilteredHist != null) {
                s = unfilteredHist.summarise();
                row.setRawArea(s.getCount() * cellArea);
            }

            rows.add(row);
        }
        setRows(rows);
    }

    public void setRows(Hist hist, Hist unfilteredHist, CellSize resolution) {
        double cellArea = resolution.toDouble() * resolution.toDouble();
        List<TableRow> rows = new ArrayList<TableRow>();
        for (Bucket b : hist.getBuckets()) {
            TableRow row = new TableRow();
            row.setId(b.getLower());

            Stats s = b.getStats();
            row.setArea(s.getCount() * cellArea);

            Bucket ub = unfilteredHist.getBucket(b.getLower());
            if (ub != null) {
                s = ub.getStats();
                row.setRawArea(s.getCount() * cellArea);
            }

            rows.add(row);
        }
        setRows(rows);
    }
}
