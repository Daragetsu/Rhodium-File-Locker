import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.Desktop;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

//I'm not good at using layouts, 
//that's why I'm using no layout managers and setting hard bounds
public class RFrame extends JFrame implements ActionListener {
    JButton openBtn;
    public RFrame(String password, EncryptionHandler handler){
        this.setTitle("Rhodium File Locker");
        this.setBounds(100, 100, 500, 500);
        this.setLayout(null);

        openBtn = new JButton("Open Unencrypted Folder");
        openBtn.setBounds(10, 10, 200, 50);
        openBtn.addActionListener(this);
        this.add(openBtn);

        this.setDropTarget(new DropTarget(){
            public synchronized void drop(DropTargetDropEvent evt) {
            try {
                evt.acceptDrop(DnDConstants.ACTION_COPY);
                //if you're dropping anything other than files here, why are you using a file locker?
                List<File> droppedFiles = (List<File>)
                    evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                for (File file : droppedFiles) {
                    File f = handler.encrypt(file);
                    if(f!=null){
                        handler.decrypt(f);
                    }else{
                        showError("Something Went Wrong");
                    }
                }
                evt.dropComplete(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        });

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                for(File file : Global.DecryptedFolder.listFiles()){
                    if(!file.delete()){
                        showError("Failed to delete file");
                    }
                }
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(openBtn)){
            try {
                Desktop.getDesktop().open(Global.DecryptedFolder);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }
    public static void showError(String error){
        JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
    }
}