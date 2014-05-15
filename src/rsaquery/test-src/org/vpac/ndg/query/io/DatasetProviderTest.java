package org.vpac.ndg.query.io;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import ucar.nc2.dataset.NetcdfDataset;

@RunWith(BlockJUnit4ClassRunner.class)
public class DatasetProviderTest extends TestCase {

	@Test
	public void test_whitelistEmpty() throws IOException {
		File cwd = new File(".").getCanonicalFile();

		FileDatasetProvider fdp = new FileDatasetProvider();

		NetcdfDataset ds = fdp.open("data/input/noise.nc", cwd.getPath());
		ds.close();
	}

	@Test(expected=IOException.class)
	public void test_whitelistDeny() throws IOException {
		File cwd = new File(".").getCanonicalFile();

		FileDatasetProvider fdp = new FileDatasetProvider();
		fdp.getWhitelist().add(new File(cwd, "data/output").getPath());

		NetcdfDataset ds = fdp.open("data/input/noise.nc", cwd.getPath());
		ds.close();
	}

	@Test
	public void test_whitelistAllow() throws IOException {
		File cwd = new File(".").getCanonicalFile();

		FileDatasetProvider fdp = new FileDatasetProvider();
		fdp.getWhitelist().add(new File(cwd, "data/input").getPath());

		NetcdfDataset ds = fdp.open("data/input/noise.nc", cwd.getPath());
		ds.close();
	}

	@Test
	public void test_registry() throws IOException {
		ProviderRegistry pr = new ProviderRegistry();
		pr.addProvider(new FileDatasetProvider());

		assertEquals(FileDatasetProvider.class, pr.getProvider("file:///").getClass());

		assertEquals(true, pr.canOpen("data/input/noise.nc"));
		assertEquals(false, pr.canOpen("foo://bar"));
	}
}
