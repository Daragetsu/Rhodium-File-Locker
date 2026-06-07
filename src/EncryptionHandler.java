import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionHandler {
    SecretKeyFactory factory;
    KeySpec spec;
    SecretKey tmp;
    SecretKey secret;
    Cipher cipher;
    AlgorithmParameters params;
    byte[] iv;
    public EncryptionHandler(String password, String saltString){
        char[] pass = password.toCharArray();
        byte[] salt = saltString.getBytes();
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            spec = new PBEKeySpec(pass, salt, 65536, 256);
            tmp = factory.generateSecret(spec);
            secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (Exception e) {
            RFrame.showError(e.getMessage());
        }
    }

    public File encrypt(File inputFile){
        try {
            String extension = "";
            int i = inputFile.getAbsolutePath().lastIndexOf('.');
            if (i > 0) {
                extension = inputFile.getAbsolutePath().substring(i+1);
            }
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            params = cipher.getParameters();
            iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            String uuid = UUID.randomUUID().toString();
            File outputFile = new File(Global.EncryptedFolder, uuid+"_"+extension+".bin");
            File IVFile = new File(Global.EncryptedFolder, uuid+"_iv.txt");
            FileWriter writer = new FileWriter(IVFile);
            for(byte b : iv){
                writer.write(((int)b)+"\n");
            }
            writer.close();
            enDecrypt(cipher, inputFile, outputFile);
            return outputFile;
        } catch (Exception e) {
            RFrame.showError(e.getMessage());
            return null;
        }
    }
    public String decrypt(File inputFile){
        String uuid = inputFile.getName();
        String id = uuid.substring(0, uuid.indexOf("_"));
        String extension = uuid.substring(uuid.indexOf("_")+1, uuid.lastIndexOf("."));
        ArrayList<Integer> ivl = new ArrayList<>();
        File IVFile = new File(Global.EncryptedFolder, id+"_iv.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(IVFile))) {
            String line;
            while((line = reader.readLine())!=null){
                ivl.add(Integer.parseInt(line));
            }
        } catch (Exception e) {
            RFrame.showError(e.getMessage());    
        }
        byte[] bytes = new byte[ivl.size()]; 
        for (int i = 0; i < ivl.size(); i++) { 
            bytes[i] = (byte) (ivl.get(i) & 0xFF);
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(bytes));
            enDecrypt(
                cipher, 
                inputFile, 
                new File(Global.DecryptedFolder, UUID.randomUUID()+"."+extension)
            );
        } catch (Exception e) {
            RFrame.showError(e.getMessage());
        }
        return id;
    }
    //I'm bad at naming
    public static void enDecrypt(Cipher cipher, File inputFile, File outputFile) throws FileNotFoundException, IOException, BadPaddingException, IllegalBlockSizeException{
        FileInputStream inputStream = new FileInputStream(inputFile);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer = new byte[2048];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, bytesRead);
            if (output != null) {
                outputStream.write(output);
            }
        }
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
            outputStream.write(outputBytes);
        }
        inputStream.close();
        outputStream.close();
    }
}