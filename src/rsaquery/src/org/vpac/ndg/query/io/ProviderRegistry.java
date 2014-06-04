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

package org.vpac.ndg.query.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.vpac.ndg.query.math.BoxReal;

import ucar.nc2.dataset.NetcdfDataset;

/**
 * <p>
 * A provider that contains uses other providers to open files. The
 * first-registered provider that {@link #canOpen(String) can open} a dataset
 * will be used.
 * </p>
 *
 * <p>
 * This is a Singleton; use {@link ProviderRegistry#getInstance() getInstance}.
 * </p>
 *
 * <p>
 * Initially the registry contains only a {@link FileDatasetProvider}, which
 * can open any local file. For security reasons you should consider either
 * {@link #clearProivders() removing it} or configuring it with a
 * {@link FileDatasetProvider#getWhitelist() whitelist}. Other providers can be
 * {@link #addProvider(DatasetProvider) added}.
 * </p>
 *
 * @author Alex Fraser
 */
public class ProviderRegistry implements DatasetProvider {

	private static ProviderRegistry instance;
	static {
		instance = new ProviderRegistry();
		instance.addProvider(new FileDatasetProvider());
	}

	public static ProviderRegistry getInstance() {
		return instance;
	}

	private List<DatasetProvider> providers;

	protected ProviderRegistry() {
		providers = new ArrayList<DatasetProvider>();
	}

	/**
	 * Register a new provider. Providers will be tested in the order that they
	 * are added.
	 * @param p The provider to register.
	 */
	public void addProvider(DatasetProvider p) {
		providers.add(p);
	}

	/**
	 * Remove all registered providers.
	 */
	public void clearProivders() {
		providers.clear();
	}

	/**
	 * @param uri A URI of a dataset.
	 * @return The provider that would be used to open that dataset.
	 */
	public DatasetProvider getProvider(String uri) {
		for (DatasetProvider p : providers) {
			if (p.canOpen(uri))
				return p;
		}
		return null;
	}

	public void removeProvider(DatasetProvider p) {
		providers.remove(p);
	}

	@Override
	public boolean canOpen(String uri) {
		DatasetProvider p = getProvider(uri);
		return p != null;
	}

	@Override
	public NetcdfDataset open(String uri, String referential,
			BoxReal boundsHint, DateTime minTimeHint, DateTime maxTimeHint,
			List<String> bands) throws IOException {

		DatasetProvider p = getProvider(uri);
		if (p == null) {
			throw new IOException(String.format(
					"No registered IO provider can open %s.", uri));
		}

		return p.open(uri, referential, boundsHint, minTimeHint, maxTimeHint,
				bands);
	}

	@Override
	public DatasetMetadata queryMetadata(String uri, String referential)
			throws IOException {

		DatasetProvider p = getProvider(uri);
		if (p == null) {
			throw new IOException(String.format(
					"No registered IO provider can open %s.", uri));
		}

		return p.queryMetadata(uri, referential);
	}

}
