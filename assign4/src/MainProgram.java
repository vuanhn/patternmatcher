import java.io.File;
import java.io.IOException;

public class MainProgram {
	public static void main(String[] args) throws IOException{
		if (args.length != 4){
			System.exit(0);
		} 
		
//		int argOffset = 0;
		String patternFilename = null;
		String sourceFilename = null;
		
//		while (argOffset < args.length) {
//			if (args[argOffset] == "-p") {
//				patternFilename = args[argOffset + 1];
//			} else if (args[argOffset] == "-s") {
//				sourceFilename = args[argOffset + 1];
//			}
//			argOffset++;
//		}
		
		patternFilename = args[1];
		sourceFilename = args[3];
		
		File patternFile = new File(patternFilename);
		File sourceFile = new File(sourceFilename);
				
		PatternImage pattern = MyImage.Pattern(patternFile);
		SourceImage source = MyImage.Source(sourceFile);
		
		SearchImages newSearch = SearchImages.search(pattern, source);
		
		newSearch.possibleMatch();
		
		System.out.println("End");
		
	}
}
