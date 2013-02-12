import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

//Abstract class for pattern and source images
public abstract class MyImage {
	int width;        //Width of image
	int height;       //Height of image
	int[] colorArray; //Array representation of image
	String name;      //Name of the file
	
	//Create a MyImage
	MyImage(File file) throws IOException {
		if(file == null) { 
			throw new IllegalArgumentException("Given null image");
		}
		BufferedImage image = ImageIO.read(file); 
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.colorArray = image.getRGB(0, 0, width, height, null, 0, width);
		this.name = file.getName();
	}
	
	//Create Pattern Image
	public static PatternImage Pattern(File file) throws IOException {
		return new PatternImage(file);
	}
	
	//Create Source Image
	public static SourceImage Source(File file) throws IOException {
		return new SourceImage(file);
	}	

	//Get image width
	public int getWidth() {
		return this.width;
	}
	
	//Get image height
	public int getHeight() {
		return this.height;
	}
	
	//Get image file name
	public String getName() {
		return this.name;
	}
	
	//Get a Pixel at (x, y), first pixel at (0,0)
	public Pixel getPixel(int x, int y) {
		//Check if pixel is in image
		if(x >= this.getWidth() || y >= this.getHeight()) {
			throw new ArrayIndexOutOfBoundsException("Getting pixel out of bound");
		}
		
		//Get index for colorArray
		int index = y * this.getWidth() + x;
		return Pixel.makePixel(colorArray[index]);
	}
	
	//Returns a PixelHash from row y, index x to x+wide
	public PixelHash rowSegmentHash(int y, int x, int wide) {	
		//Checks if y is in height and the x index won't go off the side
		//of an image
		if(y >= this.getHeight() || x + wide > this.getWidth()) {
			throw new ArrayIndexOutOfBoundsException("Row segment out of bound");
		}
		
		//Adds ARGB values in row y from index x to x+wide
		//as separate RGB values into a PixelHash
		PixelHash hash = Pixel.makePixelHash(0, 0, 0);
		Pixel pixel;
		for(int i = x; i < x+wide; i++) {
			pixel = this.getPixel(i, y);
			hash.addPixel(pixel);
		}
		
		return hash;
	}
	
	//Returns an ArrayList where rows y to y+high are represented as PixelHashes
	//The PixelHash for each row is calculated by the x to x+wide Pixels
	public ArrayList<PixelHash> rowSegmentHashes(int y, int x, int wide, int high) {
		//Initialize array to hold PixelHashes
		ArrayList<PixelHash> hashes = new ArrayList<PixelHash>();
		PixelHash hash;
		
		//Adds ARGB from index x to x+wide for each row y to y+high
		for(int j = y; j < y+high; j++) {
			hash = this.rowSegmentHash(j, x, wide);
			hashes.add(hash);
		}
		
		return hashes;
	}
	
}

//Class of all pattern images
class PatternImage extends MyImage {
	ArrayList<PixelHash> HashArray; // Holds hash codes

	//PatternImage Creator
	PatternImage(File file) throws IOException {
		super(file);
		this.HashArray = this.rowSegmentHashes(0, 0, this.getWidth(), this.getHeight());
	}
	
	//Calculates hash value of row y
	public PixelHash rowHash(int y) {
		//Get hash of row segment from 0 to width
		return HashArray.get(y);
	}
	
}

//Source image to be searched
class SourceImage extends MyImage {
	
	//SourceImage creator
	SourceImage(File file) throws IOException {
		super(file);
	}
	
}

//Checks if a pattern image is in a source image
class SearchImages {
	PatternImage pattern; //PatternImage
	SourceImage source;   //SourceImage
	int current_x;        //x location in source
	int current_y;        //y location in source
	int y_threshold;      //Don't let current_x pass
	int x_threshold;      //Don't let current_y pass
	ArrayList<PixelHash> sourceSegmentHashes; 	//Stores the rolling PixelHashes of the source image
	
	//Create a searchImages
	SearchImages(File patternFile, File sourceFile) throws IOException {
		PatternImage pattern = MyImage.Pattern(patternFile);
		SourceImage source = MyImage.Source(sourceFile);
		this.pattern = pattern;
		this.source = source;
		this.current_x = 0;
		this.current_y = 0;
		this.y_threshold = source.getHeight() - pattern.getHeight();
		this.x_threshold = source.getWidth() - pattern.getWidth();
		this.sourceSegmentHashes = 
				source.rowSegmentHashes(0, 0, pattern.getWidth(), pattern.getHeight());
	}
	
	//Create a search object
	public static SearchImages search(File patternFile, File sourceFile) throws IOException {
		return new SearchImages(patternFile, sourceFile);
	}
	
	//Mutate the sourceSegmentHashes by rolling the hash
	void nextSegmentHashes() {
		//Check if pattern image doesn't fits with current_y
		if(current_y >= y_threshold) {
			throw new RuntimeException("Can't get next source hashes");
		} else {
			//Check if pattern image doesn't fits with current_x
			//If it does, then go to next Y row
			if(current_x >= x_threshold) {
				this.nextYSegmentHashes();
			} else {
				this.nextXSegmentHashes();
			}
		}
	}
	
