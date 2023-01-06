import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static java.util.Arrays.asList;

public class Main {

    static String[] choices = {"Loading..."};
    public static JComboBox<String> dbCombo = new JComboBox<>(choices);
    static JButton install_button = new JButton("Install");
    static JTextArea textfeld = new JTextArea(20, 43);

    //static String atl_server_data = Functions.file_get_contents("https://api.atlauncher.com/v1/packs/full/all");
    static String atl_server_data = Functions.file_get_contents("https://download.nodecdn.net/containers/atl/launcher/json/packsnew.json");

    static String fs = System.getProperty("file.separator");
    static String modpack_base = fs + "atl_server" + fs;
    static String modpack_dir = modpack_base;

    static String start_filename = "DNS";

    //static String current_path = Functions.current_path();
    static String current_path = Functions.jar_work_dir();


    public static void main(String[] args) throws Exception {

        //System.out.println( atl_server_data );

        System.out.println("Hello from ATL-Server Downloader!");

        //Main.textfeld.append( current_path + System.lineSeparator() );

        Properties properties = System.getProperties();
        properties.forEach((k, v) -> System.out.println( k + ":" + v));

        new ATLIPanel();

        // properties.forEach((k, v) -> Main.textfeld.append( k + ":" + v + System.lineSeparator() ));

        //JSONArray arr = new JSONObject(atl_server_data) .getJSONArray("data"); // notice that `"posts": [...]`
        JSONArray arr = new JSONArray(atl_server_data); // notice that `"posts": [...]`

        Vector<String> comboBoxItems = new Vector<>();


        for (int i = 0; i < arr.length(); i++) {
            String type =  arr.getJSONObject(i).optString("type");
            String createServer = arr.getJSONObject(i).optString("createServer");
            if ( type.equals("public") && createServer.equals("true") ){
                String post_id = arr.getJSONObject(i).optString("name");
                //System.out.println(post_id);
                comboBoxItems.add(post_id);
            }
        }

        //String client_mods = Functions.file_get_resource("client-mods.txt");
        //Main.textfeld.append( asList(client_mods.split( System.lineSeparator() )).toString()  + System.lineSeparator());

        Collections.sort(comboBoxItems);
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(comboBoxItems);

        Main.dbCombo.removeAllItems();
        Main.dbCombo.setModel(model);
        Main.install_button.setEnabled(true);

    }

