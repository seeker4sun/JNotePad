package changwon;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JNotePad extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextPane    _textPane  = new JTextPane();
	private ActionMap    _actionMap;// = new ActionMap();
	private boolean      _isSaved   = true;
	private JFileChooser _fc        = new JFileChooser(".");
	private File         _file      = null;
	
	public JNotePad() {
		super("JNotePad");
		 
		_textPane.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				_isSaved = false;
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				_isSaved = false;
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				_isSaved = false;
			}
		});

		_actionMap = createActionMap();
		
		JScrollPane scrollPaneOfTextPane = new JScrollPane(_textPane);
		JMenuBar    menuBar = createMenuBar();
        JToolBar    toolBar = createToolBar();
        
		setJMenuBar(menuBar);
		add(scrollPaneOfTextPane);
		add(toolBar, BorderLayout.NORTH);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public ActionMap createActionMap() {
		ActionMap am = new ActionMap ();
		am.put("new",  new NewAction());
		am.put("open", new OpenAction());
		am.put("save", new SaveAction());
		am.put("save as", new SaveAsAction());
		am.put("exit", new ExitAction());
		am.put("cut",  new CutAction());
		am.put("copy", new CopyAction());
		am.put("paste",new PasteAction());
		am.put("help", new HelpAction());
		am.put("about",new AboutAction());
		
		return am;
	}
	
	public JMenuBar createMenuBar() {
		// File
		JMenu miFile = new JMenu("File");
		miFile.setMnemonic(KeyEvent.VK_F);
		miFile.add(new JMenuItem(_actionMap.get("new")));
		miFile.add(new JMenuItem(_actionMap.get("open")));
		miFile.add(new JMenuItem(_actionMap.get("save")));
		miFile.add(new JMenuItem(_actionMap.get("save as")));
		miFile.addSeparator();
		miFile.add(new JMenuItem(_actionMap.get("exit")));

		// Edit
		JMenu miEdit = new JMenu("Edit");
		miEdit.setMnemonic(KeyEvent.VK_E);
		miEdit.add(new JMenuItem(_actionMap.get("cut")));
		miEdit.add(new JMenuItem(_actionMap.get("copy")));
		miEdit.add(new JMenuItem(_actionMap.get("paste")));

		// Help
		JMenu miHelp = new JMenu("Help");
		miHelp.setMnemonic(KeyEvent.VK_H);
		miHelp.add(new JMenuItem(_actionMap.get("help")));
		miHelp.add(new JMenuItem(_actionMap.get("about")));
		
		JMenuBar menubar = new JMenuBar();
		menubar.add(miFile);
		menubar.add(miEdit);
		menubar.add(miHelp);
		
		return menubar;
	}
	
	public JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.add(new JButton(_actionMap.get("new")));
		toolbar.add(new JButton(_actionMap.get("open")));
		toolbar.add(new JButton(_actionMap.get("save")));
		toolbar.add(new JButton(_actionMap.get("save as")));
		toolbar.addSeparator();
		toolbar.add(new JButton(_actionMap.get("cut")));
		toolbar.add(new JButton(_actionMap.get("copy")));
		toolbar.add(new JButton(_actionMap.get("paste")));
		toolbar.addSeparator();
		toolbar.add(new JButton(_actionMap.get("help")));
		toolbar.add(new JButton(_actionMap.get("about")));
		
		Component[] comps = toolbar.getComponents();
		for(int i=0; i<comps.length;++i) {
			if(comps[i] instanceof JButton) {
				comps[i].setFocusable(false);
			}
		}
		
		return toolbar;
	}

	private boolean confirmSave() {
		if(_isSaved) return true;
		int ret = JOptionPane.showConfirmDialog(this, "Content has been modified. Save changes?","JNotePad", JOptionPane.YES_NO_CANCEL_OPTION);
		switch (ret) {
		case JOptionPane.YES_OPTION: {
			return save();
		}
		case JOptionPane.NO_OPTION: {
			return true;
		}
		default:
			return false;
		}
	}
	
	private boolean open() {
		if(_fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return false;
		
		File file = _fc.getSelectedFile();
		try {
			open(file);
			_file = file;
			setTitle(file.getName() + " - JNotePad");
			return true;
		} catch(IOException e) {
			JOptionPane.showMessageDialog(this, "Could not open file" + file,  "JNotePad", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private void open(File file) throws IOException {
		BufferedReader r     = new BufferedReader(new FileReader(file));
		StringBuffer   sbuf  = new StringBuffer();
		char[]         buf   = new char[1024];
		int            nRead = 0;
		
		while( (nRead = r.read(buf)) != -1 ) {
			sbuf.append(buf, 0, nRead);
		}
		r.close();
		_textPane.setText(sbuf.toString());
	}
	
	private boolean save() {
		if(_file == null) {
			return saveAs();
		} else {
			try {
				save(_file);
				return true;
			}catch(IOException e) {
				showSaveErrorMessage();
			}
			return false;
		}
	}
	
	private void showSaveErrorMessage() {
		String[] message = { "Could not save file: " + _file,
				"Access denied" };
		JOptionPane.showMessageDialog(this, message, "JNotedPad", JOptionPane.ERROR_MESSAGE);
	}

	private boolean saveAs() {
		if(_fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return false;
		
		File file = _fc.getSelectedFile();
		try {
			open(file);
			_file = file;
			setTitle(file.getName() + " - JNotePad");
			return true;
		} catch(IOException e) {
			showSaveErrorMessage();
			return false;
		}
	}
	
	private void save(File file) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(file));
		w.write(_textPane.getText());
		w.close();
	}
	
	private class NewAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NewAction() {
			super();
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl N"));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println(getValue(Action.NAME));
			if(!confirmSave()) {
				return;
			}
			_textPane.setText("");
			_isSaved = true;
		}
	}
	
	private class OpenAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public OpenAction() {
			super("Open");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctril O"));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println(getValue(Action.NAME));
			if(!confirmSave()) {
				return;
			}
			_isSaved = open();
		}
	}
	
	private class SaveAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SaveAction() {
			super("Save");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctril S"));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println(getValue(Action.NAME));
			if(!confirmSave()) {
				return;
			}
			_isSaved = save();
		}
	}
	
	private class SaveAsAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SaveAsAction() {
			super("Save As...");
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println(getValue(Action.NAME));
			_isSaved = saveAs();
		}
	}
	
	private class ExitAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ExitAction() {
			super("Exit");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt F4"));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println(getValue(Action.NAME));
			if(!confirmSave()) {
				return;
			}
			System.exit(0);
		}
	}
	
	private class CutAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CutAction() {
			super("Cut");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println(getValue(Action.NAME));
			_textPane.cut();
		}
	}
	
	private class CopyAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CopyAction() {
			super("Copy");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println(getValue(Action.NAME));
			_textPane.copy();
		}
	}
	
	private class PasteAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PasteAction() {
			super("Paste");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println(getValue(Action.NAME));
			_textPane.paste();
		}
	}
	
	private class HelpAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public HelpAction() {
			super("Help");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
		}
		
		public void actionPerformed(ActionEvent e) {
			String[] message = {"Sorry.", "Help contents are not supported yet."};
			JOptionPane.showMessageDialog(JNotePad.this, message, "JNotePad", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private class AboutAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public AboutAction() {
			super("About");
			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		}
		
		public void actionPerformed(ActionEvent e) {
			String[] message = {"JNotePad v 0.1", "Author: TaeHa"};
			JOptionPane.showMessageDialog(JNotePad.this, message, "About JNotePad", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private void start() {
		setSize(450,270);
		setLocation(100, 100);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		JNotePad app = new JNotePad();
		app.start();
	}
}
