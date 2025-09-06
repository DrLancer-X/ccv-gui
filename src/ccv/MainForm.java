package ccv;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainForm extends JFrame implements ActionListener, KeyEventDispatcher, ClipboardOwner {
	
	private final String[] ditherOptions = {"Floyd-Steinberg", "Stucki", "Jarvis-Judice-Ninke", "Burkes", "Sierra", "Stevenson-Arce"};
	
	private JTabbedPane tpane;
	private ImageDisplay tpane_original;
	private ImageDisplay tpane_converted;
	private JFileChooser img_fchooser;
	private JFileChooser chr_fchooser;
	private JFileChooser mzm_fchooser;
	private JButton convert_button;
	
	private boolean cfg_mzm = false;
	private boolean cfg_reuse = false;
	private boolean cfg_noreuse = false;
	private int cfg_offset = 0;
	private int cfg_blank = -1;
	private int cfg_threshold = 127;
	private String cfg_dither = "";
	
	private int cfg_c = 0;
	private String cfg_exclude = "";
	
	JMenuItem menu_file_save_charset;
	JMenuItem menu_file_save_mzm;
	
    JCheckBoxMenuItem menu_settings_mzm;
    JCheckBoxMenuItem menu_settings_reuse;
    JCheckBoxMenuItem menu_settings_charlimit;
    JCheckBoxMenuItem menu_settings_exclude;
    JCheckBoxMenuItem menu_settings_offset;
    JCheckBoxMenuItem menu_settings_blank;
	
	int converted_chars;
	
	public MainForm() throws IOException {
    	super("ccv");
    	img_fchooser = new JFileChooser();
    	img_fchooser.setCurrentDirectory(new File("."));
    	img_fchooser.setFileFilter(new FileNameExtensionFilter("Supported image formats", ImageIO.getReaderFileSuffixes()));
    	
    	chr_fchooser = new JFileChooser();
    	chr_fchooser.setCurrentDirectory(new File("."));
    	chr_fchooser.setFileFilter(new FileNameExtensionFilter("MegaZeux char sets", "chr"));
    	
    	mzm_fchooser = new JFileChooser();
    	mzm_fchooser.setCurrentDirectory(new File("."));
    	mzm_fchooser.setFileFilter(new FileNameExtensionFilter("MZM files", "mzm"));
    	
     	KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    	
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
        // Icon
        ImageIcon img = new ImageIcon(this.getClass().getResource("/res/ccv.png"));
        setIconImage(img.getImage());

        // Menu
        JMenuBar menu = new JMenuBar();
        JMenu menu_file = new JMenu("File");
        menu_file.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem menu_file_open = new JMenuItem("Open Image (Ctrl+O)", KeyEvent.VK_O);
        menu_file_save_charset = new JMenuItem("Save Charset (Ctrl+S)", KeyEvent.VK_S);
        menu_file_save_mzm = new JMenuItem("Save MZM (Ctrl+M)", KeyEvent.VK_M);
        
        JMenuItem menu_file_copy = new JMenuItem("Copy Image (Ctrl+C)", KeyEvent.VK_C);
        JMenuItem menu_file_paste = new JMenuItem("Paste Image (Ctrl+V)", KeyEvent.VK_P);
        
        menu_file_open.setName("OpenImage");
        menu_file_save_charset.setName("SaveCharset");
        menu_file_save_mzm.setName("SaveMZM");
        menu_file_copy.setName("Copy");
        menu_file_paste.setName("Paste");
                
        menu_file_save_charset.setEnabled(false);
        menu_file_save_mzm.setEnabled(false);
        
        menu_file_open.addActionListener(this);
        menu_file_save_charset.addActionListener(this);
        menu_file_save_mzm.addActionListener(this);
        menu_file_copy.addActionListener(this);
        menu_file_paste.addActionListener(this);

        menu_file.add(menu_file_open);
        menu_file.add(menu_file_save_charset);
        menu_file.add(menu_file_save_mzm);
        menu_file.addSeparator();
        menu_file.add(menu_file_copy);
        menu_file.add(menu_file_paste);

        JMenu menu_settings = new JMenu("Settings");
        menu_settings.setMnemonic(KeyEvent.VK_S);
        
        JRadioButtonMenuItem menu_settings_nodither;
        JMenuItem menu_settings_theshold;
        
        menu_settings_mzm = new JCheckBoxMenuItem("Create MZM");
        menu_settings_reuse = new JCheckBoxMenuItem("Reuse characters");
        menu_settings_theshold = new JMenuItem("Quantisation theshold (127)");
        menu_settings_nodither = new JRadioButtonMenuItem("No dithering");
        menu_settings_charlimit = new JCheckBoxMenuItem("Limit characters");
        menu_settings_exclude = new JCheckBoxMenuItem("Exclude characters");
        menu_settings_offset = new JCheckBoxMenuItem("MZM char offset");
        menu_settings_blank = new JCheckBoxMenuItem("Blank character");
        
        menu_settings_mzm.setName("cfg_mzm");
        menu_settings_reuse.setName("cfg_reuse");
        menu_settings_theshold.setName("cfg_threshold");
        menu_settings_nodither.setName("cfg_nodither");
        menu_settings_charlimit.setName("cfg_c");
        menu_settings_exclude.setName("cfg_exclude");
        menu_settings_offset.setName("cfg_offset");
        menu_settings_blank.setName("cfg_blank");

        menu_settings_mzm.addActionListener(this);
        menu_settings_reuse.addActionListener(this);
        menu_settings_theshold.addActionListener(this);
        menu_settings_nodither.addActionListener(this);
        menu_settings_charlimit.addActionListener(this);
        menu_settings_exclude.addActionListener(this);
        menu_settings_offset.addActionListener(this);
        menu_settings_blank.addActionListener(this);
        
        menu_settings_exclude.setEnabled(false);
        menu_settings_charlimit.setEnabled(false);
        menu_settings_offset.setEnabled(false);
        menu_settings_blank.setEnabled(false);
        
        menu_settings.add(menu_settings_mzm);
        menu_settings.add(menu_settings_reuse);
        menu_settings.add(menu_settings_theshold);
        menu_settings.addSeparator();
        menu_settings.add(menu_settings_nodither);
        
        ButtonGroup dithergroup = new ButtonGroup();
        dithergroup.add(menu_settings_nodither);
        menu_settings_nodither.setSelected(true);
        for (int i = 0; i < ditherOptions.length; i++) {
        	JRadioButtonMenuItem menu_settings_dither = new JRadioButtonMenuItem(ditherOptions[i]);
        	menu_settings_dither.setName("cfg_dither");
        	menu_settings_dither.addActionListener(this);
        	dithergroup.add(menu_settings_dither);
        	menu_settings.add(menu_settings_dither);
        }
        
        menu_settings.addSeparator();
        
        menu_settings.add(menu_settings_exclude);
        menu_settings.add(menu_settings_charlimit);
        menu_settings.add(menu_settings_offset);
        menu_settings.add(menu_settings_blank);
        
        menu.add(menu_file);
        menu.add(menu_settings);
        
        setJMenuBar(menu);
        
        // Tabbed pane
        
        tpane = new JTabbedPane();
        
        tpane_original = new ImageDisplay();
        tpane_original.LoadDefault();
        tpane.addTab("", null, tpane_original, "Original image");
        tpane.setMnemonicAt(0, KeyEvent.VK_O);
        
        updateTabOriginal();

        tpane_converted = new ImageDisplay();
        tpane.addTab("", null, tpane_converted, "Converted image");
        tpane.setMnemonicAt(1, KeyEvent.VK_C);
        
        updateTabConverted();
        
        convert_button = new JButton("Convert");
        convert_button.setName("Convert");
        convert_button.addActionListener(this);
        
        getContentPane().add(tpane, BorderLayout.CENTER);
        getContentPane().add(convert_button, BorderLayout.SOUTH);
 
        //Display the window.
        pack();
        setVisible(true);
	}
 
    private void updateTabConverted() {
    	String msg;
    	
    	if (tpane_converted.getImage() == null) {
    		msg = "none";
    	} else {
    		msg = converted_chars + " chars";
    	}
		tpane.setTitleAt(1, "Converted (" + msg + ")");
	}

	private void updateTabOriginal() {
		String msg;
		
		if (tpane_original.getImage() == null) {
			msg = "none";
		} else {
	    	int width = tpane_original.getImage().getWidth();
	    	int height = tpane_original.getImage().getHeight();
	    	msg = width + "x" + height;
		}
		tpane.setTitleAt(0, "Original (" +  msg + ")");
	}

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					@SuppressWarnings("unused")
					MainForm form = new MainForm();
			        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        });
    }

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JComponent source = (JComponent)arg0.getSource();
		String sourcename = source.getName();
		
		if (sourcename.compareTo("Convert")==0) actConvert();
		if (sourcename.compareTo("OpenImage")==0) actOpenImage();
		if (sourcename.compareTo("SaveCharset")==0) actSaveCharset();
		if (sourcename.compareTo("SaveMZM")==0) actSaveMZM();
		
		if (sourcename.compareTo("Copy")==0) clipboard_copy();
		if (sourcename.compareTo("Paste")==0) clipboard_paste();
		
		if (sourcename.compareTo("cfg_mzm")==0) actCfg_mzm(source);
		if (sourcename.compareTo("cfg_reuse")==0) actCfg_reuse(source);
		if (sourcename.compareTo("cfg_threshold")==0) actCfg_threshold(source);
		if (sourcename.compareTo("cfg_nodither")==0) actCfg_nodither(source);
		if (sourcename.compareTo("cfg_dither")==0) actCfg_dither(source);
		if (sourcename.compareTo("cfg_c")==0) actCfg_c(source);
		if (sourcename.compareTo("cfg_exclude")==0) actCfg_exclude(source);
		if (sourcename.compareTo("cfg_offset")==0) actCfg_offset(source);
		if (sourcename.compareTo("cfg_blank")==0) actCfg_blank(source);
	}

	private void actCfg_threshold(JComponent source) {
		JMenuItem src = (JMenuItem)source;
		
		String txt = "Quantisation threshold";
		
		String inputValue = JOptionPane.showInputDialog("Quantisation brightness threshold (0-255)");
		if (inputValue != null) {
			int new_threshold = Integer.parseInt(inputValue);
			
			if ((new_threshold >= 0) && (new_threshold <= 255)) {
				cfg_threshold = new_threshold;
			}
			txt += " (" + cfg_threshold + ")";
			src.setText(txt);
		}
	}

	private void actCfg_offset(JComponent source) {
		JCheckBoxMenuItem src = (JCheckBoxMenuItem)source;
		
		String txt = "MZM char offset";
		
		cfg_offset = 0;
				
		if (src.isSelected()) {
			String inputValue = JOptionPane.showInputDialog("Number of characters to offset the MZM indices by?");
			if (inputValue != null) {
				cfg_offset = Integer.parseInt(inputValue);
				if ((cfg_offset >= 1) && (cfg_offset <= 255)) {
					txt += " (" + cfg_offset + ")";
				} else {
					src.setSelected(false);
				}
			} else {
				src.setSelected(false);
			}
		}
		src.setText(txt);
	}

	private void actCfg_blank(JComponent source) {
		JCheckBoxMenuItem src = (JCheckBoxMenuItem)source;
		
		String txt = "Blank character";
		
		cfg_blank = -1;
				
		if (src.isSelected()) {
			String inputValue = JOptionPane.showInputDialog("Char to use as blank?");
			if (inputValue != null) {
				cfg_blank = Integer.parseInt(inputValue);
				if ((cfg_blank >= 1) && (cfg_blank <= 255)) {
					txt += " (" + cfg_blank + ")";
				} else {
					src.setSelected(false);
					cfg_blank = -1;
				}
			} else {
				src.setSelected(false);
				cfg_blank = -1;
			}
		}
		src.setText(txt);
	}
	
	private void actCfg_exclude(JComponent source) {
		JCheckBoxMenuItem src = (JCheckBoxMenuItem)source;
		
		String txt = "Exclude characters";
		
		cfg_exclude = "";
				
		if (src.isSelected()) {
			String inputValue = JOptionPane.showInputDialog("Exclude which characters? (e.g. 1-32,48-64)");
			if (inputValue != null && inputValue.length() > 0) {
				cfg_exclude = inputValue;
				txt += " (" + cfg_exclude + ")";
			} else {
				src.setSelected(false);
			}
		}
		src.setText(txt);
	}

	private void actCfg_c(JComponent source) {
		JCheckBoxMenuItem src = (JCheckBoxMenuItem)source;
		
		String txt = "Limit characters";
		
		cfg_c = 0;
		
		if (src.isSelected()) {
			String inputValue = JOptionPane.showInputDialog("Limit to # characters (1-256)");
			if (inputValue != null) {
				cfg_c = Integer.parseInt(inputValue);
				
				if ((cfg_c >= 1) && (cfg_c <= 256)) {
					txt += " (" + cfg_c + ")";
				} else {
					src.setSelected(false);
				}
			} else {
				src.setSelected(false);
			}
		}
		src.setText(txt);
	}

	private void actCfg_nodither(JComponent source) {
		cfg_dither = "";
	}
	
	private void actCfg_dither(JComponent source) {
		JRadioButtonMenuItem src = (JRadioButtonMenuItem)source;
		cfg_dither = src.getText();
	}

	private void actCfg_reuse(JComponent source) {		
		if (cfg_mzm) {
			cfg_reuse = true;
			cfg_noreuse = menu_settings_reuse.isSelected();
		} else {
			cfg_noreuse = false;
			cfg_reuse = menu_settings_reuse.isSelected();
		}
	}

	private void actCfg_mzm(JComponent source) {
		JCheckBoxMenuItem src = (JCheckBoxMenuItem)source;
		
		cfg_reuse = false;
		cfg_noreuse = false;
		menu_settings_reuse.setSelected(false);
		
		if (src.isSelected()) {
			cfg_mzm = true;
			menu_settings_reuse.setText("Don't reuse characters");
			
	        menu_settings_charlimit.setEnabled(true);
	        menu_settings_offset.setEnabled(true);
	        menu_settings_blank.setEnabled(true);
	        menu_settings_exclude.setEnabled(true);
		} else {
			cfg_mzm = false;
			menu_settings_reuse.setText("Reuse characters");
			
	        menu_settings_charlimit.setEnabled(false);
	        menu_settings_offset.setEnabled(false);
	        menu_settings_blank.setEnabled(false);
	        menu_settings_exclude.setEnabled(false);
		}
	}

	private void actSaveMZM() {
		int ret = mzm_fchooser.showSaveDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			write_file(mzm_fchooser.getSelectedFile(), tpane_converted.mzm, ".mzm");
		}
	}
	
	private void write_file(File output, byte[] data, String ext) {
		String outputName = output.getPath();
		if (!outputName.toLowerCase().endsWith(ext)) {
			outputName += ext;
		}
		output = new File(outputName);
		
		try {
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(output));
			os.write(data);
			os.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error writing file");
		}
	}

	private void actSaveCharset() {
		int ret = chr_fchooser.showSaveDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			write_file(chr_fchooser.getSelectedFile(), tpane_converted.charset, ".chr");
		}
	}

	private void actOpenImage() {
		int ret = img_fchooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			try {
				tpane_original.LoadImage(img_fchooser.getSelectedFile());
				if (tpane_original.getImage() == null) throw new IOException();
				
				tpane_converted.RemoveImage();
				
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error loading image");
			}
		}
		
		updateTabOriginal();
		updateTabConverted();
		
		convert_button.setEnabled(tpane_original.getImage() != null);
		tpane.setSelectedComponent(tpane_original);
	}

	private void actConvert() {
		int width = tpane_original.getImage().getWidth();
		int height = tpane_original.getImage().getHeight();
		
		if (((width % 8) > 0) || ((height % 14) > 0)) {
			JOptionPane.showMessageDialog(this, "Image dimensions need to be a multiple of 8x14.");
			return;
		}
		
		File f;
		try {
			f = File.createTempFile("input", null, new File("."));

			String tmpname = f.getName();
			f.delete();
			
			String tmpname_bmp = tmpname + ".bmp";
			String tmpname_chr = tmpname + ".chr";
			String tmpname_mzm = tmpname + ".mzm";
			
			ImageIO.write(tpane_original.getImage(), "bmp", new File(tmpname_bmp));
			
			try {
				String cmd = "ccv " + tmpname_bmp;
				
				if (cfg_dither != "") cmd += " -dither " + cfg_dither;
				
				if (cfg_mzm) {
					cmd += " -mzm";
					if (cfg_offset != 0) cmd += " -offset " + cfg_offset;
					if (cfg_blank != 0) cmd += " -blank " + cfg_blank;
					if (cfg_c != 0) cmd += " -c " + cfg_c;
					if (cfg_exclude != "") cmd += " -exclude " + cfg_exclude;
				}
				
				if (cfg_reuse) cmd += " -reuse";
				if (cfg_noreuse) cmd += " -noreuse";
				cmd += " -threshold " + cfg_threshold;
				
				//System.out.println(cmd);
				Process myprocess = Runtime.getRuntime().exec(cmd);
				myprocess.waitFor();
				
				if (cfg_mzm) {
					converted_chars = tpane_converted.LoadMZM(tmpname_chr, tmpname_mzm, cfg_offset);
				} else {
					converted_chars = tpane_converted.LoadCharset(tmpname_chr, width, height);
				}
				
				File tmp_bmp = new File(tmpname_bmp);
				tmp_bmp.delete();
				File tmp_chr = new File(tmpname_chr);
				tmp_chr.delete();
				if (cfg_mzm) {
					File tmp_mzm = new File(tmpname_mzm);
					tmp_mzm.delete();
				}
				
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error running ccv. Ensure that it is in the same directory");
			}
			
			updateTabOriginal();
			updateTabConverted();
			tpane.setSelectedComponent(tpane_converted);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error writing to directory. Ensure you have write access");
			return;
		}
		
		menu_file_save_charset.setEnabled(tpane_converted.charset != null);
		menu_file_save_mzm.setEnabled(tpane_converted.mzm != null);
		repaint();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) { 
			if ((e.getModifiers() & KeyEvent.CTRL_MASK) > 0) {
				if (e.getKeyCode() == KeyEvent.VK_C) {
					clipboard_copy();
					return true;
				}
				if (e.getKeyCode() == KeyEvent.VK_V) {
					clipboard_paste();
					return true;
				}
				if (e.getKeyCode() == KeyEvent.VK_O) {
					actOpenImage();
					return true;
				}
				if (e.getKeyCode() == KeyEvent.VK_S) {
					actSaveCharset();
					return true;
				}
				if (e.getKeyCode() == KeyEvent.VK_M) {
					actSaveMZM();
					return true;
				}
			}
		}
		
		return false;
	}

	private void clipboard_paste() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			Image image = (Image) clipboard.getData(DataFlavor.imageFlavor);
			
			if (image instanceof BufferedImage) {
				tpane_original.LoadImage((BufferedImage)image);
				tpane_converted.RemoveImage();
				
				updateTabOriginal();
				updateTabConverted();
				
				convert_button.setEnabled(tpane_original.getImage() != null);
				tpane.setSelectedComponent(tpane_original);
			}
			
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}
	}

	private void clipboard_copy() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		clipboard.setContents((ImageDisplay)tpane.getSelectedComponent(), this);
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// TODO Auto-generated method stub
		
	}
}