	//Roll source PixelHashes to next x column
	void nextXSegmentHashes() {
		for(int j = 0; j < pattern.getHeight(); j++) {
			PixelHash hash = sourceSegmentHashes.get(j);
			Pixel subPixel = source.getPixel(current_x, current_y);
			Pixel addPixel = source.getPixel(current_x + pattern.getWidth(), current_y);
			hash.subPixel(subPixel);
			hash.addPixel(addPixel);
			sourceSegmentHashes.set(j, hash);
		}
		current_x += 1;
		return;
	}
	
	//Roll source PixelHases to next y row
	void nextYSegmentHashes() {
		current_x = 0;
		current_y += 1;
		sourceSegmentHashes = 
				source.rowSegmentHashes(current_y, current_x, 
						pattern.getWidth(), pattern.getHeight());
		return;
	}
	
	//Checks if the PixelHash of the pattern image at row pattern_y is near
	//the PixelHash of the source image at row pattern_y
	boolean matchRowHash(int pattern_y) {
		PixelHash patternRowHash = pattern.rowHash(pattern_y);
		PixelHash sourceRowHash = sourceSegmentHashes.get(pattern_y);
		
		return patternRowHash.compare(sourceRowHash) < 2000;
	}
	
	//Checks if the ARGB values of a pattern and source image pixels are similar,
	//not just the hash values
	boolean deepRowComparison(int pattern_y, int source_y, int source_x) {
		int p_index = 0;                 //Index for row pattern_y
		int s_index = source_x;          //Index for row source_y
		int p_wide = pattern.getWidth(); //Width of source image segment
		Pixel p_pixel;                   //Holds pattern pixel sRGB values
		Pixel s_pixel;                   //Holds source pixel sRGB values
		
		//Loop until the end of the pattern image
		while(p_index < p_wide) {
			//Get pattern and source pixel
			p_pixel = pattern.getPixel(p_index, pattern_y);
			s_pixel = source.getPixel(s_index, source_y);
			
			//Check if Pixels are not similar enough
			if(p_pixel.compare(s_pixel) > 220) {
				return false;
			} 
			
			//Increment both to next pixel
			p_index++;
			s_index++;
		}
		//Return true is your reach the end
		return true;
	}
	
	//Look for possible matches quickly with just hash values
	void possibleMatch() {
		
		//Check special case that pattern and source image have same height
		if(current_y == y_threshold && current_x <= x_threshold) {
			if(this.matchRowHash(0)) {
				//Does a real comparison of the first row, element by element
				if(this.deepRowComparison(0, current_y, current_x)) {
					//Hands it off to another function to 
					//check if it is really a match
					this.checkMatch(current_x, current_y);
					return;
				}
			}
		}
		
		//Loop while the pattern image can still fit in the source image
		while(current_y < y_threshold) {
			while(current_x < x_threshold) {
				//Check if the PixelHashes match
				if(this.matchRowHash(0)) {
					//Does a real comparison of the first row, element by element
					if(this.deepRowComparison(0, current_y, current_x)) {
						//Hands it off to another function to 
						//check if it is really a match
						this.checkMatch(current_x, current_y);
						
						return;
					}
				}
				this.nextSegmentHashes();
			}
			this.nextSegmentHashes();
		}
	}
	
	//Checks if a segment of the source image matches the entire pattern image
	void checkMatch(int match_x, int match_y) {
		int source_x = match_x;         //x index to compare at each row
		int source_y = match_y + 1;     //y row of the source image
		int pattern_y = 1;              //y row of the pattern image
		int high = pattern.getHeight(); //Number of rows to compare
		int wide = pattern.getWidth();  //Wide of pattern image
		
		//Loop until at the last row of pattern image
		while(pattern_y < high) {
			//Exit loop if they are not equal
			if(!this.deepRowComparison(pattern_y, source_y, source_x)) {
				break;
			}
			
			//If at the last row for pattern image, report a match
			if(pattern_y == high - 1) {
				System.out.println(pattern.getName() + " matches " + source.getName()
						+ " at " + wide + "x" + high + "+"
						+ current_x + "+" + current_y);
				
				//Move PixelHash beyond this match
				for(int i = 0; i < wide - 1; i++) {
					if(current_y < y_threshold) {
						this.nextSegmentHashes();
					}
				}
			}
			
			//Increment rows
			source_y++;
			pattern_y++;
		}
		
		//Move PixelHash
		if(current_y < y_threshold){
			this.nextSegmentHashes();
		}
		return;
	}
	
	//Starts the search and looks for possible matches
	void startSearch() {
		//Continue search until the pattern image moves beyond the source image
		do {
			this.possibleMatch();
		} while(current_y < y_threshold);
	}
}