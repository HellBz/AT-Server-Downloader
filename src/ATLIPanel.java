import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.Objects;

public class ATLIPanel {

    ATLIPanel()
    {

        JFrame f= new JFrame("AT-Launcher Server Downloader");
        JPanel panel=new JPanel();

        panel.setBounds(0,0,499,399);
        panel.setBackground(Color.gray);
        Main.dbCombo.setEditable(true);
        Main.dbCombo.setVisible(true);
        Main.dbCombo.setPreferredSize(new Dimension(300,25));
        Main.dbCombo.setEditable(false);
        //Main.dbCombo.removeAllItems();
        panel.add(Main.dbCombo);


        //5-zeiliges und 20-spaltiges Textfeld wird erzeugt

        //Main.textfeld.append("Test" + System.lineSeparator() );
        //Text für das Textfeld wird gesetzt
        /*
        Main.textfeld.setText("Lorem ipsum dolor sit amet, \n " +

                "consetetur sadipscing elitr, sed diam nonumy " +
                "eirmod tempor invidunt ut labore et " +
                "dolore magna aliquyam erat, sed diam voluptua. " +
                "At vero eos et accusam et justo duo dolores et " +
                "ea rebum." + System.lineSeparator() );
         */
        //Zeilenumbruch wird eingeschaltet
        Main.textfeld.setLineWrap(false);

        //Zeilenumbrüche erfolgen nur nach ganzen Wörtern
        Main.textfeld.setWrapStyleWord(true);
        Main.textfeld.setEditable(false);
        Main.textfeld.setBackground(Color.lightGray);
        DefaultCaret caret = (DefaultCaret)Main.textfeld.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        //Ein JScrollPane, der das Textfeld beinhaltet, wird erzeugt
        JScrollPane scrollpane = new JScrollPane(Main.textfeld);

        Main.install_button.setBounds(50,100,80,30);
        Main.install_button.setEnabled(false);
        //b1.setBackground(Color.green);

        // this is anonymous class
        Main.install_button.addActionListener(evt -> {
            //then you know that is attached to this button
            String get_modpack = Objects.requireNonNull(Main.dbCombo.getSelectedItem()).toString();


            Thread install_pack = new Thread(() -> Main.install_pack( get_modpack ));
            install_pack.start();

            // cb.setVisible(!cb.isVisible());
        });
        panel.add(Main.install_button);

        //Scrollpane wird unserem Panel hinzugefügt
        panel.add(scrollpane);

        /*
        JButton b2=new JButton("Button 2");

        b2.setBounds(100,100,80,30);
        b2.setBackground(Color.red);
         panel.add(b2);
         */
        f.add(panel);
        f.setSize(500,400);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(dim.width/2-f.getSize().width/2, dim.height/2-f.getSize().height/2);
        f.setLayout(null);
        f.setVisible(true);
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String APP_ICON_PATH = "icon.png";
        ImageIcon icon = new ImageIcon( ClassLoader.getSystemResource(APP_ICON_PATH) );
        f.setIconImage( icon.getImage() );

    }
}