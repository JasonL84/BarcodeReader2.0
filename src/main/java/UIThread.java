
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.IOException;

import javax.swing.*;

import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
class PanelComp extends JPanel{
	JComponent labelPanel = new JPanel();
	JComponent textPanel = new JPanel();
	JTextArea StudNumA = new JTextArea(0,36);
	JTextArea BookNumA = new JTextArea(0,36);
	JTextArea BookNameA = new JTextArea(0,36);
	public PanelComp() {
		textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.Y_AXIS));
		labelPanel.setLayout(new BoxLayout(labelPanel,BoxLayout.Y_AXIS));
		labelPanel.add(new JLabel("Student Number"));
		textPanel.add(StudNumA);
		textPanel.add(BookNumA);
		textPanel.add(BookNameA);
		labelPanel.add(new JLabel("Book ID"));
		labelPanel.add(new JLabel("Book Name"));
		this.add(textPanel);
		this.add(labelPanel);
	}
}
public class UIThread extends Thread{
	String port;
	JButton SubmitCO = new JButton("Submit");
	JButton SubmitCI = new JButton("Submit");
	JButton SubmitReg = new JButton("Submit");
	PanelComp checkOutPanel = new PanelComp();
	PanelComp checkInPanel = new PanelComp();
	PanelComp registerPanel = new PanelComp();
	JTabbedPane pane = new JTabbedPane();
	JFrame frame = new JFrame("Test");
	public void start(String s) {
		SubmitCO.addActionListener(new SubmitListener("Check Out"));
		checkOutPanel.add(SubmitCO,BorderLayout.SOUTH);
		SubmitCI.addActionListener(new SubmitListener("Check In"));
		checkInPanel.add(SubmitCI,BorderLayout.SOUTH);
		SubmitReg.addActionListener(new SubmitListener("Register"));
		registerPanel.add(SubmitReg,BorderLayout.SOUTH);
		pane.addTab("Checkout", checkOutPanel);
		pane.addTab("Check In", checkInPanel);
		checkOutPanel.BookNameA.setEditable(false);
		checkInPanel.BookNameA.setEditable(false);
		pane.addTab("Register", registerPanel);
		frame.add(pane);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowClose());
		frame.show();
		frame.toFront();
		frame.pack();
		frame.repaint();
		port = s;
		start();
	}
	public void start() {
		run();
	}
	public void run() {
		RXTXPort c = null;
		try {
			c = new RXTXPort(port);
		} catch (PortInUseException e) {
			e.printStackTrace();
		}
		DataInputStream br = new DataInputStream(c.getInputStream());
		String in = null;
		while(true) {
			if(pane.getSelectedIndex() == 0) {
				String bookName;
				bookName = Main.getBook(checkOutPanel.BookNumA.getText());
				checkOutPanel.BookNameA.setText(bookName);
			}
			else if(pane.getSelectedIndex() == 1) {
				String bookName;
				bookName = Main.getBook(checkInPanel.BookNumA.getText());
				checkInPanel.BookNameA.setText(bookName);
			}
			try {
				in = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(in != null) {
				System.out.println(in);
				if(pane.getSelectedIndex()==0) {
					if(checkOutPanel.StudNumA.getText().equals("")) {
						checkOutPanel.StudNumA.setText(in);
					}
					else if(checkOutPanel.BookNumA.getText().equals("")) {
						checkOutPanel.BookNumA.setText(in);
					}
				}
			}
			else if(pane.getSelectedIndex()==1) {
				if(checkInPanel.StudNumA.getText().equals("")) {
					checkInPanel.StudNumA.setText(in);
				}
				else if(checkInPanel.BookNumA.getText().equals("")) {
					checkInPanel.BookNumA.setText(in);
				}

			}



			frame.repaint();
		}
	}

	private class SubmitListener implements ActionListener{
		String type;
		public SubmitListener(String type) {
			this.type = type;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {

			try {
				switch(type) {
				case "Check Out":
					Main.signOut(checkOutPanel.BookNumA.getText(), checkOutPanel.StudNumA.getText());
					checkOutPanel.BookNumA.setText(null);
					checkOutPanel.StudNumA.setText(null);
				case "Check In": 
					Main.signIn(checkInPanel.BookNumA.getText(), checkInPanel.StudNumA.getText());
					checkInPanel.BookNumA.setText(null);
					checkInPanel.StudNumA.setText(null);
				case "Register": 
					Main.registerBook(registerPanel.BookNameA.getText(), registerPanel.BookNumA.getText());
					registerPanel.BookNumA.setText(null);
					registerPanel.BookNameA.setText(null);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	class WindowClose implements WindowListener{

		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			try {
				Main.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			frame.dispose();
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

	}
}
