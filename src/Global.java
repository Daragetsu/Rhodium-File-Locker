import java.io.File;

public class Global {
    public static String home;
    public static File EncryptedFolder;
    public static File DecryptedFolder;
    public Global(){
        home = System.getProperty("user.home");
        EncryptedFolder = new File(home, "rhodium-fl/encrypted");
        DecryptedFolder = new File(home, "rhodium-fl/decrypted");
        if(!EncryptedFolder.exists()){
            EncryptedFolder.mkdirs();
        }
        if(!DecryptedFolder.exists()){
            DecryptedFolder.mkdirs();
        }
    }
}
