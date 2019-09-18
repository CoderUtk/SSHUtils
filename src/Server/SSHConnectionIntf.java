package Server;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.IOException;

interface SSHConnectionIntf {

    /**
     * @return The host-name of the server
     */
    public String getHostname();

    /**
     * @return The port for connection to server
     */
    public int getPort();

    /**
     * @return The remote port of the server for which tunneling is set
     */
    public int getRemotePort();

    /**
     * @return The location of the key file for key based authentication
     */
    public String getKeyFileLocation();

    /**
     * @return The output folder which is set for output of script execution output
     */
    public String getOutputFolder();

    /**
     * @return String that is set to check in the script output while execution
     */
    public String getErrorCheckString();

    /**
     * @return the connection timeout value in seconds
     */
    public int getTIMEOUT_VALUE();

    /**
     * @return the username to connect to the server
     */
    public String getUsername();

    /**
     * @return The remote host of the server for which tunneling is set
     */
    public String getTunnelRemoteHost();

    /**
     * @return The local port on which tunneling is set to the remote server
     */
    public int getLocalport();

    /**
     * Sets the host-name of the server
     * @param hostname 
     */
    public void setHostname(String hostname);

    /**
     * Sets the port of the server to be connected
     * @param port 
     */
    public void setPort(int port);

    /**
     * <p>Sets the password of the server</p>
     * Not applicable for key based authentication
     * @param password 
     */
    public void setPassword(String password);

    /**
     * <p>Sets the passphrase for key based authentication</p>
     * Applicable only if user's key contains passphrase
     * @param passphrase 
     */
    public void setPassphrase(String passphrase);

    /**
     * Sets the remote port of the server for which tunneling is to be set
     * @param remotePort 
     */
    public void setRemotePort(int remotePort);

    /**
     * <p>Sets the key file location for key based authentication</p>
     * Provide the absolute path with filename
     * <p>If ppk files do not work then use OpenSSH format key (Convert with puttyGen)
     * @param keyFileLocation 
     */
    public void setKeyFileLocation(String keyFileLocation);

    /**
     * Sets the output folder for script execution output
     * @param outputFolder 
     */
    public void setOutputFolder(String outputFolder);
    
    /**
     * <p>Sets the errorcheck string which is to be checked in the output of the script execution</p>
     * If the set string is found in the output of the script then ScriptExecutionFailureException is thrown
     * @param errorCheckString 
     */
    public void setErrorCheckString(String errorCheckString);

    /**
     * Sets the username of the server to be connected
     * @param username 
     */
    public void setUsername(String username);

    /**
     * Sets the remote host of the server for which tunneling is to be set
     * @param tunnelRemoteHost 
     */
    public void setTunnelRemoteHost(String tunnelRemoteHost);

    /**
     * <p>Sets the localport for the tunneling to be set </p>
     * By default tunneling is set on the local port 10900
     * @param localport 
     */
    public void setLocalport(int localport);

    /**
     * Connects to the SSH server
     * @throws JSchException 
     */
    public void connect() throws JSchException;

    /**
     * Disconnects from the SSH server
     */
    public void disconnect();

    /**
     * Executes the shell script on the server
     * @param path Path where the shell script is placed
     * @param ScriptName File name of the shell script to be executed
     * @throws ScriptExecutionFailureException If the script output contains the error check string OR if the output folder is not defined
     * @throws IOException If the script file not present
     * @throws JSchException 
     */
    public void executeScript(String path, String ScriptName) throws ScriptExecutionFailureException, IOException, JSchException;

    /**
     * Uploads the file/folder to server
     * @param Source Source file/folder to be uploaded
     * @param Destination Server destination where the file is to be uploaded
     * @throws IOException If source file not present
     * @throws JSchException
     * @throws SftpException
     * @throws ScriptExecutionFailureException 
     */
    public void uploadToServer(String Source, String Destination) throws IOException, JSchException, SftpException, ScriptExecutionFailureException;

    /**
     * Downloads file from server
     * @param Source Server Source file/folder to be downloaded
     * @param Destination Destination where file/folder is to be downloaded
     * @throws JSchException
     * @throws SftpException 
     */
    public void downloadFromServer(String Source, String Destination) throws JSchException, SftpException;

    /**
     * Renames the server file
     * @param filePath Server path where the file to be renamed is to be located
     * @param oldName Original name of the file to be renamed
     * @param newName New name of the file
     * @throws JSchException
     * @throws SftpException 
     */
    public void renameServerFile(String filePath, String oldName, String newName) throws JSchException, SftpException;

    /**
     * Removes the file / folder folder from the server
     * @param filePath Server path from where the file is to be deleted
     * @param fileOrFolderName File/folder name to be deleted
     * @throws SftpException
     * @throws JSchException 
     */
    public void removeFromServer(String filePath, String fileOrFolderName) throws SftpException, JSchException;

    /**
     * Clears the output buffer of the script execution output
     */
    public void clearOutputBuffer();
}
