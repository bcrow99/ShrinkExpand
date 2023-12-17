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

public class ErrorAnalyzer
{
	BufferedImage original_image;
	BufferedImage image;
	JMenuItem     apply_item;
	JDialog       shift_dialog;
	JTextField    shift_value;
	JDialog       correction_dialog;
	JTextField    correction_value;
	ImageCanvas   image_canvas;
	
	int           correction = 0;
	
	int           xdim, ydim;
	String        filename;
	
	int [] original_pixel;
	int [] new_pixel;
	int [] alpha;
	int [] blue;
	int [] green;
	int [] red;
	
	int [] shifted_blue;
	int [] shifted_green;
	int [] shifted_red;
	
	double [][] blue_error;
	double [][] green_error;
	double [][] red_error;
	
	int pixel_shift  = 3;
	
	ArrayList channel_list, shifted_channel_list, error_list;

	
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java ErrorAnalyzer <filename>");
			System.exit(0);
		}
		String prefix       = new String("C:/Users/Brian Crowley/Desktop/");
		String filename     = new String(args[0]);
	
		ErrorAnalyzer analyzer = new ErrorAnalyzer(prefix + filename);
	}

	public ErrorAnalyzer(String _filename)
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
			
			System.out.println("xdim = " + xdim + ", ydim = " + ydim);
			int pixel_length = xdim * ydim * 8;
		    
			
		    original_pixel = new int[xdim * ydim];
		    new_pixel      = new int[xdim * ydim];
		    alpha          = new int[xdim * ydim];
			blue           = new int[xdim * ydim];
		    green          = new int[xdim * ydim];
		    red            = new int[xdim * ydim];
		    
		    channel_list = new ArrayList();
		    channel_list.add(blue);
		    channel_list.add(green);
		    channel_list.add(red);
		    
		    
		    shifted_blue   = new int[xdim * ydim];
		    shifted_green  = new int[xdim * ydim];
		    shifted_red    = new int[xdim * ydim];
		    shifted_channel_list = new ArrayList();
		    shifted_channel_list.add(shifted_blue);
		    shifted_channel_list.add(shifted_green);
		    shifted_channel_list.add(shifted_red);
		    
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
			    
		        /*
		        for(int i = 0; i < xdim * ydim; i++)
				{
			        alpha[i] = (original_pixel[i] >> 24) & 0xff;
				    blue[i]  = (original_pixel[i] >> 16) & 0xff;
				    green[i] = (original_pixel[i] >> 8) & 0xff; 
		            red[i]   = original_pixel[i] & 0xff; 
		            
		            shifted_blue[i]  = blue[i] >> pixel_shift;
		            shifted_green[i] = green[i] >> pixel_shift;
		            shifted_red[i]   = red[i] >> pixel_shift;
		            
		            
				}
				*/
		        
		        int shift = 16;
		        for(int i = 0; i < 3; i++)
		        {
		        	int [] channel         = (int [])channel_list.get(i);
		        	int [] shifted_channel = (int [])shifted_channel_list.get(i);
		        	double [][] error      = (double [][])error_list.get(i);
		            
		        	for(int j = 0; j < ydim; j++)
			        {
			    	    for(int k = 0; k < xdim; k++)
			    	    {
			    		    channel[j * xdim + k]         = (original_pixel[j * xdim + k] >> shift) & 0xff; 
			    		    shifted_channel[j * xdim + k] = channel[j * xdim + k] >> pixel_shift;
			    		    error[j][k]                   = channel[j * xdim + k];
			    		    error[j][k]                  -= shifted_channel[j * xdim + k] << pixel_shift;
			    		    shift                        -= 8;
			    	    }
			        }
			    }
		        
		        
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
			
				JMenuItem shift_item = new JMenuItem("Pixel Shift");
				ActionListener shift_handler = new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						Point location_point = frame.getLocation();
						int x = (int) location_point.getX();
						int y = (int) location_point.getY();

						x += xdim;

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
				shift_slider.setValue(pixel_shift);
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
		public void actionPerformed(ActionEvent event)
		{
			 int shift = 16;
		     for(int i = 0; i < 3; i++)
		     {
		        int [] channel         = (int [])channel_list.get(i);
		        int [] shifted_channel = (int [])shifted_channel_list.get(i);
		        double [][] error      = (double [][])error_list.get(i);
		            
		        for(int j = 0; j < ydim; j++)
			    {
			    	for(int k = 0; k < xdim; k++)
			    	{
			    		channel[j * xdim + k]         = (original_pixel[j * xdim + k] >> shift) & 0xff; 
			    		shifted_channel[j * xdim + k] = channel[j * xdim + k] >> pixel_shift;
			    		error[j][k]                   = channel[j * xdim + k];
			    		error[j][k]                  -= shifted_channel[j * xdim + k] << pixel_shift;
			    	}
			    }
		        shift -= 8;
			 }
		     for(int i = 0; i < xdim * ydim; i++)
		    	 new_pixel[i] = 0;
		
		     shift = 16;
		     for(int i = 0; i < 3; i++)
		     {
		        int [] shifted_channel = (int [])shifted_channel_list.get(i);
		        double [][] error      = (double [][])error_list.get(i);
		            
		        for(int j = 0; j < ydim; j++)
			    {
			    	for(int k = 0; k < xdim; k++)
			    	{
			    		double current_error = error[j][k] * (double)correction / 10.;
			    		double value         = shifted_channel[j * xdim + k] << pixel_shift;
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