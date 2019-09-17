
import Server.LinuxConnection;
import Server.ScriptExecutionFailureException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.IOException;

class Test {
    
    public static void main(String[] args) throws JSchException, ScriptExecutionFailureException, IOException, SftpException {
        LinuxConnection connection = new LinuxConnection();
        //-------------------Connect-----------------------
        connection.setHostname("shrd-baod-bast-2.cloud.com");
        connection.setPort(4010);
        connection.setUsername("cloud_as1");
        //ll.setPassword("qc9_Welcome"); -- For password based authentication
        connection.isAutheticationKeyBased= true;
        connection.setKeyFileLocation("D:\\Keys\\utkarsh_latest");
        connection.setTIMEOUT_VALUE(20000);//Optional
        //------------Set tunneling-----------------------
        connection.setTunnel = true;
        connection.setTunnelRemoteHost("qbcde.xyz.cloud.com");
        connection.setRemotePort(3009);
        connection.connect();
        //----------Execute Script-----------------------
        connection.setErrorCheckString("TOOLKIT_ERROR");//Optional
        connection.printOnConsole = true;
        connection.setOutputFolder("Output");
        connection.executeScript("Scripts", "test.sh");
        connection.clearOutputBuffer();
        //---------Upload file to server-----------------------
        connection.uploadToServer("Output", "$HOME");
        //---------Download file from server-----------------------
        connection.downloadFromServer("$HOME/Output/ErrorStream.txt", "Scripts");
        //---------Remove file from server-----------------------
        connection.removeFromServer("$HOME/Output", "ErrorStream.txt");
        //---------Rename Server file-----------------------
        connection.renameServerFile("$HOME/Output", "test_17_Sep_2019_16_01_48.out", "utk.txt");
        //---------Disconnect-----------------------
        connection.disconnect();
    }
    
}
