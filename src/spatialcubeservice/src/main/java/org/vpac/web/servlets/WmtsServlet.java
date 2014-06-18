package org.vpac.web.servlets;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.vpac.ndg.common.datamodel.TaskState;
import org.vpac.ndg.common.datamodel.TaskType;
import org.vpac.ndg.storage.dao.BandDao;
import org.vpac.ndg.storage.dao.DatasetDao;
import org.vpac.ndg.storage.dao.JobProgressDao;
import org.vpac.ndg.storage.model.Dataset;
import org.vpac.ndg.storage.model.JobProgress;
import org.vpac.ndg.storage.util.DatasetUtil;
import org.vpac.ndg.task.Task;
import org.vpac.ndg.task.WmtsBandCreator;

public class WmtsServlet extends HttpServlet {

    private DatasetDao datasetDao;
    private BandDao bandDao;
    private JobProgressDao jobProgressDao;
    
    private DatasetUtil datasetUtil;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(config.getServletContext());
        this.datasetDao = (DatasetDao) ctx.getBean("datasetDao");
        this.bandDao = (BandDao) ctx.getBean("bandDao");
        this.jobProgressDao = (JobProgressDao) ctx.getBean("jobProgressDao");
        
        datasetUtil = new DatasetUtil();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestUrl = request.getRequestURI();
        String servletPath = request.getServletPath();
        String localAddr = request.getLocalAddr();
        String localName = request.getLocalName();
        String pathInfo = request.getPathInfo();

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 1) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.println("missing UUID for dataset</br>"
                    + "request should be in the form .../wmts/dataset UUID/timeslice UUID/band UUID/z/x/y.png "
                    + "</br> OR </br>"
                    + ".../wmts/query task UUID/z/x/y.png");
            return;
        }
        List<JobProgress> progressItems = this.jobProgressDao.search(TaskType.Query, TaskState.FINISHED, 0, 50);
        
        // get the UUID that may correspond to a dataset, band, or query result
        String uuid = pathParts[1];
        String urlRemainder = pathInfo.substring(pathInfo.indexOf(uuid) + uuid.length() + 1); // +1 to remove the /
        
        //look for a dataset
        Dataset dataset = datasetDao.retrieve(uuid);
        if (dataset != null) {
            doGetDataset(dataset, urlRemainder, request, response);
            return;
        }
        
        //dataset not found, so must be a query (or bad request)
        JobProgress progress = jobProgressDao.retrieve(uuid);
        if (progress != null) {
            doGetQuery(progress, urlRemainder, request, response);
            return;
        }
        
        //If we've made it this far then either the dataset or progress Id just doesn't exist
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter out = response.getWriter();
        out.println("UUID did not match a dataset or query task id");
       
    }

    private void doGetDataset(Dataset dataset,  String urlRemainder, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Path datasetDir = datasetUtil.getPath(dataset);
        Path wmtsDir = datasetDir.resolve(WmtsBandCreator.WMTS_TILE_DIR);
        Path resolvedWmtsTileFile = wmtsDir.resolve(urlRemainder);
        
        if (Files.notExists(resolvedWmtsTileFile)) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = response.getWriter();
            out.println("No tile");
        }
        else {
            writeFileToResponse(resolvedWmtsTileFile, response);   
        }
    }
    
    private void writeFileToResponse(Path path, HttpServletResponse response) throws IOException {
        ServletContext cntx= getServletContext();
        String mime = cntx.getMimeType(path.toString());
        if (mime == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        response.setContentType(mime);
        response.setContentLength((int)path.toFile().length());
        FileInputStream in = new FileInputStream(path.toFile());
        OutputStream out = response.getOutputStream();

        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
        out.close();
        in.close();
    }
    
    
    private void doGetQuery(JobProgress progress, String urlRemainder, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter out = response.getWriter();
        out.println("Query WMTS not implemented");
        return;
    }
    
    
    
    
    public void destroy() {
        // do nothing.
    }

}
