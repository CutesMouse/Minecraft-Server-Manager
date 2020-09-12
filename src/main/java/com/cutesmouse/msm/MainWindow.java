/*
 * Created by JFormDesigner on Fri Sep 11 23:39:04 CST 2020
 */

package com.cutesmouse.msm;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

/**
 * @author CutesMouse
 */
public class MainWindow extends JFrame {
    public MainWindow() {
        initComponents();
    }

    public JTextPane getLabel1() {
        return CommandWindow;
    }

    private void button1ActionPerformed(ActionEvent e) {
        new Thread(() -> {
            try {
                Main.start();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }).start();
    }

    private void button2ActionPerformed(ActionEvent e) {
        try {
            Main.command("stop");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void thisWindowClosing(WindowEvent e) {
        Main.close();
        System.exit(0);
    }

    private void SendCommand(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
        if (Command.getText().isEmpty()) return;
        try {
            Main.command(Command.getText());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Command.setText("");
    }

    private void ServerFolderActionPerformed(ActionEvent e) {
        try {
            Desktop.getDesktop().open(new File(System.getProperties().getProperty("user.dir")));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public JList getServer() {
        return Server;
    }

    private void reloadServerFiles(ActionEvent e) {
        Main.updateJarList();
    }

    private void addNewMap(ActionEvent e) {
        File prop = new File(System.getProperty("user.dir"), "server.properties");
        if (!prop.exists()) {
            JOptionPane.showMessageDialog(this,"系統找不到你的server.properties! 請先載入一次伺服器");
            return;
        }
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String fn = f.getName();
                if (fn.equals("plugins")) return false;
                if (fn.equals("logs")) return false;
                if (fn.equals("cache")) return false;
                if (fn.equals("crash-reports")) return false;
                if (fn.equals("timings")) return false;
                if (fn.matches(".*?_nether$")) return false;
                if (fn.matches(".*?_the_end$")) return false;
                return true;
            }

            @Override
            public String getDescription() {
                return "過濾系統資料夾";
            }
        });
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int flag = fc.showOpenDialog(this);
        if (flag == JFileChooser.OPEN_DIALOG) {
            File f = fc.getSelectedFile();
            if (f == null) return;
            if (!f.getParentFile().getPath().equals(System.getProperty("user.dir"))) {
                JOptionPane.showMessageDialog(this,"你無法選擇與插件目錄不相同的資料夾!","錯誤",JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                ArrayList<String> properties = new ArrayList<>(Files.readAllLines(prop.toPath())).stream().map(s -> {
                    if (s.startsWith("level-name=")) {
                        return "level-name="+f.getName();
                    }
                    return s;
                }).collect(Collectors.toCollection(ArrayList::new));
                Files.write(prop.toPath(),properties);

            } catch (IOException ioException) {
                JOptionPane.showOptionDialog(this,"系統載入server.properties時發生錯誤!\n"+ioException.getLocalizedMessage(),"讀取錯誤!",
                        JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,null,0);
                ioException.printStackTrace();
            }
        }
    }

    private void listCMD(ActionEvent e) {
        try {
            Main.command("list");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void tpsCMD(ActionEvent e) {
        try {
            Main.command("tps");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void pluginCMD(ActionEvent e) {
        try {
            Main.command("plugins");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void opCMD(ActionEvent e) {
        String s = JOptionPane.showInputDialog(this, "請輸入要賦予權限的對象", "新增管理員",JOptionPane.PLAIN_MESSAGE);
        if (s == null) return;
        if (s.isEmpty()) return;
        try {
            Main.command("op "+s);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void deopCMD(ActionEvent e) {
        String s = JOptionPane.showInputDialog(this, "請輸入要移除權限的對象", "移除管理員",JOptionPane.PLAIN_MESSAGE);
        if (s == null) return;
        if (s.isEmpty()) return;
        try {
            Main.command("deop "+s);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void setProperties(String item) {
        String status = Main.getPropStatus(item);
        String input = JOptionPane.showInputDialog(this, "目前更改的項目為 " + item + "\n當前設定值: "+status+"\n請輸入更改項目", "更改伺服器設定", JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        if (input.isEmpty()) return;
        Main.setPropStatus(item,input);
    }
    private boolean loadSend = false;
    private void loadPropItems(MouseEvent e) {
        if (loadSend) return;
        File prop = new File(System.getProperty("user.dir"), "server.properties");
        if (!prop.exists()) return;
        loadSend = true;
        JMenu menu = ((JMenu) e.getComponent());
        try {
            ArrayList<String> properties = new ArrayList<>(Files.readAllLines(prop.toPath()));
            for (String property : properties) {
                if (!property.contains("=")) continue;
                JMenuItem item = new JMenuItem(property.split("=")[0]);
                item.addActionListener(p -> setProperties(property.split("=")[0]));
                menu.add(item);
            }

        } catch (IOException ioException) {
            JOptionPane.showOptionDialog(this,"系統載入server.properties時發生錯誤!\n"+ioException.getLocalizedMessage(),"讀取錯誤!",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,null,0);
            ioException.printStackTrace();
        }
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        menuBar1 = new JMenuBar();
        files = new JMenu();
        menuItem7 = new JMenuItem();
        menu5 = new JMenu();
        menuItem6 = new JMenuItem();
        menu2 = new JMenu();
        menuItem2 = new JMenuItem();
        menuItem3 = new JMenuItem();
        menuItem4 = new JMenuItem();
        menuItem5 = new JMenuItem();
        menuItem8 = new JMenuItem();
        menu3 = new JMenu();
        menu4 = new JMenu();
        scrollPane1 = new JScrollPane();
        CommandWindow = new JTextPane();
        Start = new JButton();
        Close = new JButton();
        Command = new JTextField();
        ServerFolder = new JButton();
        scrollPane2 = new JScrollPane();
        Server = new JList();

        //======== this ========
        setTitle("Minecraft Server Manager");
        setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                thisWindowClosing(e);
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //======== menuBar1 ========
        {

            //======== files ========
            {
                files.setText(" \u6a94\u6848(F) ");

                //---- menuItem7 ----
                menuItem7.setText("\u5207\u63db\u5730\u5716");
                menuItem7.addActionListener(e -> addNewMap(e));
                files.add(menuItem7);

                //======== menu5 ========
                {
                    menu5.setText("\u66f4\u6539\u4f3a\u670d\u5668\u8a2d\u5b9a");
                    menu5.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            loadPropItems(e);
                        }
                    });
                }
                files.add(menu5);
                files.addSeparator();

                //---- menuItem6 ----
                menuItem6.setText("\u91cd\u65b0\u8b80\u53d6\u4f3a\u670d\u5668\u6a94\u6848");
                menuItem6.addActionListener(e -> reloadServerFiles(e));
                files.add(menuItem6);
            }
            menuBar1.add(files);

            //======== menu2 ========
            {
                menu2.setText(" \u6307\u4ee4(C) ");

                //---- menuItem2 ----
                menuItem2.setText("\u67e5\u770b\u4f3a\u670d\u5668\u4eba\u6578");
                menuItem2.addActionListener(e -> listCMD(e));
                menu2.add(menuItem2);

                //---- menuItem3 ----
                menuItem3.setText("\u67e5\u770bTPS");
                menuItem3.addActionListener(e -> tpsCMD(e));
                menu2.add(menuItem3);

                //---- menuItem4 ----
                menuItem4.setText("\u67e5\u770b\u5df2\u5b89\u88dd\u63d2\u4ef6");
                menuItem4.addActionListener(e -> pluginCMD(e));
                menu2.add(menuItem4);
                menu2.addSeparator();

                //---- menuItem5 ----
                menuItem5.setText("\u65b0\u589e\u7ba1\u7406\u54e1");
                menuItem5.addActionListener(e -> opCMD(e));
                menu2.add(menuItem5);

                //---- menuItem8 ----
                menuItem8.setText("\u79fb\u9664\u7ba1\u7406\u54e1");
                menuItem8.addActionListener(e -> deopCMD(e));
                menu2.add(menuItem8);
                menu2.addSeparator();

                //======== menu3 ========
                {
                    menu3.setText("\u767d\u540d\u55ae");
                }
                menu2.add(menu3);

                //======== menu4 ========
                {
                    menu4.setText("\u9ed1\u540d\u55ae");
                }
                menu2.add(menu4);
            }
            menuBar1.add(menu2);
        }
        setJMenuBar(menuBar1);

        //======== scrollPane1 ========
        {

            //---- CommandWindow ----
            CommandWindow.setFont(new Font("\u5fae\u8edf\u6b63\u9ed1\u9ad4", Font.PLAIN, 16));
            CommandWindow.setContentType("text/html");
            CommandWindow.setText("<html>\r   <head>\r \r   </head>\r   <body>\r     <p style=\"margin-top: 0\">\r       \r     </p>\r   </body>\r </html>\r ");
            CommandWindow.setEditable(false);
            scrollPane1.setViewportView(CommandWindow);
        }
        contentPane.add(scrollPane1);
        scrollPane1.setBounds(25, 20, 805, 440);

        //---- Start ----
        Start.setText("\u958b\u670d");
        Start.addActionListener(e -> button1ActionPerformed(e));
        contentPane.add(Start);
        Start.setBounds(845, 25, 125, 35);

        //---- Close ----
        Close.setText("\u95dc\u670d");
        Close.addActionListener(e -> button2ActionPerformed(e));
        contentPane.add(Close);
        Close.setBounds(845, 65, 125, 35);

        //---- Command ----
        Command.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                SendCommand(e);
            }
        });
        contentPane.add(Command);
        Command.setBounds(25, 465, 805, Command.getPreferredSize().height);

        //---- ServerFolder ----
        ServerFolder.setText("\u4f3a\u670d\u5668\u8cc7\u6599\u593e");
        ServerFolder.addActionListener(e -> ServerFolderActionPerformed(e));
        contentPane.add(ServerFolder);
        ServerFolder.setBounds(845, 455, 125, 35);

        //======== scrollPane2 ========
        {

            //---- Server ----
            Server.setFont(new Font("\u5fae\u8edf\u6b63\u9ed1\u9ad4", Font.PLAIN, 16));
            scrollPane2.setViewportView(Server);
        }
        contentPane.add(scrollPane2);
        scrollPane2.setBounds(845, 110, 125, 330);

        contentPane.setPreferredSize(new Dimension(995, 505));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu files;
    private JMenuItem menuItem7;
    private JMenu menu5;
    private JMenuItem menuItem6;
    private JMenu menu2;
    private JMenuItem menuItem2;
    private JMenuItem menuItem3;
    private JMenuItem menuItem4;
    private JMenuItem menuItem5;
    private JMenuItem menuItem8;
    private JMenu menu3;
    private JMenu menu4;
    private JScrollPane scrollPane1;
    private JTextPane CommandWindow;
    private JButton Start;
    private JButton Close;
    private JTextField Command;
    private JButton ServerFolder;
    private JScrollPane scrollPane2;
    private JList Server;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
