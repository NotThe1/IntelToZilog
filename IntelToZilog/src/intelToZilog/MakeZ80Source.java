package intelToZilog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import appLogger.AppLogger;


public class MakeZ80Source {

	private ApplicationAdapter applicationAdapter = new ApplicationAdapter();
	private AppLogger log = AppLogger.getInstance();
	private AdapterLog logAdaper = new AdapterLog();
	private StyledDocument docIntel;
	private StyledDocument docZilog;
	private JScrollBar sbarIntel;
	private JScrollBar sbarZilog;

	private InstructionSetIntel isIntel = new InstructionSetIntel();
	private Pattern patternIntel = isIntel.getInstructionPattern();
	private Matcher matcherIntel;

	private Set<String> symbols = new HashSet<String>();

	private String codeBaseIntel;
	private String codeBaseZilog;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MakeZ80Source window = new MakeZ80Source();
					window.frmTemplate.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				} // try
			}// run
		});
	}// main

	/* Standard Stuff */

	////////////////////////////////////////////////////////////////
	private void saveZilogFile(File zilogFile) {
//		Files.deleteIfExists(Paths.get(zilogFile));
		String zilogLine;
		try {
			Scanner scanner = new Scanner(textZilog.getText());
			FileWriter destination = new FileWriter(zilogFile);
			PrintWriter pwDestination = new PrintWriter(destination);
			zilogLine = scanner.nextLine();
			log.info(zilogLine);		
			pwDestination.println(zilogLine);
			
			zilogLine = scanner.nextLine();
			if (zilogLine.length()==0) {
				zilogLine = scanner.nextLine();	
			}//
			
//			zilogLine = scanner.nextLine();
			log.info(zilogLine);
			pwDestination.println(zilogLine);
			

			while (scanner.hasNextLine()) {
				scanner.next(); // skip the line number
				zilogLine = scanner.nextLine();
				log.info(zilogLine);
				pwDestination.println(zilogLine);
			} // while

			destination.close();
			pwDestination.close();
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		} //

	}// saveZilogFile

	/////// :::::::::::::::::::::::::::::::::::::::::::::::::
	private void loadIntelFile(File intelFile) {
		// String intelFileName = intelFile.getName();

		lblIntelFile.setToolTipText(intelFile.getAbsolutePath());
		lblIntelFile.setText(intelFile.getName());
		//
		clearDoc(docIntel);
		clearDoc(docZilog);
		// try {
		// tempFile = File.createTempFile(Long.toString(System.currentTimeMillis()), null);
		// tempFile.deleteOnExit();
		// System.out.println(tempFile.getAbsolutePath());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		loadSourceFile(intelFile, 1);

	}// loadIntelFile

	private int loadSourceFile(File sourceFile, int lineNumber) {
		String[] lineParts;
		String line = String.format(";     File created by MakeZ80Source on %s from:", new Date());
		insertZilog(line + System.lineSeparator(), attrGreen);
		line = String.format(";     %s",sourceFile.getAbsolutePath());
		insertZilog(line + System.lineSeparator(), attrMaroon);
		try {
			FileReader source = new FileReader(sourceFile);
			BufferedReader reader = new BufferedReader(source);
			String lineIntel = null;
			boolean instructionOnLine = false;
			String outputLine;
			while ((lineIntel = reader.readLine()) != null) {
				instructionOnLine = false;
				outputLine = String.format("%04d %s", lineNumber, lineIntel);
				// outputLine =lineIntel;

				if (!lineIntel.contains(";")) {
					matcherIntel = patternIntel.matcher(lineIntel);
					instructionOnLine = matcherIntel.find() ? true : false;
				} else if (!lineIntel.startsWith(";")) {
					lineParts = lineIntel.split(";");
					matcherIntel = patternIntel.matcher(lineParts[0]);
					instructionOnLine = matcherIntel.find() ? true : false;
				} // if

				if (instructionOnLine) {
					insertIntel(String.format("%04d %s%n", lineNumber, lineIntel), attrBlue);
					replaceCode(lineNumber, lineIntel, matcherIntel);
				} else {
					// attr = attrBlack;
					insertIntel(String.format("%04d %s%n", lineNumber, lineIntel), attrBlack);
					insertZilog(outputLine + System.lineSeparator(), attrBlack);
				} // if
				lineNumber++;

			} // while
			reader.close();
		} catch (IOException e) {
			String error = String.format("File Not Found!! - %s", sourceFile.getAbsolutePath());
			log.error(error);
		} // TRY
		insertZilog("!END ",attrMaroon);
		sbarIntel.setValue(0);
		sbarZilog.setValue(0);
		textIntel.updateUI();
		textZilog.updateUI();
		return lineNumber;
	}// loadSourceFile

	private void replaceCode(int lineNumber, String originalLine, Matcher matcher) {
		int end = matcher.end(0);
		int start = matcher.start(0);

		String part1 = originalLine.substring(0, start);
		String part2 = originalLine.substring(start, end);
		String part3 = originalLine.substring(end);

		String intelInstruction = part2;
		String zilogInstruction = isIntel.getZilogInstruction(intelInstruction);
		if (isIntel.isChangeNeeded(intelInstruction)) {
			switch (isIntel.getConversionType(intelInstruction)) {
			case None:
				doNone(lineNumber, originalLine);
				break;
			case OnlyInstructionHL:
				part3 = part3.replaceFirst("M,", "\\(HL\\),");
				part3 = part3.replaceFirst(",M", ",\\(HL\\)");
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case OnlyInstruction:
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case Instruction16BitReg:
				doInstruction16BitReg(lineNumber, part1, zilogInstruction, part3);
				break;
			case LogicInstructionHL:
				doLogicInstructionHL(lineNumber, part1, zilogInstruction, part3);
				break;
			case ConditionInstruction:
				doConditionInstruction(lineNumber, part1, intelInstruction, zilogInstruction, part3);
				break;
			case STAX:
				doStax(lineNumber, part1, zilogInstruction, part3);
				break;
			case LDAX:
				doLdax(lineNumber, part1, zilogInstruction, part3);
				break;
			case DAD:
				doDad(lineNumber, part1, zilogInstruction, part3);
				break;
			case RST:
				doRST(lineNumber, part1, zilogInstruction, part3);
				break;

			case SBI_ACI:
				doSbiAci(lineNumber, part1, zilogInstruction, part3);
				break;
			// ............
			case SHLD:
				String address = getAddress(part3);
				part3 = part3.replaceFirst(address, "(" + address + "),HL");
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case LHLD:
				address = getAddress(part3);
				part3 = part3.replaceFirst(address, "HL,(" + address + ")");
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case STA:
				address = getAddress(part3);
				part3 = part3.replaceFirst(address, "(" + address + "),A");
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case LDA:
				address = getAddress(part3);
				part3 = part3.replaceFirst(address, " A,(" + address + ")");
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case PCHL:
				part3 = "(HL) " + part3;
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case SPHL:
				part3 = "\tSP,HL " + part3.trim();
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case XCHG:
				part3 = "\tDE,HL " + part3.trim();
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case XTHL:
				part3 = "\t(SP),HL " + part3.trim();
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case OUT:
				address = getAddress(part3);
				part3 = part3.replaceFirst(address, "(" + address + "),A");
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;
			case IN:
				address = getAddress(part3);
				part3 = part3.replaceFirst(address, "A,(" + address + ")");
				doOnlyInstruction(lineNumber, part1, zilogInstruction, part3);
				break;

			}// switch SBI_ACI

		} else {
			insertZilog(String.format("%04d %s%s%s%n", lineNumber, part1, part2, part3), attrBlue);
			// if needs change
		} // if needs change
			// Scanner scanner = new Scanner(part3);
			//
			// // String arg = scanner.next();
			// String ans = scanner.hasNext() ? scanner.next() : EMPTY_STRING;
			// scanner.close();
			//

		return;
	}// replaceCode

	private String getAddress(String part3) {
		String ans = EMPTY_STRING;
		Scanner scanner = new Scanner(part3);
//		scanner.useDelimiter(";");
		if (scanner.hasNext()) {
			ans = scanner.next();
		} // if
		scanner.close();
		return ans;
	}//

	private void doNone(int lineNumber, String originalLine) {
		insertZilog(String.format("%04d %s%s%s%n", lineNumber, originalLine), attrBlue);
	}// ddoNone

	private void doOnlyInstruction(int lineNumber, String part1, String zilogInstruction, String part3) {
		insertZilog(String.format("%04d %s", lineNumber, part1), attrBlue);
		insertZilog(zilogInstruction, attrRed);
		insertZilog(part3 + System.lineSeparator(), attrBlue);

	}// doOnlyInstruction

	private void doInstruction16BitReg(int lineNumber, String part1, String zilogInstruction, String part3) {
		insertZilog(String.format("%04d %s", lineNumber, part1), attrBlue);
		insertZilog(zilogInstruction, attrRed);
		String newPart3 = part3.trim();
		if (!(newPart3.startsWith("BC") | newPart3.startsWith("DE") | newPart3.startsWith("HL"))) {
			switch (part3.trim().substring(0, 1)) {
			case "B":
				part3 = part3.replaceFirst("B", "BC");
				break;
			case "D":
				part3 = part3.replaceFirst("D", "DE");
				break;
			case "H":
				part3 = part3.replaceFirst("H", "HL");
				break;
			case "P":
				part3 = part3.replaceFirst("PSW", "AF");
				break;
			case "S":
				// ignore its SP
				break;
			}// switch
		} // if
		insertZilog(part3 + System.lineSeparator(), attrBlue);
	}// doInstruction16BitReg

	private void doLogicInstructionHL(int lineNumber, String part1, String zilogInstruction, String part3) {
		insertZilog(String.format("%04d %s", lineNumber, part1), attrBlue);
		insertZilog(zilogInstruction, attrRed);
		switch (part3.trim().substring(0, 1)) {
		case "A":
			part3 = part3.replaceFirst("A", "A,A");
			break;
		case "B":
			part3 = part3.replaceFirst("B", "A,B");
			break;
		case "C":
			part3 = part3.replaceFirst("C", "A,C");
			break;
		case "D":
			part3 = part3.replaceFirst("D", "A,D");
			break;
		case "E":
			part3 = part3.replaceFirst("E", "A,E");
			break;
		case "H":
			part3 = part3.replaceFirst("H", "A,H");
			break;
		case "L":
			part3 = part3.replaceFirst("L", "A,L");
			break;
		case "M":
			part3 = part3.replaceFirst("M", "A,(HL)");
			break;
		case "S":
			// ignore its SP
			break;
		}// switch
		insertZilog(part3 + System.lineSeparator(), attrBlue);
	}// doInstruction16BitReg

	private void doConditionInstruction(int lineNumber, String part1, String intelInstruction, String zilogInstruction,
			String part3) {
		insertZilog(String.format("%04d %s", lineNumber, part1), attrBlue);
		insertZilog(zilogInstruction, attrRed);
		String newPart3;
		if (zilogInstruction.equals("RET")) {
			newPart3 = String.format("\t%s%s", intelInstruction.trim().substring(1), part3.trim());
		} else {
			newPart3 = String.format("\t%s,%s", intelInstruction.trim().substring(1), part3.trim());
		} // if return
		insertZilog(newPart3 + System.lineSeparator(), attrBlue);
	}// doLogicInstructionHL

	private void doStax(int lineNumber, String part1, String zilogInstruction, String part3) {
		insertZilog(String.format("%04d %s", lineNumber, part1), attrBlue);
		insertZilog(zilogInstruction, attrRed);
		String newPart3;
		if (part3.trim().startsWith("B")) {
			newPart3 = part3.replaceFirst("B", "(BC),A");
		} else if (part3.trim().startsWith("D")) {
			newPart3 = part3.replaceFirst("D", "(DE),A");
		} else {
			newPart3 = "***** Bad Conversion *****";
		} //
		insertZilog(newPart3 + System.lineSeparator(), attrBlue);
	}// doStax

	private void doLdax(int lineNumber, String part1, String zilogInstruction, String part3) {
		insertZilog(String.format("%04d %s", lineNumber, part1), attrBlue);
		insertZilog(zilogInstruction, attrRed);
		String newPart3;
		String arg = part3.trim();
		String reg;
		switch (part3.trim().substring(0, 1)) {
		case "B":
			reg = arg.startsWith("BC")?"BC":"B";
			newPart3 = part3.replaceFirst(reg, "A,(BC)");
			break;
		case "D":
			reg = arg.startsWith("DE")?"DE":"D";
			newPart3 = part3.replaceFirst(reg, "A,(DE)");
			break;
		default:
			newPart3 = "***** Bad Conversion *****";
			break;	
		}//switch
		insertZilog(newPart3 + System.lineSeparator(), attrBlue);
	}// doLdax

	private void doDad(int lineNumber, String part1, String zilogInstruction, String part3) {
		insertZilog(String.format("%04d %s", lineNumber, part1), attrBlue);
		insertZilog(zilogInstruction, attrRed);
		String newPart3;
		String arg = part3.trim();
		String reg;
		switch (part3.trim().substring(0, 1)) {
		case "B":
			reg = arg.startsWith("BC")?"BC":"B";
			newPart3 = part3.replaceFirst(reg, "HL,BC");
			break;
		case "D":
			reg = arg.startsWith("DE")?"DE":"D";
			newPart3 = part3.replaceFirst(reg, "HL,DE");
			break;
		case "H":
			reg = arg.startsWith("HL")?"HL":"H";
			newPart3 = part3.replaceFirst(reg, "HL,HL");
			break;
		case "S":
			newPart3 = part3.replaceFirst("SP", "HL,SP");
			break;
		default:
			newPart3 = "***** Bad Conversion *****";
			break;
		}// switch
		insertZilog(newPart3 + System.lineSeparator(), attrBlue);
	}// doDad

	private void doRST(int lineNumber, String part1, String zilogInstruction, String part3) {
		insertZilog(String.format("%04d %s", lineNumber, part1), attrBlue);
		insertZilog(zilogInstruction, attrRed);

		int value = Integer.valueOf(part3.trim().substring(0, 1));
		String newPart3 = String.format("\t%02XH%s", value * 8, part3.trim().substring(1));

		insertZilog(newPart3 + System.lineSeparator(), attrBlue);
	}// doStack

	private void doSbiAci(int lineNumber, String part1, String zilogInstruction, String part3) {
		insertZilog(String.format("%04d %s", lineNumber, part1), attrBlue);
		insertZilog(zilogInstruction, attrRed);

		insertZilog("\tA," + part3.trim() + System.lineSeparator(), attrBlue);
	}// doSbiAci

	//////////////////// .........................................
	private void insertZilog(String str, SimpleAttributeSet attr) {
		try {
			docZilog.insertString(docZilog.getLength(), str, attr);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
	}// insertSource

	private void insertIntel(String str, SimpleAttributeSet attr) {
		try {
			docIntel.insertString(docIntel.getLength(), str, attr);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
	}// insertSource

	private void clearDoc(StyledDocument doc) {
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} // try
	}// clearDoc

	////////////////////////////////////////////////////////////////

//	private void doBtnOne() {
//		sbarIntel.setValue(0);
//		sbarZilog.setValue(0);
//	}// doBtnOne
//
//	private void doBtnTwo() {
//
//	}// doBtnTwo
//
//	private void doBtnThree() {
//
//	}// doBtnThree

	private void doFileOpen() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Source Files", "asm", "z80");
		JFileChooser fileChooser = new JFileChooser(codeBaseIntel);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);

		if (fileChooser.showOpenDialog(frmTemplate) == JFileChooser.CANCEL_OPTION) {
			System.out.println("Bailed out of the open");
			return;
		} // if - open
		codeBaseIntel = fileChooser.getSelectedFile().getParent();
		loadIntelFile(fileChooser.getSelectedFile());
		manageMenusAndButtons();
	}// doFileOpen

	// private void doFileSave() {
	//
	// }// doFileSave

	private void doFileSaveAs() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Source Files", "asm", "z80");
		JFileChooser fileChooser = new JFileChooser(codeBaseZilog);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);

		if (fileChooser.showSaveDialog(frmTemplate) == JFileChooser.CANCEL_OPTION) {
			System.out.println("Bailed out of the open");
			return;
		} // if - open
		File selectedFile = fileChooser.getSelectedFile();
		codeBaseZilog = selectedFile.getParent();
		
		String fileName = fileChooser.getSelectedFile().getAbsolutePath();
		String[] parts = fileName.split("\\.");
		
		fileName = parts[0] + ".Z80";
		
		try {
			Files.deleteIfExists(Paths.get(fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		saveZilogFile(new File(fileName));
		

	}// doFileSaveAs

	// private void doFilePrint() {
	//
	// }// doFilePrint

	private void doFileExit() {
		appClose();
		System.exit(0);
	}// doFileExit

	private void manageMenusAndButtons() {
		boolean haveZilogFile = docIntel.getLength() != 0 ? true : false;
		mnuFileSaveAs.setEnabled(haveZilogFile);
		btnFileSaveAs.setEnabled(haveZilogFile);
	}// manageMenusAndButtons

	//////////////////////////////////////////////////

	private void setAttributes() {
		StyleConstants.setForeground(attrNavy, new Color(0, 0, 128));
		StyleConstants.setForeground(attrBlack, new Color(0, 0, 0));
		StyleConstants.setForeground(attrBlue, new Color(0, 0, 255));
		StyleConstants.setForeground(attrGreen, new Color(0, 128, 0));
		StyleConstants.setForeground(attrTeal, new Color(0, 128, 128));
		StyleConstants.setForeground(attrGray, new Color(128, 128, 128));
		StyleConstants.setForeground(attrSilver, new Color(192, 192, 192));
		StyleConstants.setForeground(attrRed, new Color(255, 0, 0));
		StyleConstants.setForeground(attrMaroon, new Color(128, 0, 0));
	}// setAttributes

	/////////////////////////////////////
	private void appClose() {
		Preferences myPrefs = Preferences.userNodeForPackage(MakeZ80Source.class).node(this.getClass().getSimpleName());
		Dimension dim = frmTemplate.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = frmTemplate.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);
		myPrefs.putInt("DividerMain", splitPane1Main.getDividerLocation());
		myPrefs.putInt("DividerCode", splitPaneCode.getDividerLocation());

		myPrefs.put("IntelBase", codeBaseIntel);
		myPrefs.put("ZilogBase", codeBaseZilog);
		myPrefs = null;

	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(MakeZ80Source.class).node(this.getClass().getSimpleName());
		frmTemplate.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frmTemplate.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		splitPane1Main.setDividerLocation(myPrefs.getInt("DividerMain", 250));
		splitPaneCode.setDividerLocation(myPrefs.getInt("DividerCode", 250));

		codeBaseIntel = myPrefs.get("IntelBase", INTEL_BASE);
		codeBaseZilog = myPrefs.get("ZilogBase", ZILOG_BASE);
		myPrefs = null;
		setAttributes();
		symbols.add("$");

		// txtLog.setText(EMPTY_STRING);
		docIntel = textIntel.getStyledDocument();
		clearDoc(docIntel);
		docZilog = textZilog.getStyledDocument();
		clearDoc(docZilog);

		log.setDoc(txtLog.getStyledDocument());
		log.info("Starting...");

		manageMenusAndButtons();
	}// appInit

	public MakeZ80Source() {
		initialize();
		appInit();
	}// Constructor

	private void doLogClear() {
		log.clear();
	}// doLogClear

	private void doLogPrint() {

		Font originalFont = txtLog.getFont();
		try {
			// textPane.setFont(new Font("Courier New", Font.PLAIN, 8));
			txtLog.setFont(originalFont.deriveFont(8.0f));
			MessageFormat header = new MessageFormat("Identic Log");
			MessageFormat footer = new MessageFormat(new Date().toString() + "           Page - {0}");
			txtLog.print(header, footer);
			// textPane.setFont(new Font("Courier New", Font.PLAIN, 14));
			txtLog.setFont(originalFont);
		} catch (PrinterException e) {
			e.printStackTrace();
		} // try

	}// doLogPrint

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTemplate = new JFrame();
		frmTemplate.setTitle("Make Z80 Source from 8080 Source    1.0");
		frmTemplate.setBounds(100, 100, 721, 582);
		frmTemplate.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTemplate.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 25, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frmTemplate.getContentPane().setLayout(gridBagLayout);

		JPanel panelForButtons = new JPanel();
		GridBagConstraints gbc_panelForButtons = new GridBagConstraints();
		gbc_panelForButtons.anchor = GridBagConstraints.NORTH;
		gbc_panelForButtons.insets = new Insets(0, 0, 5, 0);
		gbc_panelForButtons.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelForButtons.gridx = 0;
		gbc_panelForButtons.gridy = 0;
		frmTemplate.getContentPane().add(panelForButtons, gbc_panelForButtons);
		GridBagLayout gbl_panelForButtons = new GridBagLayout();
		gbl_panelForButtons.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelForButtons.rowHeights = new int[] { 0, 0 };
		gbl_panelForButtons.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelForButtons.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelForButtons.setLayout(gbl_panelForButtons);

		btnLoadIntelFile = new JButton("Load Intel File");
		btnLoadIntelFile.setName(BTN_FILE_LOAD);
		btnLoadIntelFile.addActionListener(applicationAdapter);
		btnLoadIntelFile.setMinimumSize(new Dimension(100, 20));
		GridBagConstraints gbc_btnLoadIntelFile = new GridBagConstraints();
		gbc_btnLoadIntelFile.insets = new Insets(0, 0, 0, 5);
		gbc_btnLoadIntelFile.gridx = 0;
		gbc_btnLoadIntelFile.gridy = 0;
		panelForButtons.add(btnLoadIntelFile, gbc_btnLoadIntelFile);
		btnLoadIntelFile.setAlignmentX(Component.RIGHT_ALIGNMENT);
		btnLoadIntelFile.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		btnLoadIntelFile.setMaximumSize(new Dimension(0, 0));
		btnLoadIntelFile.setPreferredSize(new Dimension(100, 20));

		btnFileSaveAs = new JButton("Save as...");
		btnFileSaveAs.setName(BTN_FILE_SAVE_AS);
		btnFileSaveAs.addActionListener(applicationAdapter);
		btnFileSaveAs.setMinimumSize(new Dimension(100, 20));
		GridBagConstraints gbc_btnFileSaveAs = new GridBagConstraints();
		gbc_btnFileSaveAs.insets = new Insets(0, 0, 0, 5);
		gbc_btnFileSaveAs.gridx = 1;
		gbc_btnFileSaveAs.gridy = 0;
		panelForButtons.add(btnFileSaveAs, gbc_btnFileSaveAs);
		btnFileSaveAs.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		btnFileSaveAs.setPreferredSize(new Dimension(100, 20));
		btnFileSaveAs.setMaximumSize(new Dimension(0, 0));

		btnFileExit = new JButton("Exit");
		btnFileExit.setName(BTN_FILE_EXIT);
		btnFileExit.addActionListener(applicationAdapter);
		btnFileExit.setMinimumSize(new Dimension(100, 20));
		GridBagConstraints gbc_btnFileExit = new GridBagConstraints();
		gbc_btnFileExit.insets = new Insets(0, 0, 0, 5);
		gbc_btnFileExit.gridx = 3;
		gbc_btnFileExit.gridy = 0;
		panelForButtons.add(btnFileExit, gbc_btnFileExit);
		btnFileExit.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		btnFileExit.setPreferredSize(new Dimension(100, 20));
		btnFileExit.setMaximumSize(new Dimension(0, 0));

		splitPane1Main = new JSplitPane();
		splitPane1Main.setOneTouchExpandable(true);
		GridBagConstraints gbc_splitPane1Main = new GridBagConstraints();
		gbc_splitPane1Main.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane1Main.fill = GridBagConstraints.BOTH;
		gbc_splitPane1Main.gridx = 0;
		gbc_splitPane1Main.gridy = 1;
		frmTemplate.getContentPane().add(splitPane1Main, gbc_splitPane1Main);

		JPanel panelLeft = new JPanel();
		panelLeft.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Application Log",
				TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		splitPane1Main.setLeftComponent(panelLeft);
		GridBagLayout gbl_panelLeft = new GridBagLayout();
		gbl_panelLeft.columnWidths = new int[] { 0, 0 };
		gbl_panelLeft.rowHeights = new int[] { 0, 0 };
		gbl_panelLeft.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelLeft.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelLeft.setLayout(gbl_panelLeft);

		JScrollPane scrollPaneForLog = new JScrollPane();
		GridBagConstraints gbc_scrollPaneForLog = new GridBagConstraints();
		gbc_scrollPaneForLog.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneForLog.gridx = 0;
		gbc_scrollPaneForLog.gridy = 0;
		panelLeft.add(scrollPaneForLog, gbc_scrollPaneForLog);

		JPanel panelMain = new JPanel();
		splitPane1Main.setRightComponent(panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[] { 0, 0 };
		gbl_panelMain.rowHeights = new int[] { 0, 0 };
		gbl_panelMain.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMain.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelMain.setLayout(gbl_panelMain);

		splitPaneCode = new JSplitPane();
		splitPaneCode.setResizeWeight(0.5);
		splitPaneCode.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPaneCode.setOneTouchExpandable(true);
		GridBagConstraints gbc_splitPaneCode = new GridBagConstraints();
		gbc_splitPaneCode.fill = GridBagConstraints.BOTH;
		gbc_splitPaneCode.gridx = 0;
		gbc_splitPaneCode.gridy = 0;
		panelMain.add(splitPaneCode, gbc_splitPaneCode);

		JScrollPane scrollPaneIntel = new JScrollPane();
		splitPaneCode.setLeftComponent(scrollPaneIntel);

		textIntel = new JTextPane();
		textIntel.setEditable(false);
		textIntel.setFont(new Font("Courier New", Font.PLAIN, 14));
		sbarIntel = scrollPaneIntel.getVerticalScrollBar();
		sbarIntel.addAdjustmentListener(applicationAdapter);
		scrollPaneIntel.setViewportView(textIntel);

		lblIntelFile = new JLabel(NO_FILE);
		lblIntelFile.setFont(new Font("Trebuchet MS", Font.BOLD, 14));
		lblIntelFile.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPaneIntel.setColumnHeaderView(lblIntelFile);

		JScrollPane scrollPaneZilog = new JScrollPane();
		splitPaneCode.setRightComponent(scrollPaneZilog);

		textZilog = new JTextPane();
		textZilog.setFont(new Font("Courier New", Font.PLAIN, 14));
		sbarZilog = scrollPaneZilog.getVerticalScrollBar();
		sbarZilog.addAdjustmentListener(applicationAdapter);
		scrollPaneZilog.setViewportView(textZilog);

		txtLog = new JTextPane();
		scrollPaneForLog.setViewportView(txtLog);

		popupLog = new JPopupMenu();
		addPopup(txtLog, popupLog);

		JMenuItem popupLogClear = new JMenuItem("Clear Log");
		popupLogClear.setName(PUM_LOG_CLEAR);
		popupLogClear.addActionListener(logAdaper);
		popupLog.add(popupLogClear);

		JSeparator separator = new JSeparator();
		popupLog.add(separator);

		JMenuItem popupLogPrint = new JMenuItem("Print Log");
		popupLogPrint.setName(PUM_LOG_PRINT);
		popupLogPrint.addActionListener(logAdaper);
		popupLog.add(popupLogPrint);

		JLabel lblNewLabel = new JLabel("-");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(new Color(0, 128, 0));
		lblNewLabel.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 14));
		scrollPaneForLog.setColumnHeaderView(lblNewLabel);
		splitPane1Main.setDividerLocation(250);

		JPanel panelStatus = new JPanel();
		panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.fill = GridBagConstraints.BOTH;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 2;
		frmTemplate.getContentPane().add(panelStatus, gbc_panelStatus);

		JMenuBar menuBar = new JMenuBar();
		frmTemplate.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		mnuFileLoad = new JMenuItem("Load Intel File ...");
		mnuFileLoad.setName(MNU_FILE_LOAD);
		mnuFileLoad.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileLoad);

		JSeparator separator99 = new JSeparator();
		mnuFile.add(separator99);

		// mnuFileSave = new JMenuItem("Save...");
		// mnuFileSave.setName(MNU_FILE_SAVE);
		// mnuFileSave.addActionListener(applicationAdapter);
		// mnuFile.add(mnuFileSave);

		mnuFileSaveAs = new JMenuItem("Save As...");
		mnuFileSaveAs.setName(MNU_FILE_SAVE_AS);
		mnuFileSaveAs.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileSaveAs);

		JSeparator separator_2 = new JSeparator();
		mnuFile.add(separator_2);

		mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setName(MNU_FILE_EXIT);
		mnuFileExit.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileExit);

	}// initialize

	private static final String PUM_LOG_PRINT = "popupLogPrint";
	private static final String PUM_LOG_CLEAR = "popupLogClear";

	private static final String INTEL_BASE = "C:\\Users\\admin\\Dropbox\\Resources\\CPM\\CurrentOS\\8080";
	private static final String ZILOG_BASE = "C:\\Users\\admin\\Dropbox\\Resources\\CPM\\CurrentOS\\Z80";
	// private static final String INTEL_8080 = INTEL_BASE + "";
	// private static final String SOURCE_Z80 = INTEL_BASE + "";

	static final String EMPTY_STRING = "";
	static final String NO_FILE = "<<No File>>";

	private static final String MNU_FILE_LOAD = "mnuFileOpen";
