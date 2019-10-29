package com.vihanga.sample.scim2.java.client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.vihanga.sample.scim2.java.client.Constants.ADMIN_PASSWORD_PROPERTY_KEY;
import static com.vihanga.sample.scim2.java.client.Constants.ADMIN_USERNAME_PROPERTY_KEY;
import static com.vihanga.sample.scim2.java.client.Constants.SCIM2_ENDPOINT_PREFIX_PROPERTY_KEY;
import static com.vihanga.sample.scim2.java.client.Constants.TRUST_STORE_PASSWORD_PROPERTY_KEY;
import static com.vihanga.sample.scim2.java.client.Constants.TRUST_STORE_PATH_PROPERTY_KEY;

/**
 * Sample Java client to execute SCIM2 operations.
 */
public class SCIM2Client {

    private static final Logger logger = Logger.getLogger(SCIM2Client.class.getName());

    private static final String DEFAULT_PROPERTY_FILE_PATH = "src/main/resources/app.properties";
    private static final String DEFAULT_TRUST_STORE_PATH = "src/main/resources/wso2carbon.jks";
    private static final String DEFAULT_TRUST_STORE_PASSWORD = "wso2carbon";

    private PropertyResourceBundle properties;

    public SCIM2Client() throws IOException {

        properties = new PropertyResourceBundle(new FileInputStream(DEFAULT_PROPERTY_FILE_PATH));
        init();
    }

    public SCIM2Client(PropertyResourceBundle properties) {

        this.properties = properties;
        init();
    }

    private void init() {

        String trustStorePath = DEFAULT_TRUST_STORE_PATH;
        if (properties.containsKey(TRUST_STORE_PATH_PROPERTY_KEY)) {
            trustStorePath = properties.getString(TRUST_STORE_PATH_PROPERTY_KEY);
        }

        String trustStorePassword = DEFAULT_TRUST_STORE_PASSWORD;
        if (properties.containsKey(TRUST_STORE_PASSWORD_PROPERTY_KEY)) {
            trustStorePassword = properties.getString(TRUST_STORE_PASSWORD_PROPERTY_KEY);
        }

        setKeyStore(trustStorePath, trustStorePassword);
    }

    public static void setKeyStore(String trustStorePath, String trustStorePassword) {

        logger.log(Level.INFO, "Setting keystore in path: " + trustStorePath);
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    /**
     * Get the SCIM ID of the user.
     *
     * @param username   Username of the user.
     * @return Extracted SCIM ID.
     */
    public String getUserSCIMID(String username) {

        logger.log(Level.INFO, "Getting the SCIM ID of the user: " + username);

        String urlPath = properties.getString(SCIM2_ENDPOINT_PREFIX_PROPERTY_KEY) +
                "/Users?filter=userName+Eq+" + username;
        HttpURLConnection con = null;
        String response = "";
        try {
            URL url = new URL(urlPath);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/scim+json");
            con.setRequestProperty("Authorization", "Basic " + getAuthorizationHeaderValue());

            con.connect();
            int status = con.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    response += " " + sb.toString();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while sending SCIM request to get USER ID for the user: " +
                    username + ", URL: " + urlPath, e);
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error while closing http URL connection", ex.getMessage());
                }
            }
        }

        if (!response.isEmpty()) {
            JSONObject object = new JSONObject(response);
            JSONArray resources = (JSONArray) object.get("Resources");
            return ((JSONObject) resources.get(0)).get("id").toString();
        }
        return null;
    }

    /**
     * Update user attribute using SCIM2
     *
     * @param scimID     SCIM ID of the user to be updated.
     * @param key        Attribute key to be updated.
     * @param value      Value to be updated.
     * @return Response returned from the SCIM API.
     */
    public String updateUserAttribute(String scimID, String key, String value) {

        String data = "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:PatchOp\"],\"Operations\":[" +
                "{\"op\":\"replace\",\"path\":\"" + key + "\", value:\"" + value + "\"}]}";

        return updateUserAttribute(scimID, data);
    }

    /**
     * Update extended user attribute using SCIM2
     *
     * @param scimID     SCIM ID of the user to be updated.
     * @param key        Attribute key to be updated.
     * @param value      Value to be updated.
     * @return Response returned from the SCIM API.
     */
    public String updateExtendedUserAttribute(String scimID, String key, String value) {

        String data = "{ \"schemas\": [ \"urn:ietf:params:scim:schemas:extension:enterprise:2.0:PatchOp\" ], " +
                "\"Operations\": [ { \"op\": \"replace\", \"value\":{ " +
                "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\": { \"" +
                key + "\": \"" + value + "\" } } } ] }";

        return updateUserAttribute(scimID, data);
    }

    private String updateUserAttribute(String scimID, String data) {

        String urlPath = properties.getString("scimEndpointPrefix") + "/Users/" + scimID;
        HttpURLConnection con = null;
        String msg;
        try {
            URL url = new URL(urlPath);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Accept", "application/scim+json");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Basic " + getAuthorizationHeaderValue());

            OutputStream os = con.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();

            con.connect();
            return String.valueOf(con.getResponseCode());

        } catch (IOException e) {
            msg = "Error while sending SCIM request to update DMG account locked status: ";
            logger.log(Level.SEVERE, msg, e);
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception ex) {
                    msg = "Error while closing http URL connection: " + ex.getMessage();
                    logger.log(Level.SEVERE, msg, ex);
                }
            }
        }
        return msg;
    }

    private String getAuthorizationHeaderValue() {

        String adminUsername = properties.getString(ADMIN_USERNAME_PROPERTY_KEY);
        String adminPassword = properties.getString(ADMIN_PASSWORD_PROPERTY_KEY);
        return Base64.getEncoder().encodeToString(
                (adminUsername + ":" + adminPassword).getBytes(StandardCharsets.UTF_8));
    }
}
