package bp;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;

import sybyline.anduril.util.Util;

public class BP {
	public static class BytePusherVM {
		private char[] mem = new char[0x1000000];
		private BytePusherIODriver ioDriver;
		public BytePusherVM(BytePusherIODriver ioDriver) {
			this.ioDriver = ioDriver;
		}
		/**
		 * Load ROM into memory
		 * 
		 * @param rom
		 */
		public void load(InputStream rom) throws IOException {
			mem = new char[0x1000000];
			int pc = 0;
			int i = 0;
			while ((i = rom.read()) != -1) {
				mem[pc++] = (char) i;
			}
		}
		/**
		 * CPU loop, to be called every 60th of a second
		 */
		public void run() {
			// run 65536 instructions
			short s = ioDriver.getKeyPress();
			mem[0] = (char) ((s & 0xFF00) >> 8);
			mem[1] = (char) (s & 0xFF);
			int i = 0x10000;
			int pc = getVal(2, 3);
			while (i-- != 0) {
				mem[getVal(pc + 3, 3)] = mem[getVal(pc, 3)];
				pc = getVal(pc + 6, 3);
			}
			ioDriver.renderAudioFrame(copy(getVal(6, 2) << 8, 256));
			ioDriver.renderDisplayFrame(copy(getVal(5, 1) << 16, 256 * 256));
		}
		private int getVal(int pc, int length) {
			int v = 0;
			for (int i = 0; i < length; i++) {
				v = (v << 8) + mem[pc++];
			}
			return v;
		}
		private char[] copy(int start, int length) {
			return Arrays.copyOfRange(mem, start, start + length);
		}
	}
	public static interface BytePusherIODriver {
		/**
		 * Get the current pressed key (0-9 A-F)
		 */
		short getKeyPress();
		/**
		 * Render 256 bytes of audio
		 */
		void renderAudioFrame(char[] data);
		/**
		 * Render 256*256 pixels.
		 */
		void renderDisplayFrame(char[] data);
	}

	public static class BytePusherIODriverImpl extends KeyAdapter implements BytePusherIODriver {
		private SourceDataLine line;
		private int keyPress;
		private BufferedImage image;
		/**
		 * Initializes the audio system
		 */
		public BytePusherIODriverImpl() {
			try {
				AudioFormat f = new AudioFormat(15360, 8, 1, true, false);
				line = AudioSystem.getSourceDataLine(f);
				line.open();
				line.start();
			} catch (LineUnavailableException e) {
				throw new RuntimeException(e);
			}
		}
		/**
		 * Get the current pressed key (0-9 A-F
		 */
		public short getKeyPress() {
			short k = 0;
			switch (keyPress) {
			case KeyEvent.VK_0:
				k += 1;
				break;
			case KeyEvent.VK_1:
				k += 2;
				break;
			case KeyEvent.VK_2:
				k += 4;
				break;
			case KeyEvent.VK_3:
				k += 8;
				break;
			case KeyEvent.VK_4:
				k += 16;
				break;
			case KeyEvent.VK_5:
				k += 32;
				break;
			case KeyEvent.VK_6:
				k += 64;
				break;
			case KeyEvent.VK_7:
				k += 128;
				break;
			case KeyEvent.VK_8:
				k += 256;
				break;
			case KeyEvent.VK_9:
				k += 512;
				break;
			case KeyEvent.VK_A:
				k += 1024;
				break;
			case KeyEvent.VK_B:
				k += 2048;
				break;
			case KeyEvent.VK_C:
				k += 4096;
				break;
			case KeyEvent.VK_D:
				k += 8192;
				break;
			case KeyEvent.VK_E:
				k += 16384;
				break;
			case KeyEvent.VK_F:
				k += 32768;
				break;
			}
			return k;
		}
		/**
		 * Render 256 bytes of audio
		 */
		public void renderAudioFrame(char[] data) {
			// convert from char [] to byte []
			byte[] b = new byte[256];
			for (int i = 0; i < 256; i++) {
				b[i] = (byte) data[i];
			}
			// send buffer to audio device
			line.write(b, 0, 256);
		}
		/**
		 * Render 256*256 pixels.
		 */
		public void renderDisplayFrame(char[] data) {
			image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
			int z = 0;
			for (int y = 0; y < 256; y++) {
				for (int x = 0; x < 256; x++) {
					int c = data[z++];
					if (c < 216) {
						int blue = c % 6;
						int green = ((c - blue) / 6) % 6;
						int red = ((c - blue - (6 * green)) / 36) % 6;
						image.setRGB(x, y, (red * 0x33 << 16) + (green * 0x33 << 8) + (blue * 0x33));
					}
				}
			}
		}
		/**
		 * Invoked when a key has been pressed. See the class description for
		 * {@link KeyEvent} for a definition of a key pressed event.
		 */
		public void keyPressed(KeyEvent e) {
			keyPress = e.getKeyCode();
		}
		/**
		 * Detect the key being released so that we can clear the key press.
		 */
		public void keyReleased(KeyEvent e) {
			keyPress = 0;
		}
		/**
		 * Get the image
		 * 
		 * @return the bufferedImage
		 */
		public BufferedImage getDisplayImage() {
			return image;
		}
	}

