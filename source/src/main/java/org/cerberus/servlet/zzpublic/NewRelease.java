/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.servlet.zzpublic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.IProjectService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.crud.service.impl.ApplicationService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.crud.service.impl.ProjectService;
import org.cerberus.crud.service.impl.UserService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.version.Infos;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author vertigo
 */
@WebServlet(name = "NewRelease", urlPatterns = {"/NewRelease"})
public class NewRelease extends HttpServlet {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger("NewRelease");

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String charset = request.getCharacterEncoding();

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createPublicCalls("/NewRelease", "CALL", "NewReleaseV0 called : " + request.getRequestURL(), request);

        IApplicationService MyApplicationService = appContext.getBean(ApplicationService.class);
        IUserService MyUserService = appContext.getBean(UserService.class);
        IProjectService MyProjectService = appContext.getBean(ProjectService.class);

        // Parsing all parameters.
        String application = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("application"), "", charset);
        String release = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("release"), "", charset);
        String project = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("project"), "", charset);
        String ticket = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("ticket"), "", charset);
        String bug = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("bug"), "", charset);
        String subject = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("subject"), "", charset);
        String owner = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("owner"), "", charset);
        String link = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("link"), "", charset);
        // Those Parameters could be used later when Cerberus send the deploy request to Jenkins. 
        String jenkinsbuildid = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("jenkinsbuildid"), "", charset);
        String mavengroupid = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("mavengroupid"), "", charset);
        String mavenartifactid = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("mavenartifactid"), "", charset);
        String mavenversion = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("mavenversion"), "", charset);

        String helpMessage = "\nThis servlet is used to create or update a release entry in a 'NONE' build and 'NONE' revision.\n\nParameter list :\n"
                + "- application [mandatory] : the application that produced the release. This parameter must match the application list in Cerberus. [" + application + "]\n"
                + "- release : release number or svn number. This should be unique at the application level. 2 calls on the same application and release will update the other parameters on the same entry. [" + release + "]\n"
                + "- project : Project reference. [" + project + "]\n"
                + "- ticket : Ticket Reference. [" + ticket + "]\n"
                + "- bug : Bug reference. [" + bug + "]\n"
                + "- subject : A short description of the change. [" + subject + "]\n"
                + "- owner : User name of the developper/ person who did the commit. [" + owner + "]\n"
                + "- link : URL Link on detail documentation on the release. [" + link + "]\n\n"
                + "The following optional parameters could be used later when Cerberus send the deploy request to Jenkins.\n"
                + "- jenkinsbuildid : Jenkins Build ID. [" + jenkinsbuildid + "]\n"
                + "- mavengroupid : Maven Group ID. [" + mavengroupid + "]\n"
                + "- mavenartifactid : Maven Artifact ID. [" + mavenartifactid + "]\n"
                + "- mavenversion : Maven Version. [" + mavenversion + "]\n";

        DatabaseSpring database = appContext.getBean(DatabaseSpring.class);

        Connection connection = database.connect();
        try {

            boolean error = false;

            // Checking the parameter validity. If application has been entered, does it exist ?
            if (!application.equalsIgnoreCase("") && !MyApplicationService.exist(application)) {
                out.println("Error - Application does not exist  : " + application);
                error = true;
            }
            if (application.equalsIgnoreCase("")) {
                out.println("Error - Parameter application is mandatory.");
                error = true;
            }

            // Checking the parameter validity. If owner has been entered, does it exist ?
            if (!owner.equalsIgnoreCase("")) {
                if (MyUserService.isUserExist(owner)) {
                    owner = MyUserService.findUserByKey(owner).getLogin(); // We get the exact name from Cerberus.
                } else {
                    out.println("Warning - User does not exist : " + owner);
                }
            }

            // Checking the parameter validity. If project has been entered, does it exist ?
            if (!project.equalsIgnoreCase("") && !MyProjectService.exist(project)) {
                out.println("Warning - Project does not exist : " + project);
            }

            // Starting the database update only when no blocking error has been detected.
            if (error == false) {

                // In case the bugID is not defined, we try to guess it from the subject. should be between # and a space or CR.
                if (StringUtil.isNullOrEmpty(bug)) {
                    String[] columns = subject.split("#");
                    if (columns.length >= 2) {
                        for (int i = 1; i < columns.length; i++) {
                            String[] columnsbis = columns[i].split(" ");
                            if (columnsbis.length >= 1) {
                                if (!columnsbis[0].contains(";")) { // Bug number should not include ;
                                    bug = columnsbis[0];
                                }
                            }
                        }
                    }
                }

                // Transaction and database update.
                // Duplicate entry Verification. On the build/relivion not yet assigned (NONE/NONE),
                //  we verify that the application + release has not been submitted yet.
                //  if it exist, we update it in stead of inserting a new row.
                //  That coorespond in the cases where the Jenkins pipe is executed several times 
                //  on a single svn commit.
                Statement stmt4 = connection.createStatement();
                try {
                    String req_sel4 = "Select id FROM  buildrevisionparameters "
                            + " WHERE build='NONE' and revision='NONE' and application = '" + application + "' "
                            + "   and `release` = '" + release + "'";
                    ResultSet rsBC4 = stmt4.executeQuery(req_sel4);
                    try {
                        if (rsBC4.first()) {
                            out.println("Warning - Release entry already exist. Updating the existing entry : " + rsBC4.getString("id"));

                            String req_update = "UPDATE buildrevisionparameters "
                                    + "SET Project = '" + project + "', "
                                    + " TicketIDFixed = '" + ticket + "', "
                                    + " BugIDFixed = '" + bug + "', "
                                    + " Link = '" + link + "', "
                                    + " ReleaseOwner = '" + owner + "', "
                                    + " Subject = '" + subject + "', "
                                    + " jenkinsbuildid = '" + jenkinsbuildid + "', "
                                    + " mavengroupid = '" + mavengroupid + "', "
                                    + " mavenartifactid = '" + mavenartifactid + "', "
                                    + " mavenversion = '" + mavenversion + "'"
                                    + "WHERE id = '" + rsBC4.getString("id") + "' ";
                            Statement stmt = connection.createStatement();
                            try {
                                stmt.execute(req_update);
                            } finally {
                                stmt.close();
                            }
                        } else {
                            String req_insert = "INSERT INTO  buildrevisionparameters "
                                    + " ( `Build`, `Revision`, `Release`, `Application`"
                                    + ", `Project`, `TicketIDFixed`, `BugIDFixed`"
                                    + ", `Link`, `ReleaseOwner`, `Subject`, `jenkinsbuildid`"
                                    + ", `mavengroupid`, `mavenartifactid`, `mavenversion`) "
                                    + " VALUES ('NONE', 'NONE', '" + release + "', '" + application + "'"
                                    + ", '" + project + "', '" + ticket + "', '" + bug + "'"
                                    + ", '" + link + "', '" + owner + "', '" + subject + "', '" + jenkinsbuildid + "'"
                                    + ", '" + mavengroupid + "', '" + mavenartifactid + "', '" + mavenversion + "') ";
                            Statement stmt = connection.createStatement();
                            try {
                                stmt.execute(req_insert);
                            } finally {
                                stmt.close();
                            }
                            out.println("Release Inserted : '" + release + "' on '" + application + "' for user '" + owner + "'");
                        }
                    } finally {
                        rsBC4.close();
                    }
                } finally {
                    stmt4.close();
                }
            } else {
                // In case of errors, we display the help message.
                out.println(helpMessage);

            }

        } catch (Exception e) {
            Logger.getLogger(NewRelease.class.getName()).log(Level.SEVERE, Infos.getInstance().getProjectNameAndVersion() + " - Exception catched.", e);
            out.print("Error while inserting the release : ");
            out.println(e.toString());
        } finally {
            out.close();
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(NewRelease.class.getName(), org.apache.log4j.Level.WARN, e.toString());
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