//	private static final String MNU_FILE_SAVE = "mnuFileSave";
	private static final String MNU_FILE_SAVE_AS = "mnuFileSaveAs";
	private static final String MNU_FILE_EXIT = "mnuFileExit";

	private static final String BTN_FILE_LOAD = "btnFileLoad";
	private static final String BTN_FILE_SAVE_AS = "btnFileSaveAs";
	private static final String BTN_FILE_EXIT = "btnFileExit";

	//////////////////////////////////////////////////////////////////////////
	class ApplicationAdapter implements ActionListener, AdjustmentListener {

		@Override
		public void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
			if (adjustmentEvent.getSource() instanceof JScrollBar) {
				int value = ((JScrollBar) adjustmentEvent.getSource()).getValue();
				sbarIntel.setValue(value);
				sbarZilog.setValue(value);
			} // if scroll bar

		}// adjustmentValueChanged

		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case MNU_FILE_LOAD:
			case BTN_FILE_LOAD:
				doFileOpen();
				break;
			case MNU_FILE_SAVE_AS:
			case BTN_FILE_SAVE_AS:
				doFileSaveAs();
				break;
			case MNU_FILE_EXIT:
				doFileExit();
				break;
			}// switch
		}

	}

	class AdapterLog implements ActionListener {//
		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case PUM_LOG_PRINT:
				doLogPrint();
				break;
			case PUM_LOG_CLEAR:
				doLogClear();
				break;
			}// switch
		}// actionPerformed

		/* AdjustmentListener */

	}// class AdapterAction

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				} // if popup Trigger
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}// addPopup

	private JFrame frmTemplate;
	private JButton btnLoadIntelFile;
	private JButton btnFileSaveAs;
	private JButton btnFileExit;
	private JSplitPane splitPane1Main;
	private JSplitPane splitPaneCode;

	private JTextPane txtLog;
	private JPopupMenu popupLog;
	private JTextPane textIntel;
	private JTextPane textZilog;
	private JLabel lblIntelFile;

	private SimpleAttributeSet attrBlack = new SimpleAttributeSet();
	private SimpleAttributeSet attrBlue = new SimpleAttributeSet();
	private SimpleAttributeSet attrGray = new SimpleAttributeSet();
	private SimpleAttributeSet attrGreen = new SimpleAttributeSet();
	private SimpleAttributeSet attrRed = new SimpleAttributeSet();
	private SimpleAttributeSet attrSilver = new SimpleAttributeSet();
	private SimpleAttributeSet attrNavy = new SimpleAttributeSet();
	private SimpleAttributeSet attrMaroon = new SimpleAttributeSet();
	private SimpleAttributeSet attrTeal = new SimpleAttributeSet();
	private JMenuItem mnuFileLoad;
//	private JMenuItem mnuFileSave;
	private JMenuItem mnuFileSaveAs;
	private JMenuItem mnuFileExit;

}// class GUItemplate