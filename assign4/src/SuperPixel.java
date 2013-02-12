abstract class SuperPixel {
	int red;
	int green;
	int blue;

	SuperPixel() {}

	public static Pixel makePixel(int argb) {
		return new Pixel(argb);
	}

	public static PixelHash makePixelHash(int red, int green, int blue) {
		return new PixelHash(red, green, blue);
	}
	
	int getRed() {
		return this.red;
	}

	int getGreen() {
		return this.green;
	}

	int getBlue() {
		return this.blue;
	}
}

class Pixel extends SuperPixel {
	Pixel(int argb) {
		this.red = (argb >> 16) & 0xFF;
		this.green = (argb >> 8) & 0xFF;
		this.blue = argb & 0xFF;
	}

	int compare(Pixel otherPixel) {
		int redDiff = this.getRed() - otherPixel.getRed();
		int greenDiff = this.getGreen() - otherPixel.getGreen();
		int blueDiff = this.getBlue() - otherPixel.getBlue();
		return Math.abs(redDiff) + Math.abs(greenDiff) + Math.abs(blueDiff);
	}

}

class PixelHash extends SuperPixel {
	PixelHash(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	void addRed(int r) {
		this.red += r;
	}

	void addGreen(int g) {
		this.green += g;
	}

	void addBlue(int b) {
		this.blue += b;
	}

	void subRed(int r) {
		this.red -= r;
	}

	void subGreen(int g) {
		this.green -= g;
	}

	void subBlue(int b) {
		this.blue -= b;
	}

	void addPixel(Pixel p) {
		this.addRed(p.getRed());
		this.addGreen(p.getGreen());
		this.addBlue(p.getBlue());
		return;
	}

	void subPixel(Pixel p) {
		this.subRed(p.getRed());
		this.subGreen(p.getGreen());
		this.subBlue(p.getBlue());
		return;
	}

	int compare(PixelHash otherPixel) {
		int redDiff = this.getRed() - otherPixel.getRed();
		int greenDiff = this.getGreen() - otherPixel.getGreen();
		int blueDiff = this.getBlue() - otherPixel.getBlue();
		return Math.abs(redDiff) + Math.abs(greenDiff) + Math.abs(blueDiff);
	}
}
