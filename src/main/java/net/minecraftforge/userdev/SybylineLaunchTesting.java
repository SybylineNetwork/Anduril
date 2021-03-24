/*
 * Minecraft Forge
 * Copyright (c) 2016-2019.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.userdev;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import cpw.mods.modlauncher.Launcher;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SybylineLaunchTesting
{
    public static void main(String... args) throws InterruptedException
    {
        final String markerselection = System.getProperty("forge.logging.markers", "");
        Arrays.stream(markerselection.split(",")).forEach(marker-> System.setProperty("forge.logging.marker."+ marker.toLowerCase(Locale.ROOT), "ACCEPT"));

        ArgumentList lst = ArgumentList.from(args);

        String target = lst.getOrDefault("launchTarget", System.getenv().get("target"));

        if (target == null) {
            throw new IllegalArgumentException("Environment variable target must be set.");
        }

        lst.putLazy("gameDir", ".");
        lst.putLazy("launchTarget", target);
        lst.putLazy("fml.mcpVersion", System.getenv("MCP_VERSION"));
        lst.putLazy("fml.mcVersion", System.getenv("MC_VERSION"));
        lst.putLazy("fml.forgeGroup", System.getenv("FORGE_GROUP"));
        lst.putLazy("fml.forgeVersion", System.getenv("FORGE_VERSION"));

        if (target.contains("client")) {
            hackNatives();
/* Sybyline start */
try {
// GraalVM support - gotta figure out how to load the native libs without crashing
// new java.net.Socket().close();
// javax.imageio.ImageIO.class.getName();
//  sun.java2d.Disposer.class.getName();
}catch(Throwable e){}
/* Sybyline end */
            lst.putLazy("version", "MOD_DEV");
            lst.putLazy("assetIndex", System.getenv("assetIndex"));
            lst.putLazy("assetsDir", System.getenv().getOrDefault("assetDirectory", "assets"));

            String assets = lst.get("assetsDir");
            if (assets == null || !new File(assets).exists()) {
                throw new IllegalArgumentException("Environment variable 'assetDirectory' must be set to a valid path.");
            }

            if (!lst.hasValue("accessToken")) {
                if (!login(lst)) {
                    String username = lst.get("username");
                    if (username != null) { // Replace '#' placeholders with random numbers
                        Matcher m = Pattern.compile("#+").matcher(username);
                        StringBuffer replaced = new StringBuffer();
                        while (m.find()) {
                            m.appendReplacement(replaced, getRandomNumbers(m.group().length()));
                        }
                        m.appendTail(replaced);
                        lst.put("username", replaced.toString());
                    } else {
                        lst.putLazy("username", "Dev");
                    }
                    lst.put("accessToken", "DONT_CRASH");
                    lst.put("userProperties", "{}");
                }
            }
        }

        if (Arrays.asList(
                "fmldevclient", "fmldevserver", "fmldevdata",
                "fmluserdevclient", "fmluserdevserver", "fmluserdevdata"
            ).contains(target)) {
            //nop
        } else {
            throw new IllegalArgumentException("Unknown value for 'target' property: " + target);
        }

        Launcher.main(lst.getArguments());
        Thread.sleep(10000);// Why do we have this? -Lex 03/06/19
    }

    private static String getRandomNumbers(int length)
    {   // Generate a time-based random number, to mimic how n.m.client.Main works
        return Long.toString(System.nanoTime() % (int) Math.pow(10, length));
    }

    private static void hackNatives()
    {
        String paths = System.getProperty("java.library.path");
        String nativesDir = System.getenv().get("nativesDirectory");

        if (Strings.isNullOrEmpty(paths))
            paths = nativesDir;
        else
            paths += File.pathSeparator + nativesDir;

        System.setProperty("java.library.path", paths);

        // hack the classloader now.
        try
        {
            final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set(null, null);
        }
        catch(Throwable t) {}
    }

    /**
     * Basic implementation of Mojang's 'Yggdrasil' login system, purely intended as a dev time bare bones login.
     * Login errors are not handled.
     * Do not use this unless you know what you are doing and must use it to debug things REQUIRING authentication.
     * Forge is not responsible for any auth information passed in, saved to logs, run configs, etc...
     * BE CAREFUL WITH YOUR LOGIN INFO
     */
