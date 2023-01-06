import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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
                unzipFolder(source, target);
                System.out.println("Done");
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
        try (BufferedInputStream in = new BufferedInputStream(new URL(from).openStream());
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

    public static void unzipFolder(Path source, Path target) throws IOException {

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(source.toFile().toPath()))) {

            // list files in zip
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {

                boolean isDirectory = false;
                // example 1.1
                // some zip stored files and folders separately
                // e.g data/
                //     data/folder/
                //     data/folder/file.txt
                if (zipEntry.getName().endsWith(File.separator)) {
                    isDirectory = true;
                }

                Path newPath = zipSlipProtect(zipEntry, target);

                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {

                    // example 1.2
                    // some zip stored file path only, need create parent directories
                    // e.g data/folder/file.txt
                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }

                    // copy files, nio
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);

                    // copy files, classic
                    /*try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }*/
                }

                zipEntry = zis.getNextEntry();

            }
            zis.closeEntry();

        }

    }

    // protect zip slip attack
    public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
            throws IOException {

        // test zip slip vulnerability
        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
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