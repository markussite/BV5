// BV Ue4 SS2020 Vorgabe
//
// Copyright (C) 2019 by Klaus Jung
// All rights reserved.
// Date: 2019-05-12

package bv_ws20;

import bv_ws20.ImageAnalysisAppController.StatsProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Arrays;

public class Histogram {

	private static final int grayLevels = 256;
	RasterImage image;
    private GraphicsContext gc;
    private int maxHeight;
    private int[] histogram = new int[grayLevels];
	private double brightness = 0;
	private double contrast = 1;
	private int min = 0;
	private int max = 0;
	public Histogram(GraphicsContext gc, int maxHeight) {
		this.gc = gc;
		this.maxHeight = maxHeight;
	}
	
	public void update(RasterImage image, Point2D ellipseCenter, Dimension2D ellipseSize, int selectionMax, ObservableList<StatsProperty> statsData) {
		// TODO: calculate histogram[] out of the gray values of the image for pixels inside the given ellipse
		// Remark: Please ignore selectionMax and statsData in Exercise 4. It will be used in Exercise 5.
		this.image = image;

		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = 0;
		}
		int[] argb = image.argb;

		double horizR = ellipseSize.getWidth();
		double verticR = ellipseSize.getHeight();

		for(int x = 0; x < image.width; x++) {
			for (int y = 0; y < image.height; y++) {
				double radius = (Math.pow(x-ellipseCenter.getX(), 2)/Math.pow((horizR/2),2)+
						Math.pow(y-ellipseCenter.getY(),2)/(Math.pow(verticR/2,2))); //ellipsengleichung
				if(radius <= 1){
					int pos = y * image.width + x;
					int argbN = argb[pos];
					int grayOut = (argbN >> 16) & 0xff;
					histogram[grayOut] += 1;
				}
			}
		}
		draw();
		double mean = 0;
		double selectPixels = 0;
		if (selectionMax == -1) {
			selectionMax = 255;
			selectPixels = image.width*image.height;
		}
		else{
			for (int i = 0; i <= selectionMax; i++) {
				selectPixels += histogram[i];
			}
		}

		for(StatsProperty property : statsData){
			switch(property) {
				case Level:
					int x = 0;
					for(int i = 0; i < selectionMax+1; i++){
						x+= histogram[i];
					}
					x = x/(image.width*image.height);
					property.setValueInPercent(x);
					break;
				case Minimum:
					min = 0;
					for(int i = 0; i <= selectionMax; i++){
						if(histogram[i] > 0){
							min = i;
							break;
						}
					}
					property.setValue(min);
					break;
				case Maximum:
					max = 0;
					for(int i = selectionMax; i >= 0; i--){
						if(histogram[i] > 0){
							max = i;
							break;
						}
					}
					property.setValue(max);
					break;
				case Mean:
					mean = 0;
					for(int i = min; i <= max; i++){
						if (histogram[i] != 0) {
							mean += i*histogram[i];
						}
					}
					mean = mean/(selectPixels);
					property.setValue(mean);
					break;
				case Median:
					int mid = 0;
					int[] newHistogram = new int[image.argb.length];
					for(int i = 0; i < image.argb.length; i++)
					{
						newHistogram[i] = image.argb[i] & 0xff;
					}
					Arrays.sort(newHistogram);
					mid = newHistogram.length;
					if(mid % 2 == 0){
						System.out.println(mid);
						property.setValue(newHistogram[(mid -1) /2]);
					}
					else {
						System.out.println(mid);
						property.setValue((newHistogram[(mid / 2) - 1] + newHistogram[mid / 2]) / 2);
					}
					break;
				case Variance:
					int variance = 0;
					for(int i = min; i <= max; i++){
						int y = histogram[i];
						variance += Math.pow((i - mean),2)*y;
					}
					property.setValue(variance/((selectPixels)));
					break;
				case Entropy:
					double entropy = 0;
					for(int i = min; i <= max; i++){
						if(histogram[i] != 0){
							entropy += (-1.0 * histogram[i]/(image.width*image.height)) * (Math.log10(1.0*histogram[i]/(image.width*image.height)))/Math.log10(2);
						}
					}
					property.setValue(entropy);
					break;
			}
		}
	}
	    
	public void draw() {
		gc.clearRect(0, 0, grayLevels, maxHeight);
		gc.setLineWidth(1);

		// TODO: draw histogram[] into the gc graphic context



		gc.setStroke(Color.GREEN);
		double shift = 0.5;
		int[] newHistogram = histogram.clone();
		Arrays.sort(newHistogram);
		double s = (double) maxHeight / newHistogram[newHistogram.length - 1];
		for (int i = 0; i < histogram.length; i++) {
			gc.strokeLine(i + shift, maxHeight, i + shift, maxHeight - (s * histogram[i]));
		}

	}
	void contrastM(){
		double start = (image.height*image.width) * 0.01;
		double end = (image.height*image.width) - start;
		int pixelCountMin = 0;
		int pixelCountMax = (image.height*image.width);
		int minimum = 0, maximum = 0;


		for (int i = min; i <= max; i++) {
			pixelCountMin += histogram[i];

			if(pixelCountMin >= start) {
				maximum = i;
				break;
			}
		}

		for (int i = max; i >= min; i--) {
			pixelCountMax -= histogram[i];

			if(pixelCountMax <= end) {
				minimum= i;
				break;
			}
		}
		brightness = 128 - ((minimum+maximum)/2.0);
		contrast = (255.0/(minimum-maximum));
	}

	public double getBrightness() {
		return brightness;
	}

	public double getContrast() {
		return contrast;
	}
}
