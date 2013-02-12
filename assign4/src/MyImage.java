import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

//Abstract class for pattern and source images
public abstract class MyImage {
	int width; //Width of image
	int height; //Height of image
	int[] colorArray; //Array representation of image
	String name;
	
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

	//Get image length
	public int getWidth() {
		return this.width;
	}
	
	//Get image height
	public int getHeight() {
		return this.height;
	}
	
	public String getName() {
		return this.name;
	}
	
	//Get single integer pixel sRGB value, first pixel at (0,0)
	public Pixel getPixel(int x, int y) {
		//Check if pixel is in image
		if(x >= this.getWidth() || y >= this.getHeight()) {
			throw new ArrayIndexOutOfBoundsException("Pixel out of bound");
		}
		
		//Get index for colorArray
		int index = y * this.getWidth() + x;
		return Pixel.makePixel(colorArray[index]);
	}
	
	//Returns hash value from row y from index x to x+wide
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
	
	//Returns an array where rows y to y+high are represented as array elements
	//The hash for each row is represented for their x to x+wide pixels
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
	SourceImage source; //SourceImage
	int current_x; //x location in source
	int current_y; //y location in source
	int y_threshold; 
	int x_threshold;
	ArrayList<PixelHash> sourceSegmentHashes;
	
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
		this.sourceSegmentHashes = source.rowSegmentHashes(0, 0, pattern.getWidth(), pattern.getHeight());
	}
	
	//Create a search object
	public static SearchImages search(File patternFile, File sourceFile) throws IOException {
		return new SearchImages(patternFile, sourceFile);
	}
	
	//Mutate the sourceSegmentHashes by rolling the hash
	void nextSegmentHashes() {
		if(current_x >= x_threshold) {
			this.nextRowSegmentHashes();
		} else {
			for(int j = 0; j < pattern.getHeight(); j++) {
				PixelHash hash = sourceSegmentHashes.get(j);
				Pixel subPixel = source.getPixel(current_x, current_y);
				Pixel addPixel = source.getPixel(current_x + source.getWidth(), current_y);
				hash.subPixel(subPixel);
				hash.addPixel(addPixel);
				sourceSegmentHashes.set(j, hash);
			}
			current_x += 1;
			return;
		}
	}
	
	//Mutate the sourceSegmentHashes by moving to the next line
	void nextRowSegmentHashes() {
		if(current_y >= y_threshold) {
			throw new RuntimeException("Can't get next segment hashes from source");
		} else {
			current_x = 0;
			current_y += 1;
			sourceSegmentHashes = 
					source.rowSegmentHashes(current_y, current_x, 
							pattern.getWidth(), pattern.getHeight());
		}
		return;
	}
	
	//Checks if the hash of the pattern image at row pattern_y is equal to
	//the hash of the source image at row source_y, starting at index 
	//source_x to source_x+pattern.getWidth()
	boolean matchRowHash(int pattern_y, int source_y, int source_x) {
		
		PixelHash patternRowHash = pattern.rowHash(pattern_y);
		PixelHash sourceRowHash = source.rowSegmentHash(source_y, source_x, pattern.getWidth());
		
//		System.out.println("matchRowHash(" + pattern_y + ", " + source_y + ", " + source_x
//				+ ") = " + patternRowHash.compare(sourceRowHash));
		
		return patternRowHash.compare(sourceRowHash) < 1000;
	}
	
	//Checks if the sRGB values of a pattern and source images are the same,
	//not just the hash values
	boolean deepRowComparison(int pattern_y, int source_y, int source_x) {
		int p_index = 0; //Index for row pattern_y
		int s_index = source_x; //Index for row source_y
		int p_wide = pattern.getWidth(); //Width of source image segment
		Pixel p_pixel; //Holds pattern pixel sRGB values
		Pixel s_pixel; //Holds source pixel sRGB values
		
		
//		System.out.println("deepRowComparison(" + String.valueOf(pattern_y) + ", "
//				+ String.valueOf(source_y) + ", " + String.valueOf(source_x) + ")");
		
		//Loop until the end of the pattern image
		while(p_index < p_wide) {
			//Get pattern and source pixel
			p_pixel = pattern.getPixel(p_index, pattern_y);
			s_pixel = source.getPixel(s_index, source_y);
			
			//Check if they are not the same
			if(p_pixel.compare(s_pixel) > 300) {
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
		
//		System.out.println("possibleMatch() @ " + current_x + ", " + current_y);
		
		//Loop while the pattern image can still fit in the source image
		while(current_y < y_threshold) {
			while(current_x < x_threshold) {
				//Check if the row hashes match
				if(this.matchRowHash(0, current_y, current_x)) {
//					System.out.println("Hash Match at (" + current_x + ", " + current_y + ")");
					//Does a real comparison of rows, element by element
					if(this.deepRowComparison(0, current_y, current_x)) {
						//Hands it off to another function to 
						//check if it is really a match
						this.checkMatch(current_x, current_y);
						
						//End method to check match
						return;
					}
				}
				current_x++;
			}
			current_x = 0;
			current_y++;
		}
	}
	
	//Checks if a segment of the source image matches the entire pattern image
	void checkMatch(int match_x, int match_y) {
		int source_x = match_x; //x index to compare at each row
		int source_y = match_y + 1; //y row of the source image
		int pattern_y = 1; //y row of the pattern image
		int high = pattern.getHeight(); //Number of rows to compare
		int wide = pattern.getWidth(); //Wide of pattern image
		
//		System.out.println("checkMatch(" + String.valueOf(match_x) + ", "
//				+ String.valueOf(match_y) + ")");
		
		//Loop until at the last row of pattern image
		while(pattern_y < high) {
			//Exit loop if they are not equal
			if(!this.deepRowComparison(pattern_y, source_y, source_x)) {
				break;
			}
			
			//If at the last row for pattern image, report a match
			if(pattern_y == high - 1) {
				System.out.println(pattern.getName() + " matches " + source.getName()
						+ " at " + pattern.getWidth() + "x" + pattern.getHeight() + "+"
						+ current_x + "+" + current_y);
				
				//Mutate where the search beyond this pattern
				current_x += wide;
				current_y += high;
			}
			
			//Increment rows
			source_y++;
			pattern_y++;
		}
		
		current_x++;
		return;
	}
	
	//Starts the search and looks for possible matches
	void startSearch() {
		//Continue search until the pattern image moves beyond the source image
		while(current_y < y_threshold) {
			this.possibleMatch();
		}
	}
}