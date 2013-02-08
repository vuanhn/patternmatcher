import java.awt.image.*;


//Interface for pattern and source images
public abstract class MyImage {
	
	//Create Pattern Image
	public static MyImage Pattern(BufferedImage image) {
		return new PatternImage(image);
	}
	
	//Create Source Image
	public static MyImage Source(BufferedImage image) {
		return new SourceImage(image);
	}	
	
	//Get image length
	public abstract int getWidth();  
	//Get image height
	public abstract int getHeight();
	//Get single integer pixel sRGB value
	public abstract int getPixel(int x, int y);
	
}

class PatternImage extends MyImage {
	int width; //Width of image
	int height; //Height of image
	int[] colorArray; //Array representation of image
	
	PatternImage(BufferedImage image) {
		if(image == null) { 
			throw new IllegalArgumentException("Given null image");
		}
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.colorArray = image.getRGB(0, 0, width, height, null, 0, width);
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
		int index = y * height + x;
		return colorArray[index];
	}
	
	//Calculates hash value of one row
	public int rowHash(int row) {
		if(row >= height) {
			throw new ArrayIndexOutOfBoundsException("Out of bound");
		}
		
		// Adds all sRGB values into hash
		int hash = 0;
		for(int i = 0; i < this.getWidth(); i++) {
			hash += this.getPixel(i, row);
		}
		
		return hash;
	}
	
	//Returns an array of hash values
	public int[] rowHashes() {
		int[] hashes = new int[height];
		for(int j = 0; j < this.getHeight(); j++) {
			hashes[j] = this.rowHash(j);
		}
		return hashes;
	}
	
}

class SourceImage extends MyImage {
	int width; //Width of image
	int height; //Height of image
	int[] colorArray; //Array representation of image
	
	SourceImage(BufferedImage image) {
		if(image == null) { 
			throw new IllegalArgumentException("Given null image");
		}
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.colorArray = image.getRGB(0, 0, width, height, null, 0, width);
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
		int index = y * height + x;
		return colorArray[index];
	}
	
	public int rowSectionHash(int row, int x, int length) {
		if(row >= height) {
			throw new ArrayIndexOutOfBoundsException("Out of bound");
		}
		
		int hash = 0;
		for(int i = x; i < x + length; i++) {
			hash += this.getPixel(i, row);
		}
		
		return hash;
	}
	
	public int[] rowSectionHashes(int x, int length) {
		int[] hashes = new int[height];
		for(int j = 0; j < this.getHeight(); j++) {
			hashes[j] = this.rowSectionHash(j, x, length);
		}
		return hashes;
	}
	
}

class searchImages {
	PatternImage pattern;
	SourceImage source;
	int current_x; //x location in source
	int current_y; //y location in source
	int y_threshold = source.getHeight() - pattern.getHeight();
	int x_threshold = source.getWidth() - pattern.getWidth();
	
	searchImages(PatternImage pattern, SourceImage source) {
		this.pattern = pattern;
		this.source = source;
		this.current_x = 0;
		this.current_y = 0;
	}
	
	boolean matchRowHash(int patternRow, int sourceRow, int sourceIndex) {
		int patternRowHash = pattern.rowHash(patternRow);
		int sourceRowHash = source.rowSectionHash(sourceRow, sourceIndex, pattern.getWidth());
		return patternRowHash == sourceRowHash;
	}
	
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