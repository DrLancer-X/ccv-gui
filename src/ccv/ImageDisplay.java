package ccv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.*;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ImageDisplay extends JPanel implements Transferable {
	private BufferedImage myImage = null;
	private Font myFont;
	
    public byte[] charset = null;
    public byte[] mzm = null;
	
	public ImageDisplay() {
		super();
	}
	
	public void LoadDefault() throws IOException {
		myImage = ImageIO.read(this.getClass().getResource("/res/default.png"));
		updateImage();
	}
	
	public void LoadImage(File imageio) throws IOException {
		myImage = ImageIO.read(imageio);
		updateImage();
	}
	
	public void LoadImage(BufferedImage image) {
		myImage = image;
		updateImage();
	}
	
	public void RemoveImage() {
		myImage = null;
		charset = null;
		mzm = null;
	}
	
	private void updateImage() {
		repaint();
	}
	
	public BufferedImage getImage() {
		return myImage;
	}

    public Dimension getPreferredSize() {
    	return new Dimension(640, 350);
    }

    public void paintComponent(Graphics g) {
    	int width = this.getWidth();
    	int height = this.getHeight();
    	
    	
    	// Draw checkered background
    	
    	g.setColor(Color.WHITE);
    	g.fillRect(0, 0, width, height);
    	g.setColor(Color.LIGHT_GRAY);
    	for (int x = 0; x < width; x += 8) {
    		for (int y = 0; y < height; y += 14) {
    			if (((x/8 + y/14) % 2) == 1) {
    				g.fillRect(x, y, 8, 14);
    			}
    		}
    	}
    	
    	// If no image is loaded, display a message instead
    	
    	if (myImage == null) {
	    	String text_string = "(No image)";
	    	
	    	int string_width = g.getFontMetrics().stringWidth(text_string);
	    	int string_height = g.getFontMetrics().getHeight();
	    	
	    	int string_x = (width - string_width)/2;
	    	int string_y = (height-string_height)/2;
	    	
	    	g.setColor(Color.YELLOW);
	    	g.fillRect(string_x - 4, string_y + 4, string_width + 8, string_height);
	    	g.setColor(Color.BLACK);
	    	
	    	g.drawString(text_string, string_x, string_y + string_height);
    	} else {
    		g.drawImage(myImage, 0, 0, null);
    	}
    	/*
        super.paintComponent(g);
        
        
        
        g.setColor(new Color(100, 200, 150));
        g.drawRect(0,  0, this.getWidth(), this.getHeight());

        // Draw Text
        //g.drawString("This is my custom Panel!",10,20);
         */
    }
    
    private int load_charset(String charset_path) {
		File file = new File(charset_path);
		if (!file.exists()) {
			charset = null;
			return 0;
		}
		int file_size = (int) file.length();
		
		int file_chars = file_size / 14;
		charset = new byte[file_size];
		try {
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(charset_path));
			is.read(charset);
			is.close();
		} catch (FileNotFoundException e) {
			return 0;
		} catch (IOException e) {
			return 0;
		}
		return file_chars;
    }
    
    private void load_mzm(String mzm_path) {
		File file = new File(mzm_path);
		if (!file.exists()) {
			mzm = null;
			return;
		}
		int file_size = (int) file.length();
		
		mzm = new byte[file_size];
		try {
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(mzm_path));
			is.read(mzm);
			is.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
    }

	public int LoadCharset(String charset_path, int w, int h) {
		mzm = null;
		myImage = null;
		
		int chars = load_charset(charset_path);
		
		if (chars > 0) {
			myImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int cx = x / 8;
					int cy = y / 14;
					int px = x % 8;
					int py = y % 14;
					int chr = cy * (w / 8) + cx;
					if ((chr >= 0) && (chr < chars)) {
						myImage.setRGB(x, y, (charset[chr * 14 + py] & (128 >> px)) > 0 ? 0xFFFFFFFF : 0xFF000000);
					}
				}
			}
		}
		
		return chars;
	}

	public int LoadMZM(String charset_path, String mzm_path, int offset) {
		myImage = null;
		
		int chars = load_charset(charset_path);
		load_mzm(mzm_path);
		
		if ((charset != null) && (mzm != null)) {
			int width = (int)(mzm[5]) & 0xFF << 8 | ((int)(mzm[4]) & 0xFF);
			int height = (int)(mzm[7]) & 0xFF << 8 | ((int)(mzm[6]) & 0xFF);
						
			myImage = new BufferedImage(width * 8, height * 14, BufferedImage.TYPE_INT_ARGB);
			
			int pos = 0;
			for (int cy = 0; cy < height; cy++) {
				for (int cx = 0; cx < width; cx++) {
					int chr = mzm[pos * 2 + 16] & 0xFF;
					chr -= offset;
					for (int py = 0; py < 14; py++) {
						for (int px = 0; px < 8; px++) {
							myImage.setRGB(cx * 8 + px, cy * 14 + py, (charset[chr * 14 + py] & (128 >> px)) > 0 ? 0xFFFFFFFF : 0xFF000000);
						}
					}
					pos++;
				}
			}
		}
		
		return chars;
	}

	@Override
	public Object getTransferData(DataFlavor arg0)
			throws UnsupportedFlavorException, IOException {
		if (myImage != null) {
			if (arg0 == DataFlavor.imageFlavor) {
				return myImage;
			} else {
				throw new UnsupportedFlavorException(arg0);
			}
		}
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{DataFlavor.imageFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor arg0) {
		return arg0 == DataFlavor.imageFlavor;
	}
}
