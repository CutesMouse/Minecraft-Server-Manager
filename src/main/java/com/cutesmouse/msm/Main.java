package com.cutesmouse.msm;


import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {
    public static MainWindow t;
    public static void main(String[] main) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        t = new MainWindow();
        DefaultCaret c = new DefaultCaret();
        c.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        t.getLabel1().setCaret(c);
        updateJarList();
        t.setVisible(true);
    }
    public static void updateJarList() {
        File f = new File(System.getProperty("user.dir"));
        if (!f.exists()) return;
        List<FileListPackage> list = Arrays.stream(Objects.requireNonNull(f.listFiles(p -> p.isFile()
                && p.getName().matches(".*?.jar$"))))
                .map(FileListPackage::new).collect(Collectors.toList());
        t.getServer().setListData(list.toArray());
    }
    public static void close() {
        if (pc == null) return;
        pc.destroy();
        appendLine("關服完成!",Color.GREEN,true);
        pc = null;
        try {
            if (output != null) output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        output = null;
    }
    public static void appendLine(String s) {
        appendLine(s,null,true);
    }
    private static ArrayList<String> INFO = new ArrayList<>();
    public static void appendLine(String s, Color c, boolean line) {
        if(t== null) return;
        s = "<font face=\"微軟正黑體\" size=5"+(c != null ? " color=\"rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+"\"" : "")+">"+
                s.replace("<","&lt;").replace(">","&gt;")+"</font>";
        INFO.add(s.replace("\n","<br>")+(line ? "<br>" : ""));
        while (INFO.size() > 150) {
            INFO.remove(0);
        }
        t.getLabel1().setText("<html>"+ String.join("", INFO) +"</html>");
    }
    public static void clear() {
        INFO.clear();
        t.getLabel1().setText("<html>"+ String.join("", INFO) +"</html>");
    }
    private static BufferedWriter output;
    public static void command(String s) throws IOException {
        if (pc == null) return;
        if (output == null) output = new BufferedWriter(new OutputStreamWriter(pc.getOutputStream()));
        output.write(s);
        output.newLine();
        output.flush();
    }
    private static Process pc;
    public static String getPropStatus(String name) {
        File prop = new File(System.getProperty("user.dir"), "server.properties");
        if (!prop.exists()) return "";
        try {
            ArrayList<String> properties = new ArrayList<>(Files.readAllLines(prop.toPath()));
            for (String s : properties) {
                if (s.startsWith(name)) return s.split("=")[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static void setPropStatus(String name, String value) {
        File prop = new File(System.getProperty("user.dir"), "server.properties");
        if (!prop.exists()) return;
        try {
            ArrayList<String> properties = new ArrayList<>(Files.readAllLines(prop.toPath())).stream().map(p -> {
                if (p.startsWith(name)) return name+"="+value;
                return p;
            }).collect(Collectors.toCollection(ArrayList::new));
            Files.write(prop.toPath(),properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void start() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        appendLine("開始讀取伺服器...",Color.RED,true);
        Object selected = t.getServer().getSelectedValue();
        if (selected == null) {
            appendLine("讀取失敗! 沒有選取伺服器檔案",Color.RED,true);
            return;
        }
        String jarPath = ((FileListPackage) selected).f.getPath();
        pc = runtime.exec("java -jar \""+jarPath+"\" nogui");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(pc.getInputStream(),"x-windows-950"));
        final BufferedReader error = new BufferedReader(new InputStreamReader(pc.getErrorStream(),"x-windows-950"));
        new Thread(() -> {
            String s = null;
            while (true) {
                if (pc == null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                try {
                    if ((s = reader.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (s == null) continue;
                if (s.matches(".*?\\[Async Chat Thread - #.*?/INFO].*")) appendLine(s,Color.ORANGE,true);
                //[10:15:37] [Server thread/INFO]: CutesMouse issued server command: /say hi
                else if (s.matches(".*?\\[Server thread/INFO]:.*?issued server command:.*")) appendLine(s,new Color(97, 13, 172),true);
                //[User Authenticator #1/INFO]: UUID of player CutesMouse is 757ed551-f32d-49c5-9b5c-bcb252cf50f4
                else if (s.matches(".*?\\[User Authenticator #.*?/INFO]:.*")) appendLine(s,new Color(19, 106, 17),true);
                else if (s.matches(".*?\\[Server thread/INFO].*")) appendLine(s,new Color(0, 45, 39),true);
                else if (s.matches(".*?\\[Server thread/WARN].*")) appendLine(s,new Color(255, 0, 0),true);
                else appendLine(s);
            }

        }).start();
        new Thread(() -> {
            String s = null;
            while (true) {
                if (pc == null) {
                    try {
                        error.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                try {
                    if ((s = error.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                appendLine(s,Color.RED,true);
            }
        }).start();
        try {
            pc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        close();
    }
}
