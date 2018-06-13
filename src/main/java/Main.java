import gnu.io.*;
import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

public class Main {
	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FOLDER = "credentials";
    private static final String CLIENT_SECRET_DIR = "client_secret.json";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = Main.class.getResourceAsStream(CLIENT_SECRET_DIR);
        if(in==null) {
        	System.out.println("Error: Cannot load resource");
        	System.exit(-1);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

	public static void main(String[] args) throws PortInUseException, IOException, GeneralSecurityException  {		
		/*RXTXPort c = new RXTXPort("COM3") ;
		DataInputStream br = new DataInputStream(c.getInputStream());
		String bookId = null;
		String studentNum = null;
		while(true) {
			String s = br.readLine();
			if(s!=null) {
				if(bookId == null) {
					bookId = s;
				}
				else {
					studentNum = s;
					//Transmit data to google sheets
					bookId = null;
					studentNum = null;
				}
				Socket soc = new Socket();
				System.out.println(s);
			}
		}*/
		// Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1cAj66vchiSpRpbq47uwMjejiRodvVtQUCV0umbbogT0";
        final String range = "Sheet1!A2:E";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        List<List<Object>> values = Arrays.asList(Arrays.asList(
        		"Test","Test2"
        		
        		
        		));
        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse response = service.spreadsheets().values().update(spreadsheetId,range,body).setValueInputOption("RAW")
        		.execute();
        System.out.println("Success");
	}

}
