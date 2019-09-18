package Server;

import Utils.ZipUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author umagrawal
 */
public class SSHConnection implements SSHConnectionIntf {

    //<editor-fold defaultstate="collapsible" desc="Variables declaration">
    /**
     * set this value to true if tunnel is to be set
     * <p>Default value: false </p>
     */
    public Boolean setTunnel = false;
    /**
     * set this value to true if connection to server is key based
     * <p>Default value: false </p>
     */
    public Boolean isAutheticationKeyBased = false;
    /**
     * True if connected to the server else false
     */
    public Boolean isConnected;
    /**
     * set this value to true if the script output is to be printed on System.out stream
     * <p>Default value: false </p>
     */
    public Boolean printOnConsole = false;
    /**
     * <p>Script output of all the scripts is appended to this string buffer</p>
     * this can be used to search some string from the output of previous scripts
     * 
     */
    public static StringBuffer consolidated_output = new StringBuffer("");

    private String hostname;
    private String username;
    private int port;
    private String password;
    private String passphrase = null;
    private String keyFileLocation;
    private int remotePort;
    private String passkey;
    private String tunnelRemoteHost;
    private int localport = 10900;
    private String outputFolder;
    private int TIMEOUT_VALUE = 10000;

    private String errorCheckString = "DEFAULT_ERROR_CHECK_VALUE";
    private com.jcraft.jsch.Session session;
    private Channel channel;
    private Boolean upload_complete;
    private Boolean download_complete;
    private static FileOutputStream error_stream;
    private JProgressBar progress_bar;
    private JFrame frame;
    private int file_size;
    //</editor-fold>

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String getOutputFolder() {
        return outputFolder;
    }

    @Override
    public String getKeyFileLocation() {
        return keyFileLocation;
    }

    @Override
    public String getErrorCheckString() {
        return errorCheckString;
    }

