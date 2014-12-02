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

package org.vpac.ndg.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.CommandUtil;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.common.StringUtils;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.rasterservices.ProcessException;
import org.vpac.ndg.storagemanager.GraphicsFile;

/**
 * This task could be used to translate an image into a different format.
 * @author hsumanto
 * @author lachlan
 *
 */
public class Translator extends Task {

	final private Logger log = LoggerFactory.getLogger(Translator.class);

	private GraphicsFile source;
	private GraphicsFile target;
	private String nodata;
	private String outputType;
	private String expand;
	private List<ScalarReceiver<Double>> scale;
	private List<String> command;

	private CommandUtil commandUtil;

	public Translator() {
		super(Constant.TASK_DESCRIPTION_TRANSLATOR);
		commandUtil = new CommandUtil();
	}

	public Translator(String description) {
		super(description);
		commandUtil = new CommandUtil();
	}

	@Override
	public void initialise() throws TaskInitialisationException {
		// Perform validation on source and target fields;
		if(getSource() == null) {
			throw new TaskInitialisationException(getDescription(), Constant.ERR_SOURCE_DATASET_NOT_SPECIFIED);
		}

		if(getTarget() == null) {
			throw new TaskInitialisationException(getDescription(), Constant.ERR_TARGET_DATASET_NOT_SPECIFIED);
		}
	}

	public void prepareProjectionCommand() {
		// Set the target projection when applicable
		if(target.getEpsgId() > -1) {
			command.add("-a_srs");
			command.add("EPSG:" + target.getEpsgId());
		} else {
			if(target.getSrs() != null && !target.getSrs().isEmpty()) {
				command.add("-a_srs");
				command.add(target.getSrs());
			}
		}
	}

	public void initialiseCommand() {
		// Initialize command parameter list
		command = new ArrayList<String>();

		command.add("gdal_translate");
	}

	public void prepareCommand() {
		// Output format (e.g. netCDF, GTiff)
		command.add("-of");
		command.add(target.getFormat().toGdalString());

		// Override nodata metadata when applicable
		if (nodata != null && !nodata.isEmpty()) {
			command.add("-a_nodata");
			command.add(nodata);
		}

		// set the output type if specified
		if (outputType != null) {
		    command.add("-ot");
		    command.add(outputType);
		}
		
		//set the expand type
		if (expand != null) {
		    command.add("-expand");
		    command.add(expand);
		}
		
		// set the scale if specified
		if (scale != null) {
	        command.add("-scale");
	        for (ScalarReceiver<Double> d: scale) {
	            command.add(d.get().toString());
	        }
		}
		
		// Add extra options such as Mosaicking options, Memory management options
		addExtraOptions(command);

		// Creation options (e.g. compression)
		for (String co : target.getFormat().getCreationOptions()) {
			command.add("-co");
			command.add(co);
		}

		// Input file
		command.add(source.getFileLocation().toString());

		// Output file
		command.add(target.getFileLocation().toString());
	}

	public void executeCommand(Collection<String> actionLog) throws TaskException {
		actionLog.add(StringUtils.join(command, " "));
		try {
			commandUtil.start(command);
		} catch (ProcessException | InterruptedException | IOException e) {
			log.error("Command failed: {}", e.getMessage());
			log.error("Command was: {}", command);
			throw new TaskException(getDescription(), e);
		} 
	}

	@Override
	public void execute(Collection<String> actionLog) throws TaskException {
		if(isCheckSource()) {
			if(!getSource().exists()) {
				// During import if no input image then throws exception
				throw new TaskException(getDescription(), "Source file not exist:\n" + getSource().getFileLocation());
			}	
		}
		else {
			if(!getSource().exists()) {
				// During export if no input image, still allow translation task to finish.
				// As there is no input images, gdal_translate won't be performed and
				// therefore the target image won't exist.
				// An example: when timeslice contains no tiles during export.
				log.debug("Source is empty; will not translate.");
				return;
			}
		}

		initialiseCommand();
		prepareCommand();
		// Set projection if applicable
		prepareProjectionCommand();

		executeCommand(actionLog);
	}

	@Override
	public void rollback() {
		// Remove the translated image from temporary storage
		if(target.deleteIfExists()) {
			log.trace("Deleted {}", target);
		}
	}

	@Override
	public void finalise() {
		if(isCleanupSource()) {
			// Remove the source images from upload or temporary storage
			if(source.deleteIfExists()) {
				log.trace("Deleted {}", source);
			}
		}

		if(isCleanupTarget()) {
			// Remove the transformed image from temporary storage
			if(target.deleteIfExists()) {
				log.trace("Deleted {}", target);
			}
		}
	}

	public void addExtraOptions(List<String> args) {
		// Memory management options
		args.add("--config");
		args.add("GDAL_CACHEMAX");
		args.add(Constant.GDAL_CACHEMAX_IN_MB);
	}

	public GraphicsFile getSource() {
		return source;
	}

	public void setSource(GraphicsFile source) {
		this.source = source;
	}

	public GraphicsFile getTarget() {
		return target;
	}

	public void setTarget(GraphicsFile target) {
		this.target = target;
	}
	
	
	
	
	public List<ScalarReceiver<Double>> getScale() {
        return scale;
    }

	/**
	 * sets the -scale argument to GDAL, this is used to scale the pixel values (ie; [src_min src_max [dst_min dst_max]] )
	 * @param scale
	 */
    public void setScale(List<ScalarReceiver<Double>> scale) {
        this.scale = scale;
    }

    
    
    public String getExpand() {
        return expand;
    }

    /**
     * sets the '-expand' command line option passed to gdal. Must be gray|rgb|rgba
     * @param expand
     */
    public void setExpand(String expand) {
        this.expand = expand;
    }

    public String getOutputType() {
        return outputType;
    }
	
	/**
	 * sets the '-ot' argument passed to GDAL translate, must be one of 
	 * Byte/Int16/UInt16/UInt32/Int32/Float32/Float64/
     * CInt16/CInt32/CFloat32/CFloat64
	 * @param outputType
	 */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getNodata() {
		return nodata;
	}

	public void setNodata(String nodata) {
		this.nodata = nodata;
	}

	public List<String> getCommand() {
		return command;
	}
}
