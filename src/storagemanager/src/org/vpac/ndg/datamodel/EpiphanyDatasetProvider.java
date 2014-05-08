package org.vpac.ndg.datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.vpac.ndg.common.datamodel.CellSize;
import org.vpac.ndg.query.DatasetUtils;
import org.vpac.ndg.query.GridUtils;
import org.vpac.ndg.query.coordinates.GridProjected;
import org.vpac.ndg.query.coordinates.QueryCoordinateSystem;
import org.vpac.ndg.query.coordinates.TimeAxis;
import org.vpac.ndg.query.io.DatasetMetadata;
import org.vpac.ndg.query.io.DatasetProvider;
import org.vpac.ndg.query.math.BoxReal;
import org.vpac.ndg.storage.model.Dataset;

import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

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


/**
 * Gives access to datasets that are stored in an Epiphany.
 * @author Jin Park
 */
public class EpiphanyDatasetProvider implements DatasetProvider {

	private DatasetUtils datasetUtils;
	private GridUtils gridUtils;

	public EpiphanyDatasetProvider() {
		datasetUtils = new DatasetUtils();
		gridUtils = new GridUtils();
	}

	
	@Override
	public boolean canOpen(String uri) {
		URI parsedUri;
		try {
			parsedUri = new URI(uri);
		} catch (URISyntaxException e) {
			return false;
		}

		String scheme = parsedUri.getScheme();
		if (scheme == null)
			return false;
		if (!scheme.toLowerCase().equals("epiphany"))
			return false;
		
		return true;
	}

	// Dataset identifier looks like "epiphany:id"
	private static final Pattern DATASET_PATTERN = Pattern.compile("^([^/]+)");	
	
	@Override
	public NetcdfDataset open(String uri, String referential,
			BoxReal boundsHint, DateTime minTimeHint, DateTime maxTimeHint,
			List<String> bands) throws IOException {
		
		String epiphanyHost = "127.0.0.1";
		String epiphanyPort = "8000";
		String datasetId = findDataset(uri, referential);
		String url = "http://" + epiphanyHost + ":" + epiphanyPort + "/map/wcs/" + datasetId + "?LAYERS=" + datasetId + "&FORMAT=application%2Fx-netCDF&SERVICE=WCS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&YEAR=none&QUERY=none&GEOMETRY=none&VIEWMETHOD=none&COLOURSCHEME=Choose%20a%20color%20scheme&LEGENDEXTENT=global&NUMBERFILTERS=none&VISTYPE=none&SRS=EPSG%3A3577&BBOX=" + boundsHint.getMin().getX() + "," + boundsHint.getMin().getY() + "," + boundsHint.getMax().getX() + "," + boundsHint.getMax().getY() + "&WIDTH=5000&HEIGHT=5000";
		URL connectionUrl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) connectionUrl.openConnection();
		connection.setRequestMethod("GET");
		
//		int responseCode = connection.getResponseCode();
		InputStream in = connection.getInputStream();
		String uuid = UUID.randomUUID().toString();
		OutputStream out = new FileOutputStream("output_" + uuid + ".nc");
		
		byte[] buffer = new byte[1024];
		int bytesRead;
		while((bytesRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}
		out.flush();
		out.close();
		
//		System.out.println("RESPONSE:" + responseCode);
		NetcdfDataset returnDs = new NetcdfDataset().openDataset("/home/parallels/git/rsa/src/rsaworkers/output_" + uuid + ".nc");
		return returnDs;
	}
	
	public NetcdfDataset open(String uri, String referential)
			throws IOException {

		URI parsedUri;
		try {
			parsedUri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new IOException("Could not open dataset", e);
		}

		String path = parsedUri.getPath();
		File resolvedFile = resolve(referential, path);
		return NetcdfDataset.openDataset(resolvedFile.getAbsolutePath());
	}
	
	protected File resolve(String referential, String path) {
		File file = new File(path);
		if (file.isAbsolute())
			return file;
		else
			return new File(referential, path);
	}

	@Override
	public DatasetMetadata queryMetadata(String uri, String referential)
			throws IOException {
		
		String url = "file:///home/parallels/git/rsa/src/rsaworkers/data/input/blur.nc";
		NetcdfDataset ds = open(url, referential);
		try {
			DatasetMetadata meta = new DatasetMetadata();

			ds.enhance();
			if (ds.getCoordinateSystems().size() == 0) {
				throw new IOException(String.format(
						"Dataset %s has no coordinate system.", uri));
			}

			GridProjected grid = gridUtils.findBounds(ds);
			TimeAxis timeAxis = datasetUtils.findTimeCoordinates(ds);
			meta.setCsys(new QueryCoordinateSystem(grid, timeAxis));

			List<Variable> vars = ds.getVariables();
			List<String> varNames = new ArrayList<String>(vars.size());
			for (Variable var : vars) {
				varNames.add(var.getFullName());
			}
			meta.setVariables(varNames);

			return meta;

		} finally {
			ds.close();
		}
	}
	
	protected String findDataset(String uri, String referential)
			throws IOException {

		URI parsedUri;
		try {
			parsedUri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new IOException("Could not open dataset", e);
		}

		String path = parsedUri.getSchemeSpecificPart();
		Matcher matcher = DATASET_PATTERN.matcher(path);
		if (!matcher.matches()) {
			throw new FileNotFoundException(
					String.format("Invalid dataset name %s", path));
		}
		return matcher.group(1);
	}
	
}