	public static class BytePusher extends JFrame {
		private BytePusherVM vm;
		private BytePusherIODriverImpl driver;
		private Canvas c;
		private FrameTask frameTask;
		/**
		 * Entry point
		 * 
		 * @param args
		 */
		public static void main(String[] args) {
			BytePusher b = new BytePusher();
			b.setVisible(true);
		}
		/**
		 * Constructor
		 */
		public BytePusher() {
			setUpWindow();
			setUpVm();
		}
		/**
		 * Create a JFrame with a single canvas within. Setup a key listener which will
		 * record the keypress. This will subsequently be handled by the FrameTask which
		 * is setup to run every 60th of a second.
		 */
		private void setUpWindow() {
			// create window
			setTitle("Bytepusher for Java");
			setLayout(new GridBagLayout());
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			getContentPane().setPreferredSize(new Dimension(256 * 3, 256 * 3));
			c = new Canvas();
			getContentPane().add(c);
			// canvas must be mon focusable otherwise key listeners
			// don't work
			c.setFocusable(false);
			c.setSize(new Dimension(256 * 3, 256 * 3));
			pack();
			c.createBufferStrategy(2);
			// when window is resized, also resize canvas
			this.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					c.setSize(getWidth() - 15, getHeight() - 38);
				}
			});
		}
		/**
		 * Load ROM into VM.
		 * 
		 * @param rom
		 */
		private void loadRom(String rom) {
			try {
//				FileInputStream fis = new FileInputStream(rom);
				InputStream fis = Util.resourceStream(BP.class, "/bp/scrolling.BytePusher");
				vm.load(fis);
				fis.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		private void setUpVm() {
			// set up bytepusher vm
			driver = new BytePusherIODriverImpl();
			vm = new BytePusherVM(driver);
			// register key listened which will be used by the driver
			this.addKeyListener(driver);
			loadRom("roms/audio.BytePusher");
			// startup vm
			frameTask = new FrameTask();
			new Timer().schedule(frameTask, 0, 1000 / 60);
		}
		/**
		 * TimerTask which is setup to fire every 60th of a second
		 */
		private class FrameTask extends TimerTask {
			/**
			 * Runs the VM every 60th of a second and renders graphics
			 */
			public void run() {
				vm.run();
				// render vm image to screen
				Graphics g = c.getBufferStrategy().getDrawGraphics();
				g.drawImage(driver.getDisplayImage(), 0, 0, c.getWidth(), c.getHeight(), null);
				// flip buffer
				c.getBufferStrategy().show();
				g.dispose();
			}
		}
	}
}