    @Override
    public int getTIMEOUT_VALUE() {
        return TIMEOUT_VALUE;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getTunnelRemoteHost() {
        return tunnelRemoteHost;
    }

    @Override
    public int getLocalport() {
        return localport;
    }

    @Override
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setPassword(String password) {
        this.passkey = password;
    }

    @Override
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public void setKeyFileLocation(String keyFileLocation) {
        this.keyFileLocation = keyFileLocation;
    }

    @Override
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public void setTunnelRemoteHost(String tunnelRemoteHost) {
        this.tunnelRemoteHost = tunnelRemoteHost;
    }

    @Override
    public void setErrorCheckString(String errorCheckString) {
        this.errorCheckString = errorCheckString;
    }

    public void setTIMEOUT_VALUE(int TIMEOUT_VALUE) {
        this.TIMEOUT_VALUE = TIMEOUT_VALUE;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setLocalport(int localport) {
        this.localport = localport;
    }

    @Override
    public void connect() throws JSchException {
        JSch jsch = new JSch();
        password = passkey;
        if (isAutheticationKeyBased == true) {
            password = null;
            passphrase = passkey;
            jsch.addIdentity(keyFileLocation);
        }
        session = jsch.getSession(username, hostname, port);
        UserInfo ui = new MyUserInfo();
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setUserInfo(ui);
        session.setServerAliveInterval(TIMEOUT_VALUE);
        session.setServerAliveCountMax(10);
        if (setTunnel) {
            session = setTunneling(tunnelRemoteHost, remotePort, session);
        }
        session.connect(TIMEOUT_VALUE);
        isConnected = true;
    }

    public class MyUserInfo implements UserInfo {

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptYesNo(String str) {
            str = "Yes";
            return true;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return true;
        }

        @Override
        public void showMessage(String message) {
            //logger.info(message);
            //JOptionPane.showMessageDialog(null, message);
        }

        @Override
        public boolean promptPassword(String string) {
            return true;
        }
    }

    Session setTunneling(String tunnelRemoteHost, int tunnelRemotePort, Session l_session) {
        Boolean isTunnelSet = false;
        while (!isTunnelSet) {
            try {
                l_session.setPortForwardingL(localport, tunnelRemoteHost, tunnelRemotePort);
                isTunnelSet = true;
            } catch (JSchException ex) {
                localport += 1;
            }
        }
        return l_session;
    }

    @Override
    public void executeScript(String path, String ScriptName) throws ScriptExecutionFailureException, IOException, JSchException {
        if (outputFolder == null) {
            throw new ScriptExecutionFailureException("Output folder for Script execution is not defined");
        }
        File l_outputFolder = new File(outputFolder);
        if (!l_outputFolder.exists()) {
            l_outputFolder.mkdirs();
        }
        File errorFile = new File(outputFolder + "/" + "ErrorStream.txt");
        String script_output;
        error_stream = new FileOutputStream(errorFile);
        uploadFileToServer(path + "/" + ScriptName, "$HOME");
        File file = new File(outputFolder + "/" + "Output_" + ScriptName);
        FileWriter fw = new FileWriter(file);
        fw.write("dos2unix " + ScriptName + "\nchmod 777 " + ScriptName + "\nbash +x " + ScriptName + "\nrm " + ScriptName);
        DateFormat df = new SimpleDateFormat("dd_MMM_yyyy_HH_mm_ss");
        String formattedDate = df.format(new Date());
        String outputFileName = outputFolder + "/" + (ScriptName.substring(0, ScriptName.indexOf(".")) + "_" + formattedDate + ".out");
        fw.flush();
        fw.close();
        String command = "";
        fw = new FileWriter(outputFileName);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                command += line + "\n";
            }
        }
        channel = session.openChannel("exec");
        ((ChannelExec) channel).setErrStream(error_stream, true);
        ((ChannelExec) channel).setCommand(command);
        channel.setInputStream(null);
        channel.connect(TIMEOUT_VALUE);
        InputStream in = channel.getInputStream();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                script_output = new String(tmp, 0, i);
                fw.write(script_output);
                print(script_output);
                Boolean error_found = check_for_error(script_output);
                if (error_found) {
                    fw.flush();
                    fw.close();
                    file.delete();
                    throw new ScriptExecutionFailureException(ScriptName);
                }
            }
            if (channel.isClosed()) {
                fw.write("exit-status: " + channel.getExitStatus());
                if (channel.getExitStatus() != 0) {
                    throw new ScriptExecutionFailureException(ScriptName);
                }
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ee) {
                System.err.println(ee);
            }
        }
        channel.disconnect();
        fw.flush();
        fw.close();
        file.delete();
    }

    @Override
    public void uploadToServer(String Source, String Destination) throws IOException, JSchException, SftpException, ScriptExecutionFailureException {
        File file = new File(Source);
        if (!file.exists()) {
            throw new IOException("Source File not present");
        }
        if (file.isDirectory()) {
            uploadFolderToServer(Source, Destination, file.getName());
        }
    }

    private void uploadFileToServer(String Source, String Destination) throws IOException {
        Boolean new_upload = true;
        upload_complete = false;
        int put_status_mode = ChannelSftp.OVERWRITE;
        File f = new File(Source);
        ChannelSftp channelSftp = null;
        SftpATTRS attrs = null;
        progress_bar = new JProgressBar(0, (int) f.length());
        progress_bar.setBounds(200, 200, 400, 150);
        progress_bar.setStringPainted(true);
        frame = new JFrame("Uploading " + f.getName() + " ...");
        frame.add(progress_bar);
        frame = set_frame_parameters(frame);
        while (!upload_complete) {
            file_size = 0;
            try {
                channel = session.openChannel("sftp");
                if (!channel.isConnected()) {
                    channel.connect(TIMEOUT_VALUE);
                }
                channelSftp = (ChannelSftp) channel;
                if (Destination.contains("$HOME")) {
                    Destination = Destination.replace("$HOME", channelSftp.getHome());
                }
                attrs = channelSftp.lstat(Destination);
                if (attrs == null) {
                    channelSftp.mkdir(Destination);
                }
                channelSftp.cd(Destination);
                try {
                    attrs = channelSftp.lstat(Destination + "/" + f.getName());
                    file_size = new_upload ? 0 : (int) attrs.getSize();
                } catch (SftpException ex) {
                }
                progress_bar.setValue(file_size);
                channelSftp.put(new FileInputStream(f), f.getName(), new SftpProgressMonitor() {
                    long uploadedBytes;

                    @Override
                    public void init(int i, String Source, String Destination, long bytes) {
                        uploadedBytes = file_size;
                        //print(file_size + "/" + f.length());
                    }

                    @Override
                    public boolean count(long bytes) {
                        uploadedBytes += bytes;
                        progress_bar.setValue((int) uploadedBytes);
                        return true;
                    }

                    @Override
                    public void end() {
                        //file_size += (int) uploadedBytes;
                    }
                }, put_status_mode);
            } catch (JSchException | SftpException ex) {
                if (ex.toString().contains("session is down") || ex.toString().contains("socket write error")) {
                    new_upload = false;
                    put_status_mode = ChannelSftp.RESUME;
                    reconnect();
                }
            }
            if (file_size == f.length() || progress_bar.getValue() == progress_bar.getMaximum()) {
                upload_complete = true;
                frame.dispose();
                channelSftp.exit();
                channel.disconnect();
            }
        }
    }

    private void uploadFolderToServer(String directorySource, String directoryDestination, String destinationFolderName) throws JSchException, SftpException, ScriptExecutionFailureException, IOException {
        new File("temp/").mkdir();
        FileUtils.copyDirectory(FileUtils.getFile(directorySource), FileUtils.getFile("temp/" + destinationFolderName));
        ZipUtils appZip = new ZipUtils();
        appZip.zipFolder("temp/" + destinationFolderName, "temp/" + destinationFolderName + ".zip");
        uploadFileToServer("temp/" + destinationFolderName + ".zip", directoryDestination);
        File file = new File("temp/" + "Unzip.sh");
        FileOutputStream fos = new FileOutputStream(file);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
            bw.write("cd " + directoryDestination);
            bw.newLine();
            bw.write("unzip " + destinationFolderName + ".zip");
            bw.newLine();
            bw.write("rm " + destinationFolderName + ".zip");
        }
        executeScript("temp", "Unzip.sh");
        file.delete();
        File index = new File("temp/" + destinationFolderName);
        String[] entries = index.list();
        for (String s : entries) {
            File currentFile = new File(index.getPath(), s);
            currentFile.delete();
        }
        File zipFile = new File("temp/" + destinationFolderName + ".zip");
        zipFile.delete();
    }

    @Override
    public void clearOutputBuffer() {
        consolidated_output.delete(0, consolidated_output.length());
    }

    private JFrame set_frame_parameters(JFrame frame) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
        frame.setLocationRelativeTo(null);
        frame.setSize(400, 150);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
        return frame;
    }

    private void reconnect() {
        while (!channel.isConnected()) {
            try {
                connect();
                channel = session.openChannel("sftp");
                channel.connect();
            } catch (JSchException ex) {
            }
        }
    }

    private void print(String output) {
        if (printOnConsole) {
            System.out.println(output);
        }
        consolidated_output.append(output).append("\n");
    }

    private Boolean check_for_error(String script_output) {
        return script_output.contains(errorCheckString);
    }

    @Override
    public void downloadFromServer(String Source, String Destination) throws JSchException, SftpException {
        channel = session.openChannel("sftp");
        if (!channel.isConnected()) {
            channel.connect(TIMEOUT_VALUE);
        }
        ChannelSftp channelSftp = (ChannelSftp) channel;
        if (Source.contains("$HOME")) {
            Source = Source.replace("$HOME", channelSftp.getHome());
        }
        SftpATTRS attrs = channelSftp.lstat(Source);
        if (attrs == null) {
            throw new SftpException(SSH_FX_NO_SUCH_FILE, "File/ Folder not present at the gievn location");
        }
        if (!attrs.isDir()) {
            String sourceFileName = getSourceFileName(Source, channelSftp);
            downloadFileFromServer(Source, Destination, channelSftp, attrs, sourceFileName);
        }

    }

    private String getSourceFileName(String Source, ChannelSftp channelSftp) throws SftpException {
        Vector<ChannelSftp.LsEntry> sourceFileObject = channelSftp.ls(Source);
        String sourceFileName = sourceFileObject.get(0).getFilename();
        return sourceFileName;
    }

    private void downloadFileFromServer(String Source, String Destination, ChannelSftp channelSftp, SftpATTRS attrs, String sourceFileName) {
        Boolean new_download = true;
        download_complete = false;
        int get_status_mode = ChannelSftp.OVERWRITE;
        long sourceFileSize = attrs.getSize();
        File destFile = new File(Destination + "/" + sourceFileName);
        progress_bar = new JProgressBar(0, (int) sourceFileSize);
        progress_bar.setBounds(200, 200, 400, 150);
        progress_bar.setStringPainted(true);
        frame = new JFrame("Downloading " + destFile.getName() + " ...");
        frame.add(progress_bar);
        frame = set_frame_parameters(frame);
        while (!download_complete) {
            file_size = 0;
            try {
                channel = session.openChannel("sftp");
                if (!channel.isConnected()) {
                    channel.connect(TIMEOUT_VALUE);
                }
                file_size = new_download ? 0 : (int) destFile.length();
                channelSftp.get(Source, Destination, new SftpProgressMonitor() {
                    long downloadedBytes;

                    @Override
                    public void init(int i, String string, String string1, long l) {
                        downloadedBytes = file_size;
                    }

                    @Override
                    public boolean count(long bytes) {
                        downloadedBytes += bytes;
                        progress_bar.setValue((int) downloadedBytes);
                        return true;
                    }

                    @Override
                    public void end() {
                    }
                }, get_status_mode);
            } catch (JSchException | SftpException ex) {
                if (ex.toString().contains("session is down") || ex.toString().contains("socket write error")) {
                    new_download = false;
                    get_status_mode = ChannelSftp.RESUME;
                    reconnect();
                }
            }
            if (file_size == sourceFileSize || progress_bar.getValue() == progress_bar.getMaximum()) {
                download_complete = true;
                frame.dispose();
                channelSftp.exit();
                channel.disconnect();
            }
        }
    }

    @Override
    public void renameServerFile(String filePath, String oldName, String newName) throws JSchException, SftpException {
        ChannelSftp channelSftp = null;
        SftpATTRS attrs = null;
        channel = session.openChannel("sftp");
        channel.connect(TIMEOUT_VALUE);
        channelSftp = (ChannelSftp) channel;
        if (filePath.contains("$HOME")) {
            filePath = filePath.replace("$HOME", channelSftp.getHome());
        }
        channelSftp.cd(filePath);
        channelSftp.rename(oldName, newName);
    }

    @Override
    public void removeFromServer(String filePath, String fileOrFolderName) throws SftpException, JSchException {
        ChannelSftp channelSftp = null;
        SftpATTRS attrs = null;
        channel = session.openChannel("sftp");
        channel.connect(TIMEOUT_VALUE);
        channelSftp = (ChannelSftp) channel;
        if (filePath.contains("$HOME")) {
            filePath = filePath.replace("$HOME", channelSftp.getHome());
        }
        channelSftp.cd(filePath);
        attrs = channelSftp.lstat(filePath + "/" + fileOrFolderName);
        if (attrs.isDir()) {
            channelSftp.rmdir(fileOrFolderName);
        } else {
            channelSftp.rm(fileOrFolderName);
        }
        channelSftp.exit();
        channel.disconnect();

    }

    @Override
    public void disconnect() {
        clearOutputBuffer();
        session.disconnect();
        isConnected = false;
    }
    
}
