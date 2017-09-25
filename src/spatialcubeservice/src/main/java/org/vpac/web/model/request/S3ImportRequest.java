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

package org.vpac.web.model.request;

import java.util.ArrayList;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
/*
  Used for S3 tile import requests
 */
public class S3ImportRequest implements java.io.Serializable {

  @NotBlank
  private String bucket;
  @NotBlank
  private String dataset;
  @NotBlank
  private String resolution;
  @NotBlank
  private String timeslice;
  @NotBlank
  private String band;
  @NotBlank
  private String type;
  @NotBlank
  private String nodata;
  @NotBlank
  private String precision;
  @NotBlank
  private String extension;

  @NotNull
  @Size(min = 1)
  private ArrayList<String> files;
  @NotNull
  @Size(min = 2, max = 2)
  private ArrayList<Integer> xlims;
  @NotNull
  @Size(min = 2, max = 2)
  private ArrayList<Integer> ylims;

  @NotNull
  private Boolean continuous;
  @NotNull
  private Boolean metadata;

  public String getBucket() {
    return this.bucket;
  }

  public String getDataset() {
    return this.dataset;
  }

  public String getResolution() {
    return this.resolution;
  }

  public String getTimeslice() {
    return this.timeslice;
  }

  public String getBand() {
    return this.band;
  }

  public String getNodata() {
    return this.nodata;
  }

  public String getType() {
    return this.type;
  }

  public String getPrecision() {
    return this.precision;
  }

  public String getExtension() {
    return this.extension;
  }

  public ArrayList<String> getFiles() {
    return this.files;
  }

  public ArrayList<Integer> getXlims() {
    return this.xlims;
  }

  public ArrayList<Integer> getYlims() {
    return this.ylims;
  }

  public Boolean isContinuous() {
    return this.continuous;
  }

  public Boolean isMetadata() {
    return this.metadata;
  }
}
