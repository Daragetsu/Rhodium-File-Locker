import java.io.File;

import javax.swing.JOptionPane;

//TODO: Make it CLI compatable
//TODO: add something to show the files being encrypted and decrypted
public class App {
    private static String password; //it's not a good idea to save passwords in memory like this is it?
    public static void main(String[] args) throws Exception {
        new Global();
        password = JOptionPane.showInputDialog("");
        EncryptionHandler handler = new EncryptionHandler(password, "idk123456789");
        new RFrame(password, handler);
        for(File file : Global.EncryptedFolder.listFiles()){
            if(file.getName().endsWith(".bin")){
                handler.decrypt(file);
            }
        }
    }
}