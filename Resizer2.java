import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.util.zip.*;
import java.lang.Math.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Resizer2
{
	BufferedImage original_image;
	BufferedImage image;
	JMenuItem     apply_item;
	JDialog       quant_dialog;
	JTextField    quant_value;
	JDialog       shift_dialog;
	JTextField    shift_value;
	JDialog       correction_dialog;
	JTextField    correction_value;
	
	
	ImageCanvas   image_canvas;
	
	int           xdim, ydim;
	int           x_power_of_two, y_power_of_two;
	String        filename;
	
	int [] original_pixel;
	int [] new_pixel;
	int [] alpha;
	int [] blue;
	int [] green;
	int [] red;
	int [] blue_green;
	int [] red_green;
	int [] red_blue;

	int [] blue_error;
	int [] green_error;
	int [] red_error;
	
	int pixel_quant = 0;
	int pixel_shift = 0;
	int correction  = 0;
	int min_set_id  = 0;
	
	
	int    [] set_sum, channel_sum;
	String [] set_string;
	String [] channel_string;
	
	
	int [] channel_init;
	int [] channel_min;
	int [] channel_delta_min;
	int [] channel_length;
	int [] channel_compressed_length;
	
	ArrayList channel_list, quantized_channel_list, shifted_channel_list, error_list;
	
	ArrayList delta_list, table_list;

	
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java Resizer2 <filename>");
			System.exit(0);
		}
		String prefix       = new String("C:/Users/Brian Crowley/Desktop/");
		String filename     = new String(args[0]);
	
		Resizer2 analyzer = new Resizer2(prefix + filename);
	}

	public Resizer2(String _filename)
	{
		filename = _filename;
		try
		{
			File file = new File(filename);
			original_image = ImageIO.read(file);
			int raster_type = original_image.getType();
			ColorModel color_model = original_image.getColorModel();
			int number_of_channels = color_model.getNumColorComponents();
			int number_of_bits = color_model.getPixelSize();
			xdim = original_image.getWidth();
			ydim = original_image.getHeight();
		
			System.out.println("Xdim = " + xdim + ", ydim = " + ydim);
			
			
			int pixel_length = xdim * ydim * 8;
		    
			
		    original_pixel = new int[xdim * ydim];
		    new_pixel      = new int[xdim * ydim];
		    alpha          = new int[xdim * ydim];
			blue           = new int[xdim * ydim];
		    green          = new int[xdim * ydim];
		    red            = new int[xdim * ydim];
		    blue_green     = new int[xdim * ydim];
			red_green      = new int[xdim * ydim];
			red_blue       = new int[xdim * ydim];

		    
		    channel_list = new ArrayList();
		   
		    shifted_channel_list = new ArrayList();
		    
		    quantized_channel_list = new ArrayList();
		    
		    delta_list = new ArrayList();
		    
		    table_list = new ArrayList();
		  
		    
		    blue_error  = new int[xdim * ydim];
		    green_error = new int[xdim * ydim];
		    red_error   = new int[xdim * ydim];
		    error_list  = new ArrayList();
		    error_list.add(blue_error);
		    error_list.add(green_error);
		    error_list.add(red_error);
		    
		    
		    channel_string    = new String[6];
		    channel_string[0] = new String("blue");
		    channel_string[1] = new String("green");
		    channel_string[2] = new String("red");
		    channel_string[3] = new String("blue-green");
		    channel_string[4] = new String("red-green");
		    channel_string[5] = new String("red-blue");
		    
		    // The delta sum for each set of channels that can produce an image.
		    set_sum       = new int[10];
		    set_string    = new String[10]; 
		    set_string[0] = new String("blue, green, and red.");
		    set_string[1] = new String("blue, red, and red-green.");
		    set_string[2] = new String("blue, red, and blue-green.");
		    set_string[3] = new String("blue, blue-green, and red-green.");
		    set_string[4] = new String("blue, blue-green, and red-blue.");
		    set_string[5] = new String("green, red, and blue-green.");
		    set_string[6] = new String("red, blue-green, and red-green.");
		    set_string[7] = new String("green, blue-green, and red-green.");
		    set_string[8] = new String("green, red-green, and red-blue.");
		    set_string[9] = new String("red, red-green, red-blue.");
		    
		    channel_init      = new int[6];
		    channel_min       = new int[6];
		    channel_delta_min = new int[6];
		    channel_sum       = new int[6];
		    channel_length    = new int[6];
            channel_compressed_length = new int[6];
			System.out.println();
			
			if(raster_type == BufferedImage.TYPE_3BYTE_BGR)
			{
				int[]          pixel = new int[xdim * ydim];
				PixelGrabber pixel_grabber = new PixelGrabber(original_image, 0, 0, xdim, ydim, original_pixel, 0, xdim);
		        try
		        {
		            pixel_grabber.grabPixels();
		        }
		        catch(InterruptedException e)
		        {
		            System.err.println(e.toString());
		        }
		        if((pixel_grabber.getStatus() & ImageObserver.ABORT) != 0)
		        {
		            System.err.println("Error grabbing pixels.");
		            System.exit(1);
		        }
		        
		        for(int i = 0; i < xdim * ydim; i++)
				{
			        alpha[i] = (original_pixel[i] >> 24) & 0xff;
				    blue[i]  = (original_pixel[i] >> 16) & 0xff;
				    green[i] = (original_pixel[i] >> 8) & 0xff; 
		            red[i]   = original_pixel[i] & 0xff; 
				}
		        
		        channel_list.add(blue);
		        channel_list.add(green);
		        channel_list.add(red);
		        
		        
		       
			    image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			    // This is not the usual order, careful.
			    for(int i = 0; i < xdim; i++)
				{
				    for(int j = 0; j < ydim; j++)
				    	image.setRGB(i, j, original_pixel[j * xdim + i]);	
				} 
			    
			    JFrame frame = new JFrame("Error Analyzer");
				WindowAdapter window_handler = new WindowAdapter()
			    {
			        public void windowClosing(WindowEvent event)
			        {
			            System.exit(0);
			        }
			    };
			    frame.addWindowListener(window_handler);
			    
				image_canvas = new ImageCanvas();
				image_canvas.setSize(xdim, ydim);
				frame.getContentPane().add(image_canvas, BorderLayout.CENTER);
				
				JMenuBar menu_bar = new JMenuBar();

				JMenu     file_menu  = new JMenu("File");
				
				apply_item                 = new JMenuItem("Apply");
				ApplyHandler apply_handler = new ApplyHandler();
				apply_item.addActionListener(apply_handler);
				file_menu.add(apply_item);
				
				JMenuItem reload_item        = new JMenuItem("Reload");
				ReloadHandler reload_handler = new ReloadHandler();
				reload_item.addActionListener(reload_handler);
				file_menu.add(reload_item);
				
				JMenu settings_menu = new JMenu("Settings");
			
				JMenuItem quant_item = new JMenuItem("Pixel Quantization");
				ActionListener quant_handler = new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						Point location_point = frame.getLocation();
						int x = (int) location_point.getX();
						int y = (int) location_point.getY();

						x += xdim;

						quant_dialog.setLocation(x, y);
						quant_dialog.pack();
						quant_dialog.setVisible(true);
					}
				};
				quant_item.addActionListener(quant_handler);
				quant_dialog = new JDialog(frame, "Pixel Quantization");
				JPanel quant_panel = new JPanel(new BorderLayout());
				JSlider quant_slider = new JSlider();
				quant_slider.setMinimum(0);
				quant_slider.setMaximum(10);
				quant_slider.setValue(pixel_quant);
				quant_value = new JTextField(3);
				quant_value.setText(" " + pixel_quant + " ");
				ChangeListener quant_slider_handler = new ChangeListener()
				{
					public void stateChanged(ChangeEvent e)
					{
						JSlider slider = (JSlider) e.getSource();
						pixel_quant = slider.getValue();
						quant_value.setText(" " + pixel_quant + " ");
						if(slider.getValueIsAdjusting() == false)
						{
							apply_item.doClick();
						}
					}
				};
				quant_slider.addChangeListener(quant_slider_handler);
				quant_panel.add(quant_slider, BorderLayout.CENTER);
				quant_panel.add(quant_value, BorderLayout.EAST);
				quant_dialog.add(quant_panel);
				settings_menu.add(quant_item);
				
				JMenuItem shift_item = new JMenuItem("Pixel Shift");
				ActionListener shift_handler = new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						Point location_point = frame.getLocation();
						int x = (int) location_point.getX();
						int y = (int) location_point.getY();

						

						shift_dialog.setLocation(x, y);
						shift_dialog.pack();
						shift_dialog.setVisible(true);
					}
				};
				shift_item.addActionListener(shift_handler);
				shift_dialog = new JDialog(frame, "Pixel Shift");
				JPanel shift_panel = new JPanel(new BorderLayout());
				JSlider shift_slider = new JSlider();
				shift_slider.setMinimum(0);
				shift_slider.setMaximum(7);
				shift_slider.setValue(correction);
				shift_value = new JTextField(3);
				shift_value.setText(" " + pixel_shift + " ");
				ChangeListener shift_slider_handler = new ChangeListener()
				{
					public void stateChanged(ChangeEvent e)
					{
						JSlider slider = (JSlider) e.getSource();
						pixel_shift = slider.getValue();
						shift_value.setText(" " + pixel_shift + " ");
						if(slider.getValueIsAdjusting() == false)
						{
							apply_item.doClick();
						}
					}
				};
				shift_slider.addChangeListener(shift_slider_handler);
				shift_panel.add(shift_slider, BorderLayout.CENTER);
				shift_panel.add(shift_value, BorderLayout.EAST);
				shift_dialog.add(shift_panel);
				
				
				settings_menu.add(shift_item);
				
				JMenuItem correction_item = new JMenuItem("Error Correction");
				ActionListener correction_handler = new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						Point location_point = frame.getLocation();
						int x = (int) location_point.getX();
						int y = (int) location_point.getY();

						x += xdim;

						correction_dialog.setLocation(x, y);
						correction_dialog.pack();
						correction_dialog.setVisible(true);
					}
				};
				correction_item.addActionListener(correction_handler);
				correction_dialog = new JDialog(frame, "Error Correction");
				JPanel correction_panel = new JPanel(new BorderLayout());
				JSlider correction_slider = new JSlider();
				correction_slider.setMinimum(0);
				correction_slider.setMaximum(10);
				correction_slider.setValue(correction);
				correction_value = new JTextField(3);
				correction_value.setText(" " + correction + " ");
				ChangeListener correction_slider_handler = new ChangeListener()
				{
					public void stateChanged(ChangeEvent e)
					{
						JSlider slider = (JSlider) e.getSource();
						correction = slider.getValue();
						correction_value.setText(" " + correction + " ");
						if(slider.getValueIsAdjusting() == false)
						{
							apply_item.doClick();
						}
					}
				};
				correction_slider.addChangeListener(correction_slider_handler);
				correction_panel.add(correction_slider, BorderLayout.CENTER);
				correction_panel.add(correction_value, BorderLayout.EAST);
				correction_dialog.add(correction_panel);
				
				settings_menu.add(correction_item);
				
				
				menu_bar.add(file_menu);
				menu_bar.add(settings_menu);
				
				frame.setJMenuBar(menu_bar);
				
				frame.pack();
				frame.setLocation(400, 200);
				frame.setVisible(true);
			} 
		} 
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	class ImageCanvas extends Canvas
    {
        public synchronized void paint(Graphics g)
        {
            g.drawImage(image, 0, 0, this);
        }
    }
	
	class ApplyHandler implements ActionListener
	{
		// The minimum set changes depending on how the 
		// image is quantized, so it has to be reevaluated
		// with every change in the parameters.
		public void actionPerformed(ActionEvent event)
		{
			shifted_channel_list.clear();
			
			for(int i = 0; i < 3; i++)
			{
				int [] channel         = (int [])channel_list.get(i);
				int [] shifted_channel = new int [xdim * ydim];
				for(int j = 0; j < channel.length; j++)
					shifted_channel[j] = channel[j] >> pixel_shift;	
				shifted_channel_list.add(shifted_channel);
			}
			
			quantized_channel_list.clear();
	       
			int new_xdim = xdim;
		    int new_ydim = ydim;
		    if(pixel_quant != 0)
		    {
		    	double factor = pixel_quant;
		        factor       /= 10;
		        new_xdim = xdim - (int)(factor * (xdim / 2 - 2));
		        new_ydim = ydim - (int)(factor * (ydim / 2 - 2));
		    }
		    
		    for(int i = 0; i < 3; i++)
		    {
		    	int [] shifted_channel = (int [])shifted_channel_list.get(i);
		    	if(pixel_quant == 0)
		    		quantized_channel_list.add(shifted_channel);
		    	else
		    	{
		    		int [] resized_channel = ResizeMapper.resize(shifted_channel, xdim, new_xdim, new_ydim);
		    		quantized_channel_list.add(resized_channel);
		    	}
		    }
		    
		    int [] quantized_blue  = (int [])quantized_channel_list.get(0);
		    int [] quantized_green = (int [])quantized_channel_list.get(1);
		    int [] quantized_red   = (int [])quantized_channel_list.get(2);
		    
		    int [] quantized_blue_green = DeltaMapper.getDifference(quantized_blue, quantized_green);
		    int [] quantized_red_green  = DeltaMapper.getDifference(quantized_red, quantized_green);
		    int [] quantized_red_blue   = DeltaMapper.getDifference(quantized_red, quantized_blue);
		    
		    quantized_channel_list.add(quantized_blue_green);
		    quantized_channel_list.add(quantized_red_green);
		    quantized_channel_list.add(quantized_red_blue);
		    
		    for(int i = 0; i < 6; i++)
		    {
		    	int min = 256;
		    	int [] quantized_channel = (int [])quantized_channel_list.get(i);
		    	
		    	// Find the channel minimums.
		    	for(int j = 0; j < quantized_channel.length; j++)
		    		if(quantized_channel[j] < min)
		    			min = quantized_channel[j];
		    	channel_min[i] = min;
		    	
		    	// Get rid of the negative numbers in the difference channels.
		    	if(i > 2)
		    		for(int j = 0; j < quantized_channel.length; j++)
		    			quantized_channel[j] -= min;
		    	
		    	// Save the initial value.
		    	channel_init[i] = quantized_channel[0];
		    	
		    	// Replace the original data with the modified data.
		    	quantized_channel_list.set(i, quantized_channel);
		    	
		    	// Get the Paeth delta sum.
		    	channel_sum[i] = DeltaMapper.getPaethSum2(quantized_channel, new_xdim, new_ydim, 20);
		    	
		    }
		  
			// Find the optimal set.  
			set_sum[0] = channel_sum[0] + channel_sum[1] + channel_sum[2];
			set_sum[1] = channel_sum[0] + channel_sum[4] + channel_sum[2];
			set_sum[2] = channel_sum[0] + channel_sum[3] + channel_sum[2];
			set_sum[3] = channel_sum[0] + channel_sum[1] + channel_sum[4];
			set_sum[4] = channel_sum[0] + channel_sum[3] + channel_sum[5];
			set_sum[5] = channel_sum[3] + channel_sum[1] + channel_sum[2];
			set_sum[6] = channel_sum[3] + channel_sum[4] + channel_sum[2];
			set_sum[7] = channel_sum[3] + channel_sum[1] + channel_sum[4];
			set_sum[8] = channel_sum[5] + channel_sum[1] + channel_sum[4];
			set_sum[9] = channel_sum[5] + channel_sum[4] + channel_sum[2];
			    
			int min_index     = 0;
			int min_delta_sum = Integer.MAX_VALUE;
			for(int i = 0; i < 10; i++)
			{
				if(set_sum[i] < min_delta_sum)
				{
					min_delta_sum = set_sum[i];
					min_index = i;
				}
			}
				
			min_set_id = min_index;
			System.out.println("A set with the lowest delta sum is " + set_string[min_index]);
			
		    
			int [] channel_id = DeltaMapper.getChannels(min_set_id);
			
			table_list.clear();
			delta_list.clear();
			
			int total_bits = 0;
			for(int i = 0; i < 3; i++)
			{
				int j = channel_id[i];
				
				int [] quantized_channel = (int[])quantized_channel_list.get(j);
				
				ArrayList result = DeltaMapper.getDeltasFromValues3(quantized_channel, new_xdim, new_ydim);
                int []    delta  = (int [])result.get(1);
                delta_list.add(delta);
				
				ArrayList histogram_list = DeltaMapper.getHistogram(delta);
			    channel_delta_min[j]     = (int)histogram_list.get(0);
			    int [] histogram         = (int[])histogram_list.get(1);
				int [] string_table = DeltaMapper.getRankTable(histogram);
				table_list.add(string_table);
				
				for(int k = 1; k < delta.length; k++)
					delta[k] -= channel_delta_min[j];
				byte [] string         = new byte[xdim * ydim * 2];
				byte [] compression_string = new byte[xdim * ydim * 2];
				channel_length[j]      = DeltaMapper.packStrings2(delta, string_table, string);
				
				double zero_one_ratio = new_xdim * new_ydim;
		        if(histogram.length > 1)
		        {
					int min_value = Integer.MAX_VALUE;
					for(int k = 0; k < histogram.length; k++)
						 if(histogram[k] < min_value)
							min_value = histogram[k];
					zero_one_ratio -= min_value;
		        }	
			    zero_one_ratio  /= channel_length[j];
			    
			    if(zero_one_ratio > .5)
					channel_compressed_length[j] = DeltaMapper.compressZeroStrings(string, channel_length[j], compression_string);
			    else
			    	channel_compressed_length[j] = DeltaMapper.compressZeroStrings(string, channel_length[j], compression_string);
			    total_bits += channel_compressed_length[j];
			}
			int total_bytes = total_bits / 8;
			double compression_rate = total_bytes;
			compression_rate /= xdim * ydim * 3;
			System.out.println("Compression rate for paeth deltas is " + String.format("%.4f", compression_rate));
			//System.out.println();
			
			total_bits = 0;
			for(int i = 0; i < 3; i++)
			{
				int j = channel_id[i];
				
				int [] quantized_channel = (int[])quantized_channel_list.get(j);
				
				ArrayList result = getDeltasFromValues4(quantized_channel, new_xdim, new_ydim);
                int []    delta  = (int [])result.get(1);
                delta_list.add(delta);
				
				ArrayList histogram_list = DeltaMapper.getHistogram(delta);
			    channel_delta_min[j]     = (int)histogram_list.get(0);
			    int [] histogram         = (int[])histogram_list.get(1);
				int [] string_table = DeltaMapper.getRankTable(histogram);
				table_list.add(string_table);
				
				for(int k = 1; k < delta.length; k++)
					delta[k] -= channel_delta_min[j];
				byte [] string         = new byte[xdim * ydim * 2];
				byte [] compression_string = new byte[xdim * ydim * 2];
				channel_length[j]      = DeltaMapper.packStrings2(delta, string_table, string);
				
				double zero_one_ratio = new_xdim * new_ydim;
		        if(histogram.length > 1)
		        {
					int min_value = Integer.MAX_VALUE;
					for(int k = 0; k < histogram.length; k++)
						 if(histogram[k] < min_value)
							min_value = histogram[k];
					zero_one_ratio -= min_value;
		        }	
			    zero_one_ratio  /= channel_length[j];
			    
			    if(zero_one_ratio > .5)
					channel_compressed_length[j] = DeltaMapper.compressZeroStrings(string, channel_length[j], compression_string);
			    else
			    	channel_compressed_length[j] = DeltaMapper.compressZeroStrings(string, channel_length[j], compression_string);
			    total_bits += channel_compressed_length[j];
			}
			total_bytes = total_bits / 8;
			compression_rate = total_bytes;
			compression_rate /= xdim * ydim * 3;
			System.out.println("Compression rate for ideal deltas is " + String.format("%.4f", compression_rate));
			System.out.println();
			
			for(int i = 0; i < 3; i++)
		    {
		         int [] channel = (int [])channel_list.get(i);
		         int [] quantized_channel = (int [])quantized_channel_list.get(i);
		         int [] resized_channel   = new int[xdim * ydim];
		         if(pixel_quant != 0)
		            resized_channel = ResizeMapper.resize(quantized_channel, new_xdim, xdim, ydim);	
		         else
		        	for(int j = 0; j < quantized_channel.length; j++)
		        		resized_channel[j] = quantized_channel[j];
		        for(int j = 0; j < resized_channel.length; j++)
		        	resized_channel[j] <<= pixel_shift;
		        
		        int [] error   = (int [])error_list.get(i);
		        for(int j = 0; j < channel.length; j++)
		        	error[j]             = channel[j] - resized_channel[j]; 
		        
		        quantized_channel_list.set(i, resized_channel);
		    } 
		     
		    for(int i = 0; i < xdim * ydim; i++)
		    	 new_pixel[i] = 0;
		     int shift = 16;
		     for(int i = 0; i < 3; i++)
		     {
		        int [] quantized_channel = (int [])quantized_channel_list.get(i);
		        int [] error             = (int [])error_list.get(i);
		     
		        for(int j = 0; j < ydim; j++)
			    {
			    	for(int k = 0; k < xdim; k++)
			    	{
			    		double correction_value = error[j * xdim + k];
			    		double factor = correction;
			    		factor /= 10;
			    		correction_value *= factor;
			    		new_pixel[j * xdim + k] |= (quantized_channel[j * xdim + k] + (int)correction_value)<< shift;	
			    	}
			    }
		        
		        shift -= 8;
			}
			
		 
		    /*
			 byte [] zipped_bytes = new byte[expanded_xdim * expanded_ydim];
			 Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);	
		     deflater.setInput(expanded_error);
		     deflater.finish();
		     int zipped_byte_length = deflater.deflate(zipped_bytes);
		     deflater.end();	    	
		    	
		    	
		     double zipped_length = zipped_byte_length;
		     double zipped_rate   = zipped_length / (expanded_xdim * expanded_ydim);
		    	
		     System.out.println("The zipped compression rate for the error is " + String.format("%.2f", zipped_rate));
			*/ 
			 for(int i = 0; i < xdim; i++)
			 {
			     for(int j = 0; j < ydim; j++)
			    	 image.setRGB(i, j, new_pixel[j * xdim + i]);	
			 } 
		    
			 image_canvas.repaint();
		    
		}
	}
	
	class ReloadHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			
			for(int i = 0; i < xdim; i++)
		    {
		    	for(int j = 0; j < ydim; j++)
		    	{
		    		image.setRGB(i, j, original_pixel[j * xdim + i]);
		    	}
		    	
		    } 
			System.out.println("Reloaded original image.");
			System.out.println();
			image_canvas.repaint();
		}
	}
	
	// Get an ideal delta set and a map of which pixels are used.
    public static ArrayList getDeltasFromValues4(int src[], int xdim, int ydim)
    {
        int[]  dst        = new int[xdim * ydim];
        int[] direction   = new int[xdim * ydim];
        
        int init_value     = src[0];
        int value          = init_value;
        int delta          = 0;
        int sum            = 0;
        
        int k = 0;
        for(int i = 0; i < ydim; i++)
        {
        	if(i == 0)
        	{
                for(int j = 0; j < xdim; j++)
                {
            	    if(j == 0)
            	    {
            		    // Setting the first value to 3 to mark the delta type ideal.
            			dst[k]       = 3;
            			direction[k] = 0;
            			k++;
            	    }
            		else
            		{
            			// We don't have an upper or upper diagonal delta to check
            			// in the first row, so we just use horizontal deltas.
            		    delta        = src[k] - value;
                        value       += delta;
                        dst[k]       = delta;
                        direction[k] = 0;
                        sum         += Math.abs(delta);
                        k++;
            		}
            	}
            }
        	else
        	{
        		for(int j = 0; j < xdim; j++)
                {
            	    if(j == 0)
            	    {
            	    	// We dont have a horizontal delta or diagonal delta to check,
            	    	// so we just use a vertical delta, and reset our init value.
            	    	delta        = src[k] - init_value;
            	    	init_value   = src[k];
            	    	dst[k]       = delta;
            	    	direction[k] = 1;
            	    	sum          += Math.abs(delta);
            	    	k++;
            	    }
            	    else
            	    {
            	    	// Now we have a set of 3 possible pixels to use.
            	    	int a = src[k] - src[k - 1];
            	    	int b = src[k] - src[k - xdim];
            	    	int c = src[k] - src[k - xdim - 1];
            	    	
            	    	if(Math.abs(a) <= Math.abs(b) && Math.abs(a) <= Math.abs(c))
            	    	{
            	    		delta        = a;
            	    	    dst[k]       = delta;
            	    	    direction[k] = 0;
            	    	    sum         += Math.abs(delta);
            	    	    k++;
            	    	}
            	    	else if(Math.abs(b)<= Math.abs(c))
            	    	{
            	    		delta        = b;
            	    		dst[k]       = delta;
            	    	    direction[k] = 1;
            	    	    sum         += Math.abs(delta);
            	    	    k++;
            	    	}
            	    	else
            	    	{
            	    		delta        = c;
            	    		dst[k]       = delta;
            	    	    direction[k] = 2;
            	    	    sum         += Math.abs(delta);
            	    	    k++;	
            	    	}
            	    }
                }
        	}
        }
        ArrayList result = new ArrayList();
        result.add(sum);
        result.add(dst);
        result.add(direction);
        return result;
    }
}