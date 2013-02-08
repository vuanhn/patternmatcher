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
	public int rowSectionHash(int y, int x, int wide) {
		//Checks if y is in height and the x index won't go off the side
		//of an image
		if(y >= this.getHeight() || x + wide >= this.getWidth()) {
			throw new ArrayIndexOutOfBoundsException("Out of bound");
		}
		
		//Adds sRGB values in row y from index x to x+wide
		int hash = 0;
		for(int i = x; i < x + wide; i++) {
			hash += this.getPixel(i, y);
		}
		
		return hash;
	}
	
	//Returns an array where rows y to y+high are represented as array elements
	//The hash for each row is represented for their x to x+wide pixels
	public int[] rowSectionHashes(int y, int x, int wide, int high) {
		//Initialize array to hold hashes
		int[] hashes = new int[high];
		
		//Adds sRGB from index x to x+wide for each row y to y+high
		for(int j = high; j < y + high; j++) {
			hashes[j] = this.rowSectionHash(j, x, wide);
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
		//Check if row is in image
		if(y >= height) {
			throw new ArrayIndexOutOfBoundsException("Out of bound");
		}
		
		// Adds all sRGB values into hash
		int hash = 0;
		for(int i = 0; i < this.getWidth(); i++) {
			hash += this.getPixel(i, y);
		}
		
		return hash;
	}
	
	//Returns an array of hash values for each row
	public int[] rowHashes() {
		//Make array to hold hash values of all rows
		int[] hashes = new int[height];
		
		//Get row hash for every row 
		for(int j = 0; j < this.getHeight(); j++) {
			hashes[j] = this.rowHash(j);
		}
		
		return hashes;
	}
	
}

//Source image to be searched
class SourceImage extends MyImage {
	
	//SourceImage creator
	SourceImage(BufferedImage image) {
		super(image);
	}
	
	//Get hash value for the row number at an index for a given length
	public int rowSectionHash(int row, int index, int length) {
		//Check if row is in image
		if(row >= height) {
			throw new ArrayIndexOutOfBoundsException("Out of bound");
		}
		
		//Adds sRGB values in row sections into hash
		int hash = 0;
		for(int i = index; i < index + length; i++) {
			hash += this.getPixel(i, row);
		}
		
		return hash;
	}
	
	//Returns an array of hash values for each row section
	public int[] rowSectionHashes(int row, int index, int length, int p_height) {
		//Initialize array to hold hashes
		int[] hashes = new int[p_height];
		
		//Loop finds row section of at a specified start row and index
		for(int j = row; j < p_height + row; j++) {
			hashes[j] = this.rowSectionHash(j, index, length);
		}
		return hashes;
	}
	
}

//Checks if a pattern image is in a source image
class searchImages {
	PatternImage pattern; //PatternImage
	SourceImage source; //SourceImage
	int current_x; //x location in source
	int current_y; //y location in source
	int y_threshold; //
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
	
	//Checks if the hashes in pattern image match those in the source hashes
	//at the given row and index
	boolean matchRowHash(int patternRow, int sourceRow, int sourceIndex) {
		int patternRowHash = pattern.rowHash(patternRow);
		int sourceRowHash = source.rowSectionHash(sourceRow, sourceIndex, pattern.getWidth());
		return patternRowHash == sourceRowHash;
	}
	
	//Checks if the sRGB values of a pattern and source images are the same,
	//not just the hash values
	boolean deepRowComparison(int patternRow, int sourceRow, int sourceIndex) {
		int p_row = patternRow; //Row in pattern image
		int p_index = 0; //Index on row
		int s_row = sourceRow; //Row in pattern image
		int s_index = sourceIndex; //Index on row
		int s_width = pattern.getWidth();
		int p_pixel;
		int s_pixel;
		
		while(p_index < s_width) {
			p_pixel = pattern.getPixel(p_index, p_row);
			s_pixel = source.getPixel(s_index, s_row);
			if(p_pixel != s_pixel) {
				return false;
			} 
		}
		return true;
	}
	
	void possibleMatch() {
		while(current_y < y_threshold) {
			while(current_x < x_threshold) {
				if(this.matchRowHash(0, current_y, current_x)) {
					if(this.deepRowComparison(0, current_y, current_x)) {
						this.checkMatch(current_x, current_y);
					}
				}
				current_x++;
			}
			current_y++;
		}
	}
	
	void checkMatch(int match_x, int match_y) {
		int source_x = match_x;
		int source_y = match_y + 1;
		int pattern_y = 1;
		while(source_y < source_y + pattern.getHeight()) {
			if(!this.matchRowHash(pattern_y, source_y, source_x)) {
				break;
			}
			if(!this.deepRowComparison(pattern_y, source_y, source_x)) {
				break;
			}
			if(pattern_y == pattern.getHeight()) {
				System.out.println("MATCH TEXT");
			}
		}
		current_x += pattern.getWidth();
		current_y += pattern.getHeight();
		this.possibleMatch();
	}
}