//Sybyline start
    private static boolean login(ArgumentList args) {
    	try {
	        if (!args.hasValue("username") || !args.hasValue("password")) {
	            return false;
	        }
	        UserAuthentication auth = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
	        String user = args.get("username");
	        String pass = args.remove("password");
	        String username, uuid, accessToken, userProperties;
	        File dir = new File(System.getProperty("user.home"), "/.slt/");
	        File dataFile = new File(dir, user+".aes");
	        File hashFile = new File(dir, user+".md5");
	        Logger log = LogManager.getLogger();
	        final String MD5 = "MD5", AES = "AES";
	        try {
		        auth.setUsername(user);
		        auth.setPassword(pass);		        
	            auth.logIn();
	            username = auth.getSelectedProfile().getName();
	            uuid = auth.getSelectedProfile().getId().toString().replace("-", "");
	            accessToken = auth.getAuthenticatedToken();
	            userProperties = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().getAdapter(PropertyMap.class).toJson(auth.getUserProperties());
	            if (Boolean.getBoolean("slt.enable_offline")) try {
	            	log.info("Login succeeded! Writing encrypted state to cache...");
	            	byte[] data = (username + "\n" + uuid + "\n" + accessToken + "\n" + userProperties).getBytes(StandardCharsets.UTF_8);
	            	Cipher cipher = Cipher.getInstance(AES);
	            	cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(getKey(user, pass), AES));
	            	byte[] hash = MessageDigest.getInstance(MD5).digest(data);
	            	data = cipher.doFinal(data);
	            	dir.mkdirs();
	            	dataFile.createNewFile();
	            	Files.write(dataFile.toPath(), data);
	            	hashFile.createNewFile();
	            	Files.write(hashFile.toPath(), hash);
	            	log.info("Hash is: " + UUID.nameUUIDFromBytes(hash));
	            } catch(Exception ce) {
	            	ce.printStackTrace();
	            } else {
	            	log.info("Login succeeded! State encryption cache is disabled (didn't save state), use -Dslt.enable_offline=true to enable");
	            }
	        } catch (Exception e) {
	            log.error("Login failed!", e);
	            if (Boolean.getBoolean("slt.enable_offline")) try {
	                log.info("Searching for encrypted cache...");
	                if (!dir.isDirectory())
	                	throw new FileNotFoundException("Cache directory does not exist: " + dir.toString());
	                if (!dataFile.isFile())
	                	throw new FileNotFoundException("Cache data file does not exist: " + dataFile.toString());
	                if (!hashFile.isFile())
	                	log.error("Cache hash does not exist: " + hashFile.toString() + ", attempting launch anyway...");
	            	byte[] data = Files.readAllBytes(dataFile.toPath());
	            	UUID hashExpected = hashFile.isFile() ? UUID.nameUUIDFromBytes(Files.readAllBytes(hashFile.toPath())) : new UUID(0L, 0L);
	            	Cipher cipher = Cipher.getInstance(AES);
	            	cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getKey(user, pass), AES));
	            	data = cipher.doFinal(data);
	            	UUID hashFound = UUID.nameUUIDFromBytes(MessageDigest.getInstance(MD5).digest(data));
	            	if (hashExpected.equals(hashFound)) {
	                	log.info("Hash is correct: " + hashFound);
	            	} else {
	                	log.error("Hash is incorrect: expected " + hashExpected + ", found " + hashFound + ". Attempting to launch anyway...");
	            	}
	            	String[] lines = new String(data, StandardCharsets.UTF_8).split("\\R", 4);
	            	username = lines[0];
	            	uuid = lines[1];
	            	accessToken = lines[2];
	            	userProperties = lines[3];
	                log.info("Found encrypted cache! Trying to launch: "+username+"("+uuid+")");
				} catch (Exception ce) {
	                log.error("Couldn't find encrypted cache.", ce);
					e.addSuppressed(ce);
		            throw new RuntimeException(e); // don't set other variables
				} else {
	            	log.info("State encryption cache is disabled (didn't read state), use -Dslt.enable_offline=true to enable");
		            throw new RuntimeException(e); // don't set other variables
				}
	        }
	        args.put("username", username);
	        args.put("uuid", uuid);
	        args.put("accessToken", accessToken);
	    	args.put("userProperties", userProperties);
	        return true;
    	} finally {
    		args.remove("password"); //Just in case, so it shouldn't show up anywhere.
    	}
    }
    private static byte[] getKey(String user, String pass) {
    	byte[] raw = (user+"::"+pass).getBytes(StandardCharsets.UTF_8);
    	byte[] ret = new byte[16];
    	for (int i = 0; i < raw.length; i++)
    		ret[i % 16] ^= raw[i];
    	return ret;
    }
/* // Sybyline end
    private static boolean login(ArgumentList args) {
        if (!args.hasValue("username") || !args.hasValue("password")) {
            args.remove("password"); //Just in case, so it shouldn't show up anywhere.
            return false;
        }
        UserAuthentication auth = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(args.get("username"));
        auth.setPassword(args.remove("password"));
        try {
            auth.logIn();
        } catch (AuthenticationException e) {
            LogManager.getLogger().error("Login failed!", e);
            throw new RuntimeException(e); // don't set other variables
        }
        args.put("username",       auth.getSelectedProfile().getName());
        args.put("uuid",           auth.getSelectedProfile().getId().toString().replace("-", ""));
        args.put("accessToken",    auth.getAuthenticatedToken());
        args.put("userProperties", auth.getUserProperties().toString());
        return true;
    }
*/ // Sybyline
}