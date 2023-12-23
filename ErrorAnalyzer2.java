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

public class ErrorAnalyzer2
{
	BufferedImage original_image;
	BufferedImage image;
	JMenuItem     apply_item;
	JDialog       quant_dialog;
	JTextField    quant_value;
	JDialog       correction_dialog;
	JTextField    correction_value;
	ImageCanvas   image_canvas;
	
	int           correction = 0;
	
	int           xdim, ydim;
	int           x_power_of_two, y_power_of_two;
	String        filename;
	
	int [] original_pixel;
	int [] new_pixel;
	int [] alpha;
	int [] blue;
	int [] green;
	int [] red;

	
	double [][] blue_error;
	double [][] green_error;
	double [][] red_error;
	
	int pixel_quant  = 0;
	
	ArrayList channel_list, quantized_channel_list, error_list;

	
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java ErrorAnalyzer2 <filename>");
			System.exit(0);
		}
		String prefix       = new String("C:/Users/Brian Crowley/Desktop/");
		String filename     = new String(args[0]);
	
		ErrorAnalyzer2 analyzer = new ErrorAnalyzer2(prefix + filename);
	}

	public ErrorAnalyzer2(String _filename)
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
			
			x_power_of_two = 1;
			while(x_power_of_two < xdim)
				x_power_of_two *= 2;
			
			y_power_of_two = 1;
			while(y_power_of_two < ydim)
				y_power_of_two *= 2;
			
			
			System.out.println("Xdim = " + xdim + ", ydim = " + ydim);
			//System.out.println("X power of two = " + x_power_of_two + ", y power of two = " + y_power_of_two);
			
			
			int pixel_length = xdim * ydim * 8;
		    
			
		    original_pixel = new int[xdim * ydim];
		    new_pixel      = new int[xdim * ydim];
		    alpha          = new int[xdim * ydim];
			blue           = new int[xdim * ydim];
		    green          = new int[xdim * ydim];
		    red            = new int[xdim * ydim];
		    
		    channel_list = new ArrayList();
		   
		    
		    
		    quantized_channel_list = new ArrayList();
		  
		    
		    blue_error  = new double[ydim][xdim];
		    green_error = new double[ydim][xdim];
		    red_error   = new double[ydim][xdim];
		    error_list  = new ArrayList();
		    error_list.add(blue_error);
		    error_list.add(green_error);
		    error_list.add(red_error);
		    
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
		public void actionPerformed(ActionEvent event)
		{
			 quantized_channel_list.clear();
			
			 int shift = 16;
		     for(int i = 0; i < 3; i++)
		     {
		        int [] channel         = (int [])channel_list.get(i);
		        
		        int new_xdim = xdim;
		        int new_ydim = ydim;
		        if(pixel_quant == 0)
		        {
		           int [] quantized_channel = new int[xdim * ydim];
		           for(int j = 0; j < channel.length; j++)
		        	   quantized_channel[j] = channel[j];
		           quantized_channel_list.add(quantized_channel);
		           
		        }
		        else
		        {
		        	double factor = pixel_quant;
		        	factor       /= 10;
		        	new_xdim = xdim - (int)(factor * (xdim / 2 - 2));
		        	new_ydim = ydim - (int)(factor * (ydim / 2 - 2));
		        	
		        	int [] resized_channel = ResizeMapper.resize(channel, xdim, new_xdim, new_ydim);
		        	int [] quantized_channel = ResizeMapper.resize(resized_channel, new_xdim, xdim, ydim);
		        	quantized_channel_list.add(quantized_channel);
		        }
		        
		        int [] quantized_channel = (int [])quantized_channel_list.get(i);
		        double [][] error      = (double [][])error_list.get(i);
		            
		        for(int j = 0; j < ydim; j++)
			    {
			    	for(int k = 0; k < xdim; k++)
			    	{
			    		error[j][k]                   = channel[j * xdim + k];
			    		error[j][k]                  -= quantized_channel[j * xdim + k];
			    	}
			    }
		        shift -= 8;
			 }
		     
		  
		     for(int i = 0; i < xdim * ydim; i++)
		    	 new_pixel[i] = 0;
		     shift = 16;
		     for(int i = 0; i < 3; i++)
		     {
		        int [] quantized_channel = (int [])quantized_channel_list.get(i);
		        int [] channel           = (int [])channel_list.get(i);
		        double [][] error      = (double [][])error_list.get(i);
		            
		        for(int j = 0; j < ydim; j++)
			    {
			    	for(int k = 0; k < xdim; k++)
			    	{
			    		//double value         = channel[j * xdim + k];
			    		double value         = quantized_channel[j * xdim + k];
			    		double current_error = error[j][k] * (double)correction / 10.;
			    		value               += current_error;
			    		new_pixel[j * xdim + k] |= (int)(value) << shift;	
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
}