import java.io.File;
import java.io.IOException;

public class MainProgram {
	public static void main(String[] args) throws IOException{
//		if (args.length != 4) {
//			System.out.println("Incorrect number of arguments");
//			System.exit(1);
//		} 
//
//		int argOffset = 0;
//		String patternFilename = null;
//		String sourceFilename = null;
//		
//		while (argOffset < args.length) {
//			if (args[argOffset].equals("-p")) {
//				patternFilename = args[argOffset + 1];
//			} else if (args[argOffset].equals("-s")) {
//				sourceFilename = args[argOffset + 1];
//			}
//			argOffset++;
//		}
//
//		File patternFile = new File(patternFilename);
//		File sourceFile = new File(sourceFilename);
//
//		if (!(isValidFile(patternFile) && isValidFile(sourceFile))) {
//			System.out.println("Invalid file types");
//			System.exit(2);
//		}
//
//		SearchImages newSearch = SearchImages.search(patternFile, sourceFile);
//				
//		newSearch.startSearch();
		
		try {
			testImages();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("End");

	}


	public static void testImages() throws Exception {
		File black = new File("/course/cs4500wc/Assignments/A4/Patterns/black.jpg");
		File bb0001 = new File("/course/cs4500wc/Assignments/A4/Sources/bb0001.jpg");
		File hh0021 =  new File("/course/cs4500wc/Assignments/A4/Sources/hh0021.jpg");
		File cliff = new File("/course/cs4500wc/Assignments/A4/Patterns/cliff.png");
		File an0300 = new File("/course/cs4500wc/Assignments/A4/Sources/an0300.jpg");
		File flower = new File("/course/cs4500wc/Assignments/A4/Patterns/flower.gif");
		File ac1000 = new File("/course/cs4500wc/Assignments/A4/Sources/ac1000.jpg");
		File nature = new File("/course/cs4500wc/Assignments/A4/Patterns/nature.jpg");
		File rock = new File("/course/cs4500wc/Assignments/A4/Patterns/rock.jpg");
		File ar0800 = new File("/course/cs4500wc/Assignments/A4/Sources/ar0800.jpg");
		File tree = new File("/course/cs4500wc/Assignments/A4/Patterns/tree.jpg");
		File aa0010 = new File("/course/cs4500wc/Assignments/A4/Sources/aa0010.jpg");
		File ranch = new File("/course/cs4500wc/Assignments/A4/Patterns/ranch.jpg");
		File ai0059 = new File("/course/cs4500wc/Assignments/A4/Sources/ai0059.gif");
		File morning = new File("/course/cs4500wc/Assignments/A4/Patterns/morning.png");
		File ak432 = new File("/course/cs4500wc/Assignments/A4/Sources/ak432.png");

		SearchImages[] searchArray;

		searchArray = new SearchImages[9];

		searchArray[0] = new SearchImages(black, bb0001);
		searchArray[1] = new SearchImages(black, hh0021);
		searchArray[2] = new SearchImages(cliff, an0300);
		searchArray[3] = new SearchImages(flower, ac1000);
		searchArray[4] = new SearchImages(nature, hh0021);
		searchArray[5] = new SearchImages(rock, ar0800);
		searchArray[6] = new SearchImages(tree, aa0010);
		searchArray[7] = new SearchImages(ranch, ai0059);
		searchArray[8] = new SearchImages(morning, ak432);

		for (int i = 0; i < searchArray.length; i++) {
			searchArray[i].startSearch();
		}

	}
	
	private boolean isValidFile(File file){
		String fileName = file.getName();
		
		return fileName.matches("*.jpg") || fileName.matches("*.png") || fileName.matches("*.gif");
	}
}
