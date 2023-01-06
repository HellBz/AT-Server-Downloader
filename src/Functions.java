import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Functions {


    public static boolean pregMatch(String regex, String content) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(content.toLowerCase());
        return matcher.find();
    }

    public static void atl_file_download( String from, String to, String type, String todir) {

        //Fix to Link
        to = to.replace( Main.fs , Main.fs );
        from = from.replace(" ", "%20");

        //Main.textfeld.append("Dir: " + Main.current_path + Main.modpack_dir + System.lineSeparator());
        Main.textfeld.append("From: " + from + System.lineSeparator());
        Main.textfeld.append("To: " + to + System.lineSeparator());
        //Main.textfeld.append("Type: " + type + System.lineSeparator());

        String to_downloadfile = Main.current_path + Main.modpack_dir + to ;
        String file_basename = Paths.get(to).getFileName().toString();

        if (type.equals("extract")) {
            to_downloadfile = Main.current_path + Main.modpack_dir + "temp" + Main.fs + file_basename;
        }
        to_downloadfile = to_downloadfile.replace( Main.fs + Main.fs, Main.fs );

        //Main.textfeld.append("Save: " + to_downloadfile + System.lineSeparator());

        File download_dir = new File(to_downloadfile);
        if (!download_dir.getParentFile().exists()) {
            download_dir.getParentFile().mkdirs();
        }

        if (!download_dir.exists()) {
            download_file( from, to_downloadfile );
        }

        if (type.equals("extract")) {

            Path source = Paths.get(to_downloadfile);
            String target_path = Main.current_path + Main.modpack_dir + todir;
            target_path = target_path.replace(Main.fs + Main.fs, Main.fs );
            Path target = Paths.get( target_path );
            try {
                unzipFolder( source , target );
                System.out.println("UnZip " + file_basename + " Done");
            } catch (IOException e) {
                //e.printStackTrace();
                return;
            }

            if (download_dir.delete()) {
                Main.textfeld.append("Delete-ZIP: temp" + Main.fs + file_basename + System.lineSeparator());
            }
            if (download_dir.getParentFile().delete()) {
                Main.textfeld.append("Delete-ZIP: temp" + Main.fs + System.lineSeparator());
            }

        }

    }

    public static void download_file(String from, String to) {

        HttpURLConnection con = null;
        try {

            URL url = new URL( from );
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Host", "hellbz.de");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
            con.setRequestProperty("Origin", "http://hellbz.de");
            con.setRequestProperty("Referer", "https://hellbz.de");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

        } catch (IOException e) {
            Main.textfeld.append("Error" + e.getMessage() + System.lineSeparator());
            //throw new RuntimeException(e);
        }

        try (BufferedInputStream in = new BufferedInputStream( con.getInputStream() );
             FileOutputStream fileOutputStream = new FileOutputStream(to)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
        catch (IOException e) {
            Main.textfeld.append("Error" + e.getMessage() + System.lineSeparator());
            //throw new RuntimeException(e);
        }

    }

    public static void unzipFolder(Path fileZip, Path target) throws IOException {

        File destDir = new File( target.toString() + Main.fs );

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream( fileZip.toString() ));
        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

    }
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + Main.fs )) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static String file_get_contents(String url) {

        try {

            String header = " -H User-Agent: ATL-Server-Downloader by HellBz (+https://hellbz.de/contact/) ";
            String command = "curl" + header + url;

            Process p = Runtime.getRuntime().exec( command );
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder content = new StringBuilder();

            while ((line = in.readLine()) != null) {
                // System.out.println(line);
                content.append(line);
            }
            return content.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean file_put_contents ( String filename, String data ) {
        try {

            Main.textfeld.append("Create new File: " + filename +System.lineSeparator());

            FileWriter f_reader = new FileWriter(Main.current_path + Main.modpack_dir +  filename );
            BufferedWriter out = new BufferedWriter(f_reader);
            out.write(data);
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ( true );
    }

    public static String file_get_resource ( String filename ) {
        URL url = ClassLoader.getSystemResource( filename );
        String out = "";

        try {
            out = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    static public String file_get_resource_to_file(String target , String resourceName) /* throws Exception */ {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder;
        try {
            stream = Functions.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            jarFolder = new File(Functions.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            resStreamOut = Files.newOutputStream(Paths.get(Main.current_path + Main.modpack_dir + target ));
            Main.textfeld.append("Create new File: " + target + " (extracted from JAR-File)" + System.lineSeparator());
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            // throw ex;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                resStreamOut.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return Main.current_path + Main.modpack_dir + target ;
    }

    static public boolean deleteDirectory(File directoryToBeDeleted) {
            File[] allContents = directoryToBeDeleted.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteDirectory(file);
                }
            }
        return directoryToBeDeleted.delete();
    }

    static String jar_work_dir() {

        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = null;
        try {
            decodedPath = URLDecoder.decode(path, "UTF-8");

            File check_dir = new File( decodedPath );

            if ( check_dir.getName().contains(".") ){
                return check_dir.getParent();
            }else{
                return System.getProperty("user.dir");
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    static String removeNonAlphanumeric(String str)
    {
        // replace the given string
        // with empty string
        // except the pattern "[^a-zA-Z0-9]"
        str = str.replaceAll(
                "[^a-zA-Z0-9]", "");

        // return string
        return str;
    }

    static String nitrado_safename(String str)
    {
        // replace the given string
        // with empty string
        // except the pattern "[^a-zA-Z0-9]"
        str = str.replace(" ", "-");
        str = str.replaceAll("[^-a-zA-Z0-9]", "");

        // return string
        return str;
    }
    static List<String> resource_to_array ( String file ) {
        List<String> lines = null;
        URL url = ClassLoader.getSystemResource( file );
        Path path = null;
        try {
            path = Paths.get( url.toURI());
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch ( NullPointerException | FileSystemNotFoundException | URISyntaxException | IOException e) {
            //do_nothing
        }
        return lines;
    }
}