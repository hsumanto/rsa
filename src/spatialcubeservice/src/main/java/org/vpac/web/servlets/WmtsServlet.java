package org.vpac.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
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

public class WmtsServlet extends HttpServlet {

    private DatasetDao datasetDao;
    private BandDao bandDao;
    private JobProgressDao jobProgressDao;
    
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(config.getServletContext());
        this.datasetDao = (DatasetDao) ctx.getBean("datasetDao");
        this.bandDao = (BandDao) ctx.getBean("bandDao");
        this.jobProgressDao = (JobProgressDao) ctx.getBean("jobProgressDao");
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
            out.println("missing UUID for dataset, band or query. </br>"
                    + "request should be in the form .../wmts/UUID/z/x/y.png");
            return;
        }
        
        List<JobProgress> progressItems = this.jobProgressDao.search(TaskType.Query, TaskState.FINISHED, 0, 50);
        
        
        // get the UUID that may correspond to a dataset, band, or query result
        String uuid = pathParts[1];
        String urlRemainder = pathInfo.substring(pathInfo.indexOf(uuid)
                + uuid.length());

       
    }

    public void destroy() {
        // do nothing.
    }

}
