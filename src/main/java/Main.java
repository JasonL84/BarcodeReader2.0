import gnu.io.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

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
		if(args.length>2) {
			dateOut = args[2].toString();
			studentNum = args[3].toString();
			if(args[4].equals("Yes")) signedOut = true;
			else signedOut = false;
		}
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
	private static Sheets service;
	private static String range;
	public static boolean doneLoading = false;
	public static boolean isClose = false;
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
	private static void init(Sheets s,String port) throws IOException {
		List<List<Object>> values = s.spreadsheets().values().get("1OArA90Lt5Rgt169X8gk4wvQSmqvzE0PKD1KdNmTxY4o",range).execute().getValues();
		for(List l:values) {
			books.add(new Book(l.toArray()));
		}
		System.out.println("Current Books are:");
		for(Book b:books) System.out.println(b);
		UIThread t = new UIThread();
		t.start(port);
	}
	public static String getBook(String bookNum) {
		for(Book b:books) {
			if(b.id.equals(bookNum)) {
				return b.name;
			}
		}
		return null;
	}
	public static void close() throws IOException{
		final String spreadsheetId = "1OArA90Lt5Rgt169X8gk4wvQSmqvzE0PKD1KdNmTxY4o";
		List<List<Object>> values = new ArrayList<List<Object>>();
		for(Book b:books) {
			values.add(b.toList());
		}
		ValueRange body = new ValueRange().setValues(values);
		UpdateValuesResponse response = service.spreadsheets().values().update(spreadsheetId,range,body).setValueInputOption("RAW")
				.execute();

		System.out.println("Success");
		System.exit(0);
	}
	public static void main(String[] args) throws PortInUseException, IOException, GeneralSecurityException, InterruptedException  {		

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		final String spreadsheetId = "1OArA90Lt5Rgt169X8gk4wvQSmqvzE0PKD1KdNmTxY4o";
		service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();
		InputStream in = Main.class.getResourceAsStream("preferences.txt");
		if(in == null) {
			firstTimeSetup();
			while (!doneLoading) {
				if(doneLoading) break;
				Thread.sleep(1000);
				}
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String[] s = br.readLine().split(",");
		range = s[0]+"!B5:G";

		init(service,s[1]);

		System.out.println("Init complete");
	}
	public static void registerBook(String name,String bookId) throws IOException,PortInUseException{
			Object[] o = {bookId,name,"","",""};
			for(Book b:books) {
				if(b.id.equals(bookId)) {
					System.out.println("Error book already exists");
					return;
				}
			}
			books.add(new Book(o));
			System.out.println("Successfully added book");
	}
	public static void signIn(String bookId, String studentNum) throws Exception{

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



	public static void signOut(String bookId, String studentNum) throws Exception{
		boolean bookExists = false;
		//Check if book is already checked out
		System.out.println("Seraching for book "+bookId);
		for(Book b:books) {
			System.out.println("Comparing to book "+b.toString());
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
	public static void firstTimeSetup() throws IOException{
		JFrame frame = new JFrame("First Time Setup");
		JTextArea sheet = new JTextArea();
		JLabel sheetLabel = new JLabel("Sheet ID");
		List pa = Collections.list(CommPortIdentifier.getPortIdentifiers()); 
		ArrayList<String> s = new ArrayList<String>();
		for(Object o:pa) {
			s.add(((CommPortIdentifier)o).getName());
		}
		JComboBox portCombo = new JComboBox(s.toArray());
		JPanel p = new JPanel();
		JLabel portLabel = new JLabel("Port Reader is attatched to");
		JLabel title = new JLabel("First time setup");
		JButton submit = new JButton("Done");
		submit.addActionListener(new SubmitListener(frame,sheet,portCombo));
		p.setLayout(new BoxLayout(p,BoxLayout.PAGE_AXIS));
		p.add(title);
		p.add(sheetLabel);
		p.add(sheet);
		p.add(portLabel);
		p.add(portCombo);
		p.add(submit);
		frame.add(p);
		frame.pack();
		frame.show();
		System.out.println("Frame should be shown");
	}
}
class SubmitListener implements ActionListener{
	JFrame frame;
	File file;
	List<JTextComponent> comp;
	String out;
	JTextArea a;
	JComboBox c;
	public SubmitListener(JFrame f,JTextArea a1, JComboBox c1) throws IOException {
		frame = f;
		a = a1;
		c = c1;

	}
	@Override
	public void actionPerformed(ActionEvent e){
		file = new File(".\\src\\main\\resources\\preferences.txt");
		System.out.println("Submitting");
		Main.doneLoading = true;
		BufferedWriter br;
		try {
			out = a.getText()+","+c.getSelectedItem();
			FileWriter f = new FileWriter(file);
			br = new BufferedWriter(f);
			br.write(out);
			System.out.println("Writing "+out+"to "+file.toString());
			br.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		frame.dispose();
	}
	
}


