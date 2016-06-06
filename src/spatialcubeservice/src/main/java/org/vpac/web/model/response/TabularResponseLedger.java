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
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.stats.Ledger;

/**
 * Unstructured data.
 */
@XmlRootElement(name = "Table")
public class TabularResponseLedger extends TabularResponse<List<Double>> {
    public TabularResponseLedger() {
        setTableType("ledger");
    }

    public void setData(Ledger ledger, Ledger unfilteredLedger, CellSize resolution) {
        List<TableColumn> columns = new ArrayList<TableColumn>();
        int i = 0;
        for (String bs : ledger.getBucketingStrategies()) {
            columns.add(new TableColumn()
                .key(i++).name("Lower Bound").type("lowerBound")
                .description("The lower bound of the grouping (value range)."));
        }
        columns.add(new TableColumn()
                .key(i++).name("Area").units("m^2").type("area")
                .portionOf("rawArea")
                .description("The area of land that matches the filters."));
        columns.add(new TableColumn()
                .key(i++).name("Unfiltered Area").units("m^2")
                .type("area")
                .description("The area of available land."));
        setColumns(columns);

        double cellArea = resolution.toDouble() * resolution.toDouble();
        List<List<Double>> rows = new ArrayList<>();
        for (Map.Entry<List<Double>, Long> entry : ledger.entrySet()) {
            List<Double> cells = new ArrayList<>(entry.getKey());
            cells.add(entry.getValue() * cellArea);
            cells.add(unfilteredLedger.get(entry.getKey()) * cellArea);
            rows.add(cells);
        }
        setRows(rows);

        // Should set min and max on the input columns too; need to get this
        // info from unfilteredLedger.
    }
}
