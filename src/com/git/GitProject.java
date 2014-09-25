package com.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.transport.OpenSshConfig.Host;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * git工程checkout 和 build
 * Created by pengsheng on 2014/09/25.
 */
public class GitProject {

    private static final Log logger = LogFactory.getLog(GitProject.class);

    private static final String ID_RSA_DIR = "";// 身份认证id_rsa私钥文件路径
    private static final String GIT_SERVICE_ADDR = ""; // git服务器地址
    private static final String OUTPUT_DIR = ""; //checkout工程输出路径

    public GitProject() {
        init();
    }

    private void init() {
        CustomConfigSessionFactory jschConfigSessionFactory = new CustomConfigSessionFactory();
        SshSessionFactory.setInstance(jschConfigSessionFactory);
    }

    /**
     *  checkout工程
     * @param projectName : 工程名
     */
    public void cloneProject(String projectName) {

        if (projectName == null || "".equals(projectName))
            return;

        String projectRemoteAddr = "git@" + GIT_SERVICE_ADDR + ":git/" + projectName + ".git";
        String projectDir = OUTPUT_DIR + File.separator + projectName;

        try {

            FileUtils.deleteDirectory(new File(projectDir)); // 清空

            Git repository = Git.cloneRepository().setURI(projectRemoteAddr).setDirectory(new File(projectDir)).call();
            for (Ref b : repository.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()) {
                logger.info("cloned branch " + b.getName());
            }

        } catch (InvalidRemoteException e) {
            logger.error(e);
        } catch (TransportException e) {
            logger.error(e);
        } catch (GitAPIException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
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

    /**
     *  身份认证
     * @author pengsheng
     */
    class CustomConfigSessionFactory extends JschConfigSessionFactory {

        protected JSch getJSch(final Host host, FS fs)
                throws JSchException {
            JSch jsch = super.getJSch(host, fs);
            jsch.removeAllIdentity();
            jsch.addIdentity(ID_RSA_DIR);
            return jsch;
        }

        @Override
        protected void configure(Host host, Session session) {
            session.setConfig("StrictHostKeyChecking", "false");
        }
    }
}
