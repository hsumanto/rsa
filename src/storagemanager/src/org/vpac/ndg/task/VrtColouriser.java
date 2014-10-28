package org.vpac.ndg.task;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vpac.ndg.application.Constant;
import org.vpac.ndg.colour.NamedPalette;
import org.vpac.ndg.colour.Palette;
import org.vpac.ndg.exceptions.TaskException;
import org.vpac.ndg.exceptions.TaskInitialisationException;
import org.vpac.ndg.storagemanager.GraphicsFile;

/**
 * Class reads a VRT file and embeds a colour table in it. Assumes the dataset is in the
 * byte range (only 256 colour entries are added), and that 0 is the nodata value (this is
 * set to be transparent).
 * @author lachlan
 *
 */
public class VrtColouriser extends Task {

    final private Logger log = LoggerFactory.getLogger(VrtColouriser.class);

    public static final int NUMBER_OF_COLOURS = 256;
    public static final String INSERT_BEFORE_DEFAULT = "<ColorInterp>";
    
    private String insertBefore;
    
    private GraphicsFile source;
    private GraphicsFile target;
    private String palette;

    public VrtColouriser() {
        this("Adding Colour Table to VRT file");
    }
    
    public VrtColouriser(String description) {
        super(description);
        this.insertBefore = INSERT_BEFORE_DEFAULT;
    }

    @Override
    public void initialise() throws TaskInitialisationException {
        if (getSource() == null) {
            throw new TaskInitialisationException(getDescription(),
                    Constant.ERR_NO_INPUT_IMAGES);
        }

        if (getTarget() == null) {
            throw new TaskInitialisationException(getDescription(),
                    Constant.ERR_TARGET_DATASET_NOT_SPECIFIED);
        }
        
        if (!getSource().getFileLocation().toFile().toString().endsWith(Constant.EXT_VRT)) {
            throw new TaskInitialisationException(getDescription(),
                    "Source must be a VRT file");
        }
        
        if (!getTarget().getFileLocation().toFile().toString().endsWith(Constant.EXT_VRT)) {
            throw new TaskInitialisationException(getDescription(),
                    "Target must be a VRT file");
        }

    }
    
    /**
     * tests weather the xml should be included at this line
     * @param line
     * @return
     */
    private boolean isInsertionPoint(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith(insertBefore);
    }
    
    @Override
    public void execute() throws TaskException {
        if (source == null) {
            // Can't work with zero input files. Just return; the output list
            // will not be populated. This is not an error.
            log.debug("Source is empty; will not create a VRT.");
            return;
        }
        
        //delete the target file if it exists (as we'll overwrite it anyway)
        if (target.getFileLocation().toFile().exists()) {
            boolean isDeleted = target.getFileLocation().toFile().delete();
            if (!isDeleted) {
                throw new TaskException("Could not delete target VRT file " + target.getFileLocation().toString());
            }
        }

        log.info("Using palette {}", palette);

        //Read all the lines in the source VRT file, this shouldn't be large 
        List<String> lines;
        try {
            lines = Files.readAllLines(source.getFileLocation(), Charset.defaultCharset());
        } catch (IOException e) {
            TaskException te = new TaskException("failed to read source VRT file " + source.getFileLocation().toString(), e);
            throw te;
        }
        
        BufferedWriter writer = null;
        try {
            File output = target.getFileLocation().toFile();

            writer = new BufferedWriter(new FileWriter(output));
            
            //write each line of the source to the target, and add in some xml along the way...
            for (String line: lines) {
                if (isInsertionPoint(line)) {
                    String newXml = getColourTable();
                    writer.write(newXml);
                    writer.newLine();
                }
                writer.write(line);
                writer.newLine();
            }

        } catch (Exception e) {
            TaskException te = new TaskException("failed to write target VRT file " + target.getFileLocation().toString(), e);
            throw te;
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
        
        

    }

    /**
     * gets the colour table xml that will be embedded into the Target VRT file
     * @return
     */
    public String getColourTable() {
        
        Color[] colours = getColours();
        
        StringBuilder sb = new StringBuilder();
        sb.append("<ColorTable>" + System.lineSeparator());
        
        for (Color c: colours) {
            sb.append("    " + colourToVrtXml(c) + System.lineSeparator());
        }
        
        sb.append("</ColorTable>" + System.lineSeparator());
        return sb.toString();
    }

    protected Color[] getColours () {
        Palette p = NamedPalette.get(palette, 1, 255);
        Color[] cs = new Color[NUMBER_OF_COLOURS];

        cs[0] = new Color(0, 0, 0, 0);

        for (int i = 1; i < NUMBER_OF_COLOURS; i++) {
            cs[i] = p.get(i);
        }

        return cs;
    }

    /**
     * converts a java color object to a VRT formatted colortable line ( <Entry c1="0" c2="0" c3="0" c4="0" /> )
     * @param colour
     * @return
     */
    private String colourToVrtXml(Color colour) {
        
        return String.format("<Entry c1=\"%d\" c2=\"%d\" c3=\"%d\" c4=\"%d\" />", 
                             colour.getRed(), 
                             colour.getGreen(), 
                             colour.getBlue(), 
                             colour.getAlpha());   
    }
    
    @Override
    public void rollback() {
        // nothing to do
    }

    @Override
    public void finalise() {
        // nothing to do
    }

    public String getPalette() {
        return palette;
    }

    public void setPalette(String palette) {
        this.palette = palette;
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

    
    
    
    public String getInsertBefore() {
        return insertBefore;
    }

    /**
     * the colour table xml data will be inserted before a line starting with this string (whitespace ignored)
     * @param insertBefore
     */
    public void setInsertBefore(String insertBefore) {
        this.insertBefore = insertBefore;
    }

    /**
     * simple test main method. Displays the xml that would be added to a VRT file
     * @param args
     */
    public static void main (String[] args) {
        
        VrtColouriser colouriser = new VrtColouriser();
        
        System.out.println(colouriser.getColourTable());
        System.out.println();
        
        colouriser.setPalette("");
        System.out.println(colouriser.getColourTable());
        System.out.println();
        
    }
    
    
}
