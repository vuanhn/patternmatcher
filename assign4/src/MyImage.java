import java.awt.image.*;

//Abstract class for pattern and source images
public abstract class MyImage {
	int width; //Width of image
	int height; //Height of image
	int[] colorArray; //Array representation of image
	
	//Create a MyImage
	MyImage(BufferedImage image) {
		if(image == null) { 
			throw new IllegalArgumentException("Given null image");
		}
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.colorArray = image.getRGB(0, 0, width, height, null, 0, width);
	}
	
	//Create Pattern Image
	public static MyImage Pattern(BufferedImage image) {
		return new PatternImage(image);
	}
	
	//Create Source Image
	public static MyImage Source(BufferedImage image) {
		return new SourceImage(image);
	}	

	//Get image length
	public int getWidth() {
		return this.width;
	}
	
	//Get image height
	public int getHeight() {
		return this.height;
	}
	
	//Get single integer pixel sRGB value
	public int getPixel(int x, int y) {
		//Check if pixel is in image
		if(x >= this.getWidth() || y >= this.getHeight()) {
			throw new ArrayIndexOutOfBoundsException("Out of bound");
		}
		//Get index for colorArray
		int index = y * this.getHeight() + x;
		return colorArray[index];
	}
	
	//Returns hash value from row y from index x to x+wide
	public int rowSegmentHash(int y, int x, int wide) {
		//Checks if y is in height and the x index won't go off the side
		//of an image
		if(y >= this.getHeight() || x + wide >= this.getWidth()) {
			throw new ArrayIndexOutOfBoundsException("Out of bound");
		}
		
		//Adds sRGB values in row y from index x to x+wide
		int hash = 0;
		for(int i = x; i < x+wide; i++) {
			hash += this.getPixel(i, y);
		}
		
		return hash;
	}
	
	//Returns an array where rows y to y+high are represented as array elements
	//The hash for each row is represented for their x to x+wide pixels
	public int[] rowSegmentHashes(int y, int x, int wide, int high) {
		//Initialize array to hold hashes
		int[] hashes = new int[high];
		
		//Adds sRGB from index x to x+wide for each row y to y+high
		for(int j = high; j < y+high; j++) {
			hashes[j] = this.rowSegmentHash(j, x, wide);
		}
		
		return hashes;
	}
	
}

//Class of all pattern images
class PatternImage extends MyImage {

	//PatternImage Creator
	PatternImage(BufferedImage image) {
		super(image);
	}
	
	//Calculates hash value of row y
	public int rowHash(int y) {
		//Get hash of row segment from 0 to width
		return this.rowSegmentHash(y, 0, this.getWidth());
	}
	
	//Returns an array of hash values for each row
	public int[] rowHashes() {
		//Get hashes of all row segments from 0 to width
		return this.rowSegmentHashes(0, 0, this.getWidth(), this.getHeight());
	}
	
}

//Source image to be searched
class SourceImage extends MyImage {
	
	//SourceImage creator
	SourceImage(BufferedImage image) {
		super(image);
	}
	
}

//Checks if a pattern image is in a source image
class searchImages {
	PatternImage pattern; //PatternImage
	SourceImage source; //SourceImage
	int current_x; //x location in source
	int current_y; //y location in source
	int y_threshold; 
	int x_threshold;
	
	//Create a searchImages
	searchImages(PatternImage pattern, SourceImage source) {
		this.pattern = pattern;
		this.source = source;
		this.current_x = 0;
		this.current_y = 0;
		this.y_threshold = source.getHeight() - pattern.getHeight();
		this.x_threshold = source.getWidth() - pattern.getWidth();
	}
	
	//Checks if the hash of the pattern image at row pattern_y is equal to
	//the hash of the source image at row source_y, starting at index 
	//source_x to source_x+pattern.getWidth()
	boolean matchRowHash(int pattern_y, int source_y, int source_x) {
		int patternRowHash = pattern.rowHash(pattern_y);
		int sourceRowHash = source.rowSegmentHash(source_y, source_x, pattern.getWidth());
		return patternRowHash == sourceRowHash;
	}
	
	//Checks if the sRGB values of a pattern and source images are the same,
	//not just the hash values
	boolean deepRowComparison(int pattern_y, int source_y, int source_x) {
		int p_index = 0; //Index for row pattern_y
		int s_index = source_x; //Index for row source_y
		int p_wide = pattern.getWidth(); //Width of source image segment
		int p_pixel; //Holds pattern pixel sRGB values
		int s_pixel; //Holds source pixel sRGB values
		
		//Loop until the end of the pattern image
		while(p_index < p_wide) {
			//Get pattern and source pixel
			p_pixel = pattern.getPixel(p_index, pattern_y);
			s_pixel = source.getPixel(s_index, source_y);
			
			//Check if they are not the same
			if(p_pixel != s_pixel) {
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
		//Loop while the pattern image can still fit in the source image
		while(current_y < y_threshold) {
			while(current_x < x_threshold) {
				//Check if the row hashes match
				if(this.matchRowHash(0, current_y, current_x)) {
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
		
		//Loop until at the last row of pattern image
		while(pattern_y < high) {
			//Exit loop if they are not equal
			if(!this.deepRowComparison(pattern_y, source_y, source_x)) {
				break;
			}
			
			//If at the last row for pattern image, report a match
			if(pattern_y == high - 1) {
				System.out.println("MATCH TEXT");
				
				//Mutate where the search beyond this pattern
				current_x += wide;
				current_y += high;
			}
			
			//Increment rows
			source_y++;
			pattern_y++;
		}
		
		//Look for more possible matches
		this.possibleMatch();
		return;
	}
}