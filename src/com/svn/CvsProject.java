package com.svn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * cvs工程 checkout 和 build
 * Created by pengsheng on 2014/09/25.
 */
public class CvsProject {

    private static final Log logger = LogFactory.getLog(CvsProject.class);

    private static final String CVS_BASE_PATH = "./config/cvsbuild.xml";

    private static final String OUTPUT_DIR = "";// 工程输出路径

    public CvsProject() {

    }

    /**
     * checkout 工程
     * @param projectName ：工程名
     */
    public void checkoutProject(String projectName) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);

        File buildFile = new File(CVS_BASE_PATH);

        Project project = new Project();
        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(ps);
        consoleLogger.setOutputPrintStream(ps);
        consoleLogger.setMessageOutputLevel(2);
        project.addBuildListener(consoleLogger);
        try {
            project.fireBuildStarted();
            project.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            helper.parse(project, buildFile);
            project.setProperty("out.dir", OUTPUT_DIR);
            project.setProperty("build.package", OUTPUT_DIR + File.separator + projectName);
            /*if(_checkouttag != null && !"".equals(_checkouttag)){
				project.setProperty("cvs.tag", _checkouttag);
				project.executeTarget("tagproject");
			}*/

            project.executeTarget("checkout");
            project.fireBuildFinished(null);
            logger.info(os.toString());

        } catch (BuildException e) {
            project.fireBuildFinished(e);
            logger.error(os.toString());
        } finally {

            try {
                if (os != null)
                    os.close();

                if (ps != null)
                    ps.close();

            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    /**
     *  编译工程
     * @param projectName : 工程名
     */
    public void buildProject(String projectName) {

        if (projectName == null || "".equals(projectName))
            return;

        String projectDir = OUTPUT_DIR + File.separator + projectName;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);

        File buildFile =  new File(projectDir + File.separator + "build.xml");

        Project project = new Project();
        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(ps);
        consoleLogger.setOutputPrintStream(ps);
        consoleLogger.setMessageOutputLevel(2);
        project.addBuildListener(consoleLogger);
        try {
            project.fireBuildStarted();
            project.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            helper.configureProject(project, buildFile);
            project.executeTarget(project.getDefaultTarget());
            project.fireBuildFinished(null);
            logger.info(os.toString());

        }catch (BuildException e) {
            project.fireBuildFinished(e);
            logger.info(os.toString());
        } finally {

            try {
                if (os != null)
                    os.close();

                if (ps != null)
                    ps.close();

            } catch (IOException e) {
                logger.error(e);
            }
        }
    }
}
