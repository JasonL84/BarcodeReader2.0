import gnu.io.*;
import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
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
class Book implements Comparable{
	String id;
	String name;
	String dateOut;
	String studentNum;
	boolean signedOut;
	public Book(Object[] args) {
		id = args[0].toString();
		name = args[1].toString();
		dateOut = args[2].toString();
		studentNum = args[3].toString();
		if(args[4].equals("Yes")) signedOut = true;
		else signedOut = false;
	}
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		if(id.equals(arg0.toString())) {
			System.out.println("true");
			return 0;
		}
		return -1;
	}
	public String toString() {
		return id;
	}
	public List<Object> toList(){
		List<Object> l = new ArrayList<Object>();
		l.add(id);
		l.add(name);
		l.add(dateOut);
		l.add(studentNum);
		if(signedOut) l.add("Yes");
		else l.add("No");
		return l;
	}
}
public class Main {
	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String CREDENTIALS_FOLDER = "credentials";
	private static final String CLIENT_SECRET_DIR = "client_secret.json";
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static ArrayList<Book> books = new ArrayList<Book>();
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
	private static void init(Sheets s) throws IOException {
		final String RANGE = "Sheet1!A2:E";
		List<List<Object>> values = s.spreadsheets().values().get("1cAj66vchiSpRpbq47uwMjejiRodvVtQUCV0umbbogT0",RANGE).execute().getValues();
		for(List l:values) {
			books.add(new Book(l.toArray()));
		}
	}
	private static void close(Sheets s,List<List<Object>> values) throws IOException{
		final String spreadsheetId = "1cAj66vchiSpRpbq47uwMjejiRodvVtQUCV0umbbogT0";
		final String range = "Sheet1!A2:E";
        ValueRange body = new ValueRange().setValues(values);
		UpdateValuesResponse response = s.spreadsheets().values().update(spreadsheetId,range,body).setValueInputOption("RAW")
        		.execute();
        System.out.println("Success");
	}
	public static void main(String[] args) throws PortInUseException, IOException, GeneralSecurityException  {		
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		final String spreadsheetId = "1cAj66vchiSpRpbq47uwMjejiRodvVtQUCV0umbbogT0";
		final String range = "Sheet1!A2:E";
		Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();
		init(service);
		System.out.println("Current Books are:");
		for(Book b:books) System.out.println(b);
		List<List<Object>> values = new ArrayList<List<Object>>();
		for(Book b:books) {
			values.add(b.toList());
		}
		close(service,values);
		// Build a new authorized API client service.

		/* List<List<Object>> values = Arrays.asList(Arrays.asList(
        		"Test","Test2"


        		));
        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse response = service.spreadsheets().values().update(spreadsheetId,range,body).setValueInputOption("RAW")
        		.execute();
        System.out.println("Success");*/
	}
	private static void registerBook(int n,String name,String bookId) throws IOException,PortInUseException{
		RXTXPort c = new RXTXPort("COM3") ;
		DataInputStream br = new DataInputStream(c.getInputStream());
		for(int i = 0; i<n; i++) {
			Object[] o = {bookId,name,"","",""};
			books.add(new Book(o));
			System.out.println("Successfully added book");
		}
	}
	private static void signIn(String bookId, String studentNum) throws Exception{
		RXTXPort c = new RXTXPort("COM3") ;
		DataInputStream br = new DataInputStream(c.getInputStream());

		while(true) {
			String s = br.readLine();
			if(s!=null) {
				if(bookId == null) {
					bookId = s;
				}
				else {
					studentNum = s;
					boolean bookExists = false;
					//Check if book is already checked out
					for(Book b:books) {
						if(bookId.equals(b.toString())) {
							bookExists = true;
							if(!b.signedOut) {
								System.out.println("Error book already signed in");
							}
							else {
								b.dateOut = "";
								b.studentNum = "";
								b.signedOut = false;
								System.out.println("Success, book has been signed in");
							}
						}

					}
					if(!bookExists) System.out.println("Error, cannot find book");
					bookId = null;
					studentNum = null;
				}
			}
		}
			
	}
	private static void signOut(String bookId, String studentNum) throws Exception{
		RXTXPort c = new RXTXPort("COM3") ;
		DataInputStream br = new DataInputStream(c.getInputStream());
		while(true) {
			String s = br.readLine();
			if(s!=null) {
				if(bookId == null) {
					bookId = s;
				}
				else {
					studentNum = s;
					boolean bookExists = false;
					//Check if book is already checked out
					for(Book b:books) {
						if(bookId.equals(b.toString())) {
							bookExists = true;
							if(b.signedOut) {
								System.out.println("Error book already signed out");
							}
							else {
								b.dateOut = new Date().toString();
								b.studentNum = studentNum;
								b.signedOut = true;
								System.out.println("Success, book has been signed out");
							}
						}

					}
					if(!bookExists) System.out.println("Error, cannot find book");
					bookId = null;
					studentNum = null;
				}
			}
		}
	}
}
