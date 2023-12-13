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

public class ExpandTester
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
	int [] alpha;
	int [] red;
	int [] green;
	int [] blue;
	
	int pixel_shift  = 3;

	
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java ExpandTester <filename>");
			System.exit(0);
		}
		String prefix       = new String("C:/Users/Brian Crowley/Desktop/");
		String filename     = new String(args[0]);
	
		ExpandTester tester = new ExpandTester(prefix + filename);
	}

	public ExpandTester(String _filename)
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
		    alpha          = new int[xdim * ydim];
			blue           = new int[xdim * ydim];
		    green          = new int[xdim * ydim];
		    red            = new int[xdim * ydim];
		    
			
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
		        
			    image = new BufferedImage(xdim, ydim, BufferedImage.TYPE_INT_RGB);
			    
			  
			    for(int i = 0; i < xdim; i++)
			    {
			    	for(int j = 0; j < ydim; j++)
			    	{
			    		image.setRGB(i, j, original_pixel[j * xdim + i]);
			    	}
			    }
               
			    JFrame frame = new JFrame("Expand Tester");
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
		    int [] shifted_blue  = new int[xdim * ydim];
		    int [] shifted_green = new int[xdim * ydim];
		    int [] shifted_red   = new int[xdim * ydim];
		    int [] new_pixel     = new int[xdim * ydim];
		    
		    double [][] shifted_green_double = new double[ydim][xdim];
		    
		    double shifted_green_max = 0;
		    double shifted_green_min = 255;
		    for(int i = 0; i < ydim; i++)
		    {
		    	for(int j = 0; j < xdim; j++)
		    	{
		            shifted_blue[i * xdim + j]  = blue[i * xdim + j]  >> pixel_shift;
		            shifted_green[i * xdim + j] = green[i * xdim + j] >> pixel_shift;
			        shifted_red[i * xdim + j]   = red[i * xdim + j]   >> pixel_shift; 
			    
			        shifted_green_double[i][j] = shifted_green[i * xdim + j];
			    
			        if(shifted_green_max < shifted_green[i * xdim  + j])
			    	    shifted_green_max = shifted_green[i * xdim  + j];
			        if(shifted_green_min > shifted_green[i * xdim  + j])
			    	    shifted_green_min = shifted_green[i * xdim  + j]; 
		    	}
		    }
		    System.out.println("Shifted green min is " + shifted_green_min);
		    System.out.println("Shifted green max is " + shifted_green_max);
		    double [][] shrunken_green_double = ExpandMapper.avg4(shifted_green_double); 
		    
		    int shrunken_ydim = shrunken_green_double.length;
		    int shrunken_xdim = shrunken_green_double[0].length;
		    double shrunken_green_max = 0;
		    double shrunken_green_min = 255;
		    for(int i = 0; i < shrunken_ydim; i++)
		    {
		    	for(int j = 0; j < shrunken_xdim; j++)
		    	{
			        if(shrunken_green_max < shrunken_green_double[i][j])
			    	    shrunken_green_max = shrunken_green_double[i][j];
			        if(shrunken_green_min > shrunken_green_double[i][j])
			        	shrunken_green_min = shrunken_green_double[i][j];  
		    	}
		    }
		    System.out.println("Shrunken min is " + shrunken_green_min);
		    System.out.println("Shrunken max is " + shrunken_green_max);
		    System.out.println();
		    
		    boolean x_even = true;
		    if(xdim %2 != 0)
		        x_even = false;
		    
		    boolean y_even = true;
		    if(ydim %2 != 0)
		        y_even = false;
		    
		    
		    double [][] expanded_green_double = ExpandMapper.expand(shrunken_green_double, x_even, y_even); 
		    int expanded_ydim = expanded_green_double.length;
		    int expanded_xdim = expanded_green_double[0].length;
		    
		    //System.out.println("Expanded xdim is " + expanded_xdim + ", expanded ydim is " + expanded_ydim);
		    
		    double [][] error = new double[expanded_ydim][expanded_xdim];
		    
		    int [] expanded_green = new int[expanded_xdim * expanded_ydim];
		    int [] expanded_error = new int[expanded_xdim * expanded_ydim];
		    
		   
		    
		   
		    double expanded_min = 255;
		    double expanded_max = 0;
		    
		    double error_min = 0;
		    double error_max = 0;
		    
		    int k = 0;
		    for(int i = 0; i < expanded_ydim; i++)
		    {
		    	for(int j = 0; j < expanded_xdim; j++)
		    	{
		    		error[i][j] = expanded_green_double[i][j];
		    		error[i][j] -= shifted_green[i * xdim + j];
		    		
		    		if(expanded_green_double[i][j] < expanded_min)
		    			expanded_min = expanded_green_double[i][j];
		    		if(expanded_green_double[i][j] > expanded_max)
		    			expanded_max = expanded_green_double[i][j];
		    		if(error[i][j] < error_min)
		    			error_min = error[i][j];
		    		if(error[i][j] > error_max)
		    			error_max = error[i][j];
		    	}
		    }
		    
		    
		    
		    double expanded_scaler = 255. / expanded_max;
		    double error_range     = error_max - error_min;
		    double error_offset    = error_min;
		    
		    k = 0;
		  
		    for(int i = 0; i < expanded_ydim; i++)
		    {
		    	for(int j = 0; j < expanded_xdim; j++)
		    	{
		    		expanded_green[k] = (int)(expanded_scaler * expanded_green_double[i][j]);
		    		expanded_error[k] = (int)(error[i][j] * (double)correction / 10.);
		    		k++;
		    	}
		    }
		    
		    System.out.println("Expanded min is " + String.format("%.2f", expanded_min));
		    System.out.println("Expanded max is " + String.format("%.2f", expanded_max));
		    System.out.println("Error min is " + String.format("%.2f", error_min));
		    System.out.println("Error max is " + String.format("%.2f", error_max));
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    
		    for(int i = 0; i < ydim; i++)
		    {
		    	for(int j = 0; j < xdim; j++)
		    	{
		    	
		    		shifted_blue[i * xdim + j]  <<= pixel_shift;
		            shifted_green[i * xdim + j] <<= pixel_shift;
		            shifted_red[i * xdim + j]   <<= pixel_shift;
		            double new_green_double       = expanded_green_double[i][j];
		            int    new_green              = (int)new_green_double;
		            
		           
		            double green_error = error[i][j] * (double)correction / 10.;
		            new_green -= (int)green_error;
	            	new_green <<= pixel_shift;
		          
		         
		            new_pixel[i * xdim + j]  = 0;
		            new_pixel[i * xdim + j] |= shifted_blue[i * xdim + j] << 16; 
		            new_pixel[i * xdim + j] |= new_green << 8;
		            new_pixel[i * xdim + j] |= shifted_red[i * xdim + j];	
		    	}
		    }
		    
		    System.out.println();
		     
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