    public static void install_pack( String pack ) {

        install_button.setEnabled(false);
        dbCombo.setEnabled(false);

        //JSONArray arr = new JSONObject(atl_server_data).getJSONArray("data"); // notice that `"posts": [...]`
        JSONArray arr = new JSONArray(atl_server_data); // notice that `"posts": [...]`
        JSONObject atl_server_config = null;
        boolean biomesop = false;
        boolean is_forge = false;

        Main.textfeld.setText("");
        Main.textfeld.append("Install " + pack + System.lineSeparator());
        Main.textfeld.append("---------------------------------" + System.lineSeparator());

        String modpack_safename = null;
        String modpack_version = null;
        String modpack_safename_nitrado = null;

        for (int i = 0; i < arr.length(); i++) {
            String pack_name = arr.getJSONObject(i).optString("name");
            if (Objects.equals(pack, pack_name)) {

                //Main.textfeld.append( arr.getJSONObject(i).toString() + System.lineSeparator() );
                //modpack_safename = arr.getJSONObject(i).optString("safeName");
                String modpack_name = arr.getJSONObject(i).optString("name");
                JSONArray modpack_versions = arr.getJSONObject(i).getJSONArray("versions");
                modpack_version = modpack_versions.getJSONObject(0).optString("version");

                modpack_safename = Functions.removeNonAlphanumeric(modpack_name);

                modpack_safename_nitrado = Functions.nitrado_safename(modpack_name);

                atl_server_config = new JSONObject(Functions.file_get_contents("https://download.nodecdn.net/containers/atl/packs/" + modpack_safename + "/versions/" + modpack_version + "/Configs.json"));
                // Main.textfeld.append( atl_server_config.toString() + System.lineSeparator() );

                modpack_dir = modpack_base + fs + modpack_safename + fs + modpack_version + "/";
                break;
            }
        }


        assert atl_server_config != null;
        String[] minecraft_version = atl_server_config.optString("minecraft").split("\\.");

        //Main.textfeld.append( minecraft_version[1].toString() + System.lineSeparator() );

        //Main.textfeld.append( atl_server_config.optString("minecraft") + System.lineSeparator() );

        if (atl_server_config.has("loader")) {

            Main.textfeld.append("LOADER" + System.lineSeparator());

            JSONObject loader = atl_server_config.getJSONObject("loader");

            String loader_type = loader.optString("type");

            if (Objects.equals(loader_type, "forge")) {

                is_forge = true;

                String forge_version = loader.getJSONObject("metadata").optString("version");
                String forge_minecraft = loader.getJSONObject("metadata").optString("minecraft");

                Main.textfeld.append("Type: Forge" + System.lineSeparator());
                Main.textfeld.append("Minecraft: " + forge_version + System.lineSeparator());
                Main.textfeld.append("Version: " + forge_minecraft + System.lineSeparator());

                String from;
                if (Integer.parseInt(minecraft_version[1]) > 7) {
                    from = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + forge_minecraft + "-" + forge_version + "/forge-" + forge_minecraft + "-" + forge_version + "-installer.jar";
                } else {
                    from = "https://maven.minecraftforge.net/net/minecraftforge/forge/" + forge_minecraft + "-" + forge_version + "-" + forge_minecraft + "/forge-" + forge_minecraft + "-" + forge_version + "-" + forge_minecraft + "-installer.jar";
                }

                //String to = ls + Paths.get(from).getFileName();
                String to = fs +"forge-" + forge_minecraft + "-" + forge_version + "-installer.jar";

                //Main.textfeld.append("From: " + from + System.lineSeparator());
                //Main.textfeld.append("To: " + to + System.lineSeparator());

                Functions.atl_file_download( from , to , "" , "" );
            }

            if (Objects.equals(loader_type, "fabric")) {

                String fabric_loader = loader.getJSONObject("metadata").optString("loader");
                String fabric_minecraft = loader.getJSONObject("metadata").optString("minecraft");

                JSONArray fabric_get_installer = new JSONArray(Functions.file_get_contents("https://meta.fabricmc.net/v2/versions/installer/"));
                String fabric_latest_installer = fabric_get_installer.getJSONObject(0).optString("version");

                Main.textfeld.append("Type: Fabric" + System.lineSeparator());
                Main.textfeld.append("Minecraft: " + fabric_minecraft + System.lineSeparator());
                Main.textfeld.append("Loader: " + fabric_loader + System.lineSeparator());
                Main.textfeld.append("Installer: " + fabric_latest_installer + System.lineSeparator());

                String from = "https://meta.fabricmc.net/v2/versions/loader/" + fabric_minecraft + "/" + fabric_loader + "/" + fabric_latest_installer + "/server/jar";
                String to = "/fabric-" + fabric_minecraft + "-" + fabric_loader + ".jar";
                to = fs + start_filename + ".jar";

                //Main.textfeld.append("From: " + from + System.lineSeparator());
                //Main.textfeld.append("To: " + to + System.lineSeparator());

                Functions.atl_file_download( from , to , "" , "" );

            }
            Main.textfeld.append("---------------------------------" + System.lineSeparator());
        }

        if (atl_server_config.has("libraries")) {

            Main.textfeld.append("LIBRARIES" + System.lineSeparator());

            JSONArray libs = atl_server_config.getJSONArray("libraries");
            for (int i = 0; i < libs.length(); i++) {

                JSONObject lib = libs.getJSONObject(i);

                if (Objects.equals(lib.optString("download"), "server")) {

                    String from;

                    if (Objects.equals(lib.optString("url").substring(0, 3), "http")) {
                        from = lib.optString("url");
                    } else {
                        from = "https://download.nodecdn.net/containers/atl/" + lib.optString("url");
                    }
                    String to = fs +"libraries" + fs + lib.optString("server");

                    //Main.textfeld.append("From: " + from + System.lineSeparator());
                    //Main.textfeld.append("To: " + to + System.lineSeparator());

                    Functions.atl_file_download( from , to , "" , "" );

                }
            }
            Main.textfeld.append("---------------------------------" + System.lineSeparator());
        }

        if (atl_server_config.has("mods")) {

            Main.textfeld.append("MODS" + System.lineSeparator());

            JSONArray mods = atl_server_config.getJSONArray("mods");
            for (int i = 0; i < mods.length(); i++) {

                JSONObject mod = mods.getJSONObject(i);

                Main.textfeld.append("Mod "+ i +" from " + mods.length() + System.lineSeparator());
                Main.textfeld.append("Mod-Name "+ mod.optString("name") + System.lineSeparator());

                    String from;
                    String to = null;
                    String extractFolder = null;
                    String filename = mod.optString("file");
                    String mod_base = "" + Paths.get(filename).getFileName();

                    if ( Objects.equals(mod.optString("url").substring(0, 4), "http") ) {
                        from = mod.optString("url");
                    } else {
                        from = "https://download.nodecdn.net/containers/atl/" + mod.optString("url");
                    }

                    switch (mod.optString("type")) {
                        case "mods":
                            to = fs + "mods" + fs + mod_base;
                            break;
                        case "dependency":
                            to = fs + "mods" + fs + atl_server_config.optString("minecraft") + fs + mod_base;
                            break;
                        case "ic2lib":
                            to = fs + "mods" + fs + "ic2" + fs + mod_base;
                            break;
                        case "denlib":
                            to = fs + "mods" + fs + "denlib" + fs + mod_base;
                            break;
                        case "plugins":
                            to = fs + "plugins" + fs + mod_base;
                            break;
                        case "coremods":
                            to = fs + "coremods" + fs + mod_base;
                            break;
                        case "texturepack":
                        case "texturepackextract":
                            to = fs + "texturepacks" + fs + mod_base;
                            break;
                        case "resourcepack":
                        case "resourcepackextract":
                            to = fs + "resourcepacks" + fs + mod_base;
                            break;
                        case "shaderpack":
                        case "shaderpackextract":
                            to = fs + "shaderpacks" + fs + mod_base;
                            break;
                        case "forge":
                        case "extract":
                        default:
                            to = fs + mod_base;
                            break;
                    }

                System.out.println( "Downloading Mod " + mod.optString("file") );



/*
    https://github.com/ATLauncher/ATLauncher/blob/ab4a196af0cab1e5bae8504a3da3139a84b137af/src/main/java/com/atlauncher/data/json/Mod.java

 */

                    if (Objects.equals(mod.optString("type"), "extract")) {

                        switch (mod.optString("extractTo")) {
                            case "'root'":
                                extractFolder = fs;
                                break;
                            case "mods":
                                extractFolder = fs + "mods" + fs;
                                break;
                            default:
                                extractFolder = fs;
                                break;
                        }
                    }
                    /*String[] badmods = {
                            "aquatweaks",
                            "battletext",
                            "betterfps",
                            "custommainmenu",
                            "dynamicsurroundings",
                            "dynamiclights",
                            "resourceloader",
                            "optifine",
                            "oculus",
                            "legendarytooltips",
                            "zyinshud"
                    };*/
                List<String> badmods;
                String client_mods = Functions.file_get_resource("client-mods.txt");
                badmods = asList(client_mods.split(System.lineSeparator()));

                final String regex = "(" + String.join("|", badmods ) + ")";

                if (Functions.pregMatch(regex, to.toLowerCase()) || Objects.equals(mod.optString("server"), "false")) {
                    to = to + ".disabled";
                }

                if (Functions.pregMatch("(biomesoplenty)", to.toLowerCase())) {
                    biomesop = true;
                }

                Functions.atl_file_download( from , to , mod.optString("type")  , extractFolder );

            }
            Main.textfeld.append("---------------------------------" + System.lineSeparator());
        }

        File configs = new File(modpack_dir + "config" + fs );
        if( !configs.exists() ) {

            Main.textfeld.append("CONFIGS" + System.lineSeparator());

            String modpack_zip = "https://download.nodecdn.net/containers/atl/packs/" + modpack_safename + "/versions/" + modpack_version + "/Configs.zip";

            Functions.atl_file_download( modpack_zip , "Configs.zip", "extract", fs );

            Main.textfeld.append("---------------------------------" + System.lineSeparator());
        }

        if (atl_server_config.has("libraries")) {

            Main.textfeld.append("SERVER-JAR" + System.lineSeparator());

            String vanilla_versions = Functions.file_get_contents("https://download.nodecdn.net/containers/atl/launcher/json/minecraft_versions.json");
            JSONArray v_versions = new JSONObject(vanilla_versions).getJSONArray("versions");

            String get_vanilla_json = null;
            for (int i = 0; i < v_versions.length(); i++) {

                String id = v_versions.getJSONObject(i).optString("id");
                String type = v_versions.getJSONObject(i).optString("type");

                if (Objects.equals(id, atl_server_config.optString("minecraft")) && Objects.equals(type, "release")) {
                    get_vanilla_json = v_versions.getJSONObject(i).optString("url");
                    break;
                }
            }

            if ( null != get_vanilla_json ){

                String vanilla_server = Functions.file_get_contents(get_vanilla_json);
                JSONObject vanilla_server_download = new JSONObject(vanilla_server).getJSONObject("downloads");
                JSONObject vanilla_server_files = vanilla_server_download.getJSONObject("server");
                String server_configfile = vanilla_server_files.optString("url");

                Functions.atl_file_download( server_configfile , fs + "minecraft_server." + atl_server_config.optString("minecraft")+ ".jar" , "" , fs );

                //Main.textfeld.append(server_configfile + System.lineSeparator());
            }

            Main.textfeld.append("---------------------------------" + System.lineSeparator());
        }



        if (atl_server_config.has("deletes")) {

            Main.textfeld.append("DELETES" + System.lineSeparator());

            JSONObject deletes = atl_server_config.getJSONObject("deletes");

            if (deletes.has("folders")) {

                JSONArray folders = deletes.getJSONArray("folders");

                for (int i = 0; i < folders.length(); i++) {

                    JSONObject delete = folders.getJSONObject(i);

                    String where = "";

                    switch (delete.optString("base")) {
                        case "mods":
                            where =  "mods" + fs ;
                            break;
                        case "root":
                        default:
                            where = "";
                            break;
                    }

                    Main.textfeld.append("DELETE " + where + delete.optString("target") + System.lineSeparator());
                    Functions.deleteDirectory( new File( current_path + modpack_dir + where + delete.optString("target")  ) );
                }

            }


/*
            echo '<br><br><br><b>DELETES</b><br>';

            $deletes = $config->deletes;

            if ( @is_array( $deletes->folders ) ){

                foreach( $deletes->folders as $id => $delete ){

                        switch ($delete->base) {
                            case 'root':
                                $where = $modpack_dir;
                                break;
                            case 'mods':
                                $where = $modpack_dir."mods/";
                                break;
                            default:
                                $where = $modpack_dir;
                                break;
                        }
                    File directoryToBeDeleted = new File( Main.current_path + Main.modpack_dir + target );
                    if ( directoryToBeDeleted.exists() ) {
                         Main.current_path + Main.modpack_dir + target
                        Functions.deleteDirectory()
                        deleteDir( $where. $mod->target );
                        echo '<br>'.'FILE: ' . $where. $delete->target ;
                    }

                }
            }
        }
         */

            Main.textfeld.append("---------------------------------" + System.lineSeparator());
        }

        Main.textfeld.append("REFINEMENTS" + System.lineSeparator());

        //direct Copy
        Functions.file_get_resource_to_file(  "server-icon.png"  , "/stuff/server-icon.png" );



        //Modificated Files
        String server_properties = Functions.file_get_resource("stuff/server.properties");
        if ( biomesop ) {

            Integer mc_version = Integer.parseInt(minecraft_version[1]);
            if ((mc_version > 12) && (mc_version < 18)){
                server_properties = server_properties.replace("level-type=DEFAULT", "level-type=biomesoplenty");
                Main.textfeld.append("BiomesOPlenty: Change level-type to biomesoplenty" + System.lineSeparator());
            }else{
                server_properties = server_properties.replace("level-type=DEFAULT", "level-type=BIOMESOP");
                Main.textfeld.append("BiomesOPlenty: Change level-type to BIOMESOP" +System.lineSeparator());
            }
        }

        if ( new File( Main.current_path + Main.modpack_dir + "server.properties" ).exists()  ) {
            Path source = Paths.get(Main.current_path + Main.modpack_dir + "server.properties");
            Main.textfeld.append("Try to Backup old server.properties" + System.lineSeparator());
            try {
                Files.move(source, source.resolveSibling("server.properties.bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Functions.file_put_contents(  "server.properties"  , server_properties );
        Functions.file_put_contents( "eula.txt"  , "eula=true" );
        Functions.file_put_contents( "no_pm"  , "" );

        // $modpack_file  = ucfirst( preg_replace('/([A-Z0-9])/', '-$1', lcfirst($modpack_name) ) ) .'-'.$config->version.'-'. $config->minecraft . ".txt";
        Functions.file_put_contents( modpack_safename_nitrado + "-" + modpack_version + "-" + atl_server_config.optString("minecraft") + ".txt" , "https://atlauncher.com/pack/"  + modpack_safename );

        if (atl_server_config.optString("minecraft").substring(0, 4).equals("1.19")){

            Functions.file_put_contents(  "force_java17"  , "" );
            // if( is_forge ) Functions.file_get_resource_to_file(  start_filename + ".jar"  , "/stuff/minecraft_server.jar" );

        } else if (atl_server_config.optString("minecraft").substring(0, 4).equals("1.18")){

            Functions.file_put_contents( "force_java17"  , "" );
            // if( is_forge ) Functions.file_get_resource_to_file(  start_filename + ".jar"  , "/stuff/minecraft_server.jar" );

        } else if (atl_server_config.optString("minecraft").substring(0, 4).equals("1.17")){

            Functions.file_put_contents( "force_java16"  , "" );
            // if( is_forge ) Functions.file_get_resource_to_file(  start_filename + ".jar"  , "/stuff/minecraft_server.jar" );

        } else if (atl_server_config.optString("minecraft").equals("1.16.5")){

            Functions.file_put_contents( "force_java11"  , "" );

        }else{
            Functions.file_put_contents( "force_java8"  , "" );
        }

        if( is_forge ) Functions.file_get_resource_to_file(  start_filename + ".jar"  , "/stuff/minecraft_server.jar" );

        if ( is_forge && atl_server_config.optString("minecraft").substring(0, 3).equals("1.6") ) {
            Functions.atl_file_download( "https://cdn.atlcdn.net/legacyjavafixer-1.0.jar" , "legacyjavafixer-1.0.jar" , "mods" , "" );
        }

        Main.textfeld.append("---------------------------------" + System.lineSeparator());

        install_button.setEnabled(true);
/*
        try {
            Thread.sleep(3000 );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

*/
        dbCombo.setEnabled(true);
        install_button.setEnabled(true);

    }

}