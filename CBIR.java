/* 
 * class CBIR: set up the interface displaying pictures in database as well as showing 
 * retrieval results for each of the two Content-based image retrieval algorithms.
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.awt.*;

import javax.swing.*;

public class CBIR extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final Insets insets = new Insets(0, 0, 0, 0);
	private JLabel photographLabel = new JLabel(); // container to hold a large
	private JButton[] button; // creates an array of JButtons
	// creates an array to keep up with the image order
	private Integer[] buttonOrder = new Integer[100];
	// store size(pixel number) of each image
	private double[] imageSize = new double[100];

	private GridLayout gridLayout1;
	private GridLayout gridLayout3;
	private JPanel panelBottom1;
	private JPanel panelTop;
	private JPanel buttonPanel;
	
	// matrix to store intensity distribution
	private Double[][] intensityMatrix = new Double[100][26];
	// matrix to store colorCode distribution
	private Double[][] colorCodeMatrix = new Double[100][64];
	// matrix to store intensity distance of each two pictures
	private Double[][] intensityDistanceMatrix = new Double[100][100];
	// matrix to store colorCode distance for each two pictures
	private Double[][] colorDistanceMatrix = new Double[100][100];
    private Double[][] normalizedMatrix = new Double[103][89];
    private Double [][] feedbackMatrix = new Double[100][89];
    private Double [][] fdDistanceMatrix = new Double[100][100];
    private int fdCount = 0;
    private JCheckBox relevance;
    
    private JPanel [] picPanel;
    private JCheckBox [] boxes;
    private final int MAX_WIDTH = 900;
    private final int MAX_HEIGHT = 720;
	// store the current picture number displayed in photographLable
	int picNo = 0;
	// keeps up with the number of images displayed since the first page
	int imageCount = 0;
	int pageNo = 1;

	public static void main(String args[]) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CBIR app = new CBIR();
				app.setVisible(true);
			}
		});
	}

	public CBIR() {
		// The following lines set up the interface including the layout of the buttons and JPanels
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Icon Demo: Please Select an Image");
		setResizable(false);  // the size of the frame cannot be changed
		setSize(MAX_WIDTH, MAX_HEIGHT); // define the size of frame
		setLocationRelativeTo(null);

		panelTop = new JPanel();     // initiate panelTop and panelBottom1
		panelBottom1 = new JPanel(); 
		fixSize(panelTop, MAX_WIDTH, MAX_HEIGHT * 2 / 5);    // set fixed size to panelTop and panelBottom1
		fixSize(panelBottom1, MAX_WIDTH, MAX_HEIGHT * 3 / 5);
		
		gridLayout1 = new GridLayout(4, 5, 5, 5);   // initiate layout manager to panelTop and panelBottom1
		gridLayout3 = new GridLayout(1, 2, 5, 5);
		panelBottom1.setLayout(gridLayout1);   // set layout manager to panelTop and panelBottom1
		panelTop.setLayout(gridLayout3);

		Container ctp = getContentPane();    // set layout manager to the whole container
		ctp.setLayout(new GridBagLayout());  // add panelTop and panelBottom1 to the big container
		addComponent(ctp, panelTop, 0, 0, 1, 4, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL);
		addComponent(ctp, panelBottom1, 0, 4, 1, 6, GridBagConstraints.SOUTH, GridBagConstraints.BOTH);

		// set properties of photographLabel
		photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panelTop.add(photographLabel);  // add photographLabel to panelTop
		
		buttonPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();    // initiate layout manager for buttonPanel
		buttonPanel.setLayout(layout); // set layout
		panelTop.add(buttonPanel);   // add buttonPanel to panelTop
		
		// initiate user friendly buttons
		JButton previousPage = new JButton("Previous Page");
		JButton nextPage = new JButton("Next Page");
		JButton intensity = new JButton("Retrieve by Intensity");
		JButton colorCode = new JButton("Retrieve by Color Code");
		JButton reset = new JButton("Reset");
		JButton close = new JButton("Close");
		JButton colorCodeAndIntensity = new JButton("Color Code and Intensity");
		relevance = new JCheckBox("Relevance Feedback");
		
		// add action listener to buttons
	    nextPage.addActionListener(new nextPageHandler());
		previousPage.addActionListener(new previousPageHandler());
		intensity.addActionListener(new intensityHandler());
		colorCode.addActionListener(new colorCodeHandler());
		reset.addActionListener(new resetHandler());
		close.addActionListener(new closeHandler());
		colorCodeAndIntensity.addActionListener(new colorCodeAndIntensityHandler());
		// add ItemListener to relevance JCheckBox
		relevance.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent e) {
		    	if (relevance.isSelected()) {
		             displayFirstPage();
		    	}
		        setVisible();
		    }
		});
        
		// add user buttons and check box to buttonPanel in predefined pattern.
		addComponent(buttonPanel, close, 0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		addComponent(buttonPanel, reset, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		addComponent(buttonPanel, intensity, 0, 2, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		addComponent(buttonPanel, colorCode, 0, 3, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		addComponent(buttonPanel, previousPage, 0, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		addComponent(buttonPanel, nextPage, 1, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		addComponent(buttonPanel, colorCodeAndIntensity, 0, 4, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		addComponent(buttonPanel, relevance, 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE);
		
		button = new JButton[100];
		boxes = new JCheckBox[100];
		picPanel = new JPanel[100];
		/*
		 * This for loop goes through the images in the database and stores them
		 * as icons and adds the images to JButtons and then to the JButton
		 * array
		 */
		for (int i = 0; i < 100; i++) {
			ImageIcon icon;
			icon = new ImageIcon(getClass().getResource("images/" + (i + 1) + ".jpg"));

			if (icon != null) {
				Image resized = resizeImage(icon.getImage(), 165, 72);  // resize each image
				ImageIcon newIcon = new ImageIcon(resized);    // initiate an ImageIcon by resized image
				button[i] = new JButton(newIcon);     // paint a button using ImageIcon
				fixSize(button[i], 165, 72);         // set fixed size to each button
				button[i].setOpaque(false);      // set button background color
				button[i].setContentAreaFilled(false);  // set button content area unpainted
				// add listener to buttons
				button[i].addActionListener(new IconButtonHandler(i, icon));
				buttonOrder[i] = i;   // store image orders to be displayed in buttonOrder
			}
			picPanel[i] = new JPanel(new GridBagLayout());  // initiate picPanel
			// add button to picPanel
			addComponent(picPanel[i], button[i], 0, 0, 1, 5, GridBagConstraints.NORTH, GridBagConstraints.NONE);
			boxes[i] = new JCheckBox();     // initiate boxes
			boxes[i].setSelected(false);    // set boxes to be unselected originally
			boxes[i].setVisible(false);     // set boxes to be invisible originally
			// add boxes to picPanel
			addComponent(picPanel[i], boxes[i], 0, 5, 1, 1, GridBagConstraints.SOUTH, GridBagConstraints.BOTH);
			//panelBottom1.add(picPanel);
			
		}

		// read intensity file created by class readImage
		readIntensityFile();
		// calculate intensity distance from the intensityMatrix
		calDistanceMatrix(true);
		// read colorcode file created by class readImage
		readColorCodeFile();
		// calculate colorcode distance from the colorCodeMatrix
		calDistanceMatrix(false);
		// Normalize feature matrix
		normalizeFeatureMatrix();
		// printDistMatrix();
		displayFirstPage();
		
	}
	
	//fixSize(): used in set fixed size to panelTop and panelBotttom1, fix size of two JPanels
	private void fixSize(Container c, int width, int height) {
		c.setPreferredSize(new Dimension(width, height));
		c.setMaximumSize(c.getPreferredSize());
		c.setMinimumSize(c.getPreferredSize());
	}

	// addComponent(): the function adds buttons to buttonPanel in the designated pattern
	private void addComponent(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, int anchor, int fill) {
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, 1.0, 1.0, anchor, fill, insets, 0, 0);
		container.add(component, gbc);
	}
		
	/*
	 * resizeImage(): shrink image then the whole picture could show on each
	 * button
	 */
	private Image resizeImage(Image srcImg, int w, int h) {
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bi.createGraphics();
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return bi;
	}

	/*
	 * This method displays the first twenty images in the panelBottom. The for
	 * loop starts at number one and gets the image number stored in the
	 * buttonOrder array and assigns the value to imageButNo. The button
	 * associated with the image is then added to panelBottom1. The for loop
	 * continues this process until twenty images are displayed in the
	 * panelBottom1
	 */
	private void displayFirstPage() {
		boolean selected = relevance.isSelected();  // get selected status from relevance 
		boxes[picNo].setSelected(selected);    // set selected status to the picture selected and displayed in JLable
		
		int imageButNo = 0;
		imageCount = 0;
		panelBottom1.removeAll();
		for (int i = 0; i < 20; i++) {
			imageButNo = buttonOrder[i];
			boxes[imageButNo].setVisible(selected);   // set visible status according to the selected stutus of relevance
			panelBottom1.add(picPanel[imageButNo]);   // add picPanel to panelBottom1
			imageCount++;
		}
		panelBottom1.revalidate();
		panelBottom1.repaint();
	}
	
	// setVisible(): helper of displayFirstPage, set visible status of each check box to picPanel
	private void setVisible() {
		for (int i = 0; i < boxes.length; i++) {
			boxes[i].setVisible(relevance.isSelected());
		}
	}
	
	/*
	 * This class implements an ActionListener for each iconButton. When an icon
	 * button is clicked, the image on the the button is added to the
	 * photographLabel and the picNo is set to the image number selected and
	 * being displayed.
	 */
	private class IconButtonHandler implements ActionListener {
		int pNo = 0;
		ImageIcon iconUsed;

		IconButtonHandler(int i, ImageIcon j) {
			pNo = i;
			iconUsed = j; // sets the icon to the one used in the button
		}

		public void actionPerformed(ActionEvent e) {
			photographLabel.setIcon(iconUsed);
			picNo = pNo;
		}
	}

	/*
	 * This class implements ActionListener for Previous Page button. when
	 * Previous Page button is clicked, previous 20 images would be displayed.
	 */
	private class previousPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			int imageButNo = 0;
			int startImage = imageCount - 40;
			int endImage = imageCount - 20;
			
			if (imageCount > 59) {    // when the page to be displayed is not the first page
				panelBottom1.removeAll();
				for (int i = startImage; i < endImage; i++) {
		     	    imageButNo = buttonOrder[i];
				    panelBottom1.add(picPanel[imageButNo]);   // add picPanel to panelBottom1
				    imageCount--;
			    }
			    panelBottom1.revalidate();
				panelBottom1.repaint(); 
			} else {
				displayFirstPage();  // when the page to be displayed is the first page
			}
		}

	}
	
	/*
	 * This class implements ActionListenner for Next Page button. when next
	 * Page button is clicked, next 20 images would be displayed.
	 */
	private class nextPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			int imageButNo = 0;
			int endImage = imageCount + 20;
			if (endImage <= 100) {
				panelBottom1.removeAll();
				for (int i = imageCount; i < endImage; i++) {
					imageButNo = buttonOrder[i];
					panelBottom1.add(picPanel[imageButNo]);  // add picPanel to panelBottom1
					imageCount++;

				}
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}
	}

	/*
	 * This class implements ActionListener for intensity button. when the
	 * Retrieval by intensity button is clicked, image numbers are ordered by
	 * intensity distance and stored in buttonOrder, the first 20 images get
	 * displayed first.
	 */
	private class intensityHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			// sort the intensity distance array and store the index to buttonOrder
			Arrays.sort(buttonOrder, new Comparator<Integer>() {
				public int compare(Integer x, Integer y) {
					double dis = intensityDistanceMatrix[picNo][x] - intensityDistanceMatrix[picNo][y];
					if (dis > 0) return 1;
					if (dis < 0) return -1;
					return 0;
				}
			});
			clearRelevance();   // deselect relevance check box once intensity button is clicked
			displayFirstPage();
		}
	}

	/*
	 * This class implements ActionListener for colorCode button. when the
	 * Retrieval by colorCode button is clicked, image numbers are ordered by
	 * colorCode distance and stored in buttonOrder, the first 20 images get
	 * displayed first.
	 */
	private class colorCodeHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			// sort the intensity distance array and store the index to buttonOrder
			Arrays.sort(buttonOrder, new Comparator<Integer>() {
				public int compare(Integer x, Integer y) {
					double dis = colorDistanceMatrix[picNo][x] - colorDistanceMatrix[picNo][y];
					if (dis > 0) return 1;
					if (dis < 0) return -1;
					return 0;
				}
			});
			clearRelevance();   // deselect relevance check box once colorCode button is clicked
			displayFirstPage();
		}
	}

	/*
	 * calDistanceMatrix(): the method which calculates intensityDistanceMatrix
	 * and colorDistanceMatrix, when true was passed in, it signals the method
	 * to calculate intensityDistanceMatrix.
	 */
	private void calDistanceMatrix(Boolean flag) {
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j <= i; j++) {
				setDistance(i, j, flag);
			}
		}
		return;
	}

	/*
	 * helper of calDistanceMatrix, calculate the distance of each image to a
	 * certain image, and store the results to intensityDistanceMatrix and
	 * colorDistanceMatrix. when a flag passed in was true, it calculates
	 * intensity distance.
	 */
	private void setDistance(int firstPic, int secondPic, Boolean flag) {
		double dis = 0.0;
		int length = (flag) ? 26 : 64;
		int i = (flag) ? 1 : 0;

		Double[] firstIntensity = Arrays.copyOf((flag) ? intensityMatrix[firstPic] : colorCodeMatrix[firstPic], length);
		Double[] secondIntensity = Arrays.copyOf((flag) ? intensityMatrix[secondPic] : colorCodeMatrix[secondPic],
				length);
		for (; i < length; i++) {
			dis += Math.abs(firstIntensity[i] / imageSize[firstPic] - secondIntensity[i] / imageSize[secondPic]);
		}
		if (flag) {
			intensityDistanceMatrix[firstPic][secondPic] = dis;
			intensityDistanceMatrix[secondPic][firstPic] = dis;
		} else {
			colorDistanceMatrix[firstPic][secondPic] = dis;
			colorDistanceMatrix[secondPic][firstPic] = dis;
		}
		return;
	}
	
	/*
	 * This class implements ActionListener for colorCode and Intensity button. when the
	 * button is clicked, image numbers are ordered by the distance from fdDistanceMatrix
	 * stored in buttonOrder, the first 20 images get displayed first.
	 */
	private class colorCodeAndIntensityHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			fdCount = 0;             // store the number of pictures selected from panelBottom1 besides Query image
			
			// copy the image data from normalizedMatrix to feedbackMatrix to calculate new weights of 89 features
			System.arraycopy(normalizedMatrix[picNo], 0, feedbackMatrix[fdCount++], 0, normalizedMatrix[picNo].length);
			
			// go through the database and add all picture data to feedbackMatrix
			for (int i = 0; i < 100; i++) {
				int index = buttonOrder[i];
				if (boxes[index].isSelected() && index != picNo) {
					System.arraycopy(normalizedMatrix[index], 0, feedbackMatrix[fdCount++], 0, normalizedMatrix[index].length);
				}
			}
			
			if (fdCount == 1) {           // before 1st iteration, all weights are equal
			    for (int i = 0; i < 89; i++) {
				    feedbackMatrix[fdCount+1][i] = (1.0)/89;
			    }
			} else {
			    calStandardDeviation();   // calculate new standard deviation
			    updateWeights();          // update new weights
			    normalizeWeights();       // normalize new weights
			}
			calfdDistanceMatrix();   // calculate fdDistanceMatrix according to new feature weights
			Arrays.sort(buttonOrder, new Comparator<Integer>() {
				public int compare(Integer x, Integer y) {
					double dis = fdDistanceMatrix[picNo][x] - fdDistanceMatrix[picNo][y];
					if (dis > 0) return 1;
					if (dis < 0) return -1;
					return 0;
				}
			});
			displayFirstPage();
		}
	}

	// calStandardDeviation(): helper of colorCodeAndIntensity Handler
	private void calStandardDeviation() {
		double sum;
		
		for (int i = 0; i < 89; i++) {
			sum = 0;
			for (int j = 0; j < fdCount; j++) {
				sum += feedbackMatrix[j][i];
			}
			feedbackMatrix[fdCount][i] = sum/(fdCount);   // mean of each feature stored in feedbackMatrix
			sum = 0;
			for (int j = 0; j< fdCount; j++) {
				sum += Math.pow(feedbackMatrix[j][i] - feedbackMatrix[fdCount][i], 2);
			}
			// standard deviation of each feature stored in feedbackMatrix
			feedbackMatrix[fdCount+1][i] = Math.sqrt(sum/(fdCount - 1));  
		}
	}
	
	//updateWeights(): helper of colorCodeAndIntensity handler
	private void updateWeights() {
		double minStd = 100000000;
		for (int i = 0; i < 89; i++) {
			if (feedbackMatrix[fdCount+1][i] > 0 && feedbackMatrix[fdCount+1][i] < minStd) {
				minStd = feedbackMatrix[fdCount+1][i];   // store minimal standard deviation
			}
		}
		for (int i = 0; i < 89; i++) {
			if (feedbackMatrix[fdCount+1][i] == 0) {   // if standard deviation is 0
				if (feedbackMatrix[fdCount][i] != 0) {   // mean is not 0 when standard deviation is 0
					feedbackMatrix[fdCount+1][i] = 2.0 / minStd;
				}
			} else {
			    feedbackMatrix[fdCount+1][i] = 1.0/feedbackMatrix[fdCount+1][i];
			}
		}
	}
	
	// normalizeWeights(): helper of colorCodeAndIntensity handler
	private void normalizeWeights() {
		double sum = 0;
		for (int i = 0; i < 89; i++) {
			sum += feedbackMatrix[fdCount+1][i];  // get sum of each feather data
		}
		for (int i = 0; i< 89; i++) {
			feedbackMatrix[fdCount+1][i] = feedbackMatrix[fdCount+1][i]/sum; 
		}
	}
	
	// calfdDistanceMatrix(): helper of colorCodeAndIntensity handler
	private void calfdDistanceMatrix() {
		double dis = 0;
		for (int i = 0; i < 100; i++) {  // calculate each image to other images by weights from feedbackMatrix and normalizedmatrix
			for (int j = 0; j <= i; j++) {
				dis = 0;
				for (int z= 0; z < 89; z++) {
					dis += feedbackMatrix[fdCount+1][z] * Math.abs(normalizedMatrix[i][z] - normalizedMatrix[j][z]);
				}
				fdDistanceMatrix[i][j] = dis;
				fdDistanceMatrix[j][i] = dis;
			}
		}
	}


	/*
	 * This class implements ActionListener to reset button, when reset button
	 * is clicked, buttonOrder is renewed to original order and the first page
	 * is displayed.
	 */
	private class resetHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			clearRelevance();
			for (int i = 0; i < 100; i++) {
				buttonOrder[i] = i;
			}
			displayFirstPage();
		}
	}
	
	// clearRelevance(): Helper of resetHander, intensityHandler and colorCodeHandler
	private void clearRelevance() {
		relevance.setSelected(false);
		for (int i = 0; i < 100; i++) {
			boxes[i].setSelected(false);
			boxes[i].setVisible(false);
		}
	}

	/*
	 * This class implements Action Listener to close button, when close button
	 * is clicked, the system exit and the program terminates.
	 */
	private class closeHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

	/*
	 * readIntensityFile(): read data from the intensity.txt file and initiate
	 * intensityMatrix. when error happens in opening the file, exception will
	 * be caught.
	 */
	private void readIntensityFile() {
		int matrixCount = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader("intensity.txt"))) {
			String line = reader.readLine();

			while (line != null) {
				copyStringToArray(line, intensityMatrix[matrixCount], matrixCount);
				matrixCount++;
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("Error in reading from intensity.txt file");
		}
		return;
	}

	/*
	 * copyStringToArray(): helper of readIntensityFile() and
	 * readColorCodeFile(). Extract data from one line and parse it into double
	 * which used to fill in intensityMatrix and colorCodeMatrix.
	 */
	private void copyStringToArray(String line, Double[] array, int matrixCount) {
		String[] sArray = line.split(" ");
		if (sArray.length == 26) {
			imageSize[matrixCount] = Double.parseDouble(sArray[0]);
		}
		for (int i = 0; i < sArray.length; i++) {
			array[i] = Double.parseDouble(sArray[i]);
		}
		return;
	}

	/*
	 * readColorCodeFile(): read data from the colorcode.txt file and initiate
	 * colorCodeMatrix. when error happens in opening file, exception will be
	 * caught.
	 */
	private void readColorCodeFile() {
		int matrixCount = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader("colorcode.txt"))) {
			String line = reader.readLine();
			while (line != null) {
				copyStringToArray(line, colorCodeMatrix[matrixCount], matrixCount);
				matrixCount++;
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("Error in reading from colorcode.txt file");
		}
		return;
	}
	
	// normalizeFeatureMatrix(): get the normalizedMatrix from intensityMatrix and colorCodeMatrix
	private void normalizeFeatureMatrix() {
		initFeatureMatrix();
		normalizeMatrix();
	}
	
	// initFeatureMatrix(): helper of normalizedFeatureMatrix, copy matrixes into normalizedMatrix
	private void initFeatureMatrix() {
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 25; j++) {
				normalizedMatrix[i][j] = intensityMatrix[i][j + 1]/imageSize[i];
			}
			for (int j = 25; j < 89; j++) {
				normalizedMatrix[i][j] = colorCodeMatrix[i][j - 25]/imageSize[i];
			}
		}
	}
	
	// normalizedMatrix(): helper of normalizedFeatureMatrix, help to normalize normalizedMatrix
	private void normalizeMatrix() {
		double sum;
		for (int i = 0; i < 89; i++) {
			sum = 0;
			for (int j = 0; j < 100; j++) {
				sum += normalizedMatrix[j][i];
			}
			normalizedMatrix[100][i] = sum/100.0; // the 100th row in normalizedMatrix store mean of each feature
			
			sum = 0;
			for (int j = 0; j < 100; j++) {
				sum += Math.pow(normalizedMatrix[j][i] - normalizedMatrix[100][i], 2); 
			} 
			normalizedMatrix[101][i] = Math.sqrt(sum/99); // the 101th row in normalizedMatrix store standard deviation 
			
			for(int j = 0; j < 100; j++) {
				// when the picture's feature data is the same with mean of the feature, its data should be 0 after normalization
				normalizedMatrix[j][i] = normalizedMatrix[j][i] - normalizedMatrix[100][i];  
				// when standard deviation is nonzero
				if (normalizedMatrix[101][i] > 0) {
				    normalizedMatrix[j][i] = normalizedMatrix[j][i] / normalizedMatrix[101][i];
				}
			}
		}
		
	}

}
