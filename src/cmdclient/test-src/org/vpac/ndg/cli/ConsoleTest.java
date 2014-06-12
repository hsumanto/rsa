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

package org.vpac.ndg.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.vpac.ndg.cli.Client;

/**
 * Allows the Client to be run using regular command line arguments. stdout and
 * stderr are redirected to output streams, so their contents can be verified by
 * a test case.
 */
public class ConsoleTest {

	protected ByteArrayOutputStream output;
	protected ByteArrayOutputStream errput;
	PrintStream origStdErr;
	PrintStream origStdOut;
	protected Client client;
	protected int errcode;

	public static class ClientExitException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private int errorCode;

		public ClientExitException() {
			super();
		}

		public ClientExitException(int errorCode, Throwable cause) {
			super(String.format("Command exited with error code %d",
					errorCode), cause);
			this.errorCode = errorCode;
		}

		public ClientExitException(int errorCode) {
			super(String.format("Command exited with error code %d",
					errorCode));
			this.errorCode = errorCode;
		}

		public ClientExitException(Throwable cause) {
			super(cause);
		}

		public int getErrorCode() {
			return errorCode;
		}

	}

	@Before
	public void setUp() {
		// Redirect stdout and stderr.
		origStdOut = System.out;
		origStdErr = System.err;
		output = new ByteArrayOutputStream();
		errput = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output));
		System.setErr(new PrintStream(errput));

		// Capture error code.
		client = new Client(Paths.get(".")) {
			protected void exit(int err) {
				throw new ClientExitException(err);
			};
		};
	}

	/**
	 * Executes the command, and expects it to succeed. This swallows the exit
	 * exception only if the exit code is 0.
	 * @param args The arguments to execute in the command line client.
	 */
	protected void execute(String... args) throws ClientExitException {
		try {
			errcode = 0;
			client.execute(args);
			throw new AssertionError("Command should have exited.", null);
		} catch (ClientExitException e) {
			errcode = e.getErrorCode();
			if (errcode != 0) {
				throw new AssertionError("Expected error code of zero.", e);
			}
		}
	}

	/**
	 * Executes the command, and expects it to fail. This swallows the exit
	 * exception if the exit code is non-zero.
	 * @param args The arguments to execute in the command line client.
	 */
	protected void executeWithDoom(String... args) throws ClientExitException {
		try {
			errcode = 0;
			client.execute(args);
			throw new AssertionError("Command should have exited.", null);
		} catch (ClientExitException e) {
			errcode = e.getErrorCode();
			if (errcode == 0) {
				throw new AssertionError("Expected non-zero error code.", e);
			}
		}
	}

	@After
	public void tearDown() {
		// Redirect stdout and stderr.
		System.setOut(origStdOut);
		System.setErr(origStdErr);
		System.out.print(output);
		System.err.print(errput);

		try {
			output.close();
		} catch (IOException e) {
			System.err.println("Failed to close output stream.");
		}

		try {
			errput.close();
		} catch (IOException e) {
			System.err.println("Failed to close output stream.");
		}
	}

